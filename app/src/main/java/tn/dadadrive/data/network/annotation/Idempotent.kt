package tn.dadadrive.data.network.annotation

/**
 * Marker annotation for Retrofit endpoints that need an automatic
 * `idempotency-key` header (UUID v4) injected on every call (R-1.3).
 *
 * Detected at runtime by [tn.dadadrive.data.network.interceptor.IdempotencyKeyInterceptor]
 * via `request.tag(retrofit2.Invocation::class.java)`. The same key is
 * preserved across internal retries (rate-limit / server-error) because
 * `RetryInterceptor` replays the captured `initial` request as-is.
 *
 * Endpoints to annotate are those where a duplicated request would cause
 * user-visible damage: double ride creation, double driver acceptance,
 * double payment confirmation, double rating, etc.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Idempotent
