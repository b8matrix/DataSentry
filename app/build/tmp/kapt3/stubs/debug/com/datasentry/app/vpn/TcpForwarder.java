package com.datasentry.app.vpn;

/**
 * Handles TCP packet forwarding for the VPN using NIO.
 *
 * TCP is stateful, so we need to track connections and manage the TCP state machine.
 * We use a simplified approach: for each unique (srcPort, dstIp, dstPort), we maintain
 * a SocketChannel to the real destination.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u0000 +2\u00020\u0001:\u0002+,B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u001a\u0010\u0004\u001a\u0016\u0012\u0004\u0012\u00020\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0012\u0004\u0012\u00020\b0\u0005\u00a2\u0006\u0002\u0010\tJ(\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\u0006\u0010\u0018\u001a\u00020\bJ \u0010\u0019\u001a\u00020\u00072\u0006\u0010\u001a\u001a\u00020\u00172\u0006\u0010\u001b\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0017H\u0002J\u0010\u0010\u001d\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\u0007H\u0002J(\u0010\u001f\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u001e\u001a\u00020\u0007H\u0002J(\u0010#\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u001e\u001a\u00020\u0007H\u0002J&\u0010$\u001a\u00020\b2\u0006\u0010 \u001a\u00020\u00122\u0006\u0010%\u001a\u00020\u00172\u0006\u0010!\u001a\u00020\"2\u0006\u0010&\u001a\u00020\u0012J(\u0010\'\u001a\u00020\b2\u0006\u0010(\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"H\u0002J \u0010)\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"H\u0002J \u0010*\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0013\u001a\u00020\u00122\u0006\u0010!\u001a\u00020\"H\u0002R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\u0004\u001a\u0016\u0012\u0004\u0012\u00020\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0012\u0004\u0012\u00020\b0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006-"}, d2 = {"Lcom/datasentry/app/vpn/TcpForwarder;", "", "vpnService", "Landroid/net/VpnService;", "onPacketCaptured", "Lkotlin/Function2;", "Lcom/datasentry/app/vpn/PacketParser$ParsedPacket;", "", "", "(Landroid/net/VpnService;Lkotlin/jvm/functions/Function2;)V", "connections", "Ljava/util/concurrent/ConcurrentHashMap;", "Lcom/datasentry/app/vpn/TcpForwarder$TcpConnection;", "isRunning", "Ljava/util/concurrent/atomic/AtomicBoolean;", "selector", "Ljava/nio/channels/Selector;", "buildTcpResponsePacket", "", "originalBuffer", "parsed", "responseData", "responseLength", "", "cleanup", "connectionKey", "srcPort", "dstIp", "dstPort", "handleClose", "key", "handleData", "buffer", "tunOutput", "Ljava/io/FileOutputStream;", "handleSyn", "processPacket", "length", "originalPacket", "readResponse", "conn", "sendRst", "sendSynAck", "Companion", "TcpConnection", "app_debug"})
public final class TcpForwarder {
    @org.jetbrains.annotations.NotNull()
    private final android.net.VpnService vpnService = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.datasentry.app.vpn.PacketParser.ParsedPacket, java.lang.String, kotlin.Unit> onPacketCaptured = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "TcpForwarder";
    private static final int CONNECT_TIMEOUT_MS = 10000;
    public static final int TCP_FIN = 1;
    public static final int TCP_SYN = 2;
    public static final int TCP_RST = 4;
    public static final int TCP_PSH = 8;
    public static final int TCP_ACK = 16;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.datasentry.app.vpn.TcpForwarder.TcpConnection> connections = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean isRunning = null;
    @org.jetbrains.annotations.Nullable()
    private java.nio.channels.Selector selector;
    @org.jetbrains.annotations.NotNull()
    public static final com.datasentry.app.vpn.TcpForwarder.Companion Companion = null;
    
    public TcpForwarder(@org.jetbrains.annotations.NotNull()
    android.net.VpnService vpnService, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.datasentry.app.vpn.PacketParser.ParsedPacket, ? super java.lang.String, kotlin.Unit> onPacketCaptured) {
        super();
    }
    
    /**
     * Generate a unique key for tracking TCP connections.
     */
    private final java.lang.String connectionKey(int srcPort, java.lang.String dstIp, int dstPort) {
        return null;
    }
    
    /**
     * Process a TCP packet from the TUN interface.
     */
    public final void processPacket(@org.jetbrains.annotations.NotNull()
    byte[] buffer, int length, @org.jetbrains.annotations.NotNull()
    java.io.FileOutputStream tunOutput, @org.jetbrains.annotations.NotNull()
    byte[] originalPacket) {
    }
    
    /**
     * Handle SYN - Establish connection to destination.
     */
    private final void handleSyn(com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] buffer, java.io.FileOutputStream tunOutput, java.lang.String key) {
    }
    
    /**
     * Handle data packet - Forward to destination.
     */
    private final void handleData(com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] buffer, java.io.FileOutputStream tunOutput, java.lang.String key) {
    }
    
    /**
     * Read response from destination and send back to TUN.
     */
    private final void readResponse(com.datasentry.app.vpn.TcpForwarder.TcpConnection conn, com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] originalBuffer, java.io.FileOutputStream tunOutput) {
    }
    
    /**
     * Handle connection close.
     */
    private final void handleClose(java.lang.String key) {
    }
    
    /**
     * Build a synthetic SYN-ACK response.
     */
    private final void sendSynAck(com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] originalBuffer, java.io.FileOutputStream tunOutput) {
    }
    
    /**
     * Send RST to terminate connection.
     */
    private final void sendRst(com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] originalBuffer, java.io.FileOutputStream tunOutput) {
    }
    
    /**
     * Build TCP response packet.
     */
    private final byte[] buildTcpResponsePacket(byte[] originalBuffer, com.datasentry.app.vpn.PacketParser.ParsedPacket parsed, byte[] responseData, int responseLength) {
        return null;
    }
    
    /**
     * Cleanup all connections.
     */
    public final void cleanup() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/datasentry/app/vpn/TcpForwarder$Companion;", "", "()V", "CONNECT_TIMEOUT_MS", "", "TAG", "", "TCP_ACK", "TCP_FIN", "TCP_PSH", "TCP_RST", "TCP_SYN", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B/\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\nH\u00c6\u0003J;\u0010\u001c\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\u001e2\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010 \u001a\u00020\u0005H\u00d6\u0001J\t\u0010!\u001a\u00020\u0007H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011\u00a8\u0006\""}, d2 = {"Lcom/datasentry/app/vpn/TcpForwarder$TcpConnection;", "", "channel", "Ljava/nio/channels/SocketChannel;", "sourcePort", "", "destIp", "", "destPort", "lastActivity", "", "(Ljava/nio/channels/SocketChannel;ILjava/lang/String;IJ)V", "getChannel", "()Ljava/nio/channels/SocketChannel;", "getDestIp", "()Ljava/lang/String;", "getDestPort", "()I", "getLastActivity", "()J", "setLastActivity", "(J)V", "getSourcePort", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    public static final class TcpConnection {
        @org.jetbrains.annotations.NotNull()
        private final java.nio.channels.SocketChannel channel = null;
        private final int sourcePort = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String destIp = null;
        private final int destPort = 0;
        private long lastActivity;
        
        public TcpConnection(@org.jetbrains.annotations.NotNull()
        java.nio.channels.SocketChannel channel, int sourcePort, @org.jetbrains.annotations.NotNull()
        java.lang.String destIp, int destPort, long lastActivity) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.nio.channels.SocketChannel getChannel() {
            return null;
        }
        
        public final int getSourcePort() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDestIp() {
            return null;
        }
        
        public final int getDestPort() {
            return 0;
        }
        
        public final long getLastActivity() {
            return 0L;
        }
        
        public final void setLastActivity(long p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.nio.channels.SocketChannel component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        public final int component4() {
            return 0;
        }
        
        public final long component5() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.datasentry.app.vpn.TcpForwarder.TcpConnection copy(@org.jetbrains.annotations.NotNull()
        java.nio.channels.SocketChannel channel, int sourcePort, @org.jetbrains.annotations.NotNull()
        java.lang.String destIp, int destPort, long lastActivity) {
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