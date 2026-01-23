package com.datasentry.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0090\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u0000 F2\u00020\u0001:\u0001FB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020\fH\u0002J\b\u0010%\u001a\u00020#H\u0002J\b\u0010&\u001a\u00020#H\u0002J\b\u0010\'\u001a\u00020(H\u0002J\u001a\u0010)\u001a\u0004\u0018\u00010\f2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u001aH\u0002J\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u001aH\u0002J\u0018\u00100\u001a\u00020\f2\u0006\u0010*\u001a\u00020+2\u0006\u00101\u001a\u00020\u001aH\u0002J0\u00102\u001a\u00020#2\u0006\u00103\u001a\u00020\f2\u0006\u00104\u001a\u00020\f2\u0006\u00105\u001a\u00020\f2\u0006\u00106\u001a\u00020\u001a2\u0006\u0010/\u001a\u00020\u001aH\u0002J\b\u00107\u001a\u00020#H\u0016J\b\u00108\u001a\u00020#H\u0016J\"\u00109\u001a\u00020\u001a2\b\u0010:\u001a\u0004\u0018\u00010;2\u0006\u0010<\u001a\u00020\u001a2\u0006\u0010=\u001a\u00020\u001aH\u0016J\b\u0010>\u001a\u00020#H\u0002J\b\u0010?\u001a\u00020#H\u0002J\b\u0010@\u001a\u00020#H\u0002J\b\u0010A\u001a\u00020#H\u0002J\b\u0010B\u001a\u00020#H\u0002J\u001a\u0010C\u001a\u0004\u0018\u00010\f2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u001aH\u0002J\u001a\u0010D\u001a\u0004\u0018\u00010\f2\u0006\u00103\u001a\u00020\f2\u0006\u0010/\u001a\u00020\u001aH\u0002J\u0018\u0010E\u001a\u00020\u001a2\u0006\u0010*\u001a\u00020+2\u0006\u00101\u001a\u00020\u001aH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006G"}, d2 = {"Lcom/datasentry/app/DataSentryService;", "Landroid/net/VpnService;", "()V", "demoThread", "Ljava/lang/Thread;", "dnsHandler", "Lcom/datasentry/app/vpn/DnsOnlyHandler;", "flowStatsRepository", "Lcom/datasentry/app/data/repository/FlowStatsRepositoryImpl;", "isRunning", "", "lastAlert", "", "lastStatsBroadcastMs", "", "packetHandler", "Lcom/datasentry/app/vpn/VpnPacketHandler;", "packetRepository", "Lcom/datasentry/app/data/repository/PacketRepository;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "statsLock", "", "stopSignal", "Ljava/util/concurrent/atomic/AtomicBoolean;", "suspiciousCount", "", "suspiciousEventRepository", "Lcom/datasentry/app/data/repository/SuspiciousEventRepositoryImpl;", "trafficInspector", "Lcom/datasentry/app/inspector/TrafficInspector;", "tunReaderThread", "vpnInterface", "Landroid/os/ParcelFileDescriptor;", "broadcastLog", "", "message", "broadcastStats", "broadcastStatsThrottled", "createNotification", "Landroid/app/Notification;", "extractDestIp", "buf", "", "len", "getRiskLevel", "Lcom/datasentry/app/data/model/RiskLevel;", "dstPort", "ipv4", "off", "maybeRecordSuspicious", "protocol", "src", "dst", "srcPort", "onDestroy", "onRevoke", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "saveFlowsToDatabase", "startDemoSimulation", "startTunReader", "startVpn", "stopVpn", "summarizePacket", "suspiciousReason", "u16", "Companion", "app_debug"})
public final class DataSentryService extends android.net.VpnService {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "com.datasentry.app.STOP_VPN";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LOG_EVENT = "com.datasentry.app.VPN_LOG_EVENT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String STATS_EVENT = "com.datasentry.app.VPN_STATS_EVENT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_SUSPICIOUS_COUNT = "suspiciousCount";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_LAST_ALERT = "lastAlert";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "vpn_channel";
    private static final int NOTIFICATION_ID = 1;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "DataSentryVPN";
    public static final boolean REAL_MODE = true;
    @org.jetbrains.annotations.Nullable()
    private android.os.ParcelFileDescriptor vpnInterface;
    private boolean isRunning = false;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Thread tunReaderThread;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean stopSignal = null;
    @org.jetbrains.annotations.Nullable()
    private com.datasentry.app.vpn.VpnPacketHandler packetHandler;
    @org.jetbrains.annotations.Nullable()
    private com.datasentry.app.vpn.DnsOnlyHandler dnsHandler;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Object statsLock = null;
    private int suspiciousCount = 0;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastAlert;
    private long lastStatsBroadcastMs = 0L;
    @org.jetbrains.annotations.NotNull()
    private final com.datasentry.app.inspector.TrafficInspector trafficInspector = null;
    private com.datasentry.app.data.repository.FlowStatsRepositoryImpl flowStatsRepository;
    private com.datasentry.app.data.repository.SuspiciousEventRepositoryImpl suspiciousEventRepository;
    private com.datasentry.app.data.repository.PacketRepository packetRepository;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Thread demoThread;
    @org.jetbrains.annotations.NotNull()
    public static final com.datasentry.app.DataSentryService.Companion Companion = null;
    
    public DataSentryService() {
        super();
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void startVpn() {
    }
    
    /**
     * Starts a timer-based demo simulation that inserts fake traffic data into the database.
     * The UI observes the database via Flow, so packets appear in the list automatically.
     */
    private final void startDemoSimulation() {
    }
    
    private final void startTunReader() {
    }
    
    /**
     * Extract destination IP from an IPv4 packet for demo scenario matching.
     */
    private final java.lang.String extractDestIp(byte[] buf, int len) {
        return null;
    }
    
    private final java.lang.String summarizePacket(byte[] buf, int len) {
        return null;
    }
    
    private final void maybeRecordSuspicious(java.lang.String protocol, java.lang.String src, java.lang.String dst, int srcPort, int dstPort) {
    }
    
    private final java.lang.String suspiciousReason(java.lang.String protocol, int dstPort) {
        return null;
    }
    
    private final com.datasentry.app.data.model.RiskLevel getRiskLevel(int dstPort) {
        return null;
    }
    
    private final void saveFlowsToDatabase() {
    }
    
    private final void broadcastStatsThrottled() {
    }
    
    private final void broadcastStats() {
    }
    
    private final java.lang.String ipv4(byte[] buf, int off) {
        return null;
    }
    
    private final int u16(byte[] buf, int off) {
        return 0;
    }
    
    private final void stopVpn() {
    }
    
    private final void broadcastLog(java.lang.String message) {
    }
    
    private final android.app.Notification createNotification() {
        return null;
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onRevoke() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/datasentry/app/DataSentryService$Companion;", "", "()V", "ACTION_STOP", "", "CHANNEL_ID", "EXTRA_LAST_ALERT", "EXTRA_SUSPICIOUS_COUNT", "LOG_EVENT", "NOTIFICATION_ID", "", "REAL_MODE", "", "STATS_EVENT", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}