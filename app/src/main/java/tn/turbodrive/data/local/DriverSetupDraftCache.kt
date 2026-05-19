package tn.turbodrive.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * R-5.2 — Persistence of the driver-setup wizard draft.
 *
 * Survives process death so a driver who killed the app mid-onboarding
 * can resume where they left off. Photos are NOT stored (kept in scoped
 * cacheDir + bitmap state) ; only the typed fields + step index live here.
 */
@Singleton
class DriverSetupDraftCache
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val gson: Gson,
    ) {
        fun observe(): Flow<DriverSetupDraft?> =
            dataStore.data.map { prefs ->
                prefs[DRAFT_KEY]?.let {
                    runCatching { gson.fromJson(it, DriverSetupDraft::class.java) }.getOrNull()
                }
            }

        suspend fun load(): DriverSetupDraft? = observe().first()

        suspend fun save(draft: DriverSetupDraft) {
            val json = gson.toJson(draft)
            dataStore.edit { prefs ->
                prefs[DRAFT_KEY] = json
            }
        }

        suspend fun clear() {
            dataStore.edit { prefs ->
                prefs.remove(DRAFT_KEY)
            }
        }

        private companion object {
            private val DRAFT_KEY = stringPreferencesKey("driver_setup_draft")
        }
    }

data class DriverSetupDraft(
    val cinNumber: String? = null,
    val cinDeliveredAt: String? = null,
    val licenseSuffix: String? = null,
    val licenseIssueAt: String? = null,
    val licenseExpiryAt: String? = null,
    val licenseCategories: List<String>? = null,
    val vehicleMake: String? = null,
    val vehicleModel: String? = null,
    val vehicleYear: String? = null,
    val vehicleColor: String? = null,
    val plateNumber: String? = null,
    val vehicleType: String? = null,
    val seats: String? = null,
    val currentStep: Int = 0,
)
