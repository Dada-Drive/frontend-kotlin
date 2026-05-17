package tn.turbodrive.data.network.envelope

import retrofit2.Response

/**
 * Déballe une [Response] Retrofit portant un [ApiResponse] vers un [Result] homogène.
 *
 * Comportement :
 * - HTTP 4xx/5xx → `Result.failure(BackendException(code="HTTP_$status"))`
 * - Body null    → `Result.failure(BackendException(code="EMPTY_BODY"))`
 * - `success=true, data != null` → `Result.success(data)`
 * - `success=true, data == null` → `Result.success(Unit as T)` (cas 204 / logout / delete)
 * - `success=false`              → `Result.failure(BackendException(apiError))`
 *
 * Les call sites peuvent ensuite consommer via `.fold(onSuccess = ..., onFailure = ...)`.
 *
 * Notes :
 * - En R-1.1, `unwrap()` est strict : tout drift de format remontera en `EMPTY_BODY` ou en
 *   `success=false` (selon le contenu). Le flag `BuildConfig.STRICT_ENVELOPE` est posé pour
 *   préparer un fallback compat en R-1.x ultérieur, mais n'est pas encore lu ici.
 * - Le repo conserve son `try/catch HttpException` historique comme filet de secours.
 */
fun <T> Response<ApiResponse<T>>.unwrap(): Result<T> {
    if (!isSuccessful) {
        val msg = message().ifBlank { "HTTP ${code()}" }
        return Result.failure(
            BackendException(
                apiError = ApiError(code = "HTTP_${code()}", message = msg),
                httpCode = code(),
            ),
        )
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
