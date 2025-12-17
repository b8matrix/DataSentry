package com.datasentry.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.datasentry.app.data.local.entity.PacketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketDao {
    @Query("SELECT * FROM packets ORDER BY timestamp DESC")
    fun getAllPackets(): Flow<List<PacketEntity>>

    @Insert
    suspend fun insertPacket(packet: PacketEntity)

    @Query("DELETE FROM packets")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM packets WHERE isRisk = 1")
    fun getRiskCount(): Flow<Int>
}
