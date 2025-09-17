package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.lti.rom.di.appModule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MaterialTheme {
            val wslViewModel: WslViewModel = koinInject()
            val currentScreen by wslViewModel.currentScreen.collectAsState()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Text("LtiRom", modifier = Modifier.padding(16.dp))
                        Divider()
                        NavigationDrawerItem(
                            label = { Text(text = "WSL Interface") },
                            selected = currentScreen == Screen.MAIN,
                            onClick = {
                                wslViewModel.navigateTo(Screen.MAIN)
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text(text = "Settings") },
                            selected = currentScreen == Screen.SETTINGS,
                            onClick = {
                                wslViewModel.navigateTo(Screen.SETTINGS)
                                scope.launch { drawerState.close() }
                            }
                        )
                        NavigationDrawerItem(
                            label = { Text(text = "About") },
                            selected = currentScreen == Screen.ABOUT,
                            onClick = {
                                wslViewModel.navigateTo(Screen.ABOUT)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("LtiRom - WSL Desktop Client") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentScreen) {
                            Screen.SETUP -> SetupScreen(wslViewModel)
                            Screen.MAIN -> WslInterface(wslViewModel)
                            Screen.SETTINGS -> SettingsScreen(wslViewModel)
                            Screen.ABOUT -> AboutContent()
                        }
                    }
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