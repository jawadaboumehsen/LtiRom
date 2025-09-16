package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

@Composable
fun WslInterface(viewModel: WslViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val isWslAvailable by viewModel.isWslAvailable.collectAsState()
    val wslDistributions by viewModel.wslDistributions.collectAsState()
    val commandOutput by viewModel.commandOutput.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val fileBrowserState by viewModel.fileBrowserState.collectAsState()

    var showConnectionDialog by remember { mutableStateOf(false) }
    var commandInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WSL Interface",
                style = MaterialTheme.typography.headlineMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isConnected) {
                    Button(
                        onClick = { viewModel.disconnect() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Disconnect")
                    }
                } else {
                    Button(onClick = { showConnectionDialog = true }) {
                        Text("Connect")
                    }
                }

                Button(
                    onClick = { viewModel.openFileBrowser() },
                    enabled = isWslAvailable && !isLoading
                ) {
                    Text("Browse")
                }

                Button(
                    onClick = { viewModel.checkWslAvailability() },
                    enabled = !isLoading
                ) {
                    Text("Refresh")
                }
            }
        }
        
        // Status indicators
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isConnected -> MaterialTheme.colorScheme.primaryContainer
                    isWslAvailable -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when {
                        isConnected -> "✓ Connected to WSL"
                        isWslAvailable -> "⚠ WSL Available (Not Connected)"
                        else -> "✗ WSL Not Available"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (wslDistributions.isNotEmpty()) {
                    Text(
                        text = "Available Distributions: ${wslDistributions.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Current working directory
                Text(
                    text = "Current directory: $currentDirectory (WSL Ubuntu)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Debug information
                Text(
                    text = "Debug: Try 'echo test' or 'pwd' commands first",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Git Repository Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Git Repository",
                    style = MaterialTheme.typography.titleMedium
                )
                val gitRepoUrl by viewModel.gitRepoUrl.collectAsState()
                val gitBranch by viewModel.gitBranch.collectAsState()
                val repoStatus by viewModel.repoStatus.collectAsState()

                OutlinedTextField(
                    value = gitRepoUrl,
                    onValueChange = { viewModel.onGitRepoUrlChange(it) },
                    label = { Text("Repository URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = gitBranch,
                    onValueChange = { viewModel.onGitBranchChange(it) },
                    label = { Text("Branch") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.checkAndCloneRepository() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Check & Clone")
                }
                if (repoStatus.isNotEmpty()) {
                    Text(repoStatus)
                }
            }
        }
        
        // Command input section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Execute Command",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        label = { Text("Enter command") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Button(
                        onClick = {
                            if (commandInput.isNotBlank()) {
                                viewModel.executeCommand(commandInput)
                                commandInput = ""
                            }
                        },
                        enabled = commandInput.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Execute")
                        }
                    }
                }
                
                // Quick command buttons
                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(listOf(
                        "ls -la" to "List files",
                        "pwd" to "Current directory",
                        "whoami" to "Current user",
                        "uname -a" to "System info",
                        "df -h" to "Disk usage",
                        "ps aux" to "Running processes"
                    )) { (command, description) ->
                        OutlinedButton(
                            onClick = { 
                                commandInput = command
                                viewModel.executeCommand(command)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("$command - $description")
                        }
                    }
                }
            }
        }
        
        // Output section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Command Output",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (commandOutput.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearOutput() }) {
                            Text("Clear")
                        }
                    }
                }
                
                // Debug: Show command output state
                Text(
                    text = "Debug: commandOutput length = ${commandOutput.length}, content = '$commandOutput'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (commandOutput.isEmpty()) {
                    Text(
                        text = "No output yet. Execute a command to see results here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            items(commandOutput.lines()) { line ->
                                Text(
                                    text = line,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Connection dialog
    WslConnectionDialog(
        isOpen = showConnectionDialog,
        onDismiss = { showConnectionDialog = false },
        onConnect = { connection ->
            viewModel.connectToWsl(connection)
        }
    )

    WslFileBrowserDialog(
        isOpen = fileBrowserState.isOpen,
        onDismiss = { viewModel.closeFileBrowser() },
        onSelect = { selectedPath ->
            viewModel.onFolderSelected(selectedPath)
        },
        onNavigate = { path -> viewModel.navigateToFile(path) },
        state = fileBrowserState
    )
}