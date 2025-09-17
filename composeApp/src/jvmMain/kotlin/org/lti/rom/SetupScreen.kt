package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SetupScreen(viewModel: WslViewModel) {
    val gitRepoUrl by viewModel.gitRepoUrl.collectAsState()
    val gitBranch by viewModel.gitBranch.collectAsState()
    val host by viewModel.host.collectAsState()
    val port by viewModel.port.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val currentDirectory by viewModel.currentDirectory.collectAsState()
    val fileBrowserState by viewModel.fileBrowserState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    if (isConnected) {
        LaunchedEffect(isConnected) {
            viewModel.navigateTo(Screen.MAIN)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Initial Setup", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(value = host, onValueChange = { viewModel.onHostChange(it) }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = port, onValueChange = { viewModel.onPortChange(it) }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { viewModel.onUsernameChange(it) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { viewModel.onPasswordChange(it) }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = currentDirectory, onValueChange = {}, label = { Text("Working Directory") }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.openFileBrowser() }) {
                Text("Browse")
            }
        }
        OutlinedTextField(value = gitRepoUrl, onValueChange = { viewModel.onGitRepoUrlChange(it) }, label = { Text("Repository URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = gitBranch, onValueChange = { viewModel.onGitBranchChange(it) }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = {
            viewModel.saveSettings(
                host = host,
                port = port,
                username = username,
                password = password,
                workDir = currentDirectory,
                repoUrl = gitRepoUrl,
                branch = gitBranch,
                selectedTargetDevice = ""
            )
            viewModel.connectToWsl(
                WslService.WslConnection(
                    host = host,
                    port = port.toIntOrNull() ?: 22,
                    username = username,
                    password = password
                )
            )
        }) {
            Text("Save and Connect")
        }
    }

    if (fileBrowserState.isOpen) {
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
}
