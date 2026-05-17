package tn.turbodrive.data.network.retry

import okhttp3.Request
import okhttp3.Response
import kotlin.random.Random

internal object RetryPolicy {
    private const val MAX_SERVER_ERROR_ATTEMPTS = 3
    private val SERVER_BACKOFF_MS = longArrayOf(1_000L, 2_000L, 4_000L)

    fun shouldRetryServerError(
        attemptIndex: Int,
        response: Response,
        request: Request,
    ): Boolean {
        if (attemptIndex >= MAX_SERVER_ERROR_ATTEMPTS) return false
        val code = response.code
        if (code != 500 && code != 502 && code != 503) return false
        if (request.method == "POST" && !hasIdempotencyKey(request)) return false
        return true
    }

    fun shouldRetryRateLimit(response: Response): Boolean = response.code == 429

    fun serverBackoffMs(attemptIndex: Int): Long {
        val base = SERVER_BACKOFF_MS.getOrElse(attemptIndex) { SERVER_BACKOFF_MS.last() }
        val jitter = (base * 0.25).toLong()
        return (base + Random.nextLong(-jitter, jitter + 1)).coerceAtLeast(0L)
    }

    fun rateLimitWaitMs(response: Response): Long {
        val header = response.header("Retry-After")?.trim().orEmpty()
        val seconds = header.toLongOrNull() ?: return exponential429WithoutHeaderMs()
        return seconds.coerceAtLeast(1L) * 1000L
    }

    private fun exponential429WithoutHeaderMs(): Long {
        val base = 1_000L
        val cap = 30_000L
        val jitter = (base * 0.25).toLong()
        val v = (base + Random.nextLong(-jitter, jitter + 1)).coerceIn(0L, cap)
        return v
    }

    private fun hasIdempotencyKey(request: Request): Boolean {
        val a = request.header("idempotency-key")
        val b = request.header("Idempotency-Key")
        val c = request.header("X-Idempotency-Key")
        return sequenceOf(a, b, c).any { !it.isNullOrBlank() }
    }
}
