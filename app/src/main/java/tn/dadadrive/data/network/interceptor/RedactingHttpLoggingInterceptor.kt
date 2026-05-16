package tn.dadadrive.data.network.interceptor

import tn.dadadrive.core.logging.LogRedaction
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedactingHttpLoggingInterceptor @Inject constructor() : Interceptor {

    private val delegate = HttpLoggingInterceptor { message ->
        HttpLoggingInterceptor.Logger.DEFAULT.log(redactLogLine(message))
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    override fun intercept(chain: Interceptor.Chain): Response = delegate.intercept(chain)

    private fun redactLogLine(line: String): String {
        var out = LogRedaction.redactRequestBodyForLog(line)
        out = Regex("Bearer\\s+\\S+").replace(out, "Bearer [TOKEN]")
        return out
    }
}
