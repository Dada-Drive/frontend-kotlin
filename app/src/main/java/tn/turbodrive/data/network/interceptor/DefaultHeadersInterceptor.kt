package tn.turbodrive.data.network.interceptor

import com.turbodrive.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import tn.turbodrive.core.language.AppLanguage
import tn.turbodrive.data.storage.LanguagePreferenceStore
import tn.turbodrive.data.storage.TokenStorage
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHeadersInterceptor
    @Inject
    constructor(
        private val tokenStorage: TokenStorage,
        private val languagePreferenceStore: LanguagePreferenceStore,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val langTag = acceptLanguageTag(languagePreferenceStore.readResolvedLanguage())
            val deviceId = tokenStorage.getOrCreateDeviceId()
            val request =
                chain.request().newBuilder()
                    .header("Accept-Language", langTag)
                    .header("X-Device-Id", deviceId)
                    .header("X-App-Version", BuildConfig.VERSION_NAME)
                    .header("X-Platform", "android")
                    .build()
            return chain.proceed(request)
        }

        private fun acceptLanguageTag(language: AppLanguage): String {
            val raw =
                when (language) {
                    AppLanguage.SYSTEM -> Locale.getDefault().language.lowercase(Locale.ROOT)
                    else -> language.rawValue
                }
            return when (raw) {
                "ar", "fr", "en" -> raw
                else -> "en"
            }
        }
    }
