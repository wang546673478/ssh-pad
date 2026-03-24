package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository

/**
 * Use Case: Get recently used SSH connections
 * 
 * Clean Architecture: Domain Layer
 * Responsibility: Retrieve recently connected SSH connections sorted by lastConnectedAt
 */
class GetRecentConnectionsUseCase(
    private val repository: SSHConnectionRepository
) {
    /**
     * Execute the use case
     * @param limit Maximum number of recent connections to return
     * @return List of recent SSH connections sorted by lastConnectedAt descending
     */
    suspend operator fun invoke(limit: Int = 5): List<SSHConnection> {
        return repository.getRecentConnections(limit)
    }
}
