package com.sshpad.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sshpad.app.data.model.TabManagerState
import com.sshpad.app.data.model.TabSession
import com.sshpad.app.data.model.TerminalSession
import com.sshpad.app.presentation.ui.theme.DraculaTheme

/**
 * Multi-tab Terminal Screen
 * Week 8: Multi-tab Session Management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiTabTerminalScreen(
    initialSessions: List<TerminalSession> = emptyList(),
    onDisconnect: (String) -> Unit,
    onSendCommand: (String, String) -> Unit = { _, _ -> }
) {
    var tabState by remember {
        mutableStateOf(
            TabManagerState().let { state ->
                initialSessions.fold(state) { acc, session ->
                    acc.addTab(TabSession(terminalSession = session))
                }
            }
        )
    }
    var showMenu by remember { mutableStateOf(false) }
    var showNewConnectionDialog by remember { mutableStateOf(false) }

    val activeSession = tabState.activeTab?.terminalSession

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("SSH Pad - Multi-Tab") },
                    navigationIcon = {
                        IconButton(onClick = { /* Navigate back */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNewConnectionDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "New Tab")
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
                                    text = { Text("Next Tab") },
                                    onClick = { 
                                        tabState = tabState.nextTab()
                                        showMenu = false 
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ArrowForward, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Previous Tab") },
                                    onClick = { 
                                        tabState = tabState.previousTab()
                                        showMenu = false 
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Close Current Tab") },
                                    onClick = { 
                                        tabState.activeTabId?.let { tabId ->
                                            onDisconnect(tabId)
                                            tabState = tabState.removeTab(tabId)
                                        }
                                        showMenu = false 
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                )
                            }
                        }
                    }
                )
                
                // Tab Strip
                if (tabState.tabs.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = tabState.activeTabIndex.coerceAtLeast(0),
                        modifier = Modifier.height(48.dp),
                        edgePadding = 0.dp
                    ) {
                        tabState.tabs.forEach { tab ->
                            val isSelected = tab.tabId == tabState.activeTabId
                            Tab(
                                selected = isSelected,
                                onClick = { tabState = tabState.activateTab(tab.tabId) },
                                text = {
                                    Text(
                                        text = tab.terminalSession.connectionName.ifEmpty { "Terminal" },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            activeSession?.let { session ->
                TerminalView(
                    session = session,
                    onDisconnect = { onDisconnect(tabState.activeTabId ?: "") },
                    onSendCommand = onSendCommand
                )
            }
            
            if (tabState.tabs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No active sessions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showNewConnectionDialog = true }) {
                            Text("Open New Connection")
                        }
                    }
                }
            }
        }
    }
    
    // New Connection Dialog
    if (showNewConnectionDialog) {
        AlertDialog(
            onDismissRequest = { showNewConnectionDialog = false },
            title = { Text("New Terminal Tab") },
            text = { Text("Enter connection details to open a new terminal tab.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement new connection logic
                        showNewConnectionDialog = false
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewConnectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TerminalView(
    session: TerminalSession,
    onDisconnect: () -> Unit,
    onSendCommand: (String, String) -> Unit
) {
    var terminalOutput by remember { mutableStateOf("SSH Pad Terminal v0.3.0\n") }
    var commandInput by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(session.fontSize) }
    var showMenu by remember { mutableStateOf(false) }
    var useDraculaTheme by remember { mutableStateOf(session.theme == TerminalSession.TerminalTheme.DARK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                        onSendCommand(session.sessionId, commandInput)
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
