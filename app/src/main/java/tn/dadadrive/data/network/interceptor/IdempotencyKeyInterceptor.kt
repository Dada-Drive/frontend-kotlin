package tn.dadadrive.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import tn.dadadrive.data.network.IdempotencyKeyGenerator
import tn.dadadrive.data.network.annotation.Idempotent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injects an `idempotency-key` header (UUID v4) on every Retrofit call whose method
 * carries the [Idempotent] annotation (R-1.3).
 *
 * Must be wired **before** `RetryInterceptor` in the OkHttp chain so that the key is
 * already part of `initial = chain.request()` when the retry loop replays it -- this
 * guarantees the same key is sent across rate-limit and server-error retries, letting
 * the backend deduplicate properly.
 *
 * Behaviour matrix:
 * | Pre-existing header | Annotation present | Action |
 * |---|---|---|
 * | yes | any | preserve header (retry replay path) |
 * | no | yes | inject fresh UUID |
 * | no | no | passthrough (legacy / non-Retrofit) |
 *
 * Header name lowercase `idempotency-key` aligns with the legacy
 * [tn.dadadrive.data.network.ApiClient] OkHttp-direct path (HTTP headers are
 * case-insensitive on the wire, but consistency helps tail-grepping logs).
 */
@Singleton
class IdempotencyKeyInterceptor
    @Inject
    constructor(
        private val keyGenerator: IdempotencyKeyGenerator,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (request.header(HEADER_NAME) != null) {
                // Header already present: either the caller set it explicitly or a previous pass
                // through this interceptor stamped it. Either way, preserve to keep the key stable
                // across RetryInterceptor replays.
                return chain.proceed(request)
            }
            val invocation = request.tag(Invocation::class.java) ?: return chain.proceed(request)
            val annotated = invocation.method().isAnnotationPresent(Idempotent::class.java)
            if (!annotated) return chain.proceed(request)

            val keyed =
                request.newBuilder()
                    .header(HEADER_NAME, keyGenerator.newKey())
                    .build()
            return chain.proceed(keyed)
        }

        private companion object {
            private const val HEADER_NAME = "idempotency-key"
        }
    }
