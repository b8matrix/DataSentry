package com.datasentry.app.vpn;

/**
 * Parses IP and transport layer headers from raw packets.
 * Supports IPv4 with TCP and UDP.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u000eB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000bH\u0002J\u0018\u0010\f\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000b\u00a8\u0006\u000f"}, d2 = {"Lcom/datasentry/app/vpn/PacketParser;", "", "()V", "extractDnsDomain", "", "buffer", "", "parsed", "Lcom/datasentry/app/vpn/PacketParser$ParsedPacket;", "formatIpv4", "offset", "", "parse", "length", "ParsedPacket", "app_debug"})
public final class PacketParser {
    @org.jetbrains.annotations.NotNull()
    public static final com.datasentry.app.vpn.PacketParser INSTANCE = null;
    
    private PacketParser() {
        super();
    }
    
    /**
     * Parse a raw IP packet from the TUN interface.
     * @param buffer Raw packet bytes
     * @param length Actual length of data in buffer
     * @return ParsedPacket or null if parsing fails
     */
    @org.jetbrains.annotations.Nullable()
    public final com.datasentry.app.vpn.PacketParser.ParsedPacket parse(@org.jetbrains.annotations.NotNull()
    byte[] buffer, int length) {
        return null;
    }
    
    /**
     * Format 4 bytes at offset as dotted-decimal IPv4 address.
     */
    private final java.lang.String formatIpv4(byte[] buffer, int offset) {
        return null;
    }
    
    /**
     * Extract DNS query domain name from a DNS packet.
     * @param buffer Full packet buffer
     * @param parsed ParsedPacket with offsets
     * @return Domain name or null if not a valid DNS query
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String extractDnsDomain(@org.jetbrains.annotations.NotNull()
    byte[] buffer, @org.jetbrains.annotations.NotNull()
    com.datasentry.app.vpn.PacketParser.ParsedPacket parsed) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u001c\b\u0086\b\u0018\u00002\u00020\u0001BU\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\u0003\u0012\u0006\u0010\u000b\u001a\u00020\u0003\u0012\u0006\u0010\f\u001a\u00020\u0003\u0012\u0006\u0010\r\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0003H\u00c6\u0003J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\'\u001a\u00020\bH\u00c6\u0003J\t\u0010(\u001a\u00020\bH\u00c6\u0003J\t\u0010)\u001a\u00020\u0003H\u00c6\u0003J\t\u0010*\u001a\u00020\u0003H\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003Jm\u0010,\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010-\u001a\u00020\u00152\b\u0010.\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010/\u001a\u00020\u0003H\u00d6\u0001J\t\u00100\u001a\u00020\bH\u00d6\u0001R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0011\u0010\u0014\u001a\u00020\u00158F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0016R\u0011\u0010\u0017\u001a\u00020\u00158F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0016R\u0011\u0010\u0018\u001a\u00020\u00158F\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0016R\u0011\u0010\r\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0012R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0012R\u0011\u0010\u001c\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\u001d\u0010\u0010R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0010R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0012\u00a8\u00061"}, d2 = {"Lcom/datasentry/app/vpn/PacketParser$ParsedPacket;", "", "version", "", "headerLength", "totalLength", "protocol", "sourceIp", "", "destIp", "sourcePort", "destPort", "payloadOffset", "payloadLength", "(IIIILjava/lang/String;Ljava/lang/String;IIII)V", "getDestIp", "()Ljava/lang/String;", "getDestPort", "()I", "getHeaderLength", "isDns", "", "()Z", "isHttp", "isHttps", "getPayloadLength", "getPayloadOffset", "getProtocol", "protocolName", "getProtocolName", "getSourceIp", "getSourcePort", "getTotalLength", "getVersion", "component1", "component10", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
    public static final class ParsedPacket {
        private final int version = 0;
        private final int headerLength = 0;
        private final int totalLength = 0;
        private final int protocol = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String sourceIp = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String destIp = null;
        private final int sourcePort = 0;
        private final int destPort = 0;
        private final int payloadOffset = 0;
        private final int payloadLength = 0;
        
        public ParsedPacket(int version, int headerLength, int totalLength, int protocol, @org.jetbrains.annotations.NotNull()
        java.lang.String sourceIp, @org.jetbrains.annotations.NotNull()
        java.lang.String destIp, int sourcePort, int destPort, int payloadOffset, int payloadLength) {
            super();
        }
        
        public final int getVersion() {
            return 0;
        }
        
        public final int getHeaderLength() {
            return 0;
        }
        
        public final int getTotalLength() {
            return 0;
        }
        
        public final int getProtocol() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSourceIp() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDestIp() {
            return null;
        }
        
        public final int getSourcePort() {
            return 0;
        }
        
        public final int getDestPort() {
            return 0;
        }
        
        public final int getPayloadOffset() {
            return 0;
        }
        
        public final int getPayloadLength() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getProtocolName() {
            return null;
        }
        
        public final boolean isDns() {
            return false;
        }
        
        public final boolean isHttp() {
            return false;
        }
        
        public final boolean isHttps() {
            return false;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component10() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final int component3() {
            return 0;
        }
        
        public final int component4() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component6() {
            return null;
        }
        
        public final int component7() {
            return 0;
        }
        
        public final int component8() {
            return 0;
        }
        
        public final int component9() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.datasentry.app.vpn.PacketParser.ParsedPacket copy(int version, int headerLength, int totalLength, int protocol, @org.jetbrains.annotations.NotNull()
        java.lang.String sourceIp, @org.jetbrains.annotations.NotNull()
        java.lang.String destIp, int sourcePort, int destPort, int payloadOffset, int payloadLength) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}