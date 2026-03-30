package com.dadadrive.data.remote.interceptor

import com.dadadrive.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Ajoute automatiquement le header "Authorization: Bearer <token>" à chaque requête
 * si un token est disponible en local.
 *
 * - Si la requête a déjà un header Authorization (ex: @Header manuel), on ne le remplace pas.
 * - Les endpoints avec optionalProtect (sendOtp, verifyOtp) bénéficieront du token
 *   quand l'utilisateur est déjà connecté (ex: Google → ajout du numéro de téléphone).
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Ne pas remplacer un Authorization déjà défini manuellement
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
