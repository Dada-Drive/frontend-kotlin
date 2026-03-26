package com.dadadrive.data.local

import android.content.Context
import android.content.SharedPreferences
import com.dadadrive.core.constants.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(Constants.PREFS_AUTH_TOKEN, accessToken)
            .putString(Constants.PREFS_REFRESH_TOKEN, refreshToken)
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
}
