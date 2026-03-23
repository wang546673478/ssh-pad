package com.sshpad.app.ssh

import android.content.Context
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.ssh.verifier.StrictHostKeyVerifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.future.AuthFuture
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.config.keys.loader.DefaultPublicKeyResourceDecoder
import org.apache.sshd.common.util.buffer.Buffer
import org.apache.sshd.common.util.io.output.NullOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.EnumSet
import java.util.concurrent.TimeUnit

/**
 * SSH Client wrapper using Apache MINA sshd
 * 
 * Security features:
 * - Uses StrictHostKeyVerifier for server key verification
 * - Prevents man-in-the-middle (MITM) attacks
 * - Stores known host fingerprints securely
 */
class SSHClientWrapper(private val context: Context) {

    private val hostKeyVerifier = StrictHostKeyVerifier(context)
    
    private val sshClient = SshClient.setUpDefaultClient().apply {
        serverKeyVerifier = hostKeyVerifier
        start()
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: Flow<ConnectionState> = _connectionState

    private var currentSession: ClientSession? = null
    private var currentChannel: ClientChannel? = null

    /**
     * Connect to SSH server
     * Note: This method expects password and passphrase to be set in the connection object
     * For secure usage, use connect(connection, password, passphrase) instead
     */
    suspend fun connect(connection: SSHConnection): Result<ClientSession> {
        return connect(connection, null, null)
    }

    /**
     * Connect to SSH server with explicit credentials
     * 
     * @param connection The SSH connection metadata
     * @param password Optional password (for PASSWORD auth type)
     * @param passphrase Optional private key passphrase (for PRIVATE_KEY auth type)
     * @return Result with ClientSession on success
     */
    suspend fun connect(
        connection: SSHConnection,
        password: String?,
        passphrase: String?
    ): Result<ClientSession> {
        return try {
            _connectionState.value = ConnectionState.Connecting

            val session = sshClient.connect(
                connection.username,
                connection.host,
                connection.port
            ).verify(connection.connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
                .session

            _connectionState.value = ConnectionState.Authenticating

            // Authenticate based on auth type
            val authSuccess = when (connection.authType) {
                SSHConnection.AuthType.PASSWORD -> {
                    authenticateWithPassword(session, password)
                }
                SSHConnection.AuthType.PRIVATE_KEY -> {
                    authenticateWithPrivateKey(session, connection.privateKeyPath, passphrase)
                }
            }

            if (authSuccess) {
                currentSession = session
                _connectionState.value = ConnectionState.Connected(session)
                Result.success(session)
            } else {
                _connectionState.value = ConnectionState.Error("Authentication failed")
                session.close()
                Result.failure(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            Result.failure(e)
        }
    }

    private suspend fun authenticateWithPassword(session: ClientSession, password: String?): Boolean {
        if (password.isNullOrEmpty()) return false
        
        try {
            session.addPasswordIdentity(password)
            val authFuture = session.auth()
            authFuture.verify(30000, TimeUnit.MILLISECONDS)
            return authFuture.isDone && authFuture.isSuccess
        } catch (e: Exception) {
            return false
        }
    }

    private suspend fun authenticateWithPrivateKey(
        session: ClientSession, 
        privateKeyPath: String?, 
        passphrase: String?
    ): Boolean {
        if (privateKeyPath.isNullOrEmpty()) return false
        
        try {
            val keyFile = File(privateKeyPath)
            if (!keyFile.exists()) return false

            val keys = DefaultPublicKeyResourceDecoder().loadKeys(
                null,
                keyFile.inputStream(),
                passphrase?.toCharArray()
            )
            
            keys.forEach { key ->
                session.addPublicKeyIdentity(key)
            }

            val authFuture = session.auth()
            authFuture.verify(30000, TimeUnit.MILLISECONDS)
            return authFuture.isDone && authFuture.isSuccess
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Start interactive shell session
     */
    suspend fun startShell(): Result<ClientChannel> {
        val session = currentSession ?: return Result.failure(Exception("Not connected"))
        
        return try {
            val channel = session.createChannel("shell").apply {
                out = NullOutputStream.INSTANCE // We'll handle output separately
            }
            channel.open().verify(5000, TimeUnit.MILLISECONDS)
            currentChannel = channel
            Result.success(channel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Execute a single command and return output
     */
    suspend fun executeCommand(command: String): Result<String> {
        val session = currentSession ?: return Result.failure(Exception("Not connected"))
        
        return try {
            val channel = session.createExecChannel(command)
            channel.open().verify(5000, TimeUnit.MILLISECONDS)
            
            val outputStream = ByteArrayOutputStream()
            val errOutputStream = ByteArrayOutputStream()
            
            channel.stdout.copyTo(outputStream)
            channel.stderr.copyTo(errOutputStream)
            
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 30000, TimeUnit.MILLISECONDS)
            channel.close(false)
            
            val output = outputStream.toString(Charsets.UTF_8)
            val error = errOutputStream.toString(Charsets.UTF_8)
            
            if (error.isNotEmpty()) {
                Result.failure(Exception(error))
            } else {
                Result.success(output)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send input to shell
     */
    fun sendInput(input: String) {
        currentChannel?.in?.write(input.toByteArray())
    }

    /**
     * Resize terminal
     */
    fun resizeTerminal(width: Int, height: Int) {
        currentChannel?.let { channel ->
            try {
                channel.sendSignal(Buffer().apply {
                    putInt(width)
                    putInt(height)
                    putInt(0) // pixel width
                    putInt(0) // pixel height
                })
            } catch (e: Exception) {
                // Ignore resize errors
            }
        }
    }

    /**
     * Disconnect and cleanup
     */
    suspend fun disconnect() {
        try {
            currentChannel?.close(true)
            currentSession?.close()
        } catch (e: Exception) {
            // Ignore cleanup errors
        } finally {
            currentChannel = null
            currentSession = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    /**
     * Set keep-alive interval
     */
    fun setKeepAlive(intervalSeconds: Int) {
        currentSession?.clientKeepAliveManager?.apply {
            setKeepAliveInterval(intervalSeconds)
            setKeepAliveResponseTimeout(intervalSeconds * 3)
        }
    }

    /**
     * Cleanup client
     */
    fun stop() {
        disconnect()
        sshClient.stop()
    }

    /**
     * Get the host key verifier for managing server fingerprints
     */
    fun getHostKeyVerifier(): StrictHostKeyVerifier = hostKeyVerifier

    /**
     * Check if a host is known (has been connected before)
     */
    fun isHostKnown(host: String, port: Int): Boolean {
        return hostKeyVerifier.isHostKnown(host, port)
    }

    /**
     * Accept a host key after user confirmation
     */
    fun acceptHostKey(host: String, port: Int): Result<Unit> {
        return hostKeyVerifier.acceptHostKey(host, port)
    }

    /**
     * Reject a host key
     */
    fun rejectHostKey(host: String, port: Int) {
        hostKeyVerifier.rejectHostKey(host, port)
    }

    /**
     * Remove a known host key (for key rotation or manual removal)
     */
    fun removeHostKey(host: String, port: Int): Result<Unit> {
        return hostKeyVerifier.removeHostKey(host, port)
    }

    /**
     * Get all known hosts
     */
    fun getKnownHosts(): Map<String, com.sshpad.app.ssh.verifier.ServerFingerprint> {
        return hostKeyVerifier.getKnownHosts()
    }
}

/**
 * Connection state representation
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Authenticating : ConnectionState()
    data class Connected(val session: ClientSession) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
