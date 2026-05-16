package tn.dadadrive.data.network.authenticator

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tn.dadadrive.data.network.model.RefreshTokenResponse
import tn.dadadrive.data.storage.TokenStorage

internal object RefreshTokenExecutor {

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    fun execute(
        apiBaseUrl: String,
        refreshToken: String,
        refreshClient: OkHttpClient,
        gson: Gson,
        tokenStorage: TokenStorage,
    ): Boolean {
        val base = apiBaseUrl.trimEnd('/') + "/"
        val url = base + "auth/refresh-token"
        val json = gson.toJson(mapOf("refreshToken" to refreshToken))
        val body = json.toRequestBody(jsonMedia)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()
        refreshClient.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val str = resp.body?.string() ?: return false
            val dto = gson.fromJson(str, RefreshTokenResponse::class.java) ?: return false
            tokenStorage.saveTokens(dto.accessToken, dto.refreshToken)
            return true
        }
    }
}
