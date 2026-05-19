package tn.turbodrive.data.local.migrations

import androidx.room.migration.Migration

/**
 * All Room database migrations for AppDatabase.
 *
 * Policy: every @Database version bump requires an explicit Migration(N, N+1) here.
 * Never use fallbackToDestructiveMigration() — it silently destroys all local data.
 *
 * Workflow:
 * 1. Modify the entity (add/rename/remove column or table).
 * 2. Bump @Database(version = N+1).
 * 3. Build → app/schemas/tn.turbodrive.data.local.AppDatabase/N+1.json generated.
 * 4. Write Migration(N, N+1) below and add it to ALL.
 * 5. Add a MigrationTest covering the migration path.
 *
 * Example future migration:
 *   val MIGRATION_1_2 = object : Migration(1, 2) {
 *       override fun migrate(db: SupportSQLiteDatabase) {
 *           db.execSQL("ALTER TABLE cached_active_ride ADD COLUMN ttlSeconds INTEGER NOT NULL DEFAULT 0")
 *       }
 *   }
 */
object AllMigrations {
    val ALL: Array<Migration> =
        arrayOf(
            // Add migrations here as: MIGRATION_1_2, MIGRATION_2_3, …
        )
}
