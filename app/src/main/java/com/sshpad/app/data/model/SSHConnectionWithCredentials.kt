package com.sshpad.app.data.model

/**
 * SSH Connection with decrypted credentials
 * 
 * Security: This class should only be used transiently when credentials are needed
 * for authentication. Never store or log instances of this class.
 * 
 * @param connection The base connection metadata
 * @param password Decrypted password (if using password auth)
 * @param privateKeyPassphrase Decrypted private key passphrase (if using key auth)
 */
data class SSHConnectionWithCredentials(
    val connection: SSHConnection,
    val password: String? = null,
    val privateKeyPassphrase: String? = null
) {
    val id: String get() = connection.id
    val name: String get() = connection.name
    val host: String get() = connection.host
    val port: Int get() = connection.port
    val username: String get() = connection.username
    val authType: SSHConnection.AuthType get() = connection.authType
}
