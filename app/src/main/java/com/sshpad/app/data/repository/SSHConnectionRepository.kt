package com.sshpad.app.data.repository

import com.sshpad.app.data.model.SSHConnection
import kotlinx.coroutines.flow.Flow

/**
 * Connection with credentials for authentication
 */
data class SSHConnectionWithCredentials(
    val connection: SSHConnection,
    val password: String?,
    val passphrase: String?
)

/**
 * Repository interface for SSH connection management
 */
interface SSHConnectionRepository {
    
    /**
     * Get all connections as a Flow
     */
    fun getAllConnections(): Flow<List<SSHConnection>>
    
    /**
     * Get connection by ID
     */
    suspend fun getConnectionById(id: String): SSHConnection?
    
    /**
     * Get connection with credentials loaded from SecureStorage
     * Use this when you need password or passphrase for authentication
     */
    suspend fun getConnectionWithCredentials(id: String): SSHConnectionWithCredentials?
    
    /**
     * Get recently used connections (sorted by lastConnectedAt)
     */
    suspend fun getRecentConnections(limit: Int = 5): List<SSHConnection>
    
    /**
     * Add a new connection
     */
    suspend fun addConnection(connection: SSHConnection): Result<String>
    
    /**
     * Update an existing connection
     */
    suspend fun updateConnection(connection: SSHConnection): Result<Unit>
    
    /**
     * Delete a connection
     */
    suspend fun deleteConnection(id: String): Result<Unit>
    
    /**
     * Update last connected timestamp
     */
    suspend fun updateLastConnectedAt(id: String): Result<Unit>
    
    /**
     * Search connections by name or host
     */
    suspend fun searchConnections(query: String): List<SSHConnection>
}
