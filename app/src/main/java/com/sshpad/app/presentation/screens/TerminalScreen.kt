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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sshpad.app.presentation.ui.theme.DraculaTheme

/**
 * Terminal Screen - Interactive SSH terminal session
 * Week 7: Display connection name in title bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    connectionName: String,
    connectionHost: String,
    onDisconnect: () -> Unit,
    onSendCommand: (String) -> Unit = {}
) {
    var terminalOutput by remember { mutableStateOf("SSH Pad Terminal v0.2.0\n") }
    var commandInput by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(14f) }
    var showMenu by remember { mutableStateOf(false) }
    var useDraculaTheme by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = connectionName,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = connectionHost,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
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
                                text = { 
                                    Text(if (useDraculaTheme) "Use Default Theme" else "Use Dracula Theme")
                                },
                                onClick = { 
                                    useDraculaTheme = !useDraculaTheme
                                    showMenu = false 
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Disconnect", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onDisconnect()
                                    showMenu = false
                                }
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
                .background(if (useDraculaTheme) DraculaTheme.background else Color.Black)
        ) {
            // Terminal Output
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = terminalOutput,
                    color = if (useDraculaTheme) DraculaTheme.foreground else Color(0xFF00FF00),
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace,
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
                    color = if (useDraculaTheme) DraculaTheme.foreground else Color(0xFF00FF00),
                    fontSize = fontSize.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = if (useDraculaTheme) DraculaTheme.foreground else Color.White,
                        unfocusedTextColor = if (useDraculaTheme) DraculaTheme.foreground else Color.White
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = fontSize.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    singleLine = true,
                    onKeyEvent = { event ->
                        if (event.nativeEvent.keyCode == 66) {
                            terminalOutput += "$$commandInput\n"
                            onSendCommand(commandInput)
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
