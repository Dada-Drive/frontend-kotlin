// Équivalent Swift : APIClient.swift — en-tête Authorization Bearer sur les requêtes
package tn.turbodrive.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import tn.turbodrive.data.storage.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ajoute "Authorization: Bearer <accessToken>" si un token est présent (comme TokenStore + APIClient Swift).
 * Ne remplace pas un header Authorization déjà défini.
 */
@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val tokenStorage: TokenStorage,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            if (original.header("Authorization") != null) {
                return chain.proceed(original)
            }

            val token = tokenStorage.getAccessToken()

            val request =
                if (!token.isNullOrBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }

            return chain.proceed(request)
        }
    }
