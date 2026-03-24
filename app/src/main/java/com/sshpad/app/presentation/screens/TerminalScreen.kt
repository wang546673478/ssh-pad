package com.sshpad.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.sshpad.app.R
import com.sshpad.app.presentation.viewmodel.TerminalViewModel
import com.sshpad.app.presentation.ui.dialog.HostKeyConfirmDialog

/**
 * Terminal Screen - Interactive SSH terminal session
 * 
 * Now integrated with TerminalViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel,
    connectionId: String,
    onDisconnect: () -> Unit
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Collect output buffer from ViewModel
    val terminalOutput by viewModel.outputBuffer.collectAsStateWithLifecycle()
    
    // Collect connection state
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

    // Command input
    var commandInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    // Auto-connect when screen is shown
    LaunchedEffect(connectionId) {
        viewModel.connect(connectionId)
    }

    // Handle disconnection - navigate back when disconnected by remote
    LaunchedEffect(connectionState) {
        if (connectionState is com.sshpad.app.ssh.ConnectionState.Disconnected && uiState.isConnected) {
            onDisconnect()
        }
    }

    // Check if there's a pending host key verification and show dialog
    val pendingHostKey = uiState.pendingHostKey

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.connectionStatus,
                        fontSize = 14.sp,
                        color = if (uiState.isConnected) Color(0xFF00FF00) else Color.Red
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.disconnect(); onDisconnect() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Disconnect")
                    }
                },
                actions = {
                    // Zoom controls
                    IconButton(onClick = { viewModel.zoomIn() }) {
                        Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom In")
                    }
                    IconButton(onClick = { viewModel.zoomOut() }) {
                        Icon(Icons.Filled.ZoomOut, contentDescription = "Zoom Out")
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.clear_screen)) },
                                onClick = { 
                                    viewModel.clearOutput()
                                    showMenu = false 
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Clear, contentDescription = null)
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Disconnect", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    viewModel.disconnect()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Connection status indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isConnecting) "Connecting..." else 
                               if (uiState.isConnected) "● Connected" else "○ Disconnected",
                        color = if (uiState.isConnected) Color(0xFF00FF00) else 
                                if (uiState.isConnecting) Color.Yellow else Color.Red,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "${uiState.terminalWidth}x${uiState.terminalHeight}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                
                // Terminal Output
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = terminalOutput,
                        color = Color(0xFF00FF00), // Green terminal text
                        fontSize = uiState.fontSize.sp,
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
                        fontSize = uiState.fontSize.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { newInput: String -> commandInput = newInput },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = uiState.fontSize.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color.White
                        ),
                        singleLine = true,
                        enabled = uiState.isConnected,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (commandInput.isNotBlank()) {
                                    viewModel.sendCommand(commandInput)
                                    commandInput = ""
                                }
                            }
                        )
                    )
                }
            }

            // Show connecting indicator
            if (uiState.isConnecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF00FF00))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connecting to server...",
                            color = Color(0xFF00FF00),
                            fontSize = 14.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // Show host key confirmation dialog (TOFU)
            pendingHostKey?.let { fingerprint ->
                HostKeyConfirmDialog(
                    fingerprint = fingerprint,
                    onConfirm = { viewModel.confirmHostKey() },
                    onReject = { viewModel.rejectHostKey() }
                )
            }
        }
    }
}
