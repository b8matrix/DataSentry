package com.datasentry.app.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.datasentry.app.data.local.entity.PacketEntity
import com.datasentry.app.data.repository.PacketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simplified DNS-only packet handler.
 * 
 * This ONLY captures DNS queries (UDP port 53), forwards them, and gets responses.
 * This is much simpler than full packet forwarding and actually works reliably.
 * 
 * Key insight: We route ONLY DNS traffic through VPN, so all other traffic
 * flows normally through the real network = internet works!
 */
class DnsOnlyHandler(
    private val vpnService: VpnService,
    private val vpnInterface: ParcelFileDescriptor,
    private val packetRepository: PacketRepository
) {
    companion object {
        private const val TAG = "DnsOnlyHandler"
        private const val BUFFER_SIZE = 4096
        private const val DNS_PORT = 53
        
        // Public DNS servers to forward queries to
        private const val DNS_SERVER_1 = "8.8.8.8"
        private const val DNS_SERVER_2 = "8.8.4.4"
        
        // Map domain patterns to package names for TrafficStats lookup
        private val DOMAIN_TO_PACKAGE = mapOf(
            "youtube" to "com.google.android.youtube",
            "googlevideo" to "com.google.android.youtube",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "fbcdn" to "com.facebook.katana",
            "twitter" to "com.twitter.android",
            "x.com" to "com.twitter.android",
            "tiktok" to "com.zhiliaoapp.musically",
            "whatsapp" to "com.whatsapp",
            "snapchat" to "com.snapchat.android",
            "spotify" to "com.spotify.music",
            "netflix" to "com.netflix.mediaclient"
        )
    }
    
    private val isRunning = AtomicBoolean(false)
    private var handlerThread: Thread? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // DNS socket - protected to bypass VPN
    private var dnsSocket: DatagramSocket? = null
    
    // Track domains seen for app detection
    private val recentDomains = mutableListOf<Pair<String, Long>>()
    
    // TrafficStats for real data usage
    private val trafficStatsHelper = TrafficStatsHelper(vpnService.applicationContext)
    
    fun start() {
        if (isRunning.getAndSet(true)) return
        
        // Create and protect DNS socket
        dnsSocket = DatagramSocket().also {
            vpnService.protect(it)
            it.soTimeout = 5000
        }
        
        handlerThread = Thread {
            runDnsLoop()
        }.apply {
            name = "DnsOnlyHandler"
            isDaemon = true
            start()
        }
        
        Log.d(TAG, "DNS handler started")
    }
    
    private fun runDnsLoop() {
        val buffer = ByteArray(BUFFER_SIZE)
        Log.d(TAG, "=== runDnsLoop() STARTED ===")
        
        try {
            val tunInput = FileInputStream(vpnInterface.fileDescriptor)
            val tunOutput = FileOutputStream(vpnInterface.fileDescriptor)
            Log.d(TAG, "TUN streams opened successfully")
            
            var packetCount = 0
            var lastLogTime = 0L
            
            while (isRunning.get() && !Thread.currentThread().isInterrupted) {
                try {
                    // Read packet from TUN
                    val length = tunInput.read(buffer)
                    if (length <= 0) {
                        Thread.sleep(1)
                        continue
                    }
                    
                    packetCount++
                    
                    // Parse packet
                    val parsed = PacketParser.parse(buffer, length) ?: continue
                    
                    // Only handle UDP port 53 (DNS)
                    if (parsed.protocol != 17 || parsed.destPort != DNS_PORT) {
                        continue
                    }
                    
                    // Safety check for payload extraction
                    if (parsed.payloadOffset < 0 || parsed.payloadLength <= 0 ||
                        parsed.payloadOffset + parsed.payloadLength > length) {
                        Log.w(TAG, "Invalid payload bounds")
                        continue
                    }
                    
                    // Extract DNS query payload
                    val dnsPayload = ByteArray(parsed.payloadLength)
                    System.arraycopy(buffer, parsed.payloadOffset, dnsPayload, 0, parsed.payloadLength)
                    
                    // Parse domain name from DNS query
                    val domain = parseDnsDomain(dnsPayload)
                    
                    if (domain != null) {
                        // Log only once per second to avoid spam
                        val now = System.currentTimeMillis()
                        if (now - lastLogTime > 1000) {
                            Log.d(TAG, "DNS Query: $domain (total packets: $packetCount)")
                            lastLogTime = now
                        }
                        
                        // Log to database
                        logDnsQuery(domain, parsed)
                        
                        // Track domain for app detection
                        synchronized(recentDomains) {
                            recentDomains.add(domain to now)
                            // Keep only last 100 domains
                            while (recentDomains.size > 100) {
                                recentDomains.removeAt(0)
                            }
                        }
                    }
                    
                    // Forward DNS query to real DNS server
                    try {
                        val dnsServer = InetAddress.getByName(DNS_SERVER_1)
                        val outPacket = DatagramPacket(dnsPayload, dnsPayload.size, dnsServer, DNS_PORT)
                        dnsSocket?.send(outPacket)
                        
                        // Receive response with timeout
                        val responseBuffer = ByteArray(BUFFER_SIZE)
                        val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
                        dnsSocket?.receive(responsePacket)
                        
                        // Build IP packet to send back to TUN
                        val responseIpPacket = buildDnsResponsePacket(
                            originalPacket = buffer.copyOf(length),  // Copy to avoid buffer overwrite
                            parsed = parsed,
                            responsePayload = responsePacket.data,
                            responseLength = responsePacket.length
                        )
                        
                        synchronized(tunOutput) {
                            tunOutput.write(responseIpPacket)
                            tunOutput.flush()
                        }
                        
                    } catch (e: java.net.SocketTimeoutException) {
                        // Timeout is normal, continue
                    } catch (e: Exception) {
                        Log.w(TAG, "DNS forward error: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Packet processing error: ${e.message}")
                    // Continue processing next packet
                }
            }
        } catch (e: InterruptedException) {
            Log.d(TAG, "DNS loop interrupted")
        } catch (e: Exception) {
            if (isRunning.get()) {
                Log.e(TAG, "DNS loop error", e)
            }
        }
        
        Log.d(TAG, "DNS loop exited")
    }
    
    /**
     * Parse domain name from DNS query payload.
     */
    private fun parseDnsDomain(dnsPayload: ByteArray): String? {
        if (dnsPayload.size < 12) return null
        
        try {
            // DNS header is 12 bytes, domain starts after
            var pos = 12
            val domain = StringBuilder()
            
            while (pos < dnsPayload.size && dnsPayload[pos] != 0.toByte()) {
                val labelLen = dnsPayload[pos].toInt() and 0xFF
                if (labelLen == 0 || pos + labelLen >= dnsPayload.size) break
                
                if (domain.isNotEmpty()) domain.append('.')
                domain.append(String(dnsPayload, pos + 1, labelLen, Charsets.US_ASCII))
                pos += labelLen + 1
            }
            
            return if (domain.isNotEmpty()) domain.toString() else null
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Build UDP response packet to write back to TUN.
     */
    private fun buildDnsResponsePacket(
        originalPacket: ByteArray,
        parsed: PacketParser.ParsedPacket,
        responsePayload: ByteArray,
        responseLength: Int
    ): ByteArray {
        val ihl = parsed.headerLength
        val udpHeaderLen = 8
        val totalLen = ihl + udpHeaderLen + responseLength
        
        val packet = ByteArray(totalLen)
        
        // Copy IP header
        System.arraycopy(originalPacket, 0, packet, 0, ihl)
        
        // Swap source and destination IP
        System.arraycopy(originalPacket, 16, packet, 12, 4)  // Original dest -> new source
        System.arraycopy(originalPacket, 12, packet, 16, 4)  // Original source -> new dest
        
        // Update total length
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        
        // Clear IP checksum
        packet[10] = 0
        packet[11] = 0
        
        // Recalculate IP checksum
        val checksum = calculateIpChecksum(packet, ihl)
        packet[10] = ((checksum shr 8) and 0xFF).toByte()
        packet[11] = (checksum and 0xFF).toByte()
        
        // UDP header - swap ports
        packet[ihl] = ((parsed.destPort shr 8) and 0xFF).toByte()
        packet[ihl + 1] = (parsed.destPort and 0xFF).toByte()
        packet[ihl + 2] = ((parsed.sourcePort shr 8) and 0xFF).toByte()
        packet[ihl + 3] = (parsed.sourcePort and 0xFF).toByte()
        
        // UDP length
        val udpLen = udpHeaderLen + responseLength
        packet[ihl + 4] = ((udpLen shr 8) and 0xFF).toByte()
        packet[ihl + 5] = (udpLen and 0xFF).toByte()
        
        // UDP checksum (0 = disabled for UDP over IPv4)
        packet[ihl + 6] = 0
        packet[ihl + 7] = 0
        
        // Copy DNS response payload
        System.arraycopy(responsePayload, 0, packet, ihl + udpHeaderLen, responseLength)
        
        return packet
    }
    
    /**
     * Calculate IP header checksum.
     */
    private fun calculateIpChecksum(header: ByteArray, length: Int): Int {
        var sum = 0
        var i = 0
        while (i < length) {
            // Skip checksum field
            if (i == 10) {
                i += 2
                continue
            }
            val word = ((header[i].toInt() and 0xFF) shl 8) or (header[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        // Add carry
        while ((sum shr 16) > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv() and 0xFFFF
    }
    
    /**
     * Log DNS query to database.
     */
    private fun logDnsQuery(domain: String, parsed: PacketParser.ParsedPacket) {
        scope.launch {
            try {
                val (appName, contentType) = detectAppFromDomain(domain)
                
                // Try to get real traffic stats for this app
                val realSize = getAppTrafficSize(domain)
                
                val packet = PacketEntity(
                    timestamp = System.currentTimeMillis(),
                    sourceIp = parsed.sourceIp,
                    destIp = domain,  // Show domain instead of just 8.8.8.8
                    protocol = "DNS",
                    sizeBytes = realSize ?: parsed.totalLength,
                    appName = appName,
                    contentType = contentType,
                    isRisk = isRiskyDomain(domain)
                )
                
                packetRepository.logPacket(packet)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error logging DNS query", e)
            }
        }
    }
    
    /**
     * Get real traffic size for an app based on domain.
     */
    private fun getAppTrafficSize(domain: String): Int? {
        val lowerDomain = domain.lowercase()
        
        // Find matching package
        for ((pattern, packageName) in DOMAIN_TO_PACKAGE) {
            if (lowerDomain.contains(pattern)) {
                val traffic = trafficStatsHelper.getAppTraffic(packageName)
                if (traffic != null && traffic.totalBytes > 0) {
                    // Return delta (new bytes since last check)
                    val delta = trafficStatsHelper.getTrafficDelta(traffic.uid)
                    val totalDelta = delta.first + delta.second
                    if (totalDelta > 0) {
                        return totalDelta.toInt().coerceAtMost(Int.MAX_VALUE)
                    }
                }
                break
            }
        }
        return null
    }
    
    /**
     * Detect app name from domain.
     */
    private fun detectAppFromDomain(domain: String): Pair<String, String> {
        val lowerDomain = domain.lowercase()
        
        return when {
            // Google/YouTube
            lowerDomain.contains("youtube") || lowerDomain.contains("googlevideo") || 
            lowerDomain.contains("ytimg") -> "YouTube" to "Video"
            
            lowerDomain.contains("google") || lowerDomain.contains("gstatic") ||
            lowerDomain.contains("googleapis") -> "Google" to "Services"
            
            // Social Media
            lowerDomain.contains("instagram") || lowerDomain.contains("cdninstagram") -> "Instagram" to "Social"
            lowerDomain.contains("facebook") || lowerDomain.contains("fbcdn") ||
            lowerDomain.contains("fb.com") -> "Facebook" to "Social"
            lowerDomain.contains("twitter") || lowerDomain.contains("twimg") ||
            lowerDomain.contains("x.com") -> "Twitter/X" to "Social"
            lowerDomain.contains("tiktok") || lowerDomain.contains("tiktokcdn") ||
            lowerDomain.contains("musical.ly") -> "TikTok" to "Video"
            lowerDomain.contains("snapchat") || lowerDomain.contains("snap-") -> "Snapchat" to "Social"
            lowerDomain.contains("whatsapp") -> "WhatsApp" to "Messaging"
            lowerDomain.contains("telegram") -> "Telegram" to "Messaging"
            lowerDomain.contains("discord") -> "Discord" to "Chat"
            lowerDomain.contains("reddit") -> "Reddit" to "Social"
            lowerDomain.contains("linkedin") -> "LinkedIn" to "Social"
            
            // Streaming
            lowerDomain.contains("netflix") -> "Netflix" to "Streaming"
            lowerDomain.contains("spotify") -> "Spotify" to "Music"
            lowerDomain.contains("prime") || lowerDomain.contains("amazon") -> "Amazon" to "Shopping"
            
            // Tech 
            lowerDomain.contains("microsoft") || lowerDomain.contains("msn") ||
            lowerDomain.contains("azure") || lowerDomain.contains("windows") -> "Microsoft" to "System"
            lowerDomain.contains("apple") || lowerDomain.contains("icloud") -> "Apple" to "System"
            
            // CDN/Infrastructure
            lowerDomain.contains("cloudflare") || lowerDomain.contains("cf-") -> "Cloudflare" to "CDN"
            lowerDomain.contains("akamai") -> "Akamai" to "CDN"
            lowerDomain.contains("fastly") -> "Fastly" to "CDN"
            lowerDomain.contains("aws") || lowerDomain.contains("amazonaws") -> "AWS" to "Cloud"
            
            // Analytics/Ads
            lowerDomain.contains("analytics") || lowerDomain.contains("tracking") ||
            lowerDomain.contains("doubleclick") || lowerDomain.contains("adsserver") ||
            lowerDomain.contains("adservice") -> "Analytics" to "Tracking ⚠️"
            
            // Default
            else -> domain.split(".").takeLast(2).joinToString(".") to "Web"
        }
    }
    
    /**
     * Check if domain is potentially risky.
     */
    private fun isRiskyDomain(domain: String): Boolean {
        val lowerDomain = domain.lowercase()
        return lowerDomain.contains("analytics") ||
               lowerDomain.contains("tracking") ||
               lowerDomain.contains("telemetry") ||
               lowerDomain.contains("ads.") ||
               lowerDomain.contains("adservice")
    }
    
    fun stop() {
        isRunning.set(false)
        handlerThread?.interrupt()
        handlerThread = null
        
        try {
            dnsSocket?.close()
        } catch (_: Exception) {}
        dnsSocket = null
        
        Log.d(TAG, "DNS handler stopped")
    }
}
