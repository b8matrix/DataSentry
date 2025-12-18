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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.data.local.AppDatabase
import com.datasentry.app.data.remote.DnsPacket

class DataSentryService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private val TAG = "VPN"
    
    // Dependencies (Manual Injection for Service)
    private lateinit var repository: PacketRepository
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "DataSentryVPN"
        const val ACTION_STOP = "com.datasentry.app.STOP_VPN"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called")
        // Database will be initialized lazily when first packet is logged
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop request
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "=== STOP ACTION RECEIVED ===")
            stopForeground(STOP_FOREGROUND_REMOVE)
            cleanup()
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "DataSentry VPN Service starting...")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        try {
            val builder = Builder()
                .setSession("DataSentry")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("10.0.0.2") // We are the DNS Server! 😈
                .addRoute("10.0.0.2", 32) // Only route DNS traffic to us
                .setBlocking(true)

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                isRunning = true
                startDnsLoop()
            } else {
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
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("DataSentry Protection Active")
            .setContentText("Analyzing DNS Traffic...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startDnsLoop() {
        thread {
            Log.d(TAG, "DNS Loop started")
            val vpnInput = FileInputStream(vpnInterface!!.fileDescriptor)
            
            // Buffer for IP Packet
            val buffer = ByteBuffer.allocate(32767)
            
            // Socket to talk to Real Internet (Google DNS)
            // We must protect this socket so its traffic bypasses the VPN
            val upstreamSocket = java.net.DatagramSocket()
            protect(upstreamSocket)

            try {
                while (isRunning) {
                    val length = vpnInput.read(buffer.array())
                    if (length > 0) {
                        try {
                            val version = (buffer.get(0).toInt() shr 4)
                            val protocol = buffer.get(9).toInt()
                            // Log.d(TAG, "Read $length bytes. Ver=$version Prot=$protocol") // Debug
                            
                            if (version == 4 && protocol == 17) { // IPv4 + UDP
                                val ipHeaderLength = (buffer.get(0).toInt() and 0x0F) * 4
                                val udpHeaderLength = 8
                                val offset = ipHeaderLength + udpHeaderLength
                                
                                val payloadLength = length - offset
                                if (payloadLength > 0) {
                                    val dnsData = ByteArray(payloadLength)
                                    System.arraycopy(buffer.array(), offset, dnsData, 0, payloadLength)

                                    val dnsPacket = DnsPacket.parse(dnsData)
                                    if (dnsPacket == null) {
                                       Log.e(TAG, "Failed to parse potential DNS packet of size $payloadLength")
                                    }
                                    
                                    dnsPacket?.questions?.firstOrNull()?.let { question ->
                                        val host = question.name
                                        Log.w(TAG, "Intercepted DNS: $host") // Log.w to make it yellow/visible
                                        logPacketToDb(host)
                                        
                                        // Forwarding Logic...
                                        val googleDns = java.net.InetAddress.getByName("8.8.8.8")
                                        val packet = java.net.DatagramPacket(dnsData, dnsData.size, googleDns, 53)
                                        upstreamSocket.send(packet)
                                    }
                                }
                            } else {
                                // Log.d(TAG, "Ignored Packet: Ver=$version Prot=$protocol")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Packet parse error: ${e.message}")
                        }
                        buffer.clear()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Loop error: ${e.message}")
            } finally {
                upstreamSocket.close()
                cleanup()
            }
        }
    }

    private fun logPacketToDb(host: String) {
        // Lazy init repository
        if (!::repository.isInitialized) {
            val db = AppDatabase.getDatabase(this)
            repository = PacketRepository(db.packetDao())
        }
        
        val type = when {
            host.contains("google") -> "Analytics"
            host.contains("facebook") -> "Social"
            host.contains("netflix") -> "Video"
            else -> "Unknown"
        }
        
        val packet = com.datasentry.app.data.local.entity.PacketEntity(
            timestamp = System.currentTimeMillis(),
            sourceIp = "10.0.0.2",
            destIp = "8.8.8.8",
            protocol = "DNS",
            sizeBytes = host.length + 20,
            appName = host,
            contentType = type,
            isRisk = type == "Analytics" || type == "Social"
        )
        
        serviceScope.launch {
            repository.logPacket(packet)
        }
    }

    private fun cleanup() {
        Log.d(TAG, "cleanup() called - Closing VPN interface")
        isRunning = false
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.d(TAG, "VPN interface closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface: ${e.message}")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "=== onDestroy() CALLED - Stopping VPN ===")
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        cleanup()
        super.onDestroy()
        stopSelf()
    }

    override fun onRevoke() {
        Log.d(TAG, "=== onRevoke() CALLED - VPN Permission Revoked ===")
        cleanup()
        super.onRevoke()
    }
}
