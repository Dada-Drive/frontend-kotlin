// Équivalent Swift : Core/Constants.swift (section API — mêmes hôtes debug/release côté produit)
package tn.turbodrive.core.constants

import com.turbodrive.BuildConfig

object Constants {
    // ── API ───────────────────────────────────────────────

    /**
     * Base Retrofit alignée sur [backend-integration.md] §1.1 : préfixe `…/api/v1/` pour `dada-api`.
     * Chemins relatifs des services : `auth/…`, `users/…`, `driver/…`, `rides/…`.
     *
     * Si l’URL se termine déjà par `/api` (hébergeurs legacy sans version), on ne rajoute pas `/v1`.
     */
    val BASE_URL: String = normalizeRetrofitBaseUrl(BuildConfig.BASE_URL)
    const val API_VERSION = "v1"
    const val CONNECT_TIMEOUT_SECONDS = 10L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 60L

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
    const val PREFS_NAME = "turbodrive_prefs"

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
        if (trimmed.isEmpty()) return "http://10.0.2.2:3000/api/v1/"
        val withPath =
            when {
                trimmed.endsWith("/api/v1") -> trimmed
                trimmed.endsWith("/api") -> trimmed
                else -> "$trimmed/api/v1"
            }
        return "$withPath/"
    }
}
