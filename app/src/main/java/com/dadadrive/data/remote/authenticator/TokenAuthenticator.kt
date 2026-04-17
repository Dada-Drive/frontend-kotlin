// Équivalent Swift : APIClient.swift — branche 401 → POST /auth/refresh-token → retry (performRequest)
package com.dadadrive.data.remote.authenticator

import com.dadadrive.core.constants.Constants
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.remote.AuthNavigationEvents
import com.dadadrive.data.remote.model.RefreshTokenResponse
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val gson: Gson,
    private val authNavigationEvents: AuthNavigationEvents,
    @Named("refresh") private val refreshClient: OkHttpClient
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath

        // N'essaye pas de refresh pour les requêtes sans Bearer initial.
        // Evite de déclencher une "expiration de session" sur des endpoints publics (login/register...).
        val hadBearerAuth = !response.request.header("Authorization").isNullOrBlank()
        if (!hadBearerAuth) return null

        if (path.contains("auth/refresh-token")) {
            synchronized(lock) {
                if (!tokenManager.isGuestBrowseEnabled()) {
                    tokenManager.clearTokens()
                    authNavigationEvents.emitForceLogout()
                }
            }
            return null
        }

        if (response.request.header(RETRY_HEADER) != null) {
            synchronized(lock) {
                if (!tokenManager.isGuestBrowseEnabled()) {
                    tokenManager.clearTokens()
                    authNavigationEvents.emitForceLogout()
                }
            }
            return null
        }

        synchronized(lock) {
            val refreshToken = tokenManager.getRefreshToken() ?: run {
                // Invité : pas de jeton — laisser la requête échouer sans renvoyer au login
                if (tokenManager.isGuestBrowseEnabled()) return null
                tokenManager.clearTokens()
                authNavigationEvents.emitForceLogout()
                return null
            }

            val refreshed = runCatching { executeRefresh(refreshToken) }.getOrNull()
            if (refreshed != true) {
                tokenManager.clearTokens()
                authNavigationEvents.emitForceLogout()
                return null
            }

            val newAccess = tokenManager.getAccessToken() ?: run {
                tokenManager.clearTokens()
                authNavigationEvents.emitForceLogout()
                return null
            }

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .header(RETRY_HEADER, "1")
                .build()
        }
    }

    private fun executeRefresh(refreshToken: String): Boolean {
        val base = Constants.BASE_URL.trimEnd('/') + "/"
        val url = base + "auth/refresh-token"
        val json = gson.toJson(mapOf("refreshToken" to refreshToken))
        val body = json.toRequestBody(JSON_MEDIA)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        refreshClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val str = resp.body?.string() ?: return false
            val dto = gson.fromJson(str, RefreshTokenResponse::class.java) ?: return false
            tokenManager.saveTokens(dto.accessToken, dto.refreshToken)
            return true
        }
    }

    private companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private const val RETRY_HEADER = "X-DadaDrive-Auth-Retry"
    }
}
