// Équivalent Swift : Core/Constants.swift (section API — mêmes hôtes debug/release côté produit)
package com.dadadrive.core.constants

import com.dadadrive.BuildConfig

object Constants {

    // ── API ───────────────────────────────────────────────
    /**
     * Toujours un préfixe `…/api/` (slash final) pour Retrofit : les chemins des services sont
     * `auth/…`, `users/…`, `driver/…`, `rides/…` (équiv. Swift : base + `/auth/google` avec base `…/api`).
     *
     * Si [BuildConfig.BASE_URL] pointe seulement vers l’hôte (ex. `http://IP:5000/`), on ajoute
     * `/api` pour éviter les 404 sur `/auth/google` au lieu de `/api/auth/google`.
     */
    val BASE_URL: String = normalizeRetrofitBaseUrl(BuildConfig.BASE_URL)
    const val API_VERSION = "v1"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L

    /** Équivalent Swift : AppConstants.Trip.driverRefreshInterval (5 s). */
    const val DRIVER_POLL_INTERVAL_MS = 5_000L

    // ── Authentication ────────────────────────────────────
    const val PREFS_AUTH_TOKEN = "auth_token"
    const val PREFS_REFRESH_TOKEN = "refresh_token"
    const val PREFS_USER_ID = "user_id"
    const val PHONE_CODE_LENGTH = 6
    val GOOGLE_WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID

    // ── Navigation routes ─────────────────────────────────
    object Routes {
        const val SPLASH = "splash"
        const val ONBOARDING = "onboarding"
        const val WELCOME = "welcome"
        const val PHONE = "phone"
        const val HOME = "home"
        const val PROFILE = "profile"
    }

    // ── SharedPreferences ─────────────────────────────────
    const val PREFS_NAME = "dadadrive_prefs"

    /** Équivalent Swift : LanguageManager key `app_language` (UserDefaults). */
    const val PREFS_APP_LANGUAGE = "app_language"

    // ── Ride ─────────────────────────────────────────────
    const val DEFAULT_SEARCH_RADIUS_KM = 5
    const val MAX_PASSENGERS = 4

    // ── Date/Time ─────────────────────────────────────────
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"

    private fun normalizeRetrofitBaseUrl(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        if (trimmed.isEmpty()) return raw.trim().let { if (it.endsWith('/')) it else "$it/" }
        val withApi = if (trimmed.endsWith("/api")) trimmed else "$trimmed/api"
        return "$withApi/"
    }
}