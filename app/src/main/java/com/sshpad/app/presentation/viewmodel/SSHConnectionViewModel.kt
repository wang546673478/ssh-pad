package com.sshpad.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.domain.usecase.ConnectToServerUseCase
import com.sshpad.app.domain.usecase.CreateSSHConnectionUseCase
import com.sshpad.app.domain.usecase.DeleteSSHConnectionUseCase
import com.sshpad.app.domain.usecase.GetSSHConnectionsUseCase
import com.sshpad.app.ssh.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for SSH Connection management
 * 
 * Clean Architecture: Presentation Layer
 * Responsibility: Manage UI state for connection list and operations
 */
class SSHConnectionViewModel(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase,
    private val deleteSSHConnectionUseCase: DeleteSSHConnectionUseCase,
    private val connectToServerUseCase: ConnectToServerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SSHConnectionUiState())
    val uiState: StateFlow<SSHConnectionUiState> = _uiState.asStateFlow()

    // Connections list as StateFlow
    val connections: StateFlow<List<SSHConnection>> = getSSHConnectionsUseCase()
        .catch { e ->
            _uiState.update { it.copy(error = "Failed to load connections: ${e.message}") }
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadConnections()
    }

    /**
     * Load all connections
     */
    private fun loadConnections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Connections are loaded via the flow
        }
    }

    /**
     * Create a new SSH connection
     */
    fun createConnection(connection: SSHConnection) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            createSSHConnectionUseCase(connection)
                .onSuccess { id ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Connection '$id' created successfully"
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to create connection: ${e.message}"
                        ) 
                    }
                }
        }
    }

    /**
     * Delete an SSH connection
     */
    fun deleteConnection(connectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            deleteSSHConnectionUseCase(connectionId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Connection deleted successfully"
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to delete connection: ${e.message}"
                        ) 
                    }
                }
        }
    }

    /**
     * Connect to an SSH server
     */
    fun connectToServer(connectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            connectToServerUseCase(connectionId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Connected successfully"
                        ) 
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to connect: ${e.message}"
                        ) 
                    }
                }
        }
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
     * Factory for creating ViewModel with dependencies
     */
    class Factory(
        private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
        private val createSSHConnectionUseCase: CreateSSHConnectionUseCase,
        private val deleteSSHConnectionUseCase: DeleteSSHConnectionUseCase,
        private val connectToServerUseCase: ConnectToServerUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SSHConnectionViewModel::class.java)) {
                return SSHConnectionViewModel(
                    getSSHConnectionsUseCase,
                    createSSHConnectionUseCase,
                    deleteSSHConnectionUseCase,
                    connectToServerUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI State for SSH Connection screen
 */
data class SSHConnectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedConnection: SSHConnection? = null
)
