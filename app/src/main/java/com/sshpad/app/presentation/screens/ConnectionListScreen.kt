package com.sshpad.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sshpad.app.data.model.SSHConnection

/**
 * Connection List Screen - Main screen showing all saved SSH connections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    onConnectionClick: (String) -> Unit,
    onAddConnection: () -> Unit,
    onEditConnection: (String) -> Unit
) {
    // Sample data - will be replaced with actual data from ViewModel
    val connections = remember {
        mutableStateOf(listOf(
            SSHConnection(
                id = "1",
                name = "Production Server",
                host = "192.168.1.100",
                username = "admin",
                lastConnectedAt = System.currentTimeMillis() - 3600000
            ),
            SSHConnection(
                id = "2",
                name = "Development Server",
                host = "192.168.1.101",
                username = "dev",
                lastConnectedAt = System.currentTimeMillis() - 86400000
            ),
            SSHConnection(
                id = "3",
                name = "Staging",
                host = "staging.example.com",
                username = "deploy",
                lastConnectedAt = null
            )
        ))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SSH Connections") },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddConnection,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Connection")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Connect Section (Recent Connections)
            if (connections.value.any { it.lastConnectedAt != null }) {
                RecentConnectionsSection(
                    connections = connections.value.filter { it.lastConnectedAt != null }
                        .sortedByDescending { it.lastConnectedAt }
                        .take(3),
                    onConnectionClick = onConnectionClick
                )
            }

            // All Connections List
            Text(
                text = "All Connections",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(connections.value, key = { it.id }) { connection ->
                    ConnectionListItem(
                        connection = connection,
                        onClick = { onConnectionClick(connection.id) },
                        onEdit = { onEditConnection(connection.id) }
                    )
                }
            }
        }
    }
}

/**
 * Recent Connections Section
 */
@Composable
private fun RecentConnectionsSection(
    connections: List<SSHConnection>,
    onConnectionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Connect",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                connections.forEach { connection ->
                    FilterChip(
                        onClick = { onConnectionClick(connection.id) },
                        label = { Text(connection.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(connection.color))
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Single Connection List Item
 */
@Composable
private fun ConnectionListItem(
    connection: SSHConnection,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (connection.lastConnectedAt != null) 
                            Color(connection.color) 
                        else 
                            Color.Gray
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Connection info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = connection.getConnectionString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
