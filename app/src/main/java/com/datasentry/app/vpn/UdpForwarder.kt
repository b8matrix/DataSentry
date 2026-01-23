package com.datasentry.app.vpn

import android.net.VpnService
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles UDP packet forwarding for the VPN.
 * 
 * Key concept: We use protect() on sockets so they bypass the VPN tunnel,
 * allowing us to forward packets to the real internet.
 */
class UdpForwarder(
    private val vpnService: VpnService,
    private val onPacketCaptured: (PacketParser.ParsedPacket, String?) -> Unit  // Callback for logging
) {
    companion object {
        private const val TAG = "UdpForwarder"
        private const val UDP_TIMEOUT_MS = 30_000
    }
    
    private val isRunning = AtomicBoolean(false)
    
    // Map to track UDP "connections" (source port -> socket)
    private val udpSockets = ConcurrentHashMap<Int, DatagramSocket>()
    
    /**
     * Forward a UDP packet to the real internet.
     * 
     * @param buffer Raw packet data
     * @param length Length of valid data
     * @param tunOutput Output stream to write responses back to TUN
     * @param originalPacket Full original packet for header reconstruction
     */
    fun forwardPacket(
        buffer: ByteArray,
        length: Int,
        tunOutput: FileOutputStream,
        originalPacket: ByteArray
    ) {
        val parsed = PacketParser.parse(buffer, length) ?: return
        if (parsed.protocol != 17) return  // Only UDP
        
        try {
            // Extract UDP payload
            val payloadLength = parsed.payloadLength
            if (payloadLength <= 0) return
            
            val payload = ByteArray(payloadLength)
            System.arraycopy(buffer, parsed.payloadOffset, payload, 0, payloadLength)
            
            // Extract DNS domain if this is a DNS query
            val domain = PacketParser.extractDnsDomain(buffer, parsed)
            
            // Log the packet
            onPacketCaptured(parsed, domain)
            
            // Get or create socket for this source port
            val socket = udpSockets.getOrPut(parsed.sourcePort) {
                DatagramSocket().also { 
                    vpnService.protect(it)  // CRITICAL: Bypass VPN tunnel
                    it.soTimeout = UDP_TIMEOUT_MS
                }
            }
            
            // Send to destination
            val destAddr = InetAddress.getByName(parsed.destIp)
            val outPacket = DatagramPacket(payload, payload.size, destAddr, parsed.destPort)
            socket.send(outPacket)
            
            Log.d(TAG, "Forwarded UDP ${parsed.sourcePort} -> ${parsed.destIp}:${parsed.destPort}" +
                    if (domain != null) " (DNS: $domain)" else "")
            
            // Receive response
            val responseBuffer = ByteArray(4096)
            val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            
            try {
                socket.receive(responsePacket)
                
                // Build response packet to write back to TUN
                val responseIpPacket = buildUdpResponsePacket(
                    originalIp = buffer,
                    originalParsed = parsed,
                    responsePayload = responsePacket.data,
                    responseLength = responsePacket.length
                )
                
                synchronized(tunOutput) {
                    tunOutput.write(responseIpPacket)
                    tunOutput.flush()
                }
                
                Log.d(TAG, "Response received and written to TUN: ${responsePacket.length} bytes")
                
            } catch (e: java.net.SocketTimeoutException) {
                Log.d(TAG, "UDP response timeout for ${parsed.destIp}:${parsed.destPort}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding UDP packet", e)
        }
    }
    
    /**
     * Build a UDP response packet to write back to TUN.
     * Swaps source/dest IP and ports from the original request.
     */
    private fun buildUdpResponsePacket(
        originalIp: ByteArray,
        originalParsed: PacketParser.ParsedPacket,
        responsePayload: ByteArray,
        responseLength: Int
    ): ByteArray {
        val ihl = originalParsed.headerLength
        val udpHeaderLen = 8
        val totalLen = ihl + udpHeaderLen + responseLength
        
        val packet = ByteArray(totalLen)
        
        // Copy IP header from original (we'll modify it)
        System.arraycopy(originalIp, 0, packet, 0, ihl)
        
        // Swap source and destination IP
        System.arraycopy(originalIp, 16, packet, 12, 4)  // Original dest -> new source
        System.arraycopy(originalIp, 12, packet, 16, 4)  // Original source -> new dest
        
        // Update total length
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        
        // Clear checksum (will be ignored/recalculated by OS)
        packet[10] = 0
        packet[11] = 0
        
        // UDP header - swap ports
        packet[ihl] = ((originalParsed.destPort shr 8) and 0xFF).toByte()
        packet[ihl + 1] = (originalParsed.destPort and 0xFF).toByte()
        packet[ihl + 2] = ((originalParsed.sourcePort shr 8) and 0xFF).toByte()
        packet[ihl + 3] = (originalParsed.sourcePort and 0xFF).toByte()
        
        // UDP length
        val udpLen = udpHeaderLen + responseLength
        packet[ihl + 4] = ((udpLen shr 8) and 0xFF).toByte()
        packet[ihl + 5] = (udpLen and 0xFF).toByte()
        
        // UDP checksum (0 = disabled, valid for UDP)
        packet[ihl + 6] = 0
        packet[ihl + 7] = 0
        
        // Copy response payload
        System.arraycopy(responsePayload, 0, packet, ihl + udpHeaderLen, responseLength)
        
        return packet
    }
    
    /**
     * Clean up old sockets.
     */
    fun cleanup() {
        udpSockets.values.forEach { 
            try { it.close() } catch (e: Exception) {}
        }
        udpSockets.clear()
    }
}
