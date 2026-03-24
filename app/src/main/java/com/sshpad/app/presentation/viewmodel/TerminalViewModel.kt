package com.sshpad.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.domain.usecase.ConnectToServerUseCase
import com.sshpad.app.ssh.ConnectionState
import com.sshpad.app.ssh.SSHClientWrapper
import com.sshpad.app.ssh.verifier.ServerFingerprint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Terminal screen
 * 
 * Clean Architecture: Presentation Layer
 * Responsibility: Manage terminal UI state and SSH session
 */
class TerminalViewModel(
    private val sshClientWrapper: SSHClientWrapper,
    private val connectToServerUseCase: ConnectToServerUseCase,
    private val repository: SSHConnectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState: StateFlow<TerminalUiState> = _uiState.asStateFlow()

    // Connection state from SSH client
    val connectionState: StateFlow<ConnectionState> = connectToServerUseCase.getConnectionState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.Disconnected
        )

    // Terminal output buffer
    private val _outputBuffer = MutableStateFlow<String>("")
    val outputBuffer: StateFlow<String> = _outputBuffer.asStateFlow()

    // Current connection ID
    private var currentConnectionId: String? = null

    // Current connection host/port for TOFU verification
    private var currentConnectionHost: String? = null
    private var currentConnectionPort: Int = 22

    init {
        // Listen to pending host key verification flow from SSH client wrapper
        viewModelScope.launch {
            sshClientWrapper.pendingHostKeyVerification.collect { fingerprint ->
                _uiState.update { it.copy(pendingHostKey = fingerprint) }
            }
        }
    }

    /**
     * Check if there's a pending host key verification (TOFU)
     */
    fun hasPendingHostKeyVerification(): Boolean {
        return _uiState.value.pendingHostKey != null
    }

    /**
     * Confirm and accept the pending host key (TOFU)
     * 
     * This is called when the user clicks "Accept" on the host key confirmation dialog.
     */
    fun confirmHostKey() {
        val host = currentConnectionHost ?: return
        
        viewModelScope.launch {
            sshClientWrapper.acceptHostKey(host, currentConnectionPort)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            pendingHostKey = null,
                            isConnecting = true, // Continue connecting
                            connectionStatus = "Host key accepted, connecting..."
                        ) 
                    }
                    appendOutput("✓ Host key accepted\n")
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            pendingHostKey = null,
                            isConnecting = false,
                            isConnected = false,
                            connectionStatus = "Failed",
                            error = "Failed to accept host key: ${e.message}"
                        ) 
                    }
                    appendOutput("✗ Error accepting host key: ${e.message}\n")
                }
        }
    }

    /**
     * Reject the pending host key
     * 
     * This is called when the user clicks "Reject" on the host key confirmation dialog.
     */
    fun rejectHostKey() {
        val host = currentConnectionHost ?: return
        
        viewModelScope.launch {
            sshClientWrapper.rejectHostKey(host, currentConnectionPort)
            
            _uiState.update { 
                it.copy(
                    pendingHostKey = null,
                    isConnecting = false,
                    isConnected = false,
                    connectionStatus = "Rejected",
                    error = "Host key rejected - connection aborted"
                ) 
            }
            
            appendOutput("✗ Host key rejected - connection aborted\n")
            
            // Disconnect and clean up
            sshClientWrapper.disconnect()
            currentConnectionId = null
        }
    }

    /**
     * Connect to SSH server
     * @param connectionId The ID of the connection to use
     */
    fun connect(connectionId: String) {
        // Guard: prevent connection with invalid/empty connectionId
        if (connectionId.isBlank()) {
            _uiState.update { it.copy(error = "Invalid connection ID", isConnecting = false) }
            return
        }

        if (currentConnectionId == connectionId && isConnected()) {
            // Already connected to this server
            return
        }

        viewModelScope.launch {
            currentConnectionId = connectionId
            _uiState.update { it.copy(isConnecting = true, error = null, isConnected = false, pendingHostKey = null) }
            
            // Get connection to extract host/port for TOFU verification
            val connection = repository.getConnectionById(connectionId)
            if (connection != null) {
                currentConnectionHost = connection.host
                currentConnectionPort = connection.port
            } else {
                // Connection not found - should not happen if called from valid navigation
                _uiState.update { it.copy(isConnecting = false, error = "Connection not found") }
                appendOutput("✗ Error: Connection '$connectionId' not found\n")
                return@launch
            }
            
            connectToServerUseCase(connectionId)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isConnecting = false, 
                            isConnected = true,
                            connectionStatus = "Connected"
                        ) 
                    }
                    appendOutput("✓ Connected to server successfully\n\n")
                }
                .onFailure { e ->
                    // Check if it's a host key verification timeout
                    val errorMessage = e.message ?: "Unknown error"
                    if (errorMessage.contains("TOFU", ignoreCase = true) || 
                        errorMessage.contains("host key", ignoreCase = true)) {
                        // Host key verification failed or timed out
                        _uiState.update { 
                            it.copy(
                                isConnecting = false,
                                isConnected = false,
                                connectionStatus = "Verification Failed",
                                error = "Host key verification failed: ${e.message}"
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isConnecting = false,
                                isConnected = false,
                                connectionStatus = "Failed",
                                error = "Connection failed: ${e.message}"
                            ) 
                        }
                    }
                    appendOutput("✗ Error: ${e.message}\n")
                }
        }
    }

    /**
     * Disconnect from SSH server
     */
    fun disconnect() {
        viewModelScope.launch {
            sshClientWrapper.disconnect()
            currentConnectionId = null
            _uiState.update { 
                it.copy(
                    isConnected = false,
                    connectionStatus = "Disconnected"
                ) 
            }
            appendOutput("\n[Disconnected from server]\n")
        }
    }

    /**
     * Send command to terminal
     * @param command The command to execute
     */
    fun sendCommand(command: String) {
        if (!isConnected()) {
            appendOutput("Error: Not connected to a server\n")
            return
        }

        viewModelScope.launch {
            sshClientWrapper.sendInput("$command\n")
            appendOutput("$ $command\n")
        }
    }

    /**
     * Send raw input to terminal (for special keys)
     * @param input The raw input to send
     */
    fun sendInput(input: String) {
        if (!isConnected()) {
            return
        }

        viewModelScope.launch {
            sshClientWrapper.sendInput(input)
        }
    }

    /**
     * Resize terminal
     * @param width Terminal width in characters
     * @param height Terminal height in characters
     */
    fun resizeTerminal(width: Int, height: Int) {
        _uiState.update { 
            it.copy(
                terminalWidth = width,
                terminalHeight = height
            ) 
        }
        sshClientWrapper.resizeTerminal(width, height)
        appendOutput("[Terminal resized to ${width}x${height}]\n")
    }

    /**
     * Clear terminal output
     */
    fun clearOutput() {
        _outputBuffer.update { "" }
    }

    /**
     * Zoom in (increase font size)
     */
    fun zoomIn() {
        val newFontSize = (_uiState.value.fontSize + 2).coerceAtMost(24f)
        _uiState.update { it.copy(fontSize = newFontSize) }
    }

    /**
     * Zoom out (decrease font size)
     */
    fun zoomOut() {
        val newFontSize = (_uiState.value.fontSize - 2).coerceAtLeast(10f)
        _uiState.update { it.copy(fontSize = newFontSize) }
    }

    /**
     * Reset font size to default
     */
    fun resetFontSize() {
        _uiState.update { it.copy(fontSize = 14f) }
    }

    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return _uiState.value.isConnected && connectionState.value is ConnectionState.Connected
    }

    /**
     * Get current connection status text
     */
    fun getConnectionStatusText(): String {
        return when (val state = connectionState.value) {
            is ConnectionState.Connected -> "Connected"
            is ConnectionState.Connecting -> "Connecting..."
            is ConnectionState.Disconnected -> "Disconnected"
            is ConnectionState.Authenticating -> "Authenticating..."
            is ConnectionState.Error -> "Error: ${state.message}"
        }
    }

    /**
     * Append output to terminal buffer
     */
    private fun appendOutput(text: String) {
        _outputBuffer.update { it + text }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Disconnect when ViewModel is cleared to prevent resource leaks
        viewModelScope.launch {
            sshClientWrapper.disconnect()
            currentConnectionId = null
        }
    }
}

/**
 * UI State for Terminal screen
 */
data class TerminalUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val connectionStatus: String = "Disconnected",
    val error: String? = null,
    val terminalWidth: Int = 80,
    val terminalHeight: Int = 24,
    val fontSize: Float = 14f,
    val pendingHostKey: ServerFingerprint? = null
)
