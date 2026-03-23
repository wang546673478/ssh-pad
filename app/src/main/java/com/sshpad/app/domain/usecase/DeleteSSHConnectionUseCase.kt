package com.sshpad.app.domain.usecase

import com.sshpad.app.data.repository.SSHConnectionRepository

/**
 * Use Case: Delete an SSH connection
 * 
 * Clean Architecture: Domain Layer
 * Responsibility: Remove an SSH connection from the repository
 */
class DeleteSSHConnectionUseCase(
    private val repository: SSHConnectionRepository
) {
    /**
     * Execute the use case
     * @param connectionId The ID of the connection to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(connectionId: String): Result<Unit> {
        if (connectionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Connection ID cannot be empty"))
        }

        return repository.deleteConnection(connectionId)
    }
}
