package com.sshpad.app.data.model

/**
 * Connection History Management
 * Week 8: Connection History Optimization
 */

/**
 * Update connection with last connected timestamp and increment count
 */
fun SSHConnection.recordConnection(): SSHConnection {
    return copy(
        lastConnectedAt = System.currentTimeMillis(),
        connectionCount = connectionCount + 1,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Add a command to recent commands list (max 10)
 */
fun SSHConnection.addRecentCommand(command: String): SSHConnection {
    val updatedCommands = (listOf(command) + recentCommands).take(10)
    return copy(
        recentCommands = updatedCommands,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Toggle favorite status
 */
fun SSHConnection.toggleFavorite(): SSHConnection {
    return copy(
        isFavorite = !isFavorite,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Add a tag to the connection
 */
fun SSHConnection.addTag(tag: String): SSHConnection {
    if (tag in tags) return this
    return copy(
        tags = tags + tag,
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Remove a tag from the connection
 */
fun SSHConnection.removeTag(tag: String): SSHConnection {
    return copy(
        tags = tags.filterNot { it == tag },
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Connection history entry for quick reconnect
 */
data class ConnectionHistoryEntry(
    val connection: SSHConnection,
    val lastUsedAt: Long,
    val usageCount: Int
) {
    companion object {
        fun fromConnection(connection: SSHConnection): ConnectionHistoryEntry {
            return ConnectionHistoryEntry(
                connection = connection,
                lastUsedAt = connection.lastConnectedAt ?: connection.createdAt,
                usageCount = connection.connectionCount
            )
        }
    }
}

/**
 * Sort connections by recency and frequency
 */
fun List<SSHConnection>.sortByRecency(): List<SSHConnection> {
    return sortedWith(
        compareByDescending<SSHConnection> { it.isFavorite }
            .thenByDescending { it.lastConnectedAt ?: 0L }
            .thenByDescending { it.connectionCount }
    )
}

/**
 * Get recently used connections (last 7 days)
 */
fun List<SSHConnection>.getRecentConnections(days: Int = 7): List<SSHConnection> {
    val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    return filter { 
        it.lastConnectedAt != null && it.lastConnectedAt > cutoffTime 
    }.sortByRecency()
}

/**
 * Get frequently used connections
 */
fun List<SSHConnection>.getFrequentConnections(minCount: Int = 3): List<SSHConnection> {
    return filter { it.connectionCount >= minCount }.sortByRecency()
}

/**
 * Search connections by name, host, or username
 */
fun List<SSHConnection>.searchConnections(query: String): List<SSHConnection> {
    if (query.isBlank()) return this
    val lowerQuery = query.lowercase()
    return filter { 
        it.name.lowercase().contains(lowerQuery) ||
        it.host.lowercase().contains(lowerQuery) ||
        it.username.lowercase().contains(lowerQuery) ||
        it.tags.any { tag -> tag.lowercase().contains(lowerQuery) }
    }.sortByRecency()
}
