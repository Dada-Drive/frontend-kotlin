package tn.dadadrive.core.network

import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Interceptor
import okhttp3.Response
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * OkHttp interceptor that reports [SSLPeerUnverifiedException] -- the exception thrown
 * when certificate pinning rejects a peer certificate -- to Firebase Crashlytics, then
 * **re-throws** so the request still fails loudly.
 *
 * Wrap the Crashlytics call in `runCatching` so that an uninitialized Firebase (e.g.
 * in instrumentation contexts) cannot turn an SSL failure into a secondary crash.
 *
 * Mounted as an application-level interceptor in NetworkModule, ideally before the
 * auth/retry interceptors so refresh-related SSL failures are also captured.
 */
class CertificatePinningReporter : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: SSLPeerUnverifiedException) {
            runCatching {
                val crashlytics = FirebaseCrashlytics.getInstance()
                crashlytics.log("[cert-pinning] host=${request.url.host}")
                crashlytics.recordException(e)
            }
            throw e
        }
    }
}
