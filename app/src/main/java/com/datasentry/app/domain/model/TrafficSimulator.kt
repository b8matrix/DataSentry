package com.datasentry.app.domain.model

import com.datasentry.app.data.local.entity.PacketEntity
import com.datasentry.app.data.repository.PacketRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

class TrafficSimulator(private val repository: PacketRepository) {

    private var isSimulating = false

    suspend fun startSimulation() {
        isSimulating = true
        while (isSimulating) {
            val packet = generateRandomPacket()
            repository.logPacket(packet)
            // Random delay between 100ms and 2000ms
            delay(Random.nextLong(100, 2000))
        }
    }

    fun stopSimulation() {
        isSimulating = false
    }

    private fun generateRandomPacket(): PacketEntity {
        val apps = listOf("YouTube", "Instagram", "Chrome", "WhatsApp", "System", "Unknown")
        val ips = listOf("172.217.160.142", "157.240.16.35", "192.168.1.1", "104.244.42.1")
        val protocols = listOf("TCP", "UDP", "HTTPS") 
        
        val app = apps.random()
        val isRisk = app == "Unknown" || app == "System" && Random.nextBoolean()

        // HEURISTIC ENGINE: Guessed Content Type based on App & Size
        val sizeBytes = when(app) {
            "YouTube" -> Random.nextInt(10000, 1500000) // Heavy Video chunks
            "Instagram" -> Random.nextInt(5000, 800000) // Images/Reels
            "WhatsApp" -> Random.nextInt(50, 2000)      // Text/Audio
            else -> Random.nextInt(100, 50000)
        }

        val contentType = when {
            sizeBytes > 1000000 -> "Video 4K"
            sizeBytes > 500000 -> "Video"
            sizeBytes > 50000 -> "Image"
            sizeBytes > 1000 -> "Audio"
            else -> "Text"
        }

        return PacketEntity(
            timestamp = System.currentTimeMillis(),
            sourceIp = "192.168.1.105", 
            destIp = ips.random(),
            protocol = protocols.random(),
            sizeBytes = sizeBytes,
            appName = app,
            contentType = contentType,
            isRisk = isRisk
        )
    }
}
