package com.datasentry.app.data.model

import com.datasentry.app.data.local.entity.PacketEntity

/**
 * Represents a usage session for an app.
 * A session groups multiple packets together based on temporal proximity.
 */
data class AppSession(
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val packets: List<PacketEntity>,
    val domains: Set<String>,
    val trackers: List<String>,
    val totalBytes: Int,
    val packetCount: Int,
    val packetSizeBreakdown: Map<String, Int>,
    val privacyImpact: PrivacyImpact,
    val connectionPattern: ConnectionPattern
) {
    enum class PrivacyImpact {
        LOW,     // 0 trackers
        MEDIUM,  // 1-2 trackers
        HIGH;    // 3+ trackers
        
        fun toDisplayString(): String = when (this) {
            LOW -> "LOW"
            MEDIUM -> "MEDIUM"
            HIGH -> "HIGH"
        }
    }
    
    enum class ConnectionPattern {
        CONSTANT,   // < 1s average gap
        FREQUENT,   // 1-10s gap
        PERIODIC,   // 10-60s gap
        OCCASIONAL; // > 60s gap
        
        fun toDisplayString(): String = when (this) {
            CONSTANT -> "Constant (tracking behavior)"
            FREQUENT -> "Frequent (real-time updates)"
            PERIODIC -> "Periodic (background sync)"
            OCCASIONAL -> "Occasional (user-initiated)"
        }
    }
    
    /**
     * Get duration of session in milliseconds
     */
    fun getDuration(): Long = endTime - startTime
    
    /**
     * Get formatted duration string
     */
    fun getFormattedDuration(): String {
        val seconds = getDuration() / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        
        return when {
            minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${secs}s"
            else -> "${secs}s"
        }
    }
    
    /**
     * Get time ago string
     */
    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - endTime
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        }
    }
    
    /**
     * Get dominant traffic type
     */
    fun getDominantTrafficType(): String {
        return packetSizeBreakdown.maxByOrNull { it.value }?.key ?: "Unknown"
    }
}
