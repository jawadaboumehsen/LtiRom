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
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.lti.rom.di.appModule
import org.lti.rom.ui.theme.LtiRomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        LtiRomTheme {
            val wslViewModel: WslViewModel = koinInject()
            val currentScreen by wslViewModel.currentScreen.collectAsState()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                ModalDrawerSheet(modifier = Modifier.padding(16.dp)) {
                    Text("LtiRom", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
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
                            title = { Text("LtiRom", style = MaterialTheme.typography.headlineLarge) },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
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
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "A Kotlin Multiplatform desktop application for connecting to and managing WSL (Windows Subsystem for Linux).",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Developed by: Jules",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "For more information, please visit the project's repository.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
