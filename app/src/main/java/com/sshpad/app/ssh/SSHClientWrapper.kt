package com.sshpad.app.ssh

import android.content.Context
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.ssh.callback.HostKeyCallback
import com.sshpad.app.ssh.verifier.ServerFingerprint
import com.sshpad.app.ssh.verifier.StrictHostKeyVerifier
import com.sshpad.app.util.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.future.AuthFuture
import org.apache.sshd.client.session.ClientSession
import java.io.ByteArrayOutputStream
import org.apache.sshd.common.config.keys.FilePasswordProvider
import org.apache.sshd.common.keyprovider.FileKeyPairProvider
import java.io.File
import java.nio.file.Paths
import java.util.EnumSet
import java.util.concurrent.TimeUnit

/**
 * SSH Client wrapper using Apache MINA sshd
 * 
 * Security features:
 * - Uses StrictHostKeyVerifier for server key verification
 * - Prevents man-in-the-middle (MITM) attacks
 * - Stores known host fingerprints securely
 * - Supports TOFU (Trust On First Use) with user confirmation
 * - HostKeyCallback interface for UI integration
 */
class SSHClientWrapper(
    private val context: Context,
    private val hostKeyCallback: HostKeyCallback? = null
) {

    private val hostKeyVerifier = StrictHostKeyVerifier(context).apply {
        // Inject the callback for TOFU notifications
        onPendingVerification = { fingerprint ->
            hostKeyCallback?.onHostKeyUnknown(fingerprint)
        }
    }
    
    private val sshClient = SshClient.setUpDefaultClient().apply {
        serverKeyVerifier = hostKeyVerifier
        start()
    }

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: Flow<ConnectionState> = _connectionState

    // Pending host key verification flow (for TOFU)
    private val _pendingHostKeyVerification = MutableStateFlow<ServerFingerprint?>(null)
    val pendingHostKeyVerification: Flow<ServerFingerprint?> = _pendingHostKeyVerification

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
            authFuture.verify(AppConstants.SSH_AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
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

            // Use FileKeyPairProvider to load keys
            val keyPairProvider = FileKeyPairProvider(listOf(Paths.get(privateKeyPath)))
            if (passphrase != null) {
                keyPairProvider.setPasswordFinder(FilePasswordProvider.of(passphrase))
            }
            
            val keys = keyPairProvider.loadKeys(session)
            
            for (key in keys) {
                session.addPublicKeyIdentity(key)
            }

            val authFuture = session.auth()
            authFuture.verify(AppConstants.SSH_AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS)
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
            val channel = session.createChannel("shell")
            channel.open().verify(AppConstants.SSH_CHANNEL_OPEN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
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
            channel.open().verify(AppConstants.SSH_CHANNEL_OPEN_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            
            val stdout = ByteArrayOutputStream()
            val stderr = ByteArrayOutputStream()
            channel.getInvertedOut()?.transferTo(stdout)
            channel.getInvertedErr()?.transferTo(stderr)
            
            // Wait for channel to close with timeout
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), AppConstants.SSH_COMMAND_TIMEOUT_MS.toLong())
            channel.close(false)
            
            val output = stdout.toString("UTF-8").let { out ->
                val err = stderr.toString("UTF-8")
                if (err.isNotEmpty()) "$out$err" else out
            }
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send input to shell
     */
    fun sendInput(input: String) {
        currentChannel?.invertedIn?.write(input.toByteArray())
        currentChannel?.invertedIn?.flush()
    }

    /**
     * Resize terminal
     */
    fun resizeTerminal(width: Int, height: Int) {
        // Terminal resize not fully supported in current SSHD version
        // This is a no-op for now
    }

    /**
     * Disconnect and cleanup
     */
    fun disconnect() {
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
        // Keep-alive configuration not available in current SSHD version
        // This is a no-op for now
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
