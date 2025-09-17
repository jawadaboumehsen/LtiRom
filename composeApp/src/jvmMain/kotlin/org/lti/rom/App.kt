package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.mp.KoinPlatform
import org.lti.rom.di.appModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val wslViewModel: WslViewModel = KoinPlatform.getKoin().get()
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
                            selected = false,
                            onClick = {
                                // TODO: Create an About screen
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
    }
}
