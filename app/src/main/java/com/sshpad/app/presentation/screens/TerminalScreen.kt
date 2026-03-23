package com.sshpad.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Terminal Screen - Interactive SSH terminal session
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    connectionId: String,
    onDisconnect: () -> Unit
) {
    var terminalOutput by remember { mutableStateOf("") }
    var commandInput by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(14f) }
    var showMenu by remember { mutableStateOf(false) }

    // Mock terminal output - will be replaced with actual SSH output
    LaunchedEffect(connectionId) {
        terminalOutput = """
SSH Pad Terminal v0.1.0
Connecting to $connectionId...

Welcome to Ubuntu 22.04.3 LTS (GNU/Linux 5.15.0-91-generic x86_64)

 * Documentation:  https://help.ubuntu.com
 * Management:     https://landscape.canonical.com
 * Support:        https://ubuntu.com/advantage

Last login: Mon Mar 23 16:00:00 2026 from 192.168.1.50

$ """.trimIndent()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Terminal",
                        fontSize = 14.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Disconnect")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Zoom In") },
                                onClick = { 
                                    fontSize = (fontSize + 2).coerceAtMost(24f)
                                    showMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Zoom Out") },
                                onClick = { 
                                    fontSize = (fontSize - 2).coerceAtLeast(10f)
                                    showMenu = false 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Screen") },
                                onClick = { 
                                    terminalOutput = "$ \n"
                                    showMenu = false 
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Disconnect", color = MaterialTheme.colorScheme.error) },
                                onClick = onDisconnect
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Terminal Output
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = terminalOutput,
                    color = Color(0xFF00FF00), // Green terminal text
                    fontSize = fontSize.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Command Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = Color(0xFF00FF00),
                    fontSize = fontSize.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = fontSize.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = Color.White
                    ),
                    singleLine = true,
                    onKeyEvent = { event ->
                        // Handle Enter key
                        if (event.nativeEvent.keyCode == 66) { // KeyEvent.KEYCODE_ENTER
                            terminalOutput += "$commandInput\n"
                            // TODO: Send command to SSH server
                            commandInput = ""
                            true
                        } else {
                            false
                        }
                    }
                )
            }
        }
    }
}
