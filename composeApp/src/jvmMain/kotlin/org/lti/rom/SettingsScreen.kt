package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: WslViewModel) {
    val gitRepoUrl by viewModel.gitRepoUrl.collectAsState()
    val gitBranch by viewModel.gitBranch.collectAsState()
    val host by viewModel.host.collectAsState()
    val port by viewModel.port.collectAsState()
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val currentDirectory by viewModel.currentDirectory.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = host,
            onValueChange = { viewModel.onHostChange(it) },
            label = { Text("Host") }
        )
        OutlinedTextField(
            value = port,
            onValueChange = { viewModel.onPortChange(it) },
            label = { Text("Port") }
        )
        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Password") }
        )
        OutlinedTextField(
            value = gitRepoUrl,
            onValueChange = { viewModel.onGitRepoUrlChange(it) },
            label = { Text("Repository URL") }
        )
        OutlinedTextField(
            value = gitBranch,
            onValueChange = { viewModel.onGitBranchChange(it) },
            label = { Text("Branch") }
        )
        OutlinedTextField(
            value = currentDirectory,
            onValueChange = { /* This will be handled by the file browser */ },
            label = { Text("Working Directory") },
            enabled = false
        )
        Button(onClick = { viewModel.openFileBrowser() }) {
            Text("Select Working Directory")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = { viewModel.saveSettings() }) {
                Text("Save")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.checkAndCloneRepository() }) {
                Text("Re-check Repository")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.navigateTo(Screen.MAIN) }) {
                Text("Back")
            }
        }
    }
}
