package com.dadadrive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dadadrive.data.local.entity.RideEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY createdAt DESC")
    fun observeAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getRidesPaged(limit: Int, offset: Int): List<RideEntity>

    @Query("SELECT * FROM rides WHERE id = :rideId LIMIT 1")
    suspend fun getRideById(rideId: String): RideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRides(rides: List<RideEntity>)

    @Query("DELETE FROM rides WHERE id = :rideId")
    suspend fun deleteRide(rideId: String)

    @Query("DELETE FROM rides")
    suspend fun clearAll()
}
