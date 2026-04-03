// Équivalent Swift : APIClient.swift — en-tête Authorization Bearer sur les requêtes
package com.dadadrive.data.remote.interceptor

import com.dadadrive.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Ajoute "Authorization: Bearer <accessToken>" si un token est présent (comme TokenStore + APIClient Swift).
 * Ne remplace pas un header Authorization déjà défini.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.header("Authorization") != null) {
            return chain.proceed(original)
        }

        val token = tokenManager.getAccessToken()

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
