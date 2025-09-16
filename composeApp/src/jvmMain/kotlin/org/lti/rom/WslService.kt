package org.lti.rom

import com.jcraft.jsch.*
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
        try {
            session = jsch.getSession(connection.username, connection.host, connection.port)
            session?.setConfig("StrictHostKeyChecking", "no")
            
            if (connection.useKeyAuth && connection.keyPath.isNotEmpty()) {
                jsch.addIdentity(connection.keyPath)
            } else {
                session?.setPassword(connection.password)
            }
            
            session?.connect(5000)
            session?.isConnected == true
        } catch (e: Exception) {
            println("WSL connection failed: ${e.message}")
            false
        }
    }
    
    suspend fun executeCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            if (session?.isConnected != true) {
                return@withContext CommandResult("", "Not connected to WSL", -1, false)
            }
            
            channel = session?.openChannel("exec")
            val execChannel = channel as ChannelExec
            execChannel.setCommand(command)
            
            val inputStream = execChannel.inputStream
            val errorStream = execChannel.errStream
            
            execChannel.connect()
            
            val output = inputStream.bufferedReader().readText()
            val error = errorStream.bufferedReader().readText()
            
            execChannel.disconnect()
            
            CommandResult(output, error, execChannel.exitStatus, execChannel.exitStatus == 0)
        } catch (e: Exception) {
            CommandResult("", "Command execution failed: ${e.message}", -1, false)
        }
    }
    
    suspend fun executeWslCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] executeWslCommand called with command: '$command'")
        
        try {
            // Since we know 'wsl' works, try it first with different approaches
            val wslCmd = "wsl"
            
            // Get the current user's home directory in WSL
            val homeDir = getWslHomeDirectory()
            println("🔍 [DEBUG] WSL home directory: '$homeDir'")
            
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
                    println("🔍 [DEBUG] Attempting command: ${cmdArgs.joinToString(" ")}")
                    
                    val processBuilder = ProcessBuilder(cmdArgs)
                    processBuilder.redirectErrorStream(true) // Merge error stream with output
                    
                    println("🔍 [DEBUG] ProcessBuilder created, starting process...")
                    val process = processBuilder.start()
                    
                    println("🔍 [DEBUG] Process started, reading output...")
                    val output = process.inputStream.bufferedReader().readText()
                    val exitCode = process.waitFor()
                    
                    println("🔍 [DEBUG] Process finished with exit code: $exitCode")
                    println("🔍 [DEBUG] Output: '$output'")
                    
                    if (exitCode == 0) {
                        println("✅ [DEBUG] Command successful!")
                        return@withContext CommandResult(output, "", exitCode, true)
                    } else {
                        println("❌ [DEBUG] Command failed with exit code: $exitCode")
                        // Continue to next approach
                    }
                } catch (e: Exception) {
                    println("❌ [DEBUG] Exception with command approach ${cmdArgs.joinToString(" ")}: ${e.message}")
                    continue
                }
            }
            
            // If all approaches failed
            val errorMsg = "All WSL command approaches failed for: $command"
            println("❌ [DEBUG] All WSL command approaches failed: $errorMsg")
            CommandResult("", errorMsg, -1, false)
        } catch (e: Exception) {
            val errorMsg = "WSL command execution failed: ${e.message}"
            println("❌ [DEBUG] Outer exception: $errorMsg")
            CommandResult("", errorMsg, -1, false)
        }
    }
    
    suspend fun listWslDistributions(): List<String> = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] Listing WSL distributions...")
        
        try {
            // Try to get WSL distribution list using wsl --list
            val wslCommands = listOf("wsl", "wsl.exe", "C:\\Windows\\System32\\wsl.exe")
            
            for (wslCmd in wslCommands) {
                try {
                    println("🔍 [DEBUG] Trying to list distributions with: $wslCmd --list --verbose")
                    val processBuilder = ProcessBuilder(wslCmd, "--list", "--verbose")
                    val process = processBuilder.start()
                    val output = process.inputStream.bufferedReader().readText()
                    val exitCode = process.waitFor()
                    
                    println("🔍 [DEBUG] WSL list command exit code: $exitCode")
                    println("🔍 [DEBUG] WSL list output: '$output'")
                    
                    if (exitCode == 0) {
                        val distributions = output.lines()
                            .filter { it.isNotBlank() && !it.startsWith("NAME") }
                            .map { it.split("\\s+".toRegex())[0] }
                        
                        println("🔍 [DEBUG] Found distributions: $distributions")
                        return@withContext distributions
                    }
                } catch (e: Exception) {
                    println("❌ [DEBUG] Exception listing distributions with $wslCmd: ${e.message}")
                    continue
                }
            }
            
            println("❌ [DEBUG] Could not list WSL distributions")
            emptyList()
        } catch (e: Exception) {
            println("❌ [DEBUG] Exception in listWslDistributions: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun isWslAvailable(): Boolean = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] Checking WSL availability...")
        
        try {
            // Try to run wsl --version to check availability
            val wslCommands = listOf("wsl", "wsl.exe", "C:\\Windows\\System32\\wsl.exe")
            
            println("🔍 [DEBUG] Testing WSL commands for availability: ${wslCommands.joinToString(", ")}")
            
            for (wslCmd in wslCommands) {
                try {
                    println("🔍 [DEBUG] Testing WSL command: '$wslCmd --version'")
                    val processBuilder = ProcessBuilder(wslCmd, "--version")
                    val process = processBuilder.start()
                    val exitCode = process.waitFor()
                    
                    println("🔍 [DEBUG] WSL version check exit code: $exitCode")
                    
                    if (exitCode == 0) {
                        println("✅ [DEBUG] WSL is available via: $wslCmd")
                        return@withContext true
                    } else {
                        println("❌ [DEBUG] WSL command '$wslCmd' failed with exit code: $exitCode")
                    }
                } catch (e: Exception) {
                    println("❌ [DEBUG] Exception testing WSL command '$wslCmd': ${e.message}")
                    println("🔍 [DEBUG] Exception type: ${e.javaClass.simpleName}")
                    continue
                }
            }
            
            println("❌ [DEBUG] WSL is not available - all commands failed")
            false
        } catch (e: Exception) {
            println("❌ [DEBUG] Outer exception in isWslAvailable: ${e.message}")
            false
        }
    }
    
    fun disconnect() {
        try {
            channel?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            println("Error disconnecting: ${e.message}")
        }
    }
    
    fun isConnected(): Boolean {
        return session?.isConnected == true
    }
    
    private suspend fun getWslHomeDirectory(): String = withContext(Dispatchers.IO) {
        try {
            // First, get the current WSL user
            val username = getWslUsername()
            println("🔍 [DEBUG] WSL username: '$username'")
            
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
                        println("🔍 [DEBUG] Home directory command '$cmd' returned: '$output'")
                        return@withContext output
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            
            // Fallback to constructed home directory
            val fallbackHome = "/home/$username"
            println("🔍 [DEBUG] Using fallback home directory: '$fallbackHome'")
            fallbackHome
        } catch (e: Exception) {
            println("❌ [DEBUG] Error getting WSL home directory: ${e.message}")
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
                println("🔍 [DEBUG] WSL username: '$output'")
                return@withContext output
            }
        } catch (e: Exception) {
            println("❌ [DEBUG] Error getting WSL username: ${e.message}")
        }
        
        // Fallback username
        "ubuntu"
    }
    
    suspend fun getCurrentDirectory(): String = withContext(Dispatchers.IO) {
        try {
            // Get the home directory and use it as the current directory
            val homeDir = getWslHomeDirectory()
            println("🔍 [DEBUG] Current WSL directory: '$homeDir'")
            return@withContext homeDir
        } catch (e: Exception) {
            println("❌ [DEBUG] Error getting current directory: ${e.message}")
            "/home/ubuntu"
        }
    }
    
    suspend fun testHomeDirectoryExecution(): CommandResult = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] Testing home directory execution...")
        
        try {
            val homeDir = getWslHomeDirectory()
            println("🔍 [DEBUG] Testing commands from home directory: '$homeDir'")
            
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
                    println("🔍 [DEBUG] Test command '$cmd' - Success: ${result.success}, Output: '${result.output.trim()}'")
                    if (result.success) {
                        return@withContext result
                    }
                } catch (e: Exception) {
                    println("❌ [DEBUG] Test command '$cmd' failed: ${e.message}")
                    continue
                }
            }
            
            CommandResult("", "All home directory test commands failed", -1, false)
        } catch (e: Exception) {
            CommandResult("", "Home directory test failed: ${e.message}", -1, false)
        }
    }
    
    suspend fun testWslConnection(): CommandResult = withContext(Dispatchers.IO) {
        println("🔍 [DEBUG] Testing WSL connection...")
        
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
                println("🔍 [DEBUG] Testing command: '$testCmd'")
                val result = executeWslCommand(testCmd)
                
                println("🔍 [DEBUG] Test command '$testCmd' result - Success: ${result.success}, Exit Code: ${result.exitCode}")
                println("🔍 [DEBUG] Test command output: '${result.output}'")
                
                if (result.success) {
                    println("✅ [DEBUG] WSL connection test successful with command: $testCmd")
                    return@withContext CommandResult("WSL connection test successful with: $testCmd\nOutput: ${result.output}", "", 0, true)
                }
            }
            
            println("❌ [DEBUG] All test commands failed")
            CommandResult("", "WSL test failed: All test commands failed", -1, false)
        } catch (e: Exception) {
            println("❌ [DEBUG] Exception in testWslConnection: ${e.message}")
            CommandResult("", "WSL test error: ${e.message}", -1, false)
        }
    }
}
