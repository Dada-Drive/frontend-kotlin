package tn.dadadrive.data.network.interceptor

import tn.dadadrive.data.network.retry.RetryPolicy
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    private companion object {
        private const val MAX_429_ATTEMPTS = 3
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val initial = chain.request()
        var attempt = 0
        var response = chain.proceed(initial)
        var rateLimitAttempts = 0

        while (true) {
            if (RetryPolicy.shouldRetryRateLimit(response) && rateLimitAttempts < MAX_429_ATTEMPTS) {
                val wait = RetryPolicy.rateLimitWaitMs(response)
                response.close()
                sleepQuiet(wait)
                rateLimitAttempts++
                response = chain.proceed(initial)
                continue
            }

            if (RetryPolicy.shouldRetryServerError(attempt, response, initial)) {
                response.close()
                val wait = RetryPolicy.serverBackoffMs(attempt)
                sleepQuiet(wait)
                attempt++
                response = chain.proceed(initial)
                continue
            }

            return response
        }
    }

    private fun sleepQuiet(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
