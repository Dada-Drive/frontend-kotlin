package com.dadadrive.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dadadrive.data.local.dao.RideDao
import com.dadadrive.data.local.dao.UserDao
import com.dadadrive.data.local.entity.RideEntity
import com.dadadrive.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, RideEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun rideDao(): RideDao

    companion object {
        const val DATABASE_NAME = "dadadrive.db"
    }
}
