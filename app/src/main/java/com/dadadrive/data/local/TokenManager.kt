// Équivalent Swift : TokenStore.swift (Keychain — ici EncryptedSharedPreferences)
package com.dadadrive.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dadadrive.core.constants.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        ENCRYPTED_PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(Constants.PREFS_AUTH_TOKEN, accessToken)
            .putString(Constants.PREFS_REFRESH_TOKEN, refreshToken)
            .remove(KEY_GUEST_BROWSE)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(Constants.PREFS_AUTH_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(Constants.PREFS_REFRESH_TOKEN, null)

    fun clearTokens() {
        prefs.edit()
            .remove(Constants.PREFS_AUTH_TOKEN)
            .remove(Constants.PREFS_REFRESH_TOKEN)
            .apply()
    }

    /** Exploration sans compte : pas de jeton mais accès carte / UI passager. */
    fun setGuestBrowseEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GUEST_BROWSE, enabled).apply()
    }

    fun isGuestBrowseEnabled(): Boolean =
        prefs.getBoolean(KEY_GUEST_BROWSE, false)

    private companion object {
        private const val ENCRYPTED_PREFS_NAME = "dadadrive_encrypted_tokens"
        private const val KEY_GUEST_BROWSE = "guest_browse_map_only"
    }
}
