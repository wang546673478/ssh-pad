package com.sshpad.app.presentation.screens

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.sshpad.app.R
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.presentation.viewmodel.ConnectionListUiState
import com.sshpad.app.presentation.viewmodel.ConnectionListViewModel

/**
 * Connection List Screen - Main screen showing all saved SSH connections
 * 
 * Now integrated with ConnectionListViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    viewModel: ConnectionListViewModel,
    onConnectionClick: (String) -> Unit,
    onAddConnection: () -> Unit,
    onEditConnection: (String) -> Unit
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Collect connections list from ViewModel
    val connections by viewModel.connections.collectAsStateWithLifecycle()

    // Show snackbar for success messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    // Show snackbar for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, "Dismiss")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ssh_connections)) },
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
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Connect Section (Recent Connections)
            if (uiState.recentConnections.isNotEmpty()) {
                RecentConnectionsSection(
                    connections = uiState.recentConnections,
                    onConnectionClick = onConnectionClick
                )
            }

            // All Connections List
            Text(
                text = stringResource(R.string.all_connections),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.isLoading && connections.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (connections.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_connections_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.tap_to_add_connection),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(connections, key = { it.id }) { connection ->
                        ConnectionListItem(
                            connection = connection,
                            onClick = { onConnectionClick(connection.id) },
                            onEdit = { onEditConnection(connection.id) },
                            onDelete = { 
                                // Show confirmation before deleting
                                // In production, use a dialog
                                viewModel.deleteConnection(connection.id) 
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recent Connections Section
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                text = stringResource(R.string.quick_connect),
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
                        selected = true,
                        onClick = { onConnectionClick(connection.id) },
                        label = { Text(connection.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ComposeColor(Color.parseColor(connection.color)))
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Single Connection List Item with delete action
 */
@Composable
private fun ConnectionListItem(
    connection: SSHConnection,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                            ComposeColor(Color.parseColor(connection.color))
                        else 
                            ComposeColor.Gray
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

            // Edit and delete buttons
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_connection)) },
            text = { Text(stringResource(R.string.delete_confirmation, connection.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
