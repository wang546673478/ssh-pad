package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case: Get all SSH connections
 * 
 * Clean Architecture: Domain Layer
 * Responsibility: Retrieve all stored SSH connections
 */
class GetSSHConnectionsUseCase(
    private val repository: SSHConnectionRepository
) {
    /**
     * Execute the use case
     * @return Flow of list of all SSH connections
     */
    operator fun invoke(): Flow<List<SSHConnection>> {
        return repository.getAllConnections()
    }
}
