package com.sshpad.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.domain.usecase.CreateSSHConnectionUseCase
import com.sshpad.app.domain.usecase.DeleteSSHConnectionUseCase
import com.sshpad.app.domain.usecase.GetRecentConnectionsUseCase
import com.sshpad.app.domain.usecase.GetSSHConnectionsUseCase
import com.sshpad.app.domain.usecase.UpdateLastConnectedAtUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Connection List Screen
 * 
 * Clean Architecture: Presentation Layer
 * Responsibility: Manage UI state for connection list display and user interactions
 */
class ConnectionListViewModel(
    private val getSSHConnectionsUseCase: GetSSHConnectionsUseCase,
    private val getRecentConnectionsUseCase: GetRecentConnectionsUseCase,
    private val createSSHConnectionUseCase: CreateSSHConnectionUseCase,
    private val deleteSSHConnectionUseCase: DeleteSSHConnectionUseCase,
    private val updateLastConnectedAtUseCase: UpdateLastConnectedAtUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionListUiState())
    val uiState: StateFlow<ConnectionListUiState> = _uiState.asStateFlow()

    // Connections list as StateFlow - automatically updates when data changes
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
        // Loading is managed by the connections StateFlow
        // Initial loading state will be set when first connection is received
        loadRecentConnections()
    }

    /**
     * Load recent connections
     */
    private fun loadRecentConnections() {
        viewModelScope.launch {
            try {
                val recent = getRecentConnectionsUseCase(3)
                _uiState.update { it.copy(recentConnections = recent) }
            } catch (e: Exception) {
                // Silently fail for recent connections - not critical
            }
        }
    }

    /**
     * Create a new SSH connection
     * @param connection The SSH connection to create
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
                    // Trigger navigation or callback
                    onConnectionCreated(id)
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
     * @param connectionId The ID of the connection to delete
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
     * Select a connection for quick connect
     * @param connectionId The ID of the connection to select
     */
    fun selectConnection(connectionId: String) {
        viewModelScope.launch {
            // Update last connected timestamp
            updateLastConnectedAtUseCase(connectionId)
            
            val connection = connections.value.find { it.id == connectionId }
            _uiState.update { 
                it.copy(selectedConnection = connection) 
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
     * Callback when a connection is created - can be overridden or observed
     */
    private fun onConnectionCreated(connectionId: String) {
        // This can be used to trigger navigation or other side effects
        // In practice, the UI observes uiState for success messages
    }
}

/**
 * UI State for Connection List Screen
 */
data class ConnectionListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedConnection: SSHConnection? = null,
    val showQuickConnect: Boolean = true,
    val recentConnections: List<SSHConnection> = emptyList()
)
