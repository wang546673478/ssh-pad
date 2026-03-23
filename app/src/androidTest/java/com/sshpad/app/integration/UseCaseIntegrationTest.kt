package com.sshpad.app.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.data.repository.impl.SSHConnectionRepositoryImpl
import com.sshpad.app.domain.usecase.CreateSSHConnectionUseCase
import com.sshpad.app.domain.usecase.DeleteSSHConnectionUseCase
import com.sshpad.app.domain.usecase.GetSSHConnectionsUseCase
import com.sshpad.app.security.SecureStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Use Case Integration Tests
 * Week 7: Integration Test Coverage
 */
class UseCaseIntegrationTest {

    private lateinit var context: Context
    private lateinit var repository: SSHConnectionRepository
    private lateinit var getConnectionsUseCase: GetSSHConnectionsUseCase
    private lateinit var createConnectionUseCase: CreateSSHConnectionUseCase
    private lateinit var deleteConnectionUseCase: DeleteSSHConnectionUseCase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val secureStorage = SecureStorage(context)
        repository = SSHConnectionRepositoryImpl(context, secureStorage)
        getConnectionsUseCase = GetSSHConnectionsUseCase(repository)
        createConnectionUseCase = CreateSSHConnectionUseCase(repository)
        deleteConnectionUseCase = DeleteSSHConnectionUseCase(repository)
    }

    @Test
    fun createConnection_thenGetConnections_returnsConnection() = runTest {
        val connection = SSHConnection(
            name = "Test Server",
            host = "192.168.1.100",
            port = 22,
            username = "testuser",
            authType = SSHConnection.AuthType.PASSWORD
        )
        createConnectionUseCase(connection)
        val connections = getConnectionsUseCase().first()
        assertTrue(connections.any { it.name == "Test Server" })
    }

    @Test
    fun getConnections_emptyDatabase_returnsEmptyList() = runTest {
        val connections = getConnectionsUseCase().first()
        assertTrue(connections.isEmpty())
    }
}
