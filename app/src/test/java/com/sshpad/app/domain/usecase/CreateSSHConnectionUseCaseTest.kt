package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreateSSHConnectionUseCase
 */
class CreateSSHConnectionUseCaseTest {

    private lateinit var repository: SSHConnectionRepository
    private lateinit var useCase: CreateSSHConnectionUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CreateSSHConnectionUseCase(repository)
    }

    @Test
    fun `invoke should create connection when data is valid`() = runBlocking {
        // Given
        val connection = createValidConnection()
        coEvery { repository.addConnection(connection) } returns Result.success(connection.id)

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(connection.id, result.getOrNull())
        coVerify(exactly = 1) { repository.addConnection(connection) }
    }

    @Test
    fun `invoke should fail when name is empty`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(name = "")

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection name cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when host is empty`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(host = "")

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Host cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when port is invalid`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(port = 0)

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Port must be between 1 and 65535", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when port is too high`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(port = 70000)

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Port must be between 1 and 65535", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should fail when username is empty`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(username = "")

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Username cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should succeed with blank name when trimmed is not empty`() = runBlocking {
        // Given
        val connection = createValidConnection().copy(name = "  Valid Name  ")
        coEvery { repository.addConnection(connection) } returns Result.success(connection.id)

        // When
        val result = useCase(connection)

        // Then
        assertTrue(result.isSuccess)
    }

    private fun createValidConnection() = SSHConnection(
        id = "test-id",
        name = "Test Connection",
        host = "192.168.1.100",
        port = 22,
        username = "testuser"
    )
}
