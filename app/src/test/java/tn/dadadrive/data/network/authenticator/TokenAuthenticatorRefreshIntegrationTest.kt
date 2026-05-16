package tn.dadadrive.data.network.authenticator

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.dadadrive.data.network.AuthNavigationEvents
import tn.dadadrive.data.storage.TokenStorage

class TokenAuthenticatorRefreshIntegrationTest {

    @Test
    fun authenticateWhenRefreshFailsEmitsForceLogout() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401))
            val tokenStorage = mockk<TokenStorage>(relaxed = true)
            every { tokenStorage.getRefreshToken() } returns "rt"
            every { tokenStorage.isGuestBrowseEnabled() } returns false

            val events = AuthNavigationEvents()
            val logoutSeen = async {
                withTimeoutOrNull(5_000) {
                    events.forceLogout.first()
                }
            }

            val authenticator = TokenAuthenticator(
                tokenStorage = tokenStorage,
                gson = Gson(),
                authNavigationEvents = events,
                crashReporting = mockk(relaxed = true),
                refreshClient = OkHttpClient(),
                apiBaseUrl = server.url("/api/v1/").toString(),
            )

            val req = Request.Builder()
                .url(server.url("/api/v1/users/me"))
                .header("Authorization", "Bearer old")
                .get()
                .build()
            val resp401 = Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body("".toResponseBody("text/plain".toMediaType()))
                .build()

            authenticator.authenticate(null, resp401)

            assertNotNull(logoutSeen.await())
            verify { tokenStorage.clearTokens() }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun authenticateWhenRefreshSucceedsReturnsRetryRequest() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"accessToken":"na","refreshToken":"nr"}"""),
            )
            val tokenStorage = mockk<TokenStorage>(relaxed = true)
            every { tokenStorage.getRefreshToken() } returns "rt"
            every { tokenStorage.isGuestBrowseEnabled() } returns false
            every { tokenStorage.getAccessToken() } returns "na"

            val authenticator = TokenAuthenticator(
                tokenStorage = tokenStorage,
                gson = Gson(),
                authNavigationEvents = AuthNavigationEvents(),
                crashReporting = mockk(relaxed = true),
                refreshClient = OkHttpClient(),
                apiBaseUrl = server.url("/api/v1/").toString(),
            )

            val req = Request.Builder()
                .url(server.url("/api/v1/users/me"))
                .header("Authorization", "Bearer old")
                .get()
                .build()
            val resp401 = Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body("".toResponseBody("text/plain".toMediaType()))
                .build()

            val retry = authenticator.authenticate(null, resp401)
            assertNotNull(retry)
            assertEquals("Bearer na", retry!!.header("Authorization"))
            verify { tokenStorage.saveTokens("na", "nr") }
        } finally {
            server.shutdown()
        }
    }
}
