package com.datasentry.app.data.repository

import com.datasentry.app.data.local.dao.PacketDao
import com.datasentry.app.data.local.entity.PacketEntity
import kotlinx.coroutines.flow.Flow

class PacketRepository(private val packetDao: PacketDao) {

    val allPackets: Flow<List<PacketEntity>> = packetDao.getAllPackets()
    val riskCount: Flow<Int> = packetDao.getRiskCount()

    suspend fun logPacket(packet: PacketEntity) {
        packetDao.insertPacket(packet)
    }

    suspend fun clearLogs() {
        packetDao.clearAll()
    }
}
