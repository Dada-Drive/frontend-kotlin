package tn.turbodrive.data.local

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.turbodrive.data.local.entity.CachedActiveRideEntity
import tn.turbodrive.domain.models.ActiveRide
import tn.turbodrive.domain.models.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveRideDraftCache
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
        private val gson: Gson,
    ) {
        suspend fun saveActiveOrClear(ride: ActiveRide?) =
            withContext(Dispatchers.IO) {
                val dao = appDatabase.cachedActiveRideDao()
                if (ride == null) return@withContext
                if (ride.status !in PersistableStatuses) {
                    dao.clearAll()
                    return@withContext
                }
                val json = gson.toJson(ride)
                dao.upsert(
                    CachedActiveRideEntity(
                        rideStateJson = json,
                        cachedAt = System.currentTimeMillis(),
                    ),
                )
            }

        suspend fun clear() =
            withContext(Dispatchers.IO) {
                appDatabase.cachedActiveRideDao().clearAll()
            }

        suspend fun load(): ActiveRide? =
            withContext(Dispatchers.IO) {
                val json = appDatabase.cachedActiveRideDao().getSingleton()?.rideStateJson ?: return@withContext null
                runCatching { gson.fromJson(json, ActiveRide::class.java) }.getOrNull()
            }

        private companion object {
            private val PersistableStatuses =
                setOf(
                    RideStatus.Pending,
                    RideStatus.Scheduled,
                    RideStatus.Offered,
                    RideStatus.Accepted,
                    RideStatus.InProgress,
                )
        }
    }
