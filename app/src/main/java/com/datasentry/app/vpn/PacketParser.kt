package com.datasentry.app.vpn

import java.nio.ByteBuffer

/**
 * Parses IP and transport layer headers from raw packets.
 * Supports IPv4 with TCP and UDP.
 */
object PacketParser {
    
    data class ParsedPacket(
        val version: Int,
        val headerLength: Int,
        val totalLength: Int,
        val protocol: Int,  // 6=TCP, 17=UDP, 1=ICMP
        val sourceIp: String,
        val destIp: String,
        val sourcePort: Int,
        val destPort: Int,
        val payloadOffset: Int,
        val payloadLength: Int
    ) {
        val protocolName: String get() = when(protocol) {
            6 -> "TCP"
            17 -> "UDP"
            1 -> "ICMP"
            else -> "IP/$protocol"
        }
        
        val isDns: Boolean get() = protocol == 17 && (destPort == 53 || sourcePort == 53)
        val isHttp: Boolean get() = destPort == 80 || sourcePort == 80
        val isHttps: Boolean get() = destPort == 443 || sourcePort == 443
    }
    
    /**
     * Parse a raw IP packet from the TUN interface.
     * @param buffer Raw packet bytes
     * @param length Actual length of data in buffer
     * @return ParsedPacket or null if parsing fails
     */
    fun parse(buffer: ByteArray, length: Int): ParsedPacket? {
        if (length < 20) return null  // Minimum IPv4 header
        
        val version = (buffer[0].toInt() and 0xFF) ushr 4
        if (version != 4) return null  // Only IPv4 supported
        
        val ihl = (buffer[0].toInt() and 0x0F) * 4  // Header length in bytes
        if (length < ihl) return null
        
        val totalLength = ((buffer[2].toInt() and 0xFF) shl 8) or (buffer[3].toInt() and 0xFF)
        val protocol = buffer[9].toInt() and 0xFF
        
        val sourceIp = formatIpv4(buffer, 12)
        val destIp = formatIpv4(buffer, 16)
        
        // Parse transport layer header for ports
        var sourcePort = 0
        var destPort = 0
        var payloadOffset = ihl
        
        when (protocol) {
            6, 17 -> {  // TCP or UDP
                if (length >= ihl + 4) {
                    sourcePort = ((buffer[ihl].toInt() and 0xFF) shl 8) or (buffer[ihl + 1].toInt() and 0xFF)
                    destPort = ((buffer[ihl + 2].toInt() and 0xFF) shl 8) or (buffer[ihl + 3].toInt() and 0xFF)
                    payloadOffset = if (protocol == 17) ihl + 8 else {
                        // TCP header length from data offset field
                        val tcpHeaderLen = ((buffer[ihl + 12].toInt() and 0xF0) ushr 4) * 4
                        ihl + tcpHeaderLen
                    }
                }
            }
        }
        
        return ParsedPacket(
            version = version,
            headerLength = ihl,
            totalLength = totalLength,
            protocol = protocol,
            sourceIp = sourceIp,
            destIp = destIp,
            sourcePort = sourcePort,
            destPort = destPort,
            payloadOffset = payloadOffset,
            payloadLength = maxOf(0, totalLength - payloadOffset)
        )
    }
    
    /**
     * Format 4 bytes at offset as dotted-decimal IPv4 address.
     */
    private fun formatIpv4(buffer: ByteArray, offset: Int): String {
        return "${buffer[offset].toInt() and 0xFF}." +
               "${buffer[offset + 1].toInt() and 0xFF}." +
               "${buffer[offset + 2].toInt() and 0xFF}." +
               "${buffer[offset + 3].toInt() and 0xFF}"
    }
    
    /**
     * Extract DNS query domain name from a DNS packet.
     * @param buffer Full packet buffer
     * @param parsed ParsedPacket with offsets
     * @return Domain name or null if not a valid DNS query
     */
    fun extractDnsDomain(buffer: ByteArray, parsed: ParsedPacket): String? {
        if (!parsed.isDns) return null
        if (parsed.payloadOffset + 12 >= buffer.size) return null
        
        try {
            // DNS header is 12 bytes, domain starts after
            var pos = parsed.payloadOffset + 12
            val domain = StringBuilder()
            
            while (pos < buffer.size && buffer[pos] != 0.toByte()) {
                val labelLen = buffer[pos].toInt() and 0xFF
                if (labelLen == 0 || pos + labelLen >= buffer.size) break
                
                if (domain.isNotEmpty()) domain.append('.')
                domain.append(String(buffer, pos + 1, labelLen, Charsets.US_ASCII))
                pos += labelLen + 1
            }
            
            return if (domain.isNotEmpty()) domain.toString() else null
        } catch (e: Exception) {
            return null
        }
    }
}
