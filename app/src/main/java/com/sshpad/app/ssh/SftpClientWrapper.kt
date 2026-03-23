package com.sshpad.app.ssh

import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.sftp.client.SftpClient
import org.apache.sshd.sftp.client.SftpClientFactory
import org.apache.sshd.sftp.common.SftpConstants
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * SFTP Client Wrapper for file transfer operations
 * Week 8: SFTP File Transfer Support
 */
class SftpClientWrapper(private val session: ClientSession) {
    
    private val sftpClient: SftpClient by lazy {
        SftpClientFactory.instance().createSftpClient(session)
    }
    
    /**
     * List directory contents
     * @param remotePath Remote directory path
     * @return List of SftpEntry objects
     */
    suspend fun listDirectory(remotePath: String = "/"): List<SftpEntry> {
        return try {
            sftpClient.readDir(remotePath).map { entry ->
                SftpEntry(
                    name = entry.filename,
                    path = "${remotePath.trimEnd('/')}/${entry.filename}",
                    isDirectory = entry.attributes.isDirectory,
                    size = entry.attributes.size ?: 0L,
                    lastModified = entry.attributes.mtime ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Download a file from remote server
     * @param remotePath Remote file path
     * @param localPath Local file path
     * @param progressListener Progress callback (bytes downloaded, total bytes)
     */
    suspend fun downloadFile(
        remotePath: String,
        localPath: String,
        progressListener: ((Long, Long) -> Unit)? = null
    ): Result<Unit> {
        return try {
            val attrs = sftpClient.lstat(remotePath)
            val totalSize = attrs.size ?: 0L
            
            if (attrs.isDirectory) {
                return Result.failure(IOException("Cannot download directory"))
            }
            
            val localFile = File(localPath)
            localFile.parentFile?.mkdirs()
            
            FileOutputStream(localFile).use { output ->
                val buffer = ByteArray(8192)
                var downloadedBytes = 0L
                
                sftpClient.read(remotePath).use { input ->
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        progressListener?.invoke(downloadedBytes, totalSize)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload a file to remote server
     * @param localPath Local file path
     * @param remotePath Remote file path
     * @param progressListener Progress callback (bytes uploaded, total bytes)
     */
    suspend fun uploadFile(
        localPath: String,
        remotePath: String,
        progressListener: ((Long, Long) -> Unit)? = null
    ): Result<Unit> {
        return try {
            val localFile = File(localPath)
            if (!localFile.exists()) {
                return Result.failure(IOException("Local file does not exist"))
            }
            
            if (localFile.isDirectory) {
                return Result.failure(IOException("Cannot upload directory"))
            }
            
            val totalSize = localFile.length()
            
            sftpClient.write(remotePath).use { output ->
                localFile.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var uploadedBytes = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        uploadedBytes += bytesRead
                        progressListener?.invoke(uploadedBytes, totalSize)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a directory on remote server
     * @param remotePath Remote directory path
     */
    suspend fun createDirectory(remotePath: String): Result<Unit> {
        return try {
            sftpClient.mkdir(remotePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a file or directory on remote server
     * @param remotePath Remote file/directory path
     */
    suspend fun delete(remotePath: String): Result<Unit> {
        return try {
            val attrs = sftpClient.lstat(remotePath)
            if (attrs.isDirectory) {
                sftpClient.rmdir(remotePath)
            } else {
                sftpClient.remove(remotePath)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Rename/move a file or directory
     * @param oldPath Current path
     * @param newPath New path
     */
    suspend fun rename(oldPath: String, newPath: String): Result<Unit> {
        return try {
            sftpClient.rename(oldPath, newPath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get file attributes
     * @param remotePath Remote file path
     */
    suspend fun getAttributes(remotePath: String): Result<SftpAttributes> {
        return try {
            val attrs = sftpClient.lstat(remotePath)
            Result.success(
                SftpAttributes(
                    isDirectory = attrs.isDirectory,
                    isRegularFile = attrs.isRegularFile,
                    isSymbolicLink = attrs.isSymbolicLink,
                    size = attrs.size ?: 0L,
                    lastModified = attrs.mtime ?: 0L,
                    permissions = attrs.permissionsMask ?: 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Close the SFTP client
     */
    suspend fun close() {
        try {
            sftpClient.close()
        } catch (e: Exception) {
            // Ignore close exceptions
        }
    }
}

/**
 * SFTP file/directory entry
 */
data class SftpEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
) {
    val formattedSize: String
        get() = formatFileSize(size)
    
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * SFTP file attributes
 */
data class SftpAttributes(
    val isDirectory: Boolean,
    val isRegularFile: Boolean,
    val isSymbolicLink: Boolean,
    val size: Long,
    val lastModified: Long,
    val permissions: Int
)
