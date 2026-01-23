package com.datasentry.app.analysis;

/**
 * Aggregates packets into meaningful app sessions and performs analysis.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 \u001f2\u00020\u0001:\u0002\u001f B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J\"\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\r0\u000b2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J.\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00122\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J$\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000f0\b2\u0006\u0010\u0010\u001a\u00020\f2\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\bH\u0086@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\u00020\u00182\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\bJ\u001c\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u000f0\b2\u0006\u0010\u0010\u001a\u00020\fH\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\fH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/datasentry/app/analysis/SessionAggregator;", "", "packetRepository", "Lcom/datasentry/app/data/repository/PacketRepository;", "(Lcom/datasentry/app/data/repository/PacketRepository;)V", "analyzeConnectionPattern", "Lcom/datasentry/app/data/model/AppSession$ConnectionPattern;", "packets", "", "Lcom/datasentry/app/data/local/entity/PacketEntity;", "classifyPacketSizes", "", "", "", "createSession", "Lcom/datasentry/app/data/model/AppSession;", "appName", "startTime", "", "endTime", "createSessionsFromPackets", "getAppSessions", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSessionStats", "Lcom/datasentry/app/analysis/SessionAggregator$SessionStats;", "sessions", "getSessionsForApp", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isTracker", "", "domain", "Companion", "SessionStats", "app_debug"})
public final class SessionAggregator {
    @org.jetbrains.annotations.NotNull()
    private final com.datasentry.app.data.repository.PacketRepository packetRepository = null;
    private static final long SESSION_TIMEOUT_MS = 120000L;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> TRACKER_PATTERNS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.datasentry.app.analysis.SessionAggregator.Companion Companion = null;
    
    public SessionAggregator(@org.jetbrains.annotations.NotNull()
    com.datasentry.app.data.repository.PacketRepository packetRepository) {
        super();
    }
    
    /**
     * Get all app sessions from stored packets
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getAppSessions(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.datasentry.app.data.model.AppSession>> $completion) {
        return null;
    }
    
    /**
     * Get sessions for a specific app
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getSessionsForApp(@org.jetbrains.annotations.NotNull()
    java.lang.String appName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.datasentry.app.data.model.AppSession>> $completion) {
        return null;
    }
    
    /**
     * Create sessions from a list of packets
     */
    private final java.util.List<com.datasentry.app.data.model.AppSession> createSessionsFromPackets(java.lang.String appName, java.util.List<com.datasentry.app.data.local.entity.PacketEntity> packets) {
        return null;
    }
    
    /**
     * Create a session object with analysis
     */
    private final com.datasentry.app.data.model.AppSession createSession(java.lang.String appName, long startTime, long endTime, java.util.List<com.datasentry.app.data.local.entity.PacketEntity> packets) {
        return null;
    }
    
    /**
     * Check if a domain is a tracker
     */
    private final boolean isTracker(java.lang.String domain) {
        return false;
    }
    
    /**
     * Classify packets by size into traffic types
     */
    private final java.util.Map<java.lang.String, java.lang.Integer> classifyPacketSizes(java.util.List<com.datasentry.app.data.local.entity.PacketEntity> packets) {
        return null;
    }
    
    /**
     * Analyze connection pattern based on packet timing
     */
    private final com.datasentry.app.data.model.AppSession.ConnectionPattern analyzeConnectionPattern(java.util.List<com.datasentry.app.data.local.entity.PacketEntity> packets) {
        return null;
    }
    
    /**
     * Get session statistics
     */
    @org.jetbrains.annotations.NotNull()
    public final com.datasentry.app.analysis.SessionAggregator.SessionStats getSessionStats(@org.jetbrains.annotations.NotNull()
    java.util.List<com.datasentry.app.data.model.AppSession> sessions) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/datasentry/app/analysis/SessionAggregator$Companion;", "", "()V", "SESSION_TIMEOUT_MS", "", "TRACKER_PATTERNS", "", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J1\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0017H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0018"}, d2 = {"Lcom/datasentry/app/analysis/SessionAggregator$SessionStats;", "", "totalSessions", "", "highRiskSessions", "totalTrackers", "appsWithTrackers", "(IIII)V", "getAppsWithTrackers", "()I", "getHighRiskSessions", "getTotalSessions", "getTotalTrackers", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "toString", "", "app_debug"})
    public static final class SessionStats {
        private final int totalSessions = 0;
        private final int highRiskSessions = 0;
        private final int totalTrackers = 0;
        private final int appsWithTrackers = 0;
        
        public SessionStats(int totalSessions, int highRiskSessions, int totalTrackers, int appsWithTrackers) {
            super();
        }
        
        public final int getTotalSessions() {
            return 0;
        }
        
        public final int getHighRiskSessions() {
            return 0;
        }
        
        public final int getTotalTrackers() {
            return 0;
        }
        
        public final int getAppsWithTrackers() {
            return 0;
        }
        
        public final int component1() {
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
        public final com.datasentry.app.analysis.SessionAggregator.SessionStats copy(int totalSessions, int highRiskSessions, int totalTrackers, int appsWithTrackers) {
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