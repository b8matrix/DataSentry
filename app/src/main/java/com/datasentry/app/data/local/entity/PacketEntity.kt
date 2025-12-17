package com.datasentry.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packets")
data class PacketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sourceIp: String,
    val destIp: String,
    val protocol: String = "TCP",
    val sizeBytes: Int,
    val appName: String, // Heuristic: "YouTube", "Instagram", etc.
    val contentType: String = "Text", // Heuristic: "Video", "Audio", "Text"
    val isRisk: Boolean = false // For Hackathon "Privacy Score"
)
