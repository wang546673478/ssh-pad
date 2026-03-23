package com.sshpad.app.data.model

import java.util.UUID

/**
 * SSH Connection configuration data model
 */
data class SSHConnection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    val password: String? = null,
    val privateKeyPath: String? = null,
    val privateKeyPassphrase: String? = null,
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
