package com.datasentry.app.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.datasentry.app.data.local.entity.PacketEntity
import com.datasentry.app.data.repository.PacketRepository
import com.datasentry.app.demo.DemoScenarioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main packet handler that reads from TUN, logs packets, and forwards to real internet.
 * 
 * This is the core component that makes the VPN work while maintaining internet connectivity.
 */
class VpnPacketHandler(
    private val vpnService: VpnService,
    private val vpnInterface: ParcelFileDescriptor,
    private val packetRepository: PacketRepository
) {
    companion object {
        private const val TAG = "VpnPacketHandler"
        private const val BUFFER_SIZE = 32767
    }
    
    private val isRunning = AtomicBoolean(false)
    private var handlerThread: Thread? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private lateinit var udpForwarder: UdpForwarder
    private lateinit var tcpForwarder: TcpForwarder
    
    /**
     * Start the packet handling loop.
     */
    fun start() {
        if (isRunning.getAndSet(true)) return
        
        // Initialize forwarders with packet logging callback
        udpForwarder = UdpForwarder(vpnService) { parsed, domain ->
            logPacketToDatabase(parsed, domain)
        }
        
        tcpForwarder = TcpForwarder(vpnService) { parsed, domain ->
            logPacketToDatabase(parsed, domain)
        }
        
        handlerThread = Thread {
            runPacketLoop()
        }.apply {
            name = "VpnPacketHandler"
            isDaemon = true
            start()
        }
        
        Log.d(TAG, "Packet handler started")
    }
    
    /**
     * Main packet processing loop.
     */
    private fun runPacketLoop() {
        val buffer = ByteArray(BUFFER_SIZE)
        
        try {
            val tunInput = FileInputStream(vpnInterface.fileDescriptor)
            val tunOutput = FileOutputStream(vpnInterface.fileDescriptor)
            
            while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                // Read packet from TUN
                val length = tunInput.read(buffer)
                if (length <= 0) {
                    Thread.sleep(10)  // Avoid busy loop on empty reads
                    continue
                }
                
                // Parse to determine protocol
                val parsed = PacketParser.parse(buffer, length)
                if (parsed == null) {
                    Log.d(TAG, "Could not parse packet of length $length")
                    continue
                }
                
                // Store original for response building
                val originalPacket = buffer.copyOf(length)
                
                // Route to appropriate forwarder
                when (parsed.protocol) {
                    17 -> {  // UDP
                        udpForwarder.forwardPacket(buffer, length, tunOutput, originalPacket)
                    }
                    6 -> {  // TCP
                        tcpForwarder.processPacket(buffer, length, tunOutput, originalPacket)
                    }
                    else -> {
                        Log.d(TAG, "Ignoring protocol ${parsed.protocol}")
                    }
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "Packet loop interrupted")
        } catch (e: Exception) {
            if (isRunning.get()) {
                Log.e(TAG, "Packet loop error", e)
            }
        }
        
        Log.d(TAG, "Packet loop exited")
    }
    
    /**
     * Log packet to database for UI display.
     */
    private fun logPacketToDatabase(parsed: PacketParser.ParsedPacket, domain: String?) {
        scope.launch {
            try {
                // Detect app/content type using heuristics
                val (appName, contentType) = detectAppAndContent(parsed, domain)
                
                // Check if this is a risky connection
                val isRisk = isRiskyConnection(parsed)
                
                val packet = PacketEntity(
                    timestamp = System.currentTimeMillis(),
                    sourceIp = parsed.sourceIp,
                    destIp = parsed.destIp,
                    protocol = parsed.protocolName,
                    sizeBytes = parsed.totalLength,
                    appName = appName,
                    contentType = contentType,
                    isRisk = isRisk
                )
                
                packetRepository.logPacket(packet)
                
                Log.d(TAG, "Logged: $appName | ${parsed.protocolName} | ${parsed.destIp}:${parsed.destPort} | ${parsed.totalLength}B" +
                        if (domain != null) " | $domain" else "")
                        
            } catch (e: Exception) {
                Log.e(TAG, "Error logging packet", e)
            }
        }
    }
    
    /**
     * Detect app name and content type based on IP, port, and domain.
     */
    private fun detectAppAndContent(parsed: PacketParser.ParsedPacket, domain: String?): Pair<String, String> {
        // First try DemoScenarioEngine for known patterns
        val scenario = DemoScenarioEngine.getScenarioByIp(parsed.destIp)
        if (scenario != null) {
            val appName = when (scenario.packageName) {
                "com.google.android.youtube" -> "YouTube"
                "com.snapchat.android" -> "Snapchat"
                "com.android.chrome" -> "Chrome"
                "android" -> "System"
                else -> "Unknown"
            }
            val contentType = when (scenario.trafficType) {
                "VIDEO_STREAM_4K" -> "Video 4K"
                "EPHEMERAL_MEDIA" -> "Image"
                "WEB_NAVIGATION" -> "Text"
                "BACKGROUND_TELEMETRY" -> "Telemetry"
                else -> "Data"
            }
            return appName to contentType
        }
        
        // Fallback to domain-based detection
        if (domain != null) {
            val lowerDomain = domain.lowercase()
            return when {
                lowerDomain.contains("youtube") || lowerDomain.contains("googlevideo") -> "YouTube" to "Video"
                lowerDomain.contains("instagram") || lowerDomain.contains("cdninstagram") -> "Instagram" to "Image"
                lowerDomain.contains("facebook") || lowerDomain.contains("fbcdn") -> "Facebook" to "Social"
                lowerDomain.contains("twitter") || lowerDomain.contains("twimg") -> "Twitter" to "Social"
                lowerDomain.contains("tiktok") -> "TikTok" to "Video"
                lowerDomain.contains("snapchat") -> "Snapchat" to "Image"
                lowerDomain.contains("whatsapp") -> "WhatsApp" to "Message"
                lowerDomain.contains("google") -> "Google" to "Web"
                lowerDomain.contains("apple") || lowerDomain.contains("icloud") -> "Apple" to "Cloud"
                lowerDomain.contains("microsoft") || lowerDomain.contains("azure") -> "Microsoft" to "Cloud"
                lowerDomain.contains("amazon") || lowerDomain.contains("aws") -> "Amazon" to "Cloud"
                else -> "Unknown" to "Data"
            }
        }
        
        // Fallback to port-based detection
        return when (parsed.destPort) {
            53 -> "System" to "DNS"
            80 -> "Web" to "HTTP ⚠️"
            443 -> "App" to "HTTPS"
            5228, 5229, 5230 -> "Google" to "Push"
            5222, 5223 -> "Chat" to "XMPP"
            else -> "Unknown" to "Data"
        }
    }
    
    /**
     * Check if connection is potentially risky.
     */
    private fun isRiskyConnection(parsed: PacketParser.ParsedPacket): Boolean {
        return when (parsed.destPort) {
            21 -> true   // FTP
            23 -> true   // Telnet
            25, 587 -> true  // SMTP (email sending)
            80 -> true   // HTTP (unencrypted)
            445 -> true  // SMB
            3389 -> true // RDP
            5900 -> true // VNC
            else -> false
        }
    }
    
    /**
     * Stop the packet handler.
     */
    fun stop() {
        isRunning.set(false)
        handlerThread?.interrupt()
        handlerThread = null
        
        udpForwarder.cleanup()
        tcpForwarder.cleanup()
        
        Log.d(TAG, "Packet handler stopped")
    }
}
