package tn.dadadrive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tn.dadadrive.data.local.entity.CachedActiveRideEntity
import tn.dadadrive.data.local.entity.CachedSavedPlaceEntity
import tn.dadadrive.data.local.entity.CachedUserProfileEntity
import tn.dadadrive.data.local.entity.CachedWalletBalanceEntity

@Dao
interface CachedUserProfileDao {
    @Query("SELECT * FROM cached_user_profile WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CachedUserProfileEntity?

    @Query("SELECT * FROM cached_user_profile ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLatest(): CachedUserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedUserProfileEntity)

    @Query("DELETE FROM cached_user_profile")
    suspend fun clearAll()
}

@Dao
interface CachedWalletBalanceDao {
    @Query("SELECT * FROM cached_wallet_balance WHERE id = 'singleton' LIMIT 1")
    suspend fun getSingleton(): CachedWalletBalanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedWalletBalanceEntity)

    @Query("DELETE FROM cached_wallet_balance")
    suspend fun clearAll()
}

@Dao
interface CachedActiveRideDao {
    @Query("SELECT * FROM cached_active_ride WHERE id = 'singleton' LIMIT 1")
    suspend fun getSingleton(): CachedActiveRideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedActiveRideEntity)

    @Query("DELETE FROM cached_active_ride")
    suspend fun clearAll()
}

@Dao
interface CachedSavedPlaceDao {
    @Query("SELECT * FROM cached_saved_place ORDER BY cachedAt DESC")
    suspend fun getAll(): List<CachedSavedPlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedSavedPlaceEntity)

    @Query("DELETE FROM cached_saved_place WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cached_saved_place")
    suspend fun clearAll()
}
