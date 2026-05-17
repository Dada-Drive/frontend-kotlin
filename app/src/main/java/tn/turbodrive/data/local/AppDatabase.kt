package tn.turbodrive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import tn.turbodrive.data.local.dao.CachedActiveRideDao
import tn.turbodrive.data.local.dao.CachedSavedPlaceDao
import tn.turbodrive.data.local.dao.CachedUserProfileDao
import tn.turbodrive.data.local.dao.CachedWalletBalanceDao
import tn.turbodrive.data.local.entity.CachedActiveRideEntity
import tn.turbodrive.data.local.entity.CachedSavedPlaceEntity
import tn.turbodrive.data.local.entity.CachedUserProfileEntity
import tn.turbodrive.data.local.entity.CachedWalletBalanceEntity

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
