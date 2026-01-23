package com.datasentry.app.vpn

import android.net.VpnService
import android.util.Log
import java.io.FileOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles TCP packet forwarding for the VPN using NIO.
 * 
 * TCP is stateful, so we need to track connections and manage the TCP state machine.
 * We use a simplified approach: for each unique (srcPort, dstIp, dstPort), we maintain
 * a SocketChannel to the real destination.
 */
class TcpForwarder(
    private val vpnService: VpnService,
    private val onPacketCaptured: (PacketParser.ParsedPacket, String?) -> Unit
) {
    companion object {
        private const val TAG = "TcpForwarder"
        private const val CONNECT_TIMEOUT_MS = 10_000
        
        // TCP Flags
        const val TCP_FIN = 0x01
        const val TCP_SYN = 0x02
        const val TCP_RST = 0x04
        const val TCP_PSH = 0x08
        const val TCP_ACK = 0x10
    }
    
    data class TcpConnection(
        val channel: SocketChannel,
        val sourcePort: Int,
        val destIp: String,
        val destPort: Int,
        var lastActivity: Long = System.currentTimeMillis()
    )
    
    private val connections = ConcurrentHashMap<String, TcpConnection>()
    private val isRunning = AtomicBoolean(true)
    private var selector: Selector? = null
    
    /**
     * Generate a unique key for tracking TCP connections.
     */
    private fun connectionKey(srcPort: Int, dstIp: String, dstPort: Int): String {
        return "$srcPort-$dstIp:$dstPort"
    }
    
    /**
     * Process a TCP packet from the TUN interface.
     */
    fun processPacket(
        buffer: ByteArray,
        length: Int,
        tunOutput: FileOutputStream,
        originalPacket: ByteArray
    ) {
        val parsed = PacketParser.parse(buffer, length) ?: return
        if (parsed.protocol != 6) return  // Only TCP
        
        val key = connectionKey(parsed.sourcePort, parsed.destIp, parsed.destPort)
        
        // Get TCP flags
        val ihl = parsed.headerLength
        val flags = if (length > ihl + 13) buffer[ihl + 13].toInt() and 0xFF else 0
        
        // Log the packet
        onPacketCaptured(parsed, null)
        
        try {
            when {
                // SYN - New connection request
                (flags and TCP_SYN) != 0 && (flags and TCP_ACK) == 0 -> {
                    handleSyn(parsed, buffer, tunOutput, key)
                }
                
                // RST or FIN - Connection close
                (flags and TCP_RST) != 0 || (flags and TCP_FIN) != 0 -> {
                    handleClose(key)
                }
                
                // Data packet (ACK with payload)
                (flags and TCP_ACK) != 0 && parsed.payloadLength > 0 -> {
                    handleData(parsed, buffer, tunOutput, key)
                }
                
                // Pure ACK (no data) - acknowledge receipt
                (flags and TCP_ACK) != 0 -> {
                    // Just acknowledge, no action needed for simplified forwarding
                    Log.d(TAG, "ACK for $key")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing TCP packet for $key", e)
        }
    }
    
    /**
     * Handle SYN - Establish connection to destination.
     */
    private fun handleSyn(
        parsed: PacketParser.ParsedPacket,
        buffer: ByteArray,
        tunOutput: FileOutputStream,
        key: String
    ) {
        Log.d(TAG, "SYN: Connecting to ${parsed.destIp}:${parsed.destPort}")
        
        // Close existing connection if any
        connections.remove(key)?.channel?.close()
        
        try {
            val channel = SocketChannel.open()
            channel.configureBlocking(false)
            vpnService.protect(channel.socket())  // CRITICAL: Bypass VPN
            
            val connected = channel.connect(InetSocketAddress(parsed.destIp, parsed.destPort))
            
            if (!connected) {
                // Wait for connection to complete
                val startTime = System.currentTimeMillis()
                while (!channel.finishConnect()) {
                    if (System.currentTimeMillis() - startTime > CONNECT_TIMEOUT_MS) {
                        Log.w(TAG, "Connection timeout to ${parsed.destIp}:${parsed.destPort}")
                        channel.close()
                        sendRst(parsed, buffer, tunOutput)
                        return
                    }
                    Thread.sleep(10)
                }
            }
            
            connections[key] = TcpConnection(
                channel = channel,
                sourcePort = parsed.sourcePort,
                destIp = parsed.destIp,
                destPort = parsed.destPort
            )
            
            // Send SYN-ACK back to TUN
            sendSynAck(parsed, buffer, tunOutput)
            
            Log.d(TAG, "Connected to ${parsed.destIp}:${parsed.destPort}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to ${parsed.destIp}:${parsed.destPort}", e)
            sendRst(parsed, buffer, tunOutput)
        }
    }
    
    /**
     * Handle data packet - Forward to destination.
     */
    private fun handleData(
        parsed: PacketParser.ParsedPacket,
        buffer: ByteArray,
        tunOutput: FileOutputStream,
        key: String
    ) {
        val conn = connections[key] ?: run {
            Log.w(TAG, "No connection for data packet: $key")
            return
        }
        
        conn.lastActivity = System.currentTimeMillis()
        
        try {
            // Extract payload
            val payload = ByteBuffer.wrap(
                buffer, 
                parsed.payloadOffset, 
                parsed.payloadLength
            )
            
            // Forward to destination
            while (payload.hasRemaining()) {
                conn.channel.write(payload)
            }
            
            Log.d(TAG, "Forwarded ${parsed.payloadLength} bytes to ${parsed.destIp}:${parsed.destPort}")
            
            // Read response (non-blocking)
            readResponse(conn, parsed, buffer, tunOutput)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding data for $key", e)
            handleClose(key)
            sendRst(parsed, buffer, tunOutput)
        }
    }
    
    /**
     * Read response from destination and send back to TUN.
     */
    private fun readResponse(
        conn: TcpConnection,
        parsed: PacketParser.ParsedPacket,
        originalBuffer: ByteArray,
        tunOutput: FileOutputStream
    ) {
        try {
            val responseBuffer = ByteBuffer.allocate(32768)
            val bytesRead = conn.channel.read(responseBuffer)
            
            if (bytesRead > 0) {
                responseBuffer.flip()
                val responseData = ByteArray(bytesRead)
                responseBuffer.get(responseData)
                
                // Build TCP response packet
                val responsePacket = buildTcpResponsePacket(
                    originalBuffer, parsed, responseData, bytesRead
                )
                
                synchronized(tunOutput) {
                    tunOutput.write(responsePacket)
                    tunOutput.flush()
                }
                
                Log.d(TAG, "Response: $bytesRead bytes from ${parsed.destIp}:${parsed.destPort}")
            }
        } catch (e: Exception) {
            // Non-blocking read may throw if no data available
            Log.d(TAG, "No immediate response from ${parsed.destIp}:${parsed.destPort}")
        }
    }
    
    /**
     * Handle connection close.
     */
    private fun handleClose(key: String) {
        connections.remove(key)?.let { conn ->
            try {
                conn.channel.close()
                Log.d(TAG, "Closed connection: $key")
            } catch (e: Exception) {}
        }
    }
    
    /**
     * Build a synthetic SYN-ACK response.
     */
    private fun sendSynAck(
        parsed: PacketParser.ParsedPacket,
        originalBuffer: ByteArray,
        tunOutput: FileOutputStream
    ) {
        // Simplified: We just let the connection proceed
        // In a full implementation, we'd build a proper SYN-ACK
        Log.d(TAG, "SYN-ACK for ${parsed.sourcePort}")
    }
    
    /**
     * Send RST to terminate connection.
     */
    private fun sendRst(
        parsed: PacketParser.ParsedPacket,
        originalBuffer: ByteArray,
        tunOutput: FileOutputStream
    ) {
        Log.d(TAG, "Sending RST for ${parsed.sourcePort}")
        // Simplified: Connection will time out naturally
    }
    
    /**
     * Build TCP response packet.
     */
    private fun buildTcpResponsePacket(
        originalBuffer: ByteArray,
        parsed: PacketParser.ParsedPacket,
        responseData: ByteArray,
        responseLength: Int
    ): ByteArray {
        val ihl = parsed.headerLength
        val tcpHeaderLen = 20  // Minimum TCP header
        val totalLen = ihl + tcpHeaderLen + responseLength
        
        val packet = ByteArray(totalLen)
        
        // Copy and modify IP header
        System.arraycopy(originalBuffer, 0, packet, 0, ihl)
        
        // Swap source and destination IP
        System.arraycopy(originalBuffer, 16, packet, 12, 4)
        System.arraycopy(originalBuffer, 12, packet, 16, 4)
        
        // Update total length
        packet[2] = ((totalLen shr 8) and 0xFF).toByte()
        packet[3] = (totalLen and 0xFF).toByte()
        
        // Clear IP checksum
        packet[10] = 0
        packet[11] = 0
        
        // TCP header - swap ports
        packet[ihl] = ((parsed.destPort shr 8) and 0xFF).toByte()
        packet[ihl + 1] = (parsed.destPort and 0xFF).toByte()
        packet[ihl + 2] = ((parsed.sourcePort shr 8) and 0xFF).toByte()
        packet[ihl + 3] = (parsed.sourcePort and 0xFF).toByte()
        
        // Sequence number (simplified)
        for (i in 0..3) packet[ihl + 4 + i] = 0
        
        // ACK number (simplified)
        for (i in 0..3) packet[ihl + 8 + i] = 0
        
        // Data offset (5 = 20 bytes) and flags (ACK + PSH)
        packet[ihl + 12] = 0x50.toByte()  // Data offset = 5, no options
        packet[ihl + 13] = (TCP_ACK or TCP_PSH).toByte()
        
        // Window size
        packet[ihl + 14] = 0xFF.toByte()
        packet[ihl + 15] = 0xFF.toByte()
        
        // Checksum (0 = let OS calculate)
        packet[ihl + 16] = 0
        packet[ihl + 17] = 0
        
        // Urgent pointer
        packet[ihl + 18] = 0
        packet[ihl + 19] = 0
        
        // Copy response data
        System.arraycopy(responseData, 0, packet, ihl + tcpHeaderLen, responseLength)
        
        return packet
    }
    
    /**
     * Cleanup all connections.
     */
    fun cleanup() {
        connections.values.forEach { 
            try { it.channel.close() } catch (e: Exception) {}
        }
        connections.clear()
    }
}
