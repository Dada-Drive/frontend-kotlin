package tn.turbodrive.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.turbodrive.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import tn.turbodrive.core.constants.Constants
import tn.turbodrive.core.network.CertificatePinningReporter
import tn.turbodrive.core.network.parseCertificatePins
import tn.turbodrive.data.network.api.AuthApiService
import tn.turbodrive.data.network.api.DriverApiService
import tn.turbodrive.data.network.api.NotificationApiService
import tn.turbodrive.data.network.api.RidesApiService
import tn.turbodrive.data.network.api.WalletApiService
import tn.turbodrive.data.network.authenticator.TokenAuthenticator
import tn.turbodrive.data.network.interceptor.AuthInterceptor
import tn.turbodrive.data.network.interceptor.DefaultHeadersInterceptor
import tn.turbodrive.data.network.interceptor.IdempotencyKeyInterceptor
import tn.turbodrive.data.network.interceptor.RedactingHttpLoggingInterceptor
import tn.turbodrive.data.network.interceptor.RetryInterceptor
import tn.turbodrive.data.storage.TokenStorage
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @Named("apiBaseUrl")
    fun provideApiBaseUrl(): String = Constants.BASE_URL

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStorage: TokenStorage): AuthInterceptor = AuthInterceptor(tokenStorage)

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshOkHttpClient(
        defaultHeadersInterceptor: DefaultHeadersInterceptor,
        redactingHttpLoggingInterceptor: RedactingHttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(CertificatePinningReporter())
            .addInterceptor(defaultHeadersInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(redactingHttpLoggingInterceptor)
                }
            }
            .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        retryInterceptor: RetryInterceptor,
        idempotencyKeyInterceptor: IdempotencyKeyInterceptor,
        defaultHeadersInterceptor: DefaultHeadersInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        redactingHttpLoggingInterceptor: RedactingHttpLoggingInterceptor,
    ): OkHttpClient {
        val builder =
            OkHttpClient.Builder()
                .authenticator(tokenAuthenticator)
                // IdempotencyKey must run BEFORE RetryInterceptor : the latter captures
                // `initial = chain.request()` once and replays it as-is, so the stamped
                // header travels unchanged across rate-limit and server-error retries.
                .addInterceptor(idempotencyKeyInterceptor)
                .addInterceptor(retryInterceptor)
                .addInterceptor(CertificatePinningReporter())
                .addInterceptor(defaultHeadersInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(redactingHttpLoggingInterceptor)
        }
        certificatePinnerOrNull()?.let { builder.certificatePinner(it) }
        return builder.build()
    }

    private fun certificatePinnerOrNull(): CertificatePinner? {
        if (!BuildConfig.ENABLE_CERT_PINNING) return null
        val pins = parseCertificatePins(BuildConfig.CERTIFICATE_PINS)
        if (pins.isEmpty()) {
            // Misconfiguration : pinning activé par variant mais aucun pin injecté via local.properties / CI secret.
            // Silencieusement off = faille majeure en staging/release. Log loud pour détecter en review Logcat.
            Timber.w(
                "ENABLE_CERT_PINNING=true but CERTIFICATE_PINS is empty -- pinning DISABLED for this build!",
            )
            return null
        }
        val pinBuilder = CertificatePinner.Builder()
        for ((host, pin) in pins) {
            pinBuilder.add(host, pin)
        }
        return pinBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideDriverApiService(retrofit: Retrofit): DriverApiService = retrofit.create(DriverApiService::class.java)

    @Provides
    @Singleton
    fun provideRidesApiService(retrofit: Retrofit): RidesApiService = retrofit.create(RidesApiService::class.java)

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService = retrofit.create(NotificationApiService::class.java)

    @Provides
    @Singleton
    fun provideWalletApiService(retrofit: Retrofit): WalletApiService = retrofit.create(WalletApiService::class.java)
}
