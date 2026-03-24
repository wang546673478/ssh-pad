package com.sshpad.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.domain.usecase.CreateSSHConnectionUseCase
import com.sshpad.app.domain.usecase.GetSSHConnectionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Connection Edit Screen (Add/Edit)
 * 
 * Clean Architecture: Presentation Layer
 * Responsibility: Manage UI state for connection form, validation, and save operations
 */
class ConnectionEditViewModel(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionEditUiState())
    val uiState: StateFlow<ConnectionEditUiState> = _uiState.asStateFlow()

    // Connection ID for edit mode (set via setConnectionId)
    private var editConnectionId: String? = null
    
    // Flag to prevent duplicate loading of connection data
    private var isDataLoaded = false

    // Form state - persisted in ViewModel (survives configuration changes via StateFlow)
    private var connectionName: String? = null
    private var host: String? = null
    private var port: String? = null
    private var username: String? = null
    private var authType: String? = null

    /**
     * Set connection ID for edit mode
     */
    fun setConnectionId(connectionId: String?) {
        editConnectionId = connectionId
        if (connectionId != null && !isDataLoaded) {
            loadConnectionData()
        }
    }

    /**
     * Load connection data if editing existing connection
     */
    private fun loadConnectionData() {
        // Prevent duplicate loading
        if (isDataLoaded) return
        
        val id = editConnectionId
        if (id != null) {
            isDataLoaded = true
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                
                // Find connection from all connections
                getSSHConnectionsUseCase().collect { connections ->
                    val existingConnection = connections.find { it.id == id }
                    if (existingConnection != null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isEditMode = true,
                                connectionName = existingConnection.name,
                                host = existingConnection.host,
                                port = existingConnection.port.toString(),
                                username = existingConnection.username,
                                authType = existingConnection.authType.name,
                                keepAlive = existingConnection.keepAliveInterval.toString()
                            ) 
                        }
                        // Restore form state
                        connectionName = existingConnection.name
                        host = existingConnection.host
                        port = existingConnection.port.toString()
                        username = existingConnection.username
                        authType = existingConnection.authType.name
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Connection not found") }
                    }
                }
            }
        }
    }

    /**
     * Update connection name
     */
    fun onNameChange(name: String) {
        connectionName = name
        _uiState.update { it.copy(connectionName = name) }
    }

    /**
     * Update host
     */
    fun onHostChange(host: String) {
        this.host = host
        _uiState.update { it.copy(host = host) }
    }

    /**
     * Update port
     */
    fun onPortChange(port: String) {
        this.port = port
        _uiState.update { it.copy(port = port) }
    }

    /**
     * Update username
     */
    fun onUsernameChange(username: String) {
        this.username = username
        _uiState.update { it.copy(username = username) }
    }

    /**
     * Update password
     */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Update authentication type
     */
    fun onAuthTypeChange(authType: SSHConnection.AuthType) {
        this.authType = authType.name
        _uiState.update { it.copy(authType = authType.name) }
    }

    /**
     * Update keep-alive interval
     */
    fun onKeepAliveChange(keepAlive: String) {
        _uiState.update { it.copy(keepAlive = keepAlive) }
    }

    /**
     * Validate and save connection
     */
    fun saveConnection() {
        viewModelScope.launch {
            val name = connectionName?.trim()
            val host = this@ConnectionEditViewModel.host?.trim()
            val portStr = port
            val username = this@ConnectionEditViewModel.username?.trim()
            val keepAlive = _uiState.value.keepAlive
            
            // Validate required fields
            if (name.isNullOrBlank()) {
                _uiState.update { it.copy(error = "Connection name is required") }
                return@launch
            }
            
            if (host.isNullOrBlank()) {
                _uiState.update { it.copy(error = "Host is required") }
                return@launch
            }
            
            if (username.isNullOrBlank()) {
                _uiState.update { it.copy(error = "Username is required") }
                return@launch
            }
            
            // Parse port
            val port = portStr?.toIntOrNull() ?: 22
            
            // Create connection object
            val connection = SSHConnection(
                id = editConnectionId ?: generateConnectionId(),
                name = name!!,
                host = host!!,
                port = port,
                username = username!!,
                authType = SSHConnection.AuthType.valueOf(authType ?: "PASSWORD"),
                keepAliveInterval = keepAlive.toIntOrNull() ?: 60
            )
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Save connection
            createSSHConnectionUseCase(connection)
                .onSuccess { id ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Connection saved successfully"
                        ) 
                    }
                    onSaveSuccess(id)
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to save connection: ${e.message}"
                        ) 
                    }
                }
        }
    }

    /**
     * Generate a unique ID for new connections
     */
    private fun generateConnectionId(): String {
        return "conn_${System.currentTimeMillis()}"
    }

    /**
     * Callback when save is successful
     */
    private fun onSaveSuccess(connectionId: String) {
        // UI should observe uiState for success message and handle navigation
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Check if form is valid
     */
    fun isFormValid(): Boolean {
        val name = connectionName?.trim()
        val host = this@ConnectionEditViewModel.host?.trim()
        val username = this@ConnectionEditViewModel.username?.trim()
        
        return !name.isNullOrBlank() && !host.isNullOrBlank() && !username.isNullOrBlank()
    }
}

/**
 * UI State for Connection Edit Screen
 */
data class ConnectionEditUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val connectionName: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val password: String = "",
    val authType: String = SSHConnection.AuthType.PASSWORD.name,
    val keepAlive: String = "60"
)
