package org.lti.rom

import com.jcraft.jsch.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import org.apache.commons.exec.*
import java.io.*
import java.util.*

class WslService {
    private var session: Session? = null
    private var channel: Channel? = null
    private val jsch = JSch()
    
    data class WslConnection(
        val host: String = "localhost",
        val port: Int = 22,
        val username: String = "wsl",
        val password: String = "",
        val useKeyAuth: Boolean = false,
        val keyPath: String = ""
    )
    
    data class CommandResult(
        val output: String,
        val error: String,
        val exitCode: Int,
        val success: Boolean
    )
    
    suspend fun connect(connection: WslConnection): Boolean = withContext(Dispatchers.IO) {
        Napier.d("Attempting to connect to ${connection.username}@${connection.host}:${connection.port}")
        try {
            session = jsch.getSession(connection.username, connection.host, connection.port)
            session?.setConfig("StrictHostKeyChecking", "no")

            if (connection.useKeyAuth && connection.keyPath.isNotEmpty()) {
                Napier.d("Using key authentication with key: ${connection.keyPath}")
                jsch.addIdentity(connection.keyPath)
            } else {
                Napier.d("Using password authentication")
                session?.setPassword(connection.password)
            }

            session?.connect(5000)
            val isConnected = session?.isConnected == true
            if (isConnected) {
                Napier.d("SSH connection successful")
            } else {
                Napier.d("SSH connection failed")
            }
            isConnected
        } catch (e: JSchException) {
            Napier.e("SSH connection failed", throwable = e)
            false
        } catch (e: Exception) {
            Napier.e("An unexpected error occurred during connection", throwable = e)
            false
        }
    }

    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        Napier.d("Executing SSH command: '$command'")
        var channel: ChannelExec? = null
        try {
            if (session?.isConnected != true) {
                return@withContext CommandResult("", "Not connected to WSL via SSH", -1, false)
            }

            channel = session?.openChannel("exec") as ChannelExec
            channel.setCommand(command)
            
            val inputStream = channel.inputStream
            val errorStream = channel.errStream
            
            channel.connect()
            
            val output = inputStream.bufferedReader().readText()
            val error = errorStream.bufferedReader().readText()
            
            // Wait for the command to finish
            while (channel.exitStatus == -1) {
                delay(100)
            }

            val exitCode = channel.exitStatus
            Napier.d("SSH command executed with exit code: $exitCode")

            CommandResult(output, error, exitCode, exitCode == 0)
        } catch (e: JSchException) {
            Napier.e("SSH command execution failed", throwable = e)
            CommandResult("", "SSH command execution failed: ${e.message}", -1, false)
        } catch (e: Exception) {
            Napier.e("An unexpected error occurred during command execution", throwable = e)
            CommandResult("", "An unexpected error occurred during command execution: ${e.message}", -1, false)
        } finally {
            channel?.disconnect()
        }
    }
    
    suspend fun executeWslCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        Napier.d("executeWslCommand called with command: '$command'")
        
        try {
            // Since we know 'wsl' works, try it first with different approaches
            val wslCmd = "wsl"
            
            // Get the current user's home directory in WSL
            val homeDir = getWslHomeDirectory()
            Napier.d("WSL home directory: '$homeDir'")
            
            // Try different command approaches with home directory context
            val commandApproaches = listOf(
                // Using bash explicitly with home directory (prioritize this)
                listOf(wslCmd, "bash", "-c", "cd $homeDir && $command"),
                // Using sh explicitly with home directory
                listOf(wslCmd, "sh", "-c", "cd $homeDir && $command"),
                // Using wsl with explicit directory change
                listOf(wslCmd, "bash", "-c", "cd ~ && $command"),
                // Using wsl with explicit home path
                listOf(wslCmd, "bash", "-c", "cd /home/\$(whoami) && $command"),
                // Direct command (works for simple commands like pwd, ls)
                listOf(wslCmd, command),
                // Using cmd to handle complex commands
                listOf("cmd", "/c", "$wslCmd $command"),
                // Using PowerShell
                listOf("powershell", "-Command", "$wslCmd $command")
            )
            
            for (cmdArgs in commandApproaches) {
                try {
                    Napier.d("Attempting command: ${cmdArgs.joinToString(" ")}")
                    
                    val processBuilder = ProcessBuilder(cmdArgs)
                    processBuilder.redirectErrorStream(true) // Merge error stream with output
                    
                    Napier.d("ProcessBuilder created, starting process...")
                    val process = processBuilder.start()
                    
                    Napier.d("Process started, reading output...")
                    val output = process.inputStream.bufferedReader().readText()
                    val exitCode = process.waitFor()
                    
                    Napier.d("Process finished with exit code: $exitCode")
                    Napier.d("Output: '$output'")
                    
                    if (exitCode == 0) {
                        Napier.d("Command successful!")
                        return@withContext CommandResult(output, "", exitCode, true)
                    } else {
                        Napier.d("Command failed with exit code: $exitCode")
                        // Continue to next approach
                    }
                } catch (e: Exception) {
                    Napier.e("Exception with command approach ${cmdArgs.joinToString(" ")}", throwable = e)
                    continue
                }
            }
            
            // If all approaches failed
            val errorMsg = "All WSL command approaches failed for: $command"
            Napier.e(errorMsg)
            CommandResult("", errorMsg, -1, false)
        } catch (e: Exception) {
            val errorMsg = "WSL command execution failed: ${e.message}"
            Napier.e(errorMsg, throwable = e)
            CommandResult("", errorMsg, -1, false)
        }
    }
    
    suspend fun listWslDistributions(): List<String> = withContext(Dispatchers.IO) {
        Napier.d("Listing WSL distributions...")
        
        try {
            // Try to get WSL distribution list using wsl --list
            val wslCommands = listOf("wsl", "wsl.exe", "C:\\Windows\\System32\\wsl.exe")
            
            for (wslCmd in wslCommands) {
                try {
                    Napier.d("Trying to list distributions with: $wslCmd --list --verbose")
                    val processBuilder = ProcessBuilder(wslCmd, "--list", "--verbose")
                    val process = processBuilder.start()
                    val output = process.inputStream.bufferedReader().readText()
                    val exitCode = process.waitFor()
                    
                    Napier.d("WSL list command exit code: $exitCode")
                    Napier.d("WSL list output: '$output'")
                    
                    if (exitCode == 0) {
                        val distributions = output.lines()
                            .filter { it.isNotBlank() && !it.startsWith("NAME") }
                            .map { it.split("\\s+".toRegex())[0] }
                        
                        Napier.d("Found distributions: $distributions")
                        return@withContext distributions
                    }
                } catch (e: Exception) {
                    Napier.e("Exception listing distributions with $wslCmd", throwable = e)
                    continue
                }
            }
            
            Napier.e("Could not list WSL distributions")
            emptyList()
        } catch (e: Exception) {
            Napier.e("Exception in listWslDistributions", throwable = e)
            emptyList()
        }
    }
    
    suspend fun isWslAvailable(): Boolean = withContext(Dispatchers.IO) {
        Napier.d("Checking WSL availability...")
        
        try {
            // Try to run wsl --version to check availability
            val wslCommands = listOf("wsl", "wsl.exe", "C:\\Windows\\System32\\wsl.exe")
            
            Napier.d("Testing WSL commands for availability: ${wslCommands.joinToString(", ")}")
            
            for (wslCmd in wslCommands) {
                try {
                    Napier.d("Testing WSL command: '$wslCmd --version'")
                    val processBuilder = ProcessBuilder(wslCmd, "--version")
                    val process = processBuilder.start()
                    val exitCode = process.waitFor()
                    
                    Napier.d("WSL version check exit code: $exitCode")
                    
                    if (exitCode == 0) {
                        Napier.d("WSL is available via: $wslCmd")
                        return@withContext true
                    } else {
                        Napier.d("WSL command '$wslCmd' failed with exit code: $exitCode")
                    }
                } catch (e: Exception) {
                    Napier.e("Exception testing WSL command '$wslCmd'", throwable = e)
                    Napier.d("Exception type: ${e.javaClass.simpleName}")
                    continue
                }
            }
            
            Napier.e("WSL is not available - all commands failed")
            false
        } catch (e: Exception) {
            Napier.e("Outer exception in isWslAvailable", throwable = e)
            false
        }
    }
    
    fun disconnect() {
        try {
            channel?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            Napier.e("Error disconnecting", throwable = e)
        }
    }
    
    fun isConnected(): Boolean {
        return session?.isConnected == true
    }
    
    private suspend fun getWslHomeDirectory(): String = withContext(Dispatchers.IO) {
        try {
            // First, get the current WSL user
            val username = getWslUsername()
            Napier.d("WSL username: '$username'")
            
            // Try to get the home directory using different methods
            val homeCommands = listOf(
                "echo \$HOME",
                "echo ~",
                "pwd",
                "cd ~ && pwd"
            )
            
            for (cmd in homeCommands) {
                try {
                    val processBuilder = ProcessBuilder("wsl", "bash", "-c", cmd)
                    val process = processBuilder.start()
                    val output = process.inputStream.bufferedReader().readText().trim()
                    val exitCode = process.waitFor()
                    
                    if (exitCode == 0 && output.isNotEmpty()) {
                        Napier.d("Home directory command '$cmd' returned: '$output'")
                        return@withContext output
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            
            // Fallback to constructed home directory
            val fallbackHome = "/home/$username"
            Napier.d("Using fallback home directory: '$fallbackHome'")
            fallbackHome
        } catch (e: Exception) {
            Napier.e("Error getting WSL home directory", throwable = e)
            "/home/ubuntu"
        }
    }
    
    private suspend fun getWslUsername(): String = withContext(Dispatchers.IO) {
        try {
            val processBuilder = ProcessBuilder("wsl", "whoami")
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            
            if (exitCode == 0 && output.isNotEmpty()) {
                Napier.d("WSL username: '$output'")
                return@withContext output
            }
        } catch (e: Exception) {
            Napier.e("Error getting WSL username", throwable = e)
        }
        
        // Fallback username
        "ubuntu"
    }
    
    suspend fun getCurrentDirectory(): String = withContext(Dispatchers.IO) {
        try {
            // Get the home directory and use it as the current directory
            val homeDir = getWslHomeDirectory()
            Napier.d("Current WSL directory: '$homeDir'")
            return@withContext homeDir
        } catch (e: Exception) {
            Napier.e("Error getting current directory", throwable = e)
            "/home/ubuntu"
        }
    }
    
    suspend fun testHomeDirectoryExecution(): CommandResult = withContext(Dispatchers.IO) {
        Napier.d("Testing home directory execution...")
        
        try {
            val homeDir = getWslHomeDirectory()
            Napier.d("Testing commands from home directory: '$homeDir'")
            
            // Test commands that should show we're in the home directory
            val testCommands = listOf(
                "pwd",
                "ls -la",
                "echo 'Current user:' && whoami",
                "echo 'Home contents:' && ls ~"
            )
            
            for (cmd in testCommands) {
                try {
                    val result = executeWslCommand(cmd)
                    Napier.d("Test command '$cmd' - Success: ${result.success}, Output: '${result.output.trim()}'")
                    if (result.success) {
                        return@withContext result
                    }
                } catch (e: Exception) {
                    Napier.e("Test command '$cmd' failed", throwable = e)
                    continue
                }
            }
            
            CommandResult("", "All home directory test commands failed", -1, false)
        } catch (e: Exception) {
            CommandResult("", "Home directory test failed: ${e.message}", -1, false)
        }
    }
    
    suspend fun testWslConnection(): CommandResult = withContext(Dispatchers.IO) {
        Napier.d("Testing WSL connection...")
        
        try {
            // Test with multiple simple commands to find what works
            val testCommands = listOf(
                "pwd",
                "whoami", 
                "ls",
                "echo hello",
                "/bin/echo hello",
                "which echo",
                "which bash",
                "which sh"
            )
            
            for (testCmd in testCommands) {
                Napier.d("Testing command: '$testCmd'")
                val result = executeWslCommand(testCmd)
                
                Napier.d("Test command '$testCmd' result - Success: ${result.success}, Exit Code: ${result.exitCode}")
                Napier.d("Test command output: '${result.output}'")
                
                if (result.success) {
                    Napier.d("WSL connection test successful with command: $testCmd")
                    return@withContext CommandResult("WSL connection test successful with: $testCmd\nOutput: ${result.output}", "", 0, true)
                }
            }
            
            Napier.e("All test commands failed")
            CommandResult("", "WSL test failed: All test commands failed", -1, false)
        } catch (e: Exception) {
            Napier.e("Exception in testWslConnection", throwable = e)
            CommandResult("", "WSL test error: ${e.message}", -1, false)
        }
    }

    suspend fun listFiles(path: String): List<WslFile> {
        val command = "ls -la $path"
        val result = executeWslCommand(command)
        if (result.success) {
            return result.output.lines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("\\s+".toRegex(), 9)
                    if (parts.size < 9) return@mapNotNull null
                    val permissions = parts[0]
                    val isDirectory = permissions.startsWith("d")
                    val name = parts[8]
                    if (name == "." || name == "..") return@mapNotNull null
                    WslFile(name, "$path/$name", isDirectory)
                }
        }
        return emptyList()
    }
}
