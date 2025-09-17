package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Connection", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(value = host, onValueChange = { viewModel.onHostChange(it) }, label = { Text("Host") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = port, onValueChange = { viewModel.onPortChange(it) }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = username, onValueChange = { viewModel.onUsernameChange(it) }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = password, onValueChange = { viewModel.onPasswordChange(it) }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Project", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(value = currentDirectory, onValueChange = {}, label = { Text("Working Directory") }, enabled = false, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.openFileBrowser() }) {
                        Text("Select Working Directory")
                    }
                    OutlinedTextField(value = gitRepoUrl, onValueChange = { viewModel.onGitRepoUrlChange(it) }, label = { Text("Repository URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = gitBranch, onValueChange = { viewModel.onGitBranchChange(it) }, label = { Text("Branch") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.checkAndCloneRepository() }) {
                        Text("Re-check Repository")
                    }
                }
            }
        }

        item {
            Row {
                Button(onClick = {
                    viewModel.saveSettings()
                    viewModel.navigateTo(Screen.MAIN)
                }) {
                    Text("Save and Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.navigateTo(Screen.MAIN) }) {
                    Text("Back")
                }
            }
        }
    }
}
