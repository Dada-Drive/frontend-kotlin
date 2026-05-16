package tn.dadadrive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import tn.dadadrive.data.local.dao.CachedActiveRideDao
import tn.dadadrive.data.local.dao.CachedSavedPlaceDao
import tn.dadadrive.data.local.dao.CachedUserProfileDao
import tn.dadadrive.data.local.dao.CachedWalletBalanceDao
import tn.dadadrive.data.local.entity.CachedActiveRideEntity
import tn.dadadrive.data.local.entity.CachedSavedPlaceEntity
import tn.dadadrive.data.local.entity.CachedUserProfileEntity
import tn.dadadrive.data.local.entity.CachedWalletBalanceEntity

@Database(
    version = 1,
    entities = [
        CachedUserProfileEntity::class,
        CachedWalletBalanceEntity::class,
        CachedActiveRideEntity::class,
        CachedSavedPlaceEntity::class,
    ],
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedUserProfileDao(): CachedUserProfileDao
    abstract fun cachedWalletBalanceDao(): CachedWalletBalanceDao
    abstract fun cachedActiveRideDao(): CachedActiveRideDao
    abstract fun cachedSavedPlaceDao(): CachedSavedPlaceDao
}
