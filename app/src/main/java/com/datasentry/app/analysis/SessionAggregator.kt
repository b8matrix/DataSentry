package com.datasentry.app.analysis

import com.datasentry.app.data.local.entity.PacketEntity
import com.datasentry.app.data.model.AppSession
import com.datasentry.app.data.repository.PacketRepository
import kotlinx.coroutines.flow.first

/**
 * Aggregates packets into meaningful app sessions and performs analysis.
 */
class SessionAggregator(private val packetRepository: PacketRepository) {
    
    companion object {
        // Session ends after 2 minutes of inactivity
        private const val SESSION_TIMEOUT_MS = 2 * 60 * 1000L
        
        // Tracker domain patterns
        private val TRACKER_PATTERNS = listOf(
            "analytics",
            "tracking",
            "telemetry",
            "doubleclick",
            "facebook-analytics",
            "graph.facebook",
            "crashlytics",
            "firebase-analytics",
            "adjust.com",
            "apps.mopub",
            "googleadservices",
            "googlesyndication",
            "adservice",
            "adsystem"
        )
    }
    
    /**
     * Get all app sessions from stored packets
     */
    suspend fun getAppSessions(): List<AppSession> {
        val allPackets = packetRepository.allPackets.first()
        
        // Group by app
        val byApp = allPackets.groupBy { it.appName }
        
        return byApp.flatMap { (appName, packets) ->
            createSessionsFromPackets(appName, packets)
        }.sortedByDescending { it.endTime }
    }
    
    /**
     * Get sessions for a specific app
     */
    suspend fun getSessionsForApp(appName: String): List<AppSession> {
        val packets = packetRepository.allPackets.first().filter { it.appName == appName }
        return createSessionsFromPackets(appName, packets)
    }
    
    /**
     * Create sessions from a list of packets
     */
    private fun createSessionsFromPackets(
        appName: String,
        packets: List<PacketEntity>
    ): List<AppSession> {
        if (packets.isEmpty()) return emptyList()
        
        val sessions = mutableListOf<AppSession>()
        val sortedPackets = packets.sortedBy { it.timestamp }
        
        var currentSessionPackets = mutableListOf<PacketEntity>()
        var sessionStart = sortedPackets.first().timestamp
        
        for (packet in sortedPackets) {
            if (currentSessionPackets.isEmpty()) {
                // Start new session
                currentSessionPackets.add(packet)
                sessionStart = packet.timestamp
            } else {
                val lastPacket = currentSessionPackets.last()
                val gap = packet.timestamp - lastPacket.timestamp
                
                if (gap > SESSION_TIMEOUT_MS) {
                    // Session ended - save it and start new one
                    sessions.add(
                        createSession(
                            appName,
                            sessionStart,
                            lastPacket.timestamp,
                            currentSessionPackets.toList()
                        )
                    )
                    
                    currentSessionPackets.clear()
                    currentSessionPackets.add(packet)
                    sessionStart = packet.timestamp
                } else {
                    // Continue current session
                    currentSessionPackets.add(packet)
                }
            }
        }
        
        // Don't forget last session
        if (currentSessionPackets.isNotEmpty()) {
            sessions.add(
                createSession(
                    appName,
                    sessionStart,
                    currentSessionPackets.last().timestamp,
                    currentSessionPackets
                )
            )
        }
        
        return sessions
    }
    
    /**
     * Create a session object with analysis
     */
    private fun createSession(
        appName: String,
        startTime: Long,
        endTime: Long,
        packets: List<PacketEntity>
    ): AppSession {
        // Extract unique domains
        val domains = packets.map { it.destIp }.filter { it.isNotBlank() }.toSet()
        
        // Identify trackers
        val trackers = domains.filter { isTracker(it) }
        
        // Calculate total bytes
        val totalBytes = packets.sumOf { it.sizeBytes }
        
        // Breakdown by packet size
        val sizeBreakdown = classifyPacketSizes(packets)
        
        // Determine privacy impact
        val privacyImpact = when {
            trackers.size >= 3 -> AppSession.PrivacyImpact.HIGH
            trackers.size >= 1 -> AppSession.PrivacyImpact.MEDIUM
            else -> AppSession.PrivacyImpact.LOW
        }
        
        // Analyze connection pattern
        val connectionPattern = analyzeConnectionPattern(packets)
        
        return AppSession(
            appName = appName,
            startTime = startTime,
            endTime = endTime,
            packets = packets,
            domains = domains,
            trackers = trackers,
            totalBytes = totalBytes,
            packetCount = packets.size,
            packetSizeBreakdown = sizeBreakdown,
            privacyImpact = privacyImpact,
            connectionPattern = connectionPattern
        )
    }
    
    /**
     * Check if a domain is a tracker
     */
    private fun isTracker(domain: String): Boolean {
        val lowerDomain = domain.lowercase()
        return TRACKER_PATTERNS.any { pattern ->
            lowerDomain.contains(pattern)
        }
    }
    
    /**
     * Classify packets by size into traffic types
     */
    private fun classifyPacketSizes(packets: List<PacketEntity>): Map<String, Int> {
        val classification = packets.groupingBy { packet ->
            when {
                packet.sizeBytes < 200 -> "Text/Chat"
                packet.sizeBytes < 1000 -> "Images/Data"
                else -> "Video/Files"
            }
        }.eachCount()
        
        return classification
    }
    
    /**
     * Analyze connection pattern based on packet timing
     */
    private fun analyzeConnectionPattern(packets: List<PacketEntity>): AppSession.ConnectionPattern {
        if (packets.size < 2) return AppSession.ConnectionPattern.OCCASIONAL
        
        val sortedPackets = packets.sortedBy { it.timestamp }
        val intervals = sortedPackets.zipWithNext { a, b ->
            b.timestamp - a.timestamp
        }
        
        if (intervals.isEmpty()) return AppSession.ConnectionPattern.OCCASIONAL
        
        val avgInterval = intervals.average()
        
        return when {
            avgInterval < 1000 -> AppSession.ConnectionPattern.CONSTANT
            avgInterval < 10000 -> AppSession.ConnectionPattern.FREQUENT
            avgInterval < 60000 -> AppSession.ConnectionPattern.PERIODIC
            else -> AppSession.ConnectionPattern.OCCASIONAL
        }
    }
    
    /**
     * Get session statistics
     */
    fun getSessionStats(sessions: List<AppSession>): SessionStats {
        val totalSessions = sessions.size
        val highRiskSessions = sessions.count { it.privacyImpact == AppSession.PrivacyImpact.HIGH }
        val totalTrackers = sessions.sumOf { it.trackers.size }
        val appsWithTrackers = sessions.count { it.trackers.isNotEmpty() }
        
        return SessionStats(
            totalSessions = totalSessions,
            highRiskSessions = highRiskSessions,
            totalTrackers = totalTrackers,
            appsWithTrackers = appsWithTrackers
        )
    }
    
    data class SessionStats(
        val totalSessions: Int,
        val highRiskSessions: Int,
        val totalTrackers: Int,
        val appsWithTrackers: Int
    )
}
