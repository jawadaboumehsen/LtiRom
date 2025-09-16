package org.lti.rom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
        val wslViewModel = remember { WslViewModel() }
        val currentScreen by wslViewModel.currentScreen.collectAsState()

        when (currentScreen) {
            Screen.SETUP -> SetupScreen(wslViewModel)
            Screen.MAIN -> {
                var currentTab by remember { mutableStateOf(0) }
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
            Screen.SETTINGS -> SettingsScreen(wslViewModel)
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
    }
}
