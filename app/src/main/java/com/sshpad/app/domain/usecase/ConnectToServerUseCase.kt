package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
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
        // Get connection
        val connection = repository.getConnectionById(connectionId)
            ?: return Result.failure(Exception("Connection not found"))

        // Connect using SSH client
        // Note: Credentials should be passed separately via SSHClientWrapper
        // This is simplified - in production, use connect with credentials
        return sshClientWrapper.connect(connection)
            .map { /* Connection successful */ Unit }
    }

    /**
     * Get connection state flow
     */
    fun getConnectionState(): Flow<com.sshpad.app.ssh.ConnectionState> {
        return sshClientWrapper.connectionState
    }
}
