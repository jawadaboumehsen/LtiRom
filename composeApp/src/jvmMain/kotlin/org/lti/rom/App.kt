package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentTab by remember { mutableStateOf(0) }
        val wslViewModel = remember { WslViewModel() }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = { Text("LtiRom - WSL Desktop Client") }
            )

            // Tab Row
            TabRow(selectedTabIndex = currentTab) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    text = { Text("WSL Interface") }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    text = { Text("About") }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (currentTab) {
                    0 -> WslInterface(wslViewModel)
                    1 -> AboutContent()
                }
            }
        }
    }
}

@Composable
fun AboutContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LtiRom WSL Desktop Client",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "A Kotlin Multiplatform desktop application for connecting to and managing WSL (Windows Subsystem for Linux).",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium
                )

                val features = listOf(
                    "Connect to WSL via SSH",
                    "Execute commands directly",
                    "View command output in real-time",
                    "Quick access to common commands",
                    "List available WSL distributions",
                    "Modern Material 3 UI"
                )

                features.forEach { feature ->
                    Text("• $feature")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Getting Started:",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("1. Ensure WSL is installed and running on your Windows system")
                Text("2. Click 'Connect' to establish a connection to WSL")
                Text("3. Use the command input to execute Linux commands")
                Text("4. View results in the output panel below")
            }
        }

        val greeting = remember { Greeting().greet() }
        Text(
            text = "Built with: $greeting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}