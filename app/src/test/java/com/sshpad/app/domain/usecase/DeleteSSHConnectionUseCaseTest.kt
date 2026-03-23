package com.sshpad.app.domain.usecase

import com.sshpad.app.data.repository.SSHConnectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DeleteSSHConnectionUseCase
 */
class DeleteSSHConnectionUseCaseTest {

    private lateinit var repository: SSHConnectionRepository
    private lateinit var useCase: DeleteSSHConnectionUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteSSHConnectionUseCase(repository)
    }

    @Test
    fun `invoke should delete connection when ID is valid`() = runBlocking {
        // Given
        val connectionId = "valid-id"
        coEvery { repository.deleteConnection(connectionId) } returns Result.success(Unit)

        // When
        val result = useCase(connectionId)

        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.deleteConnection(connectionId) }
    }

    @Test
    fun `invoke should fail when ID is empty`() = runBlocking {
        // Given
        val connectionId = ""

        // When
        val result = useCase(connectionId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection ID cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.deleteConnection(any()) }
    }

    @Test
    fun `invoke should fail when ID is blank`() = runBlocking {
        // Given
        val connectionId = "   "

        // When
        val result = useCase(connectionId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Connection ID cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.deleteConnection(any()) }
    }

    @Test
    fun `invoke should propagate repository failure`() = runBlocking {
        // Given
        val connectionId = "test-id"
        val errorMessage = "Connection not found"
        coEvery { repository.deleteConnection(connectionId) } returns Result.failure(
            Exception(errorMessage)
        )

        // When
        val result = useCase(connectionId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
}
