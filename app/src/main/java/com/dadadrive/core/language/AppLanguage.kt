// Équivalent Swift : Core/LanguageManager.swift — enum AppLanguage
package com.dadadrive.core.language

import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

enum class AppLanguage(val rawValue: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    FRENCH("fr"),
    ARABIC("ar");

    val displayName: String
        get() = when (this) {
            SYSTEM -> "System Default"
            ENGLISH -> "English"
            FRENCH -> "Français"
            ARABIC -> "العربية"
        }

    val flag: String
        get() = when (this) {
            SYSTEM -> "🌐"
            ENGLISH -> "🇬🇧"
            FRENCH -> "🇫🇷"
            ARABIC -> "🇸🇦"
        }

    /** Équiv. Swift : `locale` sur AppLanguage (Locale.current pour system). */
    fun resolvedLocale(): Locale = when (this) {
        SYSTEM -> Locale.getDefault()
        ENGLISH -> Locale.ENGLISH
        FRENCH -> Locale.FRENCH
        ARABIC -> Locale.forLanguageTag("ar")
    }

    /** Équiv. Swift : layoutDirection (arabe → RTL). */
    fun layoutDirection(): LayoutDirection =
        if (this == ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    companion object {
        fun fromRawValue(raw: String): AppLanguage =
            entries.find { it.rawValue == raw } ?: SYSTEM

        /**
         * Langues listées dans le menu profil. [SYSTEM] n’y figure pas : c’est le mode par défaut
         * (suivre la langue de l’appareil).
         */
        val menuChoices: List<AppLanguage> = listOf(ENGLISH, FRENCH, ARABIC)
    }
}
