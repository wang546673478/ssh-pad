package com.sshpad.app.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.data.repository.SSHConnectionWithCredentials
import com.sshpad.app.security.SecureStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Repository implementation with secure credential storage
 * 
 * Security:
 * - Connection metadata stored in DataStore (non-sensitive)
 * - Passwords and passphrases stored in EncryptedSharedPreferences via SecureStorage
 */
class SSHConnectionRepositoryImpl(
    private val context: Context,
    private val secureStorage: SecureStorage
) : SSHConnectionRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ssh_connections")
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get credentials from SecureStorage for a connection
     */
    private suspend fun getCredentials(connectionId: String): ConnectionCredentials {
        return ConnectionCredentials(
            password = secureStorage.getPassword(connectionId),
            passphrase = secureStorage.getPassphrase(connectionId)
        )
    }

    /**
     * Save credentials to SecureStorage
     */
    private suspend fun saveCredentials(connectionId: String, password: String?, passphrase: String?) {
        password?.let { secureStorage.savePassword(connectionId, it) }
        passphrase?.let { secureStorage.savePassphrase(connectionId, it) }
    }

    /**
     * Delete credentials from SecureStorage
     */
    private suspend fun deleteCredentials(connectionId: String) {
        secureStorage.deleteCredentials(connectionId)
    }

    @Serializable
    private data class ConnectionJson(
        val id: String,
        val name: String,
        val host: String,
        val port: Int,
        val username: String,
        val authType: String,
        val privateKeyPath: String?,
        val keepAliveInterval: Int,
        val connectionTimeout: Int,
        val lastConnectedAt: Long?,
        val createdAt: Long,
        val updatedAt: Long,
        val color: String
    )

    private data class ConnectionCredentials(
        val password: String? = null,
        val passphrase: String? = null
    )

    override fun getAllConnections(): Flow<List<SSHConnection>> {
        return context.dataStore.data.map { preferences ->
            val connectionsJson = preferences[CONNECTIONS_KEY] ?: return@map emptyList()
            try {
                val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
                connectionList.map { it.toDomainModel() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getConnectionById(id: String): SSHConnection? {
        val preferences = context.dataStore.data.firstOrNull() ?: return null
        val connectionsJson = preferences[CONNECTIONS_KEY] ?: return null
        return try {
            val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
            connectionList.find { it.id == id }?.toDomainModel()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get connection with credentials loaded from SecureStorage
     * Use this when you need to access password or passphrase
     */
    override suspend fun getConnectionWithCredentials(id: String): SSHConnectionWithCredentials? {
        val connection = getConnectionById(id) ?: return null
        val credentials = getCredentials(id)
        return SSHConnectionWithCredentials(
            connection = connection,
            password = credentials.password,
            passphrase = credentials.passphrase
        )
    }

    override suspend fun getRecentConnections(limit: Int): List<SSHConnection> {
        val preferences = context.dataStore.data.firstOrNull() ?: return emptyList()
        val connectionsJson = preferences[CONNECTIONS_KEY] ?: return emptyList()
        return try {
            val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
            connectionList
                .filter { it.lastConnectedAt != null }
                .sortedByDescending { it.lastConnectedAt }
                .take(limit)
                .map { it.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addConnection(connection: SSHConnection): Result<String> {
        return try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[CONNECTIONS_KEY] ?: "[]"
                val currentList = json.decodeFromString<List<ConnectionJson>>(currentJson)
                val newList = currentList + connection.toJsonModel()
                preferences[CONNECTIONS_KEY] = json.encodeToString(newList)
            }
            Result.success(connection.id)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add a new connection with credentials
     * @param connection The connection metadata
     * @param password Optional password (will be stored encrypted)
     * @param passphrase Optional private key passphrase (will be stored encrypted)
     */
    suspend fun addConnectionWithCredentials(
        connection: SSHConnection,
        password: String? = null,
        passphrase: String? = null
    ): Result<String> {
        return addConnection(connection).onSuccess { connectionId ->
            saveCredentials(connectionId, password, passphrase)
        }
    }

    override suspend fun updateConnection(connection: SSHConnection): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[CONNECTIONS_KEY] ?: "[]"
                val currentList = json.decodeFromString<List<ConnectionJson>>(currentJson)
                val updatedList = currentList.map { 
                    if (it.id == connection.id) connection.toJsonModel() else it 
                }
                preferences[CONNECTIONS_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteConnection(id: String): Result<Unit> {
        return try {
            // Delete credentials from SecureStorage first
            deleteCredentials(id)
            
            // Then delete metadata from DataStore
            context.dataStore.edit { preferences ->
                val currentJson = preferences[CONNECTIONS_KEY] ?: "[]"
                val currentList = json.decodeFromString<List<ConnectionJson>>(currentJson)
                val filteredList = currentList.filter { it.id != id }
                preferences[CONNECTIONS_KEY] = json.encodeToString(filteredList)
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastConnectedAt(id: String): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                val currentJson = preferences[CONNECTIONS_KEY] ?: "[]"
                val currentList = json.decodeFromString<List<ConnectionJson>>(currentJson)
                val updatedList = currentList.map { 
                    if (it.id == id) {
                        it.copy(lastConnectedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
                    } else {
                        it
                    }
                }
                preferences[CONNECTIONS_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchConnections(query: String): List<SSHConnection> {
        val preferences = context.dataStore.data.firstOrNull() ?: return emptyList()
        val connectionsJson = preferences[CONNECTIONS_KEY] ?: return emptyList()
        val allConnections = try {
            val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
            connectionList.map { it.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }

        return allConnections.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.host.contains(query, ignoreCase = true) ||
            it.username.contains(query, ignoreCase = true)
        }
    }

    private fun SSHConnection.toJsonModel(): ConnectionJson {
        return ConnectionJson(
            id = this.id,
            name = this.name,
            host = this.host,
            port = this.port,
            username = this.username,
            authType = this.authType.name,
            privateKeyPath = this.privateKeyPath,
            keepAliveInterval = this.keepAliveInterval,
            connectionTimeout = this.connectionTimeout,
            lastConnectedAt = this.lastConnectedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            color = this.color
        )
    }

    private suspend fun ConnectionJson.toDomainModelWithCredentials(): SSHConnection {
        val credentials = getCredentials(this.id)
        return SSHConnection(
            id = this.id,
            name = this.name,
            host = this.host,
            port = this.port,
            username = this.username,
            authType = SSHConnection.AuthType.valueOf(this.authType),
            privateKeyPath = this.privateKeyPath,
            keepAliveInterval = this.keepAliveInterval,
            connectionTimeout = this.connectionTimeout,
            lastConnectedAt = this.lastConnectedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            color = this.color
        )
        // Note: password and passphrase are retrieved from SecureStorage when needed
        // They are not part of the SSHConnection model for security
    }

    private fun ConnectionJson.toDomainModel(): SSHConnection {
        return SSHConnection(
            id = this.id,
            name = this.name,
            host = this.host,
            port = this.port,
            username = this.username,
            authType = SSHConnection.AuthType.valueOf(this.authType),
            privateKeyPath = this.privateKeyPath,
            keepAliveInterval = this.keepAliveInterval,
            connectionTimeout = this.connectionTimeout,
            lastConnectedAt = this.lastConnectedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            color = this.color
        )
    }

    companion object {
        private val CONNECTIONS_KEY = stringPreferencesKey("ssh_connections_list")
    }
}
