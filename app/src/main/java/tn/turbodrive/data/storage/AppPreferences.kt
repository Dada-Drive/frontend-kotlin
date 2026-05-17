package tn.turbodrive.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val dataStore: DataStore<Preferences>,
    ) {
        private val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        suspend fun readIntroOnboardingComplete(): Boolean {
            val snapshot = dataStore.data.first()
            val fromStore = snapshot[KEY_INTRO_ONBOARDING_DS]
            if (fromStore != null) return fromStore
            val legacy = prefs.getBoolean(KEY_INTRO_ONBOARDING_LEGACY, false)
            if (legacy) {
                dataStore.edit { it[KEY_INTRO_ONBOARDING_DS] = true }
            }
            return legacy
        }

        suspend fun setIntroOnboardingComplete(value: Boolean) {
            dataStore.edit { it[KEY_INTRO_ONBOARDING_DS] = value }
            prefs.edit().putBoolean(KEY_INTRO_ONBOARDING_LEGACY, value).apply()
        }

        private companion object {
            private const val PREFS_NAME = "turbodrive_app"
            private const val KEY_INTRO_ONBOARDING_LEGACY = "intro_onboarding_done"
            private val KEY_INTRO_ONBOARDING_DS = booleanPreferencesKey("intro_onboarding_complete")
        }
    }
