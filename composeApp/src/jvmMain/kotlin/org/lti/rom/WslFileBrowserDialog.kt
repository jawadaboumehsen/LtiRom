package org.lti.rom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun WslFileBrowserDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onNavigate: (String) -> Unit,
    state: FileBrowserState
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.currentPath != "/") {
                        IconButton(onClick = {
                            val parentPath = File(state.currentPath).parent ?: "/"
                            onNavigate(parentPath)
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                        }
                    }
                    Text("Select a folder: ${state.currentPath}")
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.files) { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (file.isDirectory) {
                                        onNavigate(file.path)
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Text(if (file.isDirectory) "📁" else "📄")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(file.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onSelect(state.currentPath) }) {
                    Text("Select")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
