package com.sshpad.app.domain.usecase

import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.data.repository.SSHConnectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetSSHConnectionsUseCase
 */
class GetSSHConnectionsUseCaseTest {

    private lateinit var repository: SSHConnectionRepository
    private lateinit var useCase: GetSSHConnectionsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSSHConnectionsUseCase(repository)
    }

    @Test
    fun `invoke should return connections from repository`() = runBlocking {
        // Given
        val expectedConnections = listOf(
            createTestConnection("1"),
            createTestConnection("2")
        )
        coEvery { repository.getAllConnections() } returns flowOf(expectedConnections)

        // When
        val result = useCase().first()

        // Then
        assertEquals(expectedConnections, result)
        coVerify(exactly = 1) { repository.getAllConnections() }
    }

    @Test
    fun `invoke should return empty list when repository returns empty`() = runBlocking {
        // Given
        coEvery { repository.getAllConnections() } returns flowOf(emptyList())

        // When
        val result = useCase().first()

        // Then
        assertTrue(result.isEmpty())
    }

    private fun createTestConnection(id: String) = SSHConnection(
        id = id,
        name = "Test Connection $id",
        host = "192.168.1.$id",
        port = 22,
        username = "testuser"
    )
}
