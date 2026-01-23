package com.datasentry.app.vpn

import android.content.Context
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.util.Log

/**
 * Uses Android's TrafficStats API to get real per-app data usage.
 * 
 * This provides actual bytes sent/received per app, which DNS capture cannot provide.
 */
class TrafficStatsHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "TrafficStatsHelper"
    }
    
    data class AppTrafficInfo(
        val packageName: String,
        val appName: String,
        val uid: Int,
        val rxBytes: Long,  // Received
        val txBytes: Long,  // Transmitted
        val totalBytes: Long
    )
    
    // Cache of previous readings to calculate delta
    private val previousReadings = mutableMapOf<Int, Pair<Long, Long>>()
    
    /**
     * Get traffic stats for all installed apps.
     */
    fun getAllAppsTraffic(): List<AppTrafficInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return apps.mapNotNull { appInfo ->
            try {
                val uid = appInfo.uid
                val rxBytes = TrafficStats.getUidRxBytes(uid)
                val txBytes = TrafficStats.getUidTxBytes(uid)
                
                // Skip apps with no traffic
                if (rxBytes <= 0 && txBytes <= 0) return@mapNotNull null
                
                val appName = pm.getApplicationLabel(appInfo).toString()
                
                AppTrafficInfo(
                    packageName = appInfo.packageName,
                    appName = appName,
                    uid = uid,
                    rxBytes = rxBytes,
                    txBytes = txBytes,
                    totalBytes = rxBytes + txBytes
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.totalBytes }
    }
    
    /**
     * Get traffic delta (new data since last check) for a specific UID.
     */
    fun getTrafficDelta(uid: Int): Pair<Long, Long> {
        val currentRx = TrafficStats.getUidRxBytes(uid)
        val currentTx = TrafficStats.getUidTxBytes(uid)
        
        val previous = previousReadings[uid] ?: (0L to 0L)
        val deltaRx = if (currentRx > previous.first) currentRx - previous.first else 0L
        val deltaTx = if (currentTx > previous.second) currentTx - previous.second else 0L
        
        previousReadings[uid] = currentRx to currentTx
        
        return deltaRx to deltaTx
    }
    
    /**
     * Get traffic for a specific package name.
     */
    fun getAppTraffic(packageName: String): AppTrafficInfo? {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val uid = appInfo.uid
            
            val rxBytes = TrafficStats.getUidRxBytes(uid)
            val txBytes = TrafficStats.getUidTxBytes(uid)
            
            AppTrafficInfo(
                packageName = packageName,
                appName = pm.getApplicationLabel(appInfo).toString(),
                uid = uid,
                rxBytes = rxBytes,
                txBytes = txBytes,
                totalBytes = rxBytes + txBytes
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting traffic for $packageName", e)
            null
        }
    }
    
    /**
     * Get top N apps by data usage.
     */
    fun getTopApps(limit: Int = 10): List<AppTrafficInfo> {
        return getAllAppsTraffic().take(limit)
    }
    
    /**
     * Format bytes to human readable.
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024L * 1024L -> String.format("%.1f GB", bytes / (1024f * 1024f * 1024f))
            bytes >= 1024L * 1024L -> String.format("%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024L -> String.format("%.1f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }
}
