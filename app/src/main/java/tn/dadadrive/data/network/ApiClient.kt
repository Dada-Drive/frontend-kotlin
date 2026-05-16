package tn.dadadrive.data.network

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tn.dadadrive.core.constants.Constants
import tn.dadadrive.data.network.model.PaginatedResponse
import tn.dadadrive.data.network.model.PaginationMeta
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {

    suspend inline fun <reified T : Any> request(endpoint: ApiEndpoint): T {
        val type = object : TypeToken<T>() {}.type
        return request(endpoint, type)
    }

    suspend fun <T : Any> request(endpoint: ApiEndpoint, type: Type): T = withContext(Dispatchers.IO) {
        val url = buildUrl(endpoint)
        val builder = Request.Builder().url(url)
        endpoint.extraHeaders.forEach { (k, v) -> builder.header(k, v) }
        when (endpoint) {
            is ApiEndpoint.Get -> builder.get()
            is ApiEndpoint.PostJson -> {
                val json = gson.toJson(endpoint.body)
                val body = json.toRequestBody(JSON_MEDIA)
                builder.post(body)
                endpoint.idempotencyKey?.let { builder.header("idempotency-key", it) }
            }
            is ApiEndpoint.Delete -> builder.delete()
        }
        okHttpClient.newCall(builder.build()).execute().use { resp ->
            val bodyStr = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw ApiEnvelopeException(resp.code, bodyStr, resp.header("Retry-After"))
            }
            val root = runCatching { JsonParser.parseString(bodyStr) }.getOrNull()?.asJsonObject
                ?: throw ApiEnvelopeException(resp.code, bodyStr, null)
            val success = root.get("success")?.takeUnless { it.isJsonNull }?.asBoolean
            if (success == false) {
                val err = root.getAsJsonObject("error")
                val code = err?.get("code")?.asString
                throw ApiBusinessException(resp.code, code, bodyStr)
            }
            val payload = when (success) {
                true -> root.get("data") ?: throw ApiEnvelopeException(resp.code, bodyStr, null)
                else -> root
            }
            return@withContext gson.fromJson(payload, type)
                ?: throw ApiEnvelopeException(resp.code, bodyStr, null)
        }
    }

    suspend inline fun <reified T : Any> requestPaginated(endpoint: ApiEndpoint): PaginatedResponse<T> =
        requestPaginated(endpoint, T::class.java)

    suspend fun <T : Any> requestPaginated(endpoint: ApiEndpoint, elementClass: Class<T>): PaginatedResponse<T> {
        val itemType = TypeToken.getParameterized(List::class.java, elementClass).type
        return requestPaginatedInternal(endpoint, itemType)
    }

    private suspend fun <T : Any> requestPaginatedInternal(
        endpoint: ApiEndpoint,
        itemType: Type,
    ): PaginatedResponse<T> = withContext(Dispatchers.IO) {
        val url = buildUrl(endpoint)
        val builder = Request.Builder().url(url)
        endpoint.extraHeaders.forEach { (k, v) -> builder.header(k, v) }
        builder.get()
        okHttpClient.newCall(builder.build()).execute().use { resp ->
            val bodyStr = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw ApiEnvelopeException(resp.code, bodyStr, resp.header("Retry-After"))
            }
            val root = runCatching { JsonParser.parseString(bodyStr) }.getOrNull()?.asJsonObject
                ?: throw ApiEnvelopeException(resp.code, bodyStr, null)
            val success = root.get("success")?.takeUnless { it.isJsonNull }?.asBoolean
            if (success == false) {
                val err = root.getAsJsonObject("error")
                val code = err?.get("code")?.asString
                throw ApiBusinessException(resp.code, code, bodyStr)
            }
            val dataEl = when (success) {
                true -> root.get("data") ?: throw ApiEnvelopeException(resp.code, bodyStr, null)
                else -> root
            }
            @Suppress("UNCHECKED_CAST")
            val list: List<T> = gson.fromJson(dataEl, itemType) as? List<T> ?: emptyList()
            val metaObj = root.getAsJsonObject("meta")
            val meta = if (metaObj != null) {
                gson.fromJson(metaObj, PaginationMeta::class.java)
            } else {
                PaginationMeta(total = list.size, page = 1, limit = list.size, pages = 1)
            } ?: PaginationMeta(total = list.size, page = 1, limit = list.size, pages = 1)
            return@withContext PaginatedResponse(items = list, meta = meta)
        }
    }

    fun buildUrl(endpoint: ApiEndpoint): String {
        val base = Constants.BASE_URL.trimEnd('/')
        val path = endpoint.path.trimStart('/')
        val httpUrl = "$base/$path".toHttpUrlOrNull() ?: error("Invalid URL")
        val builder = httpUrl.newBuilder()
        endpoint.queryParams.forEach { (k, v) -> builder.addQueryParameter(k, v) }
        return builder.build().toString()
    }

    private companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

sealed class ApiEndpoint {
    abstract val path: String
    abstract val queryParams: Map<String, String>
    abstract val extraHeaders: Map<String, String>

    data class Get(
        override val path: String,
        override val queryParams: Map<String, String> = emptyMap(),
        override val extraHeaders: Map<String, String> = emptyMap(),
    ) : ApiEndpoint()

    data class PostJson(
        override val path: String,
        val body: Any?,
        val idempotencyKey: String? = null,
        override val queryParams: Map<String, String> = emptyMap(),
        override val extraHeaders: Map<String, String> = emptyMap(),
    ) : ApiEndpoint()

    data class Delete(
        override val path: String,
        override val queryParams: Map<String, String> = emptyMap(),
        override val extraHeaders: Map<String, String> = emptyMap(),
    ) : ApiEndpoint()
}

class ApiEnvelopeException(
    val httpCode: Int,
    val rawBody: String,
    val retryAfterHeader: String?,
) : Exception("HTTP $httpCode")

class ApiBusinessException(
    val httpCode: Int,
    val errorCode: String?,
    val rawBody: String,
) : Exception(errorCode ?: "business")
