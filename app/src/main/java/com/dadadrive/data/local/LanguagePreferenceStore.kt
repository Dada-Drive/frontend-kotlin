// Équivalent Swift : LanguageManager.swift (UserDefaults + application des Locale/LayoutDirection)
package com.dadadrive.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.dadadrive.core.constants.Constants
import com.dadadrive.core.language.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagePreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Langue effective : préférence explicite (en/fr/ar ou clé « system ») si présente,
     * sinon « suit l’appareil » ([AppLanguage.SYSTEM] → locales vides, vraie langue système Android).
     */
    fun readResolvedLanguage(): AppLanguage {
        val raw = prefs.getString(Constants.PREFS_APP_LANGUAGE, null) ?: return AppLanguage.SYSTEM
        return AppLanguage.fromRawValue(raw)
    }

    /** Efface toute préférence et laisse Android appliquer la langue du système (recommandé par défaut). */
    fun clearOverrideAndFollowSystem() {
        prefs.edit().remove(Constants.PREFS_APP_LANGUAGE).apply()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    /**
     * Persiste une langue explicite, ou bascule sur le système si [AppLanguage.SYSTEM].
     */
    fun persistAndApply(language: AppLanguage) {
        if (language == AppLanguage.SYSTEM) {
            clearOverrideAndFollowSystem()
            return
        }
        prefs.edit().putString(Constants.PREFS_APP_LANGUAGE, language.rawValue).apply()
        applyToFramework(language)
    }

    /** Applique sans écrire (démarrage + cas déjà persisté). */
    fun applyToFramework(language: AppLanguage) {
        val list = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLanguage.FRENCH -> LocaleListCompat.forLanguageTags("fr")
            AppLanguage.ARABIC -> LocaleListCompat.forLanguageTags("ar")
        }
        AppCompatDelegate.setApplicationLocales(list)
    }

    fun syncApplicationLocalesWithStoredOrDeviceDefaults() {
        applyToFramework(readResolvedLanguage())
    }
}
