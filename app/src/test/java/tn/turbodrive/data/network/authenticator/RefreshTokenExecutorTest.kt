package tn.turbodrive.data.network.authenticator

import com.google.gson.Gson
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.turbodrive.data.storage.TokenStorage

class RefreshTokenExecutorTest {
    @Test
    fun refreshSuccessSavesTokens() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""{"accessToken":"newAccess","refreshToken":"newRefresh"}"""),
            )
            val gson = Gson()
            val tokenStorage = mockk<TokenStorage>(relaxed = true)
            val ok =
                RefreshTokenExecutor.execute(
                    apiBaseUrl = server.url("/api/v1/").toString(),
                    refreshToken = "oldRefresh",
                    refreshClient = OkHttpClient(),
                    gson = gson,
                    tokenStorage = tokenStorage,
                )
            assertTrue(ok)
            verify { tokenStorage.saveTokens("newAccess", "newRefresh") }
            assertEquals("/api/v1/auth/refresh-token", server.takeRequest().path)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun refreshHttpErrorDoesNotSaveTokens() {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401))
            val gson = Gson()
            val tokenStorage = mockk<TokenStorage>(relaxed = true)
            val ok =
                RefreshTokenExecutor.execute(
                    apiBaseUrl = server.url("/api/v1/").toString(),
                    refreshToken = "rt",
                    refreshClient = OkHttpClient(),
                    gson = gson,
                    tokenStorage = tokenStorage,
                )
            assertFalse(ok)
            verify(exactly = 0) { tokenStorage.saveTokens(any(), any()) }
        } finally {
            server.shutdown()
        }
    }
}
