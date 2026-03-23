package com.sshpad.app.data.repository.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

@Serializable
private data class ConnectionJson(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val username: String,
    val authType: String,
    val password: String?,
    val privateKeyPath: String?,
    val privateKeyPassphrase: String?,
    val keepAliveInterval: Int,
    val connectionTimeout: Int,
    val lastConnectedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val color: String
)

class SSHConnectionRepositoryImpl(
    private val context: Context
) : SSHConnectionRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ssh_connections")
    private val json = Json { ignoreUnknownKeys = true }

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
        return context.dataStore.data.map { preferences ->
            val connectionsJson = preferences[CONNECTIONS_KEY] ?: return@map null
            try {
                val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
                connectionList.find { it.id == id }?.toDomainModel()
            } catch (e: Exception) {
                null
            }
        }.firstOrNull()
    }

    override suspend fun getRecentConnections(limit: Int): List<SSHConnection> {
        return context.dataStore.data.map { preferences ->
            val connectionsJson = preferences[CONNECTIONS_KEY] ?: return@map emptyList()
            try {
                val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
                connectionList
                    .filter { it.lastConnectedAt != null }
                    .sortedByDescending { it.lastConnectedAt }
                    .take(limit)
                    .map { it.toDomainModel() }
            } catch (e: Exception) {
                emptyList()
            }
        }.firstOrNull() ?: emptyList()
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
        val allConnections = context.dataStore.data.map { preferences ->
            val connectionsJson = preferences[CONNECTIONS_KEY] ?: return@map emptyList<SSHConnection>()
            try {
                val connectionList = json.decodeFromString<List<ConnectionJson>>(connectionsJson)
                connectionList.map { it.toDomainModel() }
            } catch (e: Exception) {
                emptyList()
            }
        }.firstOrNull() ?: emptyList()

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
            password = this.password,
            privateKeyPath = this.privateKeyPath,
            privateKeyPassphrase = this.privateKeyPassphrase,
            keepAliveInterval = this.keepAliveInterval,
            connectionTimeout = this.connectionTimeout,
            lastConnectedAt = this.lastConnectedAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            color = this.color
        )
    }

    private fun ConnectionJson.toDomainModel(): SSHConnection {
        return SSHConnection(
            id = this.id,
            name = this.name,
            host = this.host,
            port = this.port,
            username = this.username,
            authType = SSHConnection.AuthType.valueOf(this.authType),
            password = this.password,
            privateKeyPath = this.privateKeyPath,
            privateKeyPassphrase = this.privateKeyPassphrase,
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
