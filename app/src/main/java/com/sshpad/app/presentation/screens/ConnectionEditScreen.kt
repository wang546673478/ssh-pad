package com.sshpad.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sshpad.app.data.model.SSHConnection

/**
 * Connection Edit Screen - Add or edit SSH connection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionEditScreen(
    connectionId: String? = null,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authType by remember { mutableStateOf(SSHConnection.AuthType.PASSWORD) }
    var keepAlive by remember { mutableStateOf("60") }

    // If editing, load connection data (mock for now)
    LaunchedEffect(connectionId) {
        if (connectionId != null) {
            // TODO: Load connection from repository
            name = "Production Server"
            host = "192.168.1.100"
            username = "admin"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (connectionId == null) "New Connection" else "Edit Connection") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSave() },
                        enabled = name.isNotBlank() && host.isNotBlank() && username.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Connection Name") },
                placeholder = { Text("e.g., Production Server") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Host
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Host") },
                placeholder = { Text("IP address or hostname") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Port
            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.width(120.dp),
                singleLine = true
            )

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Authentication Type
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Authentication",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = authType == SSHConnection.AuthType.PASSWORD,
                            onClick = { authType = SSHConnection.AuthType.PASSWORD },
                            label = { Text("Password") }
                        )
                        FilterChip(
                            selected = authType == SSHConnection.AuthType.PRIVATE_KEY,
                            onClick = { authType = SSHConnection.AuthType.PRIVATE_KEY },
                            label = { Text("Private Key") }
                        )
                    }
                }
            }

            // Password or Private Key based on auth type
            when (authType) {
                SSHConnection.AuthType.PASSWORD -> {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        password = true
                    )
                }
                SSHConnection.AuthType.PRIVATE_KEY -> {
                    OutlinedButton(
                        onClick = { /* Open file picker */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select Private Key File")
                    }
                }
            }

            // Keep-Alive Interval
            OutlinedTextField(
                value = keepAlive,
                onValueChange = { keepAlive = it },
                label = { Text("Keep-Alive Interval (seconds)") },
                modifier = Modifier.width(200.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save button (for non-FAB platforms)
            Button(
                onClick = { onSave() },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && host.isNotBlank() && username.isNotBlank()
            ) {
                Text("Save Connection")
            }
        }
    }
}
