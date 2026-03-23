package com.sshpad.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive SSH credentials using Android Keystore
 * 
 * Security features:
 * - Uses Android Keystore for key generation and storage
 * - AES-256-GCM encryption for values
 * - AES-256-SIV encryption for keys
 * - Hardware-backed security when available
 * - Automatic key rotation support
 * 
 * Usage:
 * ```
 * val secureStorage = SecureStorage(context)
 * secureStorage.savePassword(connectionId, "mySecretPassword")
 * val password = secureStorage.getPassword(connectionId)
 * secureStorage.deletePassword(connectionId)
 * ```
 */
class SecureStorage(context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "ssh_credentials_encrypted",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Save password securely
     * @param connectionId Unique identifier for the SSH connection
     * @param password The password to encrypt and store
     */
    fun savePassword(connectionId: String, password: String) {
        val key = getPasswordKey(connectionId)
        sharedPreferences.edit()
            .putString(key, password)
            .apply()
    }

    /**
     * Save private key passphrase securely
     * @param connectionId Unique identifier for the SSH connection
     * @param passphrase The passphrase to encrypt and store
     */
    fun savePassphrase(connectionId: String, passphrase: String) {
        val key = getPassphraseKey(connectionId)
        sharedPreferences.edit()
            .putString(key, passphrase)
            .apply()
    }

    /**
     * Retrieve password securely
     * @param connectionId Unique identifier for the SSH connection
     * @return The decrypted password, or null if not found
     */
    fun getPassword(connectionId: String): String? {
        val key = getPasswordKey(connectionId)
        return sharedPreferences.getString(key, null)
    }

    /**
     * Retrieve private key passphrase securely
     * @param connectionId Unique identifier for the SSH connection
     * @return The decrypted passphrase, or null if not found
     */
    fun getPassphrase(connectionId: String): String? {
        val key = getPassphraseKey(connectionId)
        return sharedPreferences.getString(key, null)
    }

    /**
     * Delete stored password
     * @param connectionId Unique identifier for the SSH connection
     */
    fun deletePassword(connectionId: String) {
        val key = getPasswordKey(connectionId)
        sharedPreferences.edit().remove(key).apply()
    }

    /**
     * Delete stored passphrase
     * @param connectionId Unique identifier for the SSH connection
     */
    fun deletePassphrase(connectionId: String) {
        val key = getPassphraseKey(connectionId)
        sharedPreferences.edit().remove(key).apply()
    }

    /**
     * Delete all credentials for a connection
     * @param connectionId Unique identifier for the SSH connection
     */
    fun deleteCredentials(connectionId: String) {
        getPasswordKey(connectionId).let { key ->
            sharedPreferences.edit().remove(key).apply()
        }
        getPassphraseKey(connectionId).let { key ->
            sharedPreferences.edit().remove(key).apply()
        }
    }

    /**
     * Clear all stored credentials (use with caution)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Check if password exists for a connection
     */
    fun hasPassword(connectionId: String): Boolean {
        return getPassword(connectionId) != null
    }

    /**
     * Check if passphrase exists for a connection
     */
    fun hasPassphrase(connectionId: String): Boolean {
        return getPassphrase(connectionId) != null
    }

    private fun getPasswordKey(connectionId: String): String {
        return "password:$connectionId"
    }

    private fun getPassphraseKey(connectionId: String): String {
        return "passphrase:$connectionId"
    }
}
