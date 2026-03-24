package com.sshpad.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sshpad.app.R
import com.sshpad.app.data.model.SSHConnection
import com.sshpad.app.ssh.SSHClientWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service for maintaining SSH connections in background
 */
class SSHConnectionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val binder = LocalBinder()
    private lateinit var sshClient: SSHClientWrapper
    private var connectionJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): SSHConnectionService = this@SSHConnectionService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        sshClient = SSHClientWrapper(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val connectionId = intent.getStringExtra(EXTRA_CONNECTION_ID)
                if (connectionId != null) {
                    startConnection(connectionId)
                }
            }
            ACTION_DISCONNECT -> {
                stopConnection()
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        sshClient.stop()
    }

    /**
     * Start SSH connection
     */
    fun startConnection(connectionId: String) {
        // In real implementation, fetch connection from repository
        // For now, we'll just show notification
        startForeground(NOTIFICATION_ID, createNotification("Connecting..."))
        
        connectionJob = serviceScope.launch {
            sshClient.connectionState.collectLatest { state ->
                when (state) {
                    is com.sshpad.app.ssh.ConnectionState.Connected -> {
                        updateNotification("Connected", "SSH session active")
                    }
                    is com.sshpad.app.ssh.ConnectionState.Error -> {
                        updateNotification("Connection Error", state.message)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Stop SSH connection
     */
    fun stopConnection() {
        connectionJob?.cancel()
        serviceScope.launch {
            sshClient.disconnect()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Create notification channel
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SSH Connection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active SSH connection status"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create notification
     */
    private fun createNotification(status: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SSH Pad")
            .setContentText(status)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Update notification
     */
    private fun updateNotification(title: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    companion object {
        private const val CHANNEL_ID = "ssh_connection_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_CONNECT = "com.sshpad.app.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.sshpad.app.ACTION_DISCONNECT"
        const val EXTRA_CONNECTION_ID = "connection_id"
    }
}
