package com.datasentry.app.vpn;

/**
 * Handles UDP packet forwarding for the VPN.
 *
 * Key concept: We use protect() on sockets so they bypass the VPN tunnel,
 * allowing us to forward packets to the real internet.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u001a\u0010\u0004\u001a\u0016\u0012\u0004\u0012\u00020\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0012\u0004\u0012\u00020\b0\u0005\u00a2\u0006\u0002\u0010\tJ(\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u000eH\u0002J\u0006\u0010\u0016\u001a\u00020\bJ&\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\u00112\u0006\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u0011R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\u0004\u001a\u0016\u0012\u0004\u0012\u00020\u0006\u0012\u0006\u0012\u0004\u0018\u00010\u0007\u0012\u0004\u0012\u00020\b0\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000f0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/datasentry/app/vpn/UdpForwarder;", "", "vpnService", "Landroid/net/VpnService;", "onPacketCaptured", "Lkotlin/Function2;", "Lcom/datasentry/app/vpn/PacketParser$ParsedPacket;", "", "", "(Landroid/net/VpnService;Lkotlin/jvm/functions/Function2;)V", "isRunning", "Ljava/util/concurrent/atomic/AtomicBoolean;", "udpSockets", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Ljava/net/DatagramSocket;", "buildUdpResponsePacket", "", "originalIp", "originalParsed", "responsePayload", "responseLength", "cleanup", "forwardPacket", "buffer", "length", "tunOutput", "Ljava/io/FileOutputStream;", "originalPacket", "Companion", "app_debug"})
public final class UdpForwarder {
    @org.jetbrains.annotations.NotNull()
    private final android.net.VpnService vpnService = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.datasentry.app.vpn.PacketParser.ParsedPacket, java.lang.String, kotlin.Unit> onPacketCaptured = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "UdpForwarder";
    private static final int UDP_TIMEOUT_MS = 30000;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean isRunning = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.Integer, java.net.DatagramSocket> udpSockets = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.datasentry.app.vpn.UdpForwarder.Companion Companion = null;
    
    public UdpForwarder(@org.jetbrains.annotations.NotNull()
    android.net.VpnService vpnService, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.datasentry.app.vpn.PacketParser.ParsedPacket, ? super java.lang.String, kotlin.Unit> onPacketCaptured) {
        super();
    }
    
    /**
     * Forward a UDP packet to the real internet.
     *
     * @param buffer Raw packet data
     * @param length Length of valid data
     * @param tunOutput Output stream to write responses back to TUN
     * @param originalPacket Full original packet for header reconstruction
     */
    public final void forwardPacket(@org.jetbrains.annotations.NotNull()
    byte[] buffer, int length, @org.jetbrains.annotations.NotNull()
    java.io.FileOutputStream tunOutput, @org.jetbrains.annotations.NotNull()
    byte[] originalPacket) {
    }
    
    /**
     * Build a UDP response packet to write back to TUN.
     * Swaps source/dest IP and ports from the original request.
     */
    private final byte[] buildUdpResponsePacket(byte[] originalIp, com.datasentry.app.vpn.PacketParser.ParsedPacket originalParsed, byte[] responsePayload, int responseLength) {
        return null;
    }
    
    /**
     * Clean up old sockets.
     */
    public final void cleanup() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/datasentry/app/vpn/UdpForwarder$Companion;", "", "()V", "TAG", "", "UDP_TIMEOUT_MS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}