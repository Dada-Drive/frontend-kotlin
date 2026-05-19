package tn.turbodrive.data.network.envelope

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

/**
 * Lazy Gson instance for error-body envelope extraction on HTTP 4xx/5xx (R-1.4 / A6).
 * Default config matches production `NetworkModule.provideGson()` — no field-naming policy,
 * so backend envelopes using snake_case for the error field stay parseable.
 */
private val errorBodyGson: Gson by lazy { Gson() }

/**
 * Déballe une [Response] Retrofit portant un [ApiResponse] vers un [Result] homogène.
 *
 * Comportement :
 * - HTTP 4xx/5xx **avec** envelope `{success: false, error: {code, ...}}` →
 *     `Result.failure(BackendException(apiError = parsedError, httpCode = status))` (R-1.4)
 * - HTTP 4xx/5xx **sans** envelope (body vide, HTML, JSON cassé) →
 *     `Result.failure(BackendException(code="HTTP_$status"))` (fallback historique)
 * - Body null    → `Result.failure(BackendException(code="EMPTY_BODY"))`
 * - `success=true, data != null` → `Result.success(data)`
 * - `success=true, data == null` → `Result.success(Unit as T)` (cas 204 / logout / delete)
 * - `success=false`              → `Result.failure(BackendException(apiError))`
 *
 * Les call sites peuvent ensuite consommer via `.fold(onSuccess = ..., onFailure = ...)`.
 *
 * Notes :
 * - R-1.4 (A6) : sur HTTP 4xx/5xx, on tente de parser l'enveloppe `error.code` du body
 *   pour préserver le mapping `BackendErrorCode` (ex. `OTP_RATE_LIMITED` au lieu du
 *   générique `HTTP_429`). Fallback `HTTP_$code` si parse impossible.
 * - Le repo conserve son `try/catch HttpException` historique comme filet de secours.
 */
fun <T> Response<ApiResponse<T>>.unwrap(): Result<T> {
    if (!isSuccessful) {
        val parsed = parseEnvelopeFromErrorBody(errorBody()?.string())
        val apiError =
            parsed
                ?: ApiError(
                    code = "HTTP_${code()}",
                    message = message().ifBlank { "HTTP ${code()}" },
                )
        return Result.failure(BackendException(apiError = apiError, httpCode = code()))
    }
    val body =
        body()
            ?: return Result.failure(
                BackendException(
                    apiError = ApiError(code = "EMPTY_BODY", message = "Response body is null"),
                    httpCode = code(),
                ),
            )
    return when {
        body.success && body.data != null -> Result.success(body.data)
        body.success && body.data == null -> {
            // Endpoints 204 / logout / delete : succès sans payload. Le caller doit cibler ApiResponse<Unit>.
            @Suppress("UNCHECKED_CAST")
            Result.success(Unit as T)
        }
        else -> {
            val err =
                body.error
                    ?: ApiError(code = "UNKNOWN", message = "Backend returned success=false without error payload")
            Result.failure(BackendException(apiError = err, httpCode = code()))
        }
    }
}

/**
 * Extracts an [ApiError] from a Retrofit `errorBody().string()` when the response is non-2xx (R-1.4 / A6).
 *
 * Returns `null` if the body is missing, not JSON, or doesn't carry a recognizable
 * `error.code` field. Callers then fall back to the legacy `HTTP_$status` synthetic code.
 *
 * Resilient by design : never throws, accommodates backends that return HTML / nginx pages
 * / empty bodies on infrastructure errors (5xx behind a load balancer, gateway timeouts, etc.).
 */
private fun parseEnvelopeFromErrorBody(raw: String?): ApiError? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        val root = errorBodyGson.fromJson(raw, JsonObject::class.java) ?: return@runCatching null
        val errorNode = root.getAsJsonObject("error") ?: return@runCatching null
        val code =
            errorNode.get("code")?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
                ?: return@runCatching null
        val message =
            errorNode.get("message")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        val details =
            errorNode.getAsJsonObject("details")?.entrySet()?.associate { (k, v) ->
                k to if (v.isJsonNull) null else v
            }
        ApiError(code = code, message = message, details = details)
    }.getOrNull()
}
