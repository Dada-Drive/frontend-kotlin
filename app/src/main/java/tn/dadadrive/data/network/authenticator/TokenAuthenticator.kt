package tn.dadadrive.data.network.authenticator

import tn.dadadrive.core.diagnostics.CrashReporting
import tn.dadadrive.data.network.AuthNavigationEvents
import tn.dadadrive.data.storage.TokenStorage
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val gson: Gson,
    private val authNavigationEvents: AuthNavigationEvents,
    private val crashReporting: CrashReporting,
    @Named("refresh") private val refreshClient: OkHttpClient,
    @Named("apiBaseUrl") private val apiBaseUrl: String,
) : Authenticator {

    private val refreshMutex = Mutex()
    private val consecutiveRefreshFailures = AtomicInteger(0)
    private val refreshCooldownUntilMs = AtomicLong(0L)

    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
        val path = response.request.url.encodedPath

        val hadBearerAuth = !response.request.header("Authorization").isNullOrBlank()
        if (!hadBearerAuth) return@runBlocking null

        if (path.contains("auth/refresh-token")) {
            refreshMutex.withLock {
                if (!tokenStorage.isGuestBrowseEnabled()) {
                    tokenStorage.clearTokens()
                    authNavigationEvents.emitForceLogout()
                }
            }
            return@runBlocking null
        }

        if (response.request.header(RETRY_HEADER) != null) {
            refreshMutex.withLock {
                if (!tokenStorage.isGuestBrowseEnabled()) {
                    tokenStorage.clearTokens()
                    authNavigationEvents.emitForceLogout()
                }
            }
            return@runBlocking null
        }

        refreshMutex.withLock {
            val now = System.currentTimeMillis()
            if (now < refreshCooldownUntilMs.get()) {
                return@withLock null
            }

            val refreshToken = tokenStorage.getRefreshToken() ?: run {
                if (tokenStorage.isGuestBrowseEnabled()) return@withLock null
                tokenStorage.clearTokens()
                authNavigationEvents.emitForceLogout()
                return@withLock null
            }

            val refreshed = runCatching {
                RefreshTokenExecutor.execute(
                    apiBaseUrl = apiBaseUrl,
                    refreshToken = refreshToken,
                    refreshClient = refreshClient,
                    gson = gson,
                    tokenStorage = tokenStorage,
                )
            }.getOrNull()
            if (refreshed != true) {
                val fails = consecutiveRefreshFailures.incrementAndGet()
                if (fails >= 3) {
                    refreshCooldownUntilMs.set(now + COOLDOWN_MS)
                    consecutiveRefreshFailures.set(0)
                }
                crashReporting.recordTokenRefreshFailure(path, fails)
                tokenStorage.clearTokens()
                authNavigationEvents.emitForceLogout()
                return@withLock null
            }

            consecutiveRefreshFailures.set(0)
            refreshCooldownUntilMs.set(0L)

            val newAccess = tokenStorage.getAccessToken() ?: run {
                tokenStorage.clearTokens()
                authNavigationEvents.emitForceLogout()
                return@withLock null
            }

            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .header(RETRY_HEADER, "1")
                .build()
        }
    }

    private companion object {
        private const val RETRY_HEADER = "X-DadaDrive-Auth-Retry"
        private const val COOLDOWN_MS = 5 * 60 * 1000L
    }
}
