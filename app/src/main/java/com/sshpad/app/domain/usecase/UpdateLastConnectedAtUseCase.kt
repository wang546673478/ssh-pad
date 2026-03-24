package com.sshpad.app.domain.usecase

import com.sshpad.app.data.repository.SSHConnectionRepository

/**
 * Use case to update the last connected timestamp for an SSH connection
 */
class UpdateLastConnectedAtUseCase(
    private val repository: SSHConnectionRepository
) {
    suspend operator fun invoke(connectionId: String): Result<Unit> {
        return repository.updateLastConnectedAt(connectionId)
    }
}
