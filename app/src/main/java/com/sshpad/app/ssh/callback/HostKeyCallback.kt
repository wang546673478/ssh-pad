package com.sshpad.app.ssh.callback

import com.sshpad.app.ssh.verifier.ServerFingerprint

/**
 * Host Key Callback Interface for TOFU (Trust On First Use)
 * 
 * This interface provides callbacks for host key verification events,
 * allowing the UI layer to respond to unknown host key scenarios.
 * 
 * Security: This callback is critical for preventing man-in-the-middle (MITM) attacks
 * by ensuring user confirmation on first-time connections.
 */
interface HostKeyCallback {
    
    /**
     * Called when connecting to an unknown host (TOFU scenario)
     * 
     * This method is invoked when the SSH client encounters a server whose host key
     * is not yet stored in the known hosts. The UI should display the fingerprint
     * to the user and request confirmation.
     * 
     * @param fingerprint The server's host key fingerprint containing:
     *   - host: Server hostname or IP
     *   - port: Server port
     *   - fingerprint: SHA256 fingerprint string
     *   - algorithm: Key algorithm (e.g., "ssh-rsa", "ecdsa-sha2-nistp256")
     *   - addedAt: Timestamp when fingerprint was generated
     */
    fun onHostKeyUnknown(fingerprint: ServerFingerprint)
}
