package tn.dadadrive.di

import com.dadadrive.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tn.dadadrive.core.constants.Constants
import tn.dadadrive.core.network.parseCertificatePins
import tn.dadadrive.data.network.api.AuthApiService
import tn.dadadrive.data.network.api.DriverApiService
import tn.dadadrive.data.network.api.NotificationApiService
import tn.dadadrive.data.network.api.RidesApiService
import tn.dadadrive.data.network.api.WalletApiService
import tn.dadadrive.data.network.authenticator.TokenAuthenticator
import tn.dadadrive.data.network.interceptor.AuthInterceptor
import tn.dadadrive.data.network.interceptor.DefaultHeadersInterceptor
import tn.dadadrive.data.network.interceptor.RedactingHttpLoggingInterceptor
import tn.dadadrive.data.network.interceptor.RetryInterceptor
import tn.dadadrive.data.storage.TokenStorage
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
        defaultHeadersInterceptor: DefaultHeadersInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        redactingHttpLoggingInterceptor: RedactingHttpLoggingInterceptor,
    ): OkHttpClient {
        val builder =
            OkHttpClient.Builder()
                .authenticator(tokenAuthenticator)
                .addInterceptor(retryInterceptor)
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
        if (pins.isEmpty()) return null
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
