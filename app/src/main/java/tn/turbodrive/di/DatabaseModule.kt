package tn.turbodrive.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tn.turbodrive.data.local.AppDatabase
import tn.turbodrive.data.local.migrations.AllMigrations
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        @Suppress("SpreadOperator") // addMigrations() is a vararg API — spread is unavoidable here
        Room.databaseBuilder(context, AppDatabase::class.java, "turbodrive_cache.db")
            // REMOVED: fallbackToDestructiveMigration() — destroys all local data on version bump.
            // Explicit migrations required for every @Database version increment.
            .addMigrations(*AllMigrations.ALL)
            .build()
}
