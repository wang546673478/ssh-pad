package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.data.repository.SSHConnectionWithCredentials
import com.sshpad.app.ssh.SSHClientWrapper
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Connect to an SSH server
 * 
 * Clean Architecture: Domain Layer
 * Responsibility: Establish SSH connection to a server
 */
class ConnectToServerUseCase(
    private val repository: SSHConnectionRepository,
    private val sshClientWrapper: SSHClientWrapper
) {
    /**
     * Execute the use case
     * @param connectionId The ID of the connection to use
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(connectionId: String): Result<Unit> {
        // Get connection with credentials from SecureStorage
        val connectionWithCredentials = repository.getConnectionWithCredentials(connectionId)
            ?: return Result.failure(Exception("Connection not found"))

        val connection = connectionWithCredentials.connection

        // Connect using SSH client with credentials from SecureStorage
        val connectResult = sshClientWrapper.connect(
            connection = connection,
            password = connectionWithCredentials.password,
            passphrase = connectionWithCredentials.passphrase
        )
        
        if (connectResult.isFailure) {
            return Result.failure(connectResult.exceptionOrNull() ?: Exception("Connection failed"))
        }
        
        // Start shell session after successful connection
        val shellResult = sshClientWrapper.startShell()
        
        // Update last connected timestamp
        repository.updateLastConnectedAt(connectionId)
        
        return if (shellResult.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(shellResult.exceptionOrNull() ?: Exception("Failed to start shell"))
        }
    }

    /**
     * Get connection state flow
     */
    fun getConnectionState(): Flow<com.sshpad.app.ssh.ConnectionState> {
        return sshClientWrapper.connectionState
    }
}
