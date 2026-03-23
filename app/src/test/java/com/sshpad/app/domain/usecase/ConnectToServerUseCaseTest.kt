package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import com.sshpad.app.ssh.SSHClientWrapper
import com.sshpad.app.ssh.ConnectionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConnectToServerUseCase
 */
class ConnectToServerUseCaseTest {

    private lateinit var repository: SSHConnectionRepository
    private lateinit var sshClientWrapper: SSHClientWrapper
    private lateinit var useCase: ConnectToServerUseCase

    @Before
    fun setup() {
        repository = mockk()
        sshClientWrapper = mockk()
        useCase = ConnectToServerUseCase(repository, sshClientWrapper)
    }

    @Test
    fun `invoke should connect when connection exists`() = runBlocking {
        // Given
        val connection = createValidConnection()
        coEvery { repository.getConnectionById(connection.id) } returns connection
        coEvery { sshClientWrapper.connect(connection) } returns Result.success(mockk())

        // When
        val result = useCase(connection.id)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getConnectionById(connection.id) }
        coVerify(exactly = 1) { sshClientWrapper.connect(connection) }
    }

    @Test
    fun `invoke should fail when connection not found`() = runBlocking {
        // Given
        val connectionId = "non-existent-id"
        coEvery { repository.getConnectionById(connectionId) } returns null

        // When
        val result = useCase(connectionId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection not found", result.exceptionOrNull()?.message)
        coVerify(exactly = 1) { repository.getConnectionById(connectionId) }
        coVerify(exactly = 0) { sshClientWrapper.connect(any<SSHConnection>()) }
    }

    @Test
    fun `invoke should fail when connection fails`() = runBlocking {
        // Given
        val connection = createValidConnection()
        val errorMessage = "Connection refused"
        coEvery { repository.getConnectionById(connection.id) } returns connection
        coEvery { sshClientWrapper.connect(connection) } returns Result.failure(
            Exception(errorMessage)
        )

        // When
        val result = useCase(connection.id)

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when authentication fails`() = runBlocking {
        // Given
        val connection = createValidConnection()
        coEvery { repository.getConnectionById(connection.id) } returns connection
        coEvery { sshClientWrapper.connect(connection) } returns Result.failure(
            Exception("Authentication failed")
        )

        // When
        val result = useCase(connection.id)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Authentication failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when timeout occurs`() = runBlocking {
        // Given
        val connection = createValidConnection()
        coEvery { repository.getConnectionById(connection.id) } returns connection
        coEvery { sshClientWrapper.connect(connection) } returns Result.failure(
            Exception("Connection timeout")
        )

        // When
        val result = useCase(connection.id)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection timeout", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getConnectionState should return state flow from SSH client`() = runBlocking {
        // Given
        val expectedState = ConnectionState.Disconnected
        every { sshClientWrapper.connectionState } returns flowOf(expectedState)

        // When
        val result = useCase.getConnectionState()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `invoke should use connection with correct credentials`() = runBlocking {
        // Given
        val connection = createValidConnection(
            id = "test-123",
            host = "192.168.1.100",
            port = 2222
        )
        coEvery { repository.getConnectionById(connection.id) } returns connection
        coEvery { sshClientWrapper.connect(connection) } returns Result.success(mockk())

        // When
        val result = useCase(connection.id)

        // Then
        assertTrue(result.isSuccess)
        coVerify { sshClientWrapper.connect(match { 
            it.id == "test-123" && it.host == "192.168.1.100" && it.port == 2222 
        }) }
    }

    private fun createValidConnection(
        id: String = "test-id",
        host: String = "192.168.1.100",
        port: Int = 22
    ) = SSHConnection(
        id = id,
        name = "Test Connection",
        host = host,
        port = port,
        username = "testuser",
        authType = SSHConnection.AuthType.PASSWORD
    )
}
