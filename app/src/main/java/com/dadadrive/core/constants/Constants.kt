package com.dadadrive.core.constants

// ─────────────────────────────────────────────────────────
// APP-WIDE CONSTANTS
// ─────────────────────────────────────────────────────────

object Constants {

    // ── API ───────────────────────────────────────────────
    const val BASE_URL = "https://api.dadadrive.com/"
    const val API_VERSION = "v1"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L

    // ── Authentication ────────────────────────────────────
    const val PREFS_AUTH_TOKEN = "auth_token"
    const val PREFS_REFRESH_TOKEN = "refresh_token"
    const val PREFS_USER_ID = "user_id"
    const val PHONE_CODE_LENGTH = 6

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

    // ── Ride ─────────────────────────────────────────────
    const val DEFAULT_SEARCH_RADIUS_KM = 5
    const val MAX_PASSENGERS = 4

    // ── Date/Time ─────────────────────────────────────────
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"
}
