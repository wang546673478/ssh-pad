package com.sshpad.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sshpad.app.R
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.presentation.viewmodel.ConnectionEditViewModel

/**
 * Connection Edit Screen - Add or edit SSH connection
 * 
 * Now integrated with ConnectionEditViewModel for state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionEditScreen(
    viewModel: ConnectionEditViewModel,
    connectionId: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // Initialize ViewModel with connectionId for edit mode
    LaunchedEffect(connectionId) {
        if (connectionId != null) {
            viewModel.setConnectionId(connectionId)
        }
    }
    
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle success message and trigger navigation
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            onSave()
            viewModel.clearSuccessMessage()
        }
    }

    // Handle errors
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error, "Dismiss")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.edit_connection) else stringResource(R.string.new_connection)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveConnection() },
                        enabled = viewModel.isFormValid() && !uiState.isLoading
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.isEditMode) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Name
                OutlinedTextField(
                    value = uiState.connectionName,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text(stringResource(R.string.connection_name)) },
                    placeholder = { Text(stringResource(R.string.connection_name_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("name") == true
                )

                // Host
                OutlinedTextField(
                    value = uiState.host,
                    onValueChange = { viewModel.onHostChange(it) },
                    label = { Text(stringResource(R.string.host)) },
                    placeholder = { Text(stringResource(R.string.host_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("Host") == true
                )

                // Port
                OutlinedTextField(
                    value = uiState.port,
                    onValueChange = { viewModel.onPortChange(it) },
                    label = { Text(stringResource(R.string.port)) },
                    modifier = Modifier.width(120.dp),
                    singleLine = true
                )

                // Username
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChange(it) },
                    label = { Text(stringResource(R.string.username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("Username") == true
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
                            text = stringResource(R.string.authentication),
                            style = MaterialTheme.typography.titleSmall
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.authType == SSHConnection.AuthType.PASSWORD.name,
                                onClick = { viewModel.onAuthTypeChange(SSHConnection.AuthType.PASSWORD) },
                                label = { Text(stringResource(R.string.password)) }
                            )
                            FilterChip(
                                selected = uiState.authType == SSHConnection.AuthType.PRIVATE_KEY.name,
                                onClick = { viewModel.onAuthTypeChange(SSHConnection.AuthType.PRIVATE_KEY) },
                                label = { Text(stringResource(R.string.private_key)) }
                            )
                        }
                    }
                }

                // Password or Private Key based on auth type
                when (uiState.authType) {
                    SSHConnection.AuthType.PASSWORD.name -> {
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = { Text(stringResource(R.string.password)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                    }
                    SSHConnection.AuthType.PRIVATE_KEY.name -> {
                        OutlinedButton(
                            onClick = { /* Open file picker */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.select_private_key_file))
                        }
                    }
                }

                // Keep-Alive Interval
                OutlinedTextField(
                    value = uiState.keepAlive,
                    onValueChange = { viewModel.onKeepAliveChange(it) },
                    label = { Text(stringResource(R.string.keep_alive_interval)) },
                    modifier = Modifier.width(200.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.weight(1f))

                // Save button (for non-FAB platforms)
                Button(
                    onClick = { viewModel.saveConnection() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.isFormValid() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isEditMode) stringResource(R.string.update_connection) else stringResource(R.string.create_connection))
                }
            }
        }
    }
}
