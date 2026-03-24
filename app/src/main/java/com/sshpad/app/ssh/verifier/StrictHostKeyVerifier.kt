package com.sshpad.app.ssh.verifier

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sshpad.app.util.SafeLogger
import org.apache.sshd.client.keyverifier.ServerKeyVerifier
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.SshException
import java.net.SocketAddress
import java.security.KeyPair
import java.security.PublicKey
import java.util.concurrent.ConcurrentHashMap

/**
 * Strict host key verifier that stores and verifies server fingerprints
 * 
 * Security features:
 * - Stores known host fingerprints in EncryptedSharedPreferences
 * - Prompts user on first connection (TOFU - Trust On First Use)
 * - Blocks until user confirms or rejects unknown hosts
 * - Rejects mismatched fingerprints (prevents MITM attacks)
 * - Uses Android Keystore for encryption
 */
class StrictHostKeyVerifier(
    private val context: Context
) : ServerKeyVerifier {

    private val knownHosts: MutableMap<String, ServerFingerprint> = ConcurrentHashMap()
    private val pendingVerification = ConcurrentHashMap<String, ServerFingerprint>()
    private val confirmationLocks = ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock>()
    private val confirmationConditions = ConcurrentHashMap<String, java.util.concurrent.locks.Condition>()
    
    // Callback for notifying UI about pending verifications
    var onPendingVerification: ((ServerFingerprint) -> Unit)? = null
    
    private val preferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        EncryptedSharedPreferences.create(
            context,
            "ssh_host_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Load known hosts from encrypted storage
     */
    init {
        loadKnownHosts()
    }

    private fun loadKnownHosts() {
        preferences.all.forEach { (host, fingerprintData) ->
            if (fingerprintData is String) {
                parseFingerprint(fingerprintData)?.let { fingerprint ->
                    knownHosts[host] = fingerprint
                }
            }
        }
    }

    /**
     * Verify server key during connection
     * 
     * This method blocks and waits for user confirmation on first-time connections (TOFU).
     * 
     * @param session The client session
     * @param remoteAddress The server address
     * @param serverKey The server's public key
     * @return true if the key is accepted (by user or known host), false if rejected
     */
    override fun verifyServerKey(
        session: ClientSession,
        remoteAddress: SocketAddress,
        serverKey: PublicKey
    ): Boolean {
        // Extract host and port from remoteAddress or session
        val (host, port) = extractHostPort(remoteAddress, session)
        
        val hostKey = getHostKey(host, port)
        val currentFingerprint = extractFingerprint(serverKey)
        
        // Check if we have a stored fingerprint for this host
        val storedFingerprint = knownHosts[hostKey]
        
        return when {
            // First time connecting - TOFU (Trust On First Use) with mandatory user confirmation
            storedFingerprint == null -> {
                val fingerprint = ServerFingerprint(
                    host = host,
                    port = port,
                    fingerprint = currentFingerprint,
                    algorithm = serverKey.algorithm,
                    addedAt = System.currentTimeMillis()
                )
                
                // Store pending verification and BLOCK until user confirms
                // This pauses the connection flow
                pendingVerification[hostKey] = fingerprint
                
                SafeLogger.i(
                    "SSH_SECURITY",
                    "TOFU: Waiting for user confirmation for $hostKey ($currentFingerprint)"
                )
                
                // Wait for user decision (blocks the SSH connection thread)
                waitForUserConfirmation(hostKey)
            }
            // Fingerprint matches - accept
            storedFingerprint.fingerprint == currentFingerprint -> {
                true
            }
            // Fingerprint mismatch - REJECT (possible MITM attack)
            else -> {
                // Log security event
                SafeLogger.e(
                    "SSH_SECURITY",
                    "HOST KEY MISMATCH for $hostKey! Possible MITM attack." +
                    "\nExpected: ${storedFingerprint.fingerprint}" +
                    "\nReceived: $currentFingerprint"
                )
                false
            }
        }
    }

    private fun extractHostPort(address: SocketAddress, session: ClientSession): Pair<String, Int> {
        return when (address) {
            is java.net.InetSocketAddress -> address.hostName to address.port
            else -> session.remoteAddress?.let {
                if (it is java.net.InetSocketAddress) {
                    it.hostName to it.port
                } else {
                    "unknown" to 22
                }
            } ?: ("unknown" to 22)
        }
    }

    /**
     * Wait for user confirmation on TOFU (blocks the connection thread)
     * 
     * @param hostKey The host key identifier
     * @return true if user accepted, false if rejected or timeout
     */
    private fun waitForUserConfirmation(hostKey: String): Boolean {
        val lock = confirmationLocks.computeIfAbsent(hostKey) {
            java.util.concurrent.locks.ReentrantLock()
        }
        val condition = confirmationConditions.computeIfAbsent(hostKey) {
            lock.newCondition()
        }
        
        lock.lock()
        try {
            // Wait for signal from UI (with 5 minute timeout)
            val signaled = condition.await(5, java.util.concurrent.TimeUnit.MINUTES)
            
            if (!signaled) {
                // Timeout - reject connection
                SafeLogger.w("SSH_SECURITY", "TOFU timeout for $hostKey - rejecting connection")
                pendingVerification.remove(hostKey)
                return false
            }
            
            // Check if still pending (might have been rejected)
            val isAccepted = pendingVerification.containsKey(hostKey)
            if (!isAccepted) {
                SafeLogger.i("SSH_SECURITY", "TOFU rejected by user for $hostKey")
                return false
            }
            
            SafeLogger.i("SSH_SECURITY", "TOFU confirmed by user for $hostKey")
            return true
            
        } catch (e: InterruptedException) {
            SafeLogger.e("SSH_SECURITY", "TOFU wait interrupted for $hostKey")
            Thread.currentThread().interrupt()
            return false
        } finally {
            lock.unlock()
        }
    }

    /**
     * Signal user decision to the waiting connection thread
     */
    private fun signalUserDecision(hostKey: String, accepted: Boolean) {
        val lock = confirmationLocks[hostKey] ?: return
        val condition = confirmationConditions[hostKey] ?: return
        
        lock.lock()
        try {
            condition.signal() // Wake up the waiting thread
        } finally {
            lock.unlock()
        }
    }

    /**
     * Accept a pending host key (called after user confirmation)
     * 
     * This saves the fingerprint and signals the waiting connection thread to proceed.
     */
    fun acceptHostKey(host: String, port: Int): Result<Unit> {
        val hostKey = getHostKey(host, port)
        val pending = pendingVerification[hostKey] ?: return Result.failure(
            Exception("No pending verification for $hostKey")
        )

        // Save to encrypted storage
        saveHostKey(hostKey, pending)
        
        // Move to known hosts
        knownHosts[hostKey] = pending
        pendingVerification.remove(hostKey)

        // Signal the waiting connection thread to proceed
        signalUserDecision(hostKey, accepted = true)

        SafeLogger.i("SSH_SECURITY", "Host key accepted and saved for $hostKey")
        return Result.success(Unit)
    }

    /**
     * Reject a pending host key
     * 
     * This removes the pending verification and signals the connection thread to abort.
     */
    fun rejectHostKey(host: String, port: Int) {
        val hostKey = getHostKey(host, port)
        
        // Remove from pending (connection thread will see this and return false)
        pendingVerification.remove(hostKey)
        
        // Signal the waiting connection thread to abort
        signalUserDecision(hostKey, accepted = false)
        
        SafeLogger.i("SSH_SECURITY", "Host key rejected for $hostKey")
    }

    /**
     * Remove a known host (for key rotation or manual removal)
     */
    fun removeHostKey(host: String, port: Int): Result<Unit> {
        val hostKey = getHostKey(host, port)
        
        // Remove from encrypted storage
        preferences.edit().remove(hostKey).apply()
        
        // Remove from memory
        knownHosts.remove(hostKey)
        
        SafeLogger.i("SSH_SECURITY", "Host key removed for $hostKey")
        return Result.success(Unit)
    }

    /**
     * Get all known hosts
     */
    fun getKnownHosts(): Map<String, ServerFingerprint> = knownHosts.toMap()

    /**
     * Get pending verifications
     */
    fun getPendingVerifications(): Map<String, ServerFingerprint> = pendingVerification.toMap()

    /**
     * Check if a host is known
     */
    fun isHostKnown(host: String, port: Int): Boolean {
        return knownHosts.containsKey(getHostKey(host, port))
    }

    private fun getHostKey(host: String, port: Int): String {
        return "[$host]:$port"
    }

    private fun extractFingerprint(publicKey: PublicKey): String {
        // Generate SHA256 fingerprint (OpenSSH format)
        val keyBytes = publicKey.encoded
        
        // Use SHA-256 hash
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(keyBytes)
        
        // Format as SHA256:XX:XX:XX...
        return "SHA256:" + digest.joinToString(":") { 
            "%02x".format(it) 
        }
    }

    private fun parseFingerprint(fingerprintData: String): ServerFingerprint? {
        return try {
            val parts = fingerprintData.split("|")
            if (parts.size < 4) return null
            
            ServerFingerprint(
                host = parts[0],
                port = parts[1].toIntOrNull() ?: return null,
                fingerprint = parts[2],
                algorithm = parts[3],
                addedAt = parts[4].toLongOrNull() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun saveHostKey(hostKey: String, fingerprint: ServerFingerprint) {
        val data = "${fingerprint.host}|${fingerprint.port}|${fingerprint.fingerprint}|${fingerprint.algorithm}|${fingerprint.addedAt}"
        preferences.edit().putString(hostKey, data).apply()
    }
}

/**
 * Server fingerprint data class
 */
data class ServerFingerprint(
    val host: String,
    val port: Int,
    val fingerprint: String,
    val algorithm: String,
    val addedAt: Long
) {
    fun getDisplayString(): String {
        return "$algorithm:$fingerprint"
    }
    
    fun getHostKeyString(): String {
        return "[$host]:$port"
    }
}
