package com.sshpad.app.data.model

import java.util.UUID

/**
 * SSH Connection configuration data model
 * 
 * Security Notes:
 * - password and privateKeyPassphrase are NOT stored in this model
 * - Sensitive credentials must be stored in SecureStorage (EncryptedSharedPreferences)
 * - This model only contains non-sensitive connection metadata
 */
data class SSHConnection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    // Note: password is intentionally excluded from this model
    // Store in SecureStorage using connection.id as key
    val privateKeyPath: String? = null,
    // Note: privateKeyPassphrase is intentionally excluded from this model
    // Store in SecureStorage using connection.id as key
    val keepAliveInterval: Int = 60, // seconds
    val connectionTimeout: Int = 30000, // milliseconds
    val lastConnectedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: String = "#2196F3" // Connection color for UI
) {
    /**
     * Authentication types supported
     */
    enum class AuthType {
        PASSWORD,
        PRIVATE_KEY
    }

    /**
     * Connection status
     */
    enum class Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        AUTHENTICATING,
        ERROR
    }

    /**
     * Get the connection string (user@host:port)
     */
    fun getConnectionString(): String = "$username@$host:$port"

    /**
     * Get display name with host info
     */
    fun getDisplayName(): String = "$name ($host)"
}
