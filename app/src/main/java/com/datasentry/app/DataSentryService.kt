package com.datasentry.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class DataSentryService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val TAG = "VPN"

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "DataSentryVPN"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DataSentry VPN Service starting...")

        // Create notification channel for foreground service
        createNotificationChannel()

        // Start as foreground service
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Configure and establish VPN
        try {
            val builder = Builder()
                .setSession("DataSentry")
                .addAddress("10.0.0.2", 32)  // Virtual IP for VPN interface
                // .addRoute("0.0.0.0", 0)      // Capture ALL traffic (DISABLED FOR SAFE DEMO)
                .setBlocking(false)           // Non-blocking I/O

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                Log.d(TAG, "VPN interface established successfully")
                isRunning = true
                startPacketLoop()
            } else {
                Log.e(TAG, "Failed to establish VPN interface")
                stopSelf()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error establishing VPN: ${e.message}", e)
            stopSelf()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DataSentry VPN",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "VPN traffic monitoring service"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID
        return Notification.Builder(this, channelId)
            .setContentTitle("DataSentry VPN Active")
            .setContentText("Monitoring network traffic")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startPacketLoop() {
        thread {
            Log.d(TAG, "Packet loop thread started")
            val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            val vpnOutput = FileOutputStream(vpnInterface!!.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767) // Max IP packet size

            try {
                while (isRunning) {
                    // Read packet from VPN interface
                    val length = vpnInput.read(buffer.array())
                    
                    if (length > 0) {
                        // Log packet size
                        Log.d(TAG, "Packet size: $length bytes")

                        // PASSTHROUGH: Immediately write packet back to network
                        // In a real VPN, you would:
                        // 1. Parse the packet
                        // 2. Send it through a tunnel
                        // 3. Receive response from tunnel
                        // 4. Write response back to vpnOutput
                        
                        // For now, we just pass it through (this maintains connectivity)
                        buffer.limit(length)
                        vpnOutput.write(buffer.array(), 0, length)
                        buffer.clear()
                    } else if (length < 0) {
                        Log.d(TAG, "End of stream reached")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in packet loop: ${e.message}", e)
            } finally {
                Log.d(TAG, "Packet loop thread stopping")
                cleanup()
            }
        }
    }

    private fun cleanup() {
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "DataSentry VPN Service stopping...")
        cleanup()
        super.onDestroy()
    }

    override fun onRevoke() {
        Log.d(TAG, "VPN permission revoked by user")
        cleanup()
        super.onRevoke()
    }
}
