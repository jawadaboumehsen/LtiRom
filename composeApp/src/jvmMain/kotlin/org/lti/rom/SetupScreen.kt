package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Initial Setup", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Connection", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = host, onValueChange = { viewModel.onHostChange(it) }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = port, onValueChange = { viewModel.onPortChange(it) }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = username, onValueChange = { viewModel.onUsernameChange(it) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { viewModel.onPasswordChange(it) }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Project", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = currentDirectory, onValueChange = {}, label = { Text("Working Directory") }, enabled = false, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.openFileBrowser() }) {
                    Text("Select Working Directory")
                }
                OutlinedTextField(value = gitRepoUrl, onValueChange = { viewModel.onGitRepoUrlChange(it) }, label = { Text("Repository URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = gitBranch, onValueChange = { viewModel.onGitBranchChange(it) }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.saveSettings()
            viewModel.connectToWsl(
                WslService.WslConnection(
                    host = host,
                    port = port.toIntOrNull() ?: 22,
                    username = username,
                    password = password
                )
            )
            viewModel.navigateTo(Screen.MAIN)
        }) {
            Text("Save and Connect")
        }
    }
}
