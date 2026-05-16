package tn.dadadrive.core.diagnostics

import android.content.Context
import android.os.Build
import com.dadadrive.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import tn.dadadrive.core.language.AppLanguage
import tn.dadadrive.core.logging.AppLogger
import tn.dadadrive.data.storage.LanguagePreferenceStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporting @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languagePreferenceStore: LanguagePreferenceStore,
) {

    fun initialize() {
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.setCrashlyticsCollectionEnabled(true)
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("os_version", Build.VERSION.SDK_INT.toString())
            crashlytics.setCustomKey("language", languageTag(languagePreferenceStore.readResolvedLanguage()))
            crashlytics.setCustomKey("active_ride", "false")
            FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
        }.onFailure { e ->
            AppLogger.e("CrashReporting init failed: ${e.message}", e)
        }
    }

    fun setUserRole(role: String?) {
        if (role.isNullOrBlank()) return
        FirebaseCrashlytics.getInstance().setCustomKey("user_role", role)
    }

    fun setUserIdHash(hash: String?) {
        if (hash.isNullOrBlank()) return
        FirebaseCrashlytics.getInstance().setCustomKey("user_id_hash", hash)
    }

    fun setActiveRide(active: Boolean) {
        FirebaseCrashlytics.getInstance().setCustomKey("active_ride", if (active) "true" else "false")
    }

    fun recordNonFatal(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun recordTokenRefreshFailure(path: String, consecutiveFailures: Int) {
        FirebaseCrashlytics.getInstance().log("token_refresh_fail path=$path failures=$consecutiveFailures")
        FirebaseCrashlytics.getInstance().recordException(
            TokenRefreshNonFatalException("refresh failed path=$path failures=$consecutiveFailures")
        )
    }

    fun recordSocketConnectionFailure(message: String) {
        FirebaseCrashlytics.getInstance().log("socket_connect_fail $message")
        FirebaseCrashlytics.getInstance().recordException(SocketConnectNonFatalException(message))
    }

    private fun languageTag(language: AppLanguage): String = when (language) {
        AppLanguage.SYSTEM -> AppLanguage.ENGLISH.rawValue
        else -> language.rawValue
    }

    class TokenRefreshNonFatalException(message: String) : RuntimeException(message)
    class SocketConnectNonFatalException(message: String) : RuntimeException(message)
}
