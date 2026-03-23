package com.sshpad.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sshpad.app.domain.usecase.ConnectToServerUseCase
import com.sshpad.app.ssh.ConnectionState
import com.sshpad.app.ssh.SSHClientWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val connectToServerUseCase: ConnectToServerUseCase
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

    /**
     * Connect to SSH server
     */
    fun connect(connectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, error = null) }
            
            connectToServerUseCase(connectionId)
                .onSuccess {
                    _uiState.update { it.copy(isConnecting = false, isConnected = true) }
                    appendOutput("Connected to server\n")
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isConnecting = false,
                            error = "Connection failed: ${e.message}"
                        ) 
                    }
                    appendOutput("Error: ${e.message}\n")
                }
        }
    }

    /**
     * Disconnect from SSH server
     */
    fun disconnect() {
        viewModelScope.launch {
            sshClientWrapper.disconnect()
            _uiState.update { it.copy(isConnected = false) }
            appendOutput("Disconnected from server\n")
        }
    }

    /**
     * Send command to terminal
     */
    fun sendCommand(command: String) {
        viewModelScope.launch {
            sshClientWrapper.sendInput("$command\n")
            appendOutput("$ $command\n")
        }
    }

    /**
     * Resize terminal
     */
    fun resizeTerminal(width: Int, height: Int) {
        sshClientWrapper.resizeTerminal(width, height)
    }

    /**
     * Append output to terminal buffer
     */
    private fun appendOutput(text: String) {
        _outputBuffer.update { it + text }
    }

    /**
     * Clear terminal output
     */
    fun clearOutput() {
        _outputBuffer.update { "" }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        // Disconnect when ViewModel is cleared
        viewModelScope.launch {
            sshClientWrapper.disconnect()
        }
    }

    /**
     * Factory for creating ViewModel
     */
    class Factory(
        private val sshClientWrapper: SSHClientWrapper,
        private val connectToServerUseCase: ConnectToServerUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TerminalViewModel::class.java)) {
                return TerminalViewModel(sshClientWrapper, connectToServerUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * UI State for Terminal screen
 */
data class TerminalUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val error: String? = null,
    val terminalWidth: Int = 80,
    val terminalHeight: Int = 24
)
