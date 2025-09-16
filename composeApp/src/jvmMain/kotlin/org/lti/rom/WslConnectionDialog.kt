package org.lti.rom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun WslConnectionDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConnect: (WslService.WslConnection) -> Unit
) {
    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "WSL Connection Settings",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    var host by remember { mutableStateOf("localhost") }
                    var port by remember { mutableStateOf("22") }
                    var username by remember { mutableStateOf("wsl") }
                    var password by remember { mutableStateOf("") }
                    var useKeyAuth by remember { mutableStateOf(false) }
                    var keyPath by remember { mutableStateOf("") }
                    
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = useKeyAuth,
                            onCheckedChange = { useKeyAuth = it }
                        )
                        Text("Use SSH Key Authentication")
                    }
                    
                    if (useKeyAuth) {
                        OutlinedTextField(
                            value = keyPath,
                            onValueChange = { keyPath = it },
                            label = { Text("SSH Key Path") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val connection = WslService.WslConnection(
                                    host = host,
                                    port = port.toIntOrNull() ?: 22,
                                    username = username,
                                    password = password,
                                    useKeyAuth = useKeyAuth,
                                    keyPath = keyPath
                                )
                                onConnect(connection)
                                onDismiss()
                            }
                        ) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}
