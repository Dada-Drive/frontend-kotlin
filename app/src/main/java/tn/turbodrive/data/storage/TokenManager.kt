// Équivalent Swift : TokenStore.swift (Keychain — ici EncryptedSharedPreferences)
package tn.turbodrive.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import tn.turbodrive.core.constants.Constants
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TokenStorage {
        private val prefs: SharedPreferences =
            run {
                try {
                    val p =
                        EncryptedSharedPreferences.create(
                            context,
                            ENCRYPTED_PREFS_NAME,
                            MasterKey.Builder(context)
                                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                                .build(),
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                        )
                    Log.i("TURBODRIVE_BOOT", "TokenManager: chiffrement OK (EncryptedSharedPreferences)")
                    p
                } catch (e: Exception) {
                    Log.e(
                        "TokenManager",
                        "EncryptedSharedPreferences indisponible (Keystore / ROM), repli clair. Cause: ${e.message}",
                        e,
                    )
                    Log.w("TURBODRIVE_BOOT", "TokenManager: FICHIER CLAIR (fallback) — ${e::class.java.simpleName}")
                    context.getSharedPreferences(
                        "${ENCRYPTED_PREFS_NAME}_plain_fallback",
                        Context.MODE_PRIVATE,
                    )
                }
            }

        override fun saveTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            prefs.edit()
                .putString(Constants.PREFS_AUTH_TOKEN, accessToken)
                .putString(Constants.PREFS_REFRESH_TOKEN, refreshToken)
                .remove(KEY_GUEST_BROWSE)
                .apply()
        }

        override fun saveAccessToken(accessToken: String) {
            prefs.edit().putString(Constants.PREFS_AUTH_TOKEN, accessToken).remove(KEY_GUEST_BROWSE).apply()
        }

        override fun saveRefreshToken(refreshToken: String) {
            prefs.edit().putString(Constants.PREFS_REFRESH_TOKEN, refreshToken).apply()
        }

        override fun getAccessToken(): String? = prefs.getString(Constants.PREFS_AUTH_TOKEN, null)

        override fun getRefreshToken(): String? = prefs.getString(Constants.PREFS_REFRESH_TOKEN, null)

        override fun clearTokens() {
            prefs.edit()
                .remove(Constants.PREFS_AUTH_TOKEN)
                .remove(Constants.PREFS_REFRESH_TOKEN)
                .apply()
        }

        override fun getOrCreateDeviceId(): String {
            val existing = prefs.getString(KEY_DEVICE_ID, null)
            if (!existing.isNullOrBlank()) return existing
            val created = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, created).apply()
            return created
        }

        override fun saveDeviceId(deviceId: String) {
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }

        /** Exploration sans compte : pas de jeton mais accès carte / UI passager. */
        override fun setGuestBrowseEnabled(enabled: Boolean) {
            prefs.edit().putBoolean(KEY_GUEST_BROWSE, enabled).apply()
        }

        override fun isGuestBrowseEnabled(): Boolean = prefs.getBoolean(KEY_GUEST_BROWSE, false)

        private companion object {
            private const val ENCRYPTED_PREFS_NAME = "turbodrive_encrypted_tokens"
            private const val KEY_GUEST_BROWSE = "guest_browse_map_only"
            private const val KEY_DEVICE_ID = "device_id_uuid"
        }
    }
