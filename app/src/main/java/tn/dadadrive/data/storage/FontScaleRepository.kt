package tn.dadadrive.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tn.dadadrive.core.theme.AppFontScalePreference

private val KEY_APP_FONT_SCALE = stringPreferencesKey("app_font_scale")

@Singleton
class FontScaleRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val preference: Flow<AppFontScalePreference> = dataStore.data.map { prefs ->
        AppFontScalePreference.fromStored(prefs[KEY_APP_FONT_SCALE])
    }

    val scaleFactor: Flow<Float> = preference.map { it.scaleFactor }

    suspend fun setPreference(value: AppFontScalePreference) {
        dataStore.edit { it[KEY_APP_FONT_SCALE] = value.storageValue }
    }
}
