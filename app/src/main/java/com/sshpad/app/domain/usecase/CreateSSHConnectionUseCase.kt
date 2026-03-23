package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository

/**
 * Use Case: Create a new SSH connection
 * 
 * Clean Architecture: Domain Layer
 * Responsibility: Add a new SSH connection to the repository
 */
class CreateSSHConnectionUseCase(
    private val repository: SSHConnectionRepository
) {
    /**
     * Execute the use case
     * @param connection The SSH connection to create
     * @return Result with connection ID on success, or error
     */
    suspend operator fun invoke(connection: SSHConnection): Result<String> {
        // Validate connection before creating
        val validationError = validateConnection(connection)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        return repository.addConnection(connection)
    }

    /**
     * Validate connection data
     * @return Error message if invalid, null if valid
     */
    private fun validateConnection(connection: SSHConnection): String? {
        if (connection.name.isBlank()) {
            return "Connection name cannot be empty"
        }
        if (connection.host.isBlank()) {
            return "Host cannot be empty"
        }
        if (connection.port !in 1..65535) {
            return "Port must be between 1 and 65535"
        }
        if (connection.username.isBlank()) {
            return "Username cannot be empty"
        }
        return null
    }
}
