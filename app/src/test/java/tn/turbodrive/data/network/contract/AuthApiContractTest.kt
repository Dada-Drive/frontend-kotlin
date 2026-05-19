package tn.turbodrive.data.network.contract

import com.google.gson.JsonParser
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tn.turbodrive.data.network.api.AuthApiService
import tn.turbodrive.data.network.envelope.BackendException
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.data.network.model.LogoutRequest
import tn.turbodrive.data.network.model.SendOtpRequest
import tn.turbodrive.data.network.model.VerifyOtpRequest

/**
 * Contract tests for the **MVP-1 Auth flow** — locks the request/response shape
 * the frontend expects from the backend.
 *
 * Each test mirrors a row in `docs/BACKEND_CONTRACT.md §7.1`. If a backend
 * implementation deviates (renamed field, missing envelope, different error
 * code), the matching test breaks here and points to the divergence.
 *
 * MVP-1 endpoints covered:
 * - POST `/auth/send-otp`
 * - POST `/auth/verify-otp` (existing user + new user variants)
 * - GET `/auth/me`
 * - POST `/auth/logout`
 *
 * NOT covered here (other test files):
 * - `/auth/refresh-token` flow → see [tn.turbodrive.data.network.authenticator.RefreshTokenExecutorTest]
 *   and [tn.turbodrive.data.network.authenticator.TokenAuthenticatorRefreshIntegrationTest]
 * - Envelope mechanics → see [tn.turbodrive.data.network.envelope.ApiCallTest]
 */
class AuthApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var api: AuthApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        api = ContractTestSupport.retrofitService(server)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `POST send-otp parses success envelope with expiresIn`() =
        kotlinx.coroutines.runBlocking {
            // Backend contract : { "success": true, "data": { "message": "...", "expiresIn": 300 } }
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": true,
                      "data": { "message": "OTP envoyé via WhatsApp", "expiresIn": 300 },
                      "error": null
                    }
                    """.trimIndent(),
                ),
            )

            val result = api.sendOtp(SendOtpRequest(phone = "+21698123456")).unwrap()

            assertTrue("Envelope must unwrap to success", result.isSuccess)
            val body = result.getOrThrow()
            assertEquals("OTP envoyé via WhatsApp", body.message)
            assertEquals(300, body.expiresIn)

            // Outgoing request shape
            val recorded = server.takeRequest()
            assertEquals("POST", recorded.method)
            assertEquals("/api/v1/auth/send-otp", recorded.path)
            val outgoing = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
            assertEquals("+21698123456", outgoing["phone"].asString)
        }

    @Test
    fun `POST verify-otp parses tokens and user for existing user`() =
        kotlinx.coroutines.runBlocking {
            // Contract : verifyOtp on existing user returns accessToken + refreshToken + user, isNewUser=false
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": true,
                      "data": {
                        "message": "OK",
                        "accessToken": "jwt.access.value",
                        "refreshToken": "jwt.refresh.value",
                        "user": {
                          "id": "u-123",
                          "full_name": "Ali Ben Salah",
                          "email": "ali@example.tn",
                          "phone": "+21698123456",
                          "role": "RIDER",
                          "avatar_url": "https://cdn.example/ali.jpg"
                        },
                        "isNewUser": false
                      },
                      "error": null
                    }
                    """.trimIndent(),
                ),
            )

            val result =
                api.verifyOtp(
                    VerifyOtpRequest(phone = "+21698123456", code = "123456"),
                ).unwrap()

            assertTrue(result.isSuccess)
            val body = result.getOrThrow()
            assertEquals("jwt.access.value", body.accessToken)
            assertEquals("jwt.refresh.value", body.refreshToken)
            assertEquals(false, body.isNewUser)
            val user = body.user
            assertNotNull(user)
            assertEquals("u-123", user!!.id)
            assertEquals("Ali Ben Salah", user.fullName)
            assertEquals("ali@example.tn", user.email)
            assertEquals("+21698123456", user.phone)
            assertEquals("RIDER", user.role)
            assertEquals("https://cdn.example/ali.jpg", user.avatarUrl)
        }

    @Test
    fun `POST verify-otp parses new user variant with isNewUser=true`() =
        kotlinx.coroutines.runBlocking {
            // New user just signed up via OTP : user.full_name may be null until profile completed
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": true,
                      "data": {
                        "accessToken": "at",
                        "refreshToken": "rt",
                        "user": {
                          "id": "u-new",
                          "full_name": null,
                          "email": null,
                          "phone": "+21698999000",
                          "role": "RIDER"
                        },
                        "isNewUser": true
                      },
                      "error": null
                    }
                    """.trimIndent(),
                ),
            )

            val body =
                api.verifyOtp(
                    VerifyOtpRequest(phone = "+21698999000", code = "654321"),
                ).unwrap().getOrThrow()

            assertEquals(true, body.isNewUser)
            assertEquals("at", body.accessToken)
            assertNull("New user full_name may be null", body.user!!.fullName)
            assertNull("New user email may be null", body.user!!.email)
        }

    @Test
    fun `GET auth me parses user payload wrapped in GetMeResponseDto`() =
        kotlinx.coroutines.runBlocking {
            // Contract : GetMeResponseDto wraps a UserDto under "user" key
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": true,
                      "data": {
                        "user": {
                          "id": "u-123",
                          "full_name": "Ali Ben Salah",
                          "email": "ali@example.tn",
                          "phone": "+21698123456",
                          "role": "DRIVER",
                          "avatar_url": null
                        }
                      },
                      "error": null
                    }
                    """.trimIndent(),
                ),
            )

            val body = api.getMe().unwrap().getOrThrow()
            assertEquals("u-123", body.user.id)
            assertEquals("DRIVER", body.user.role)
            assertNull(body.user.avatarUrl)

            val recorded = server.takeRequest()
            assertEquals("GET", recorded.method)
            assertEquals("/api/v1/auth/me", recorded.path)
        }

    @Test
    fun `POST logout returns Unit on success=true with null data`() =
        kotlinx.coroutines.runBlocking {
            // Contract : logout returns ApiResponse<Unit> — data may be null, envelope success=true
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """{ "success": true, "data": null, "error": null }""",
                ),
            )

            val result = api.logout(LogoutRequest(refreshToken = "rt-to-revoke")).unwrap()

            assertTrue(result.isSuccess)
            assertEquals(Unit, result.getOrThrow())

            val recorded = server.takeRequest()
            val outgoing = JsonParser.parseString(recorded.body.readUtf8()).asJsonObject
            assertEquals("rt-to-revoke", outgoing["refreshToken"].asString)
        }

    @Test
    fun `verify-otp with invalid code returns BackendException OTP_INVALID`() =
        kotlinx.coroutines.runBlocking {
            // Contract : even on HTTP 200, success=false + error.code routes the right BackendErrorCode
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": false,
                      "data": null,
                      "error": { "code": "OTP_INVALID", "message": "Code incorrect" }
                    }
                    """.trimIndent(),
                ),
            )

            val result =
                api.verifyOtp(
                    VerifyOtpRequest(phone = "+21698123456", code = "000000"),
                ).unwrap()

            assertTrue(result.isFailure)
            val ex = result.exceptionOrNull() as BackendException
            assertEquals("OTP_INVALID", ex.apiError.code)
            assertEquals("Code incorrect", ex.apiError.message)
            assertEquals(200, ex.httpCode)
        }

    @Test
    fun `send-otp rate-limited returns BackendException with retry_after details`() =
        kotlinx.coroutines.runBlocking {
            // Contract §6 + §8 : RATE_LIMITED on HTTP 429 with details.retry_after
            server.enqueue(
                ContractTestSupport.jsonError(
                    httpCode = 429,
                    body =
                        """
                        {
                          "success": false,
                          "data": null,
                          "error": {
                            "code": "OTP_RATE_LIMITED",
                            "message": "Trop de demandes, réessaie dans 60s",
                            "details": { "retry_after": 60 }
                          }
                        }
                        """.trimIndent(),
                ),
            )

            val result =
                api.sendOtp(SendOtpRequest(phone = "+21698123456")).unwrap()

            assertTrue(result.isFailure)
            val ex = result.exceptionOrNull() as BackendException
            // ⚠️ Frontend behavior: HTTP 4xx ALWAYS maps to "HTTP_${code}" in unwrap(),
            // ignoring any envelope-level error.code that may be present in the body.
            // This is a known limitation of the current envelope implementation
            // (see ApiCall.kt line 24-32). The richer error code from the body is lost.
            // Tracked anomaly A6 — to fix when the real backend lands so 429 surfaces
            // OTP_RATE_LIMITED to the user, not a generic HTTP_429.
            assertEquals("HTTP_429", ex.apiError.code)
            assertEquals(429, ex.httpCode)
        }

    @Test
    fun `verify-otp expired returns BackendException OTP_EXPIRED on HTTP 200`() =
        kotlinx.coroutines.runBlocking {
            // The contract says backend may return business errors on HTTP 200 with success=false.
            // This ensures the front correctly surfaces OTP_EXPIRED (not a generic HTTP code).
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """
                    {
                      "success": false,
                      "data": null,
                      "error": { "code": "OTP_EXPIRED", "message": "Code expiré" }
                    }
                    """.trimIndent(),
                ),
            )

            val result =
                api.verifyOtp(
                    VerifyOtpRequest(phone = "+21698123456", code = "111111"),
                ).unwrap()

            val ex = result.exceptionOrNull() as BackendException
            assertEquals("OTP_EXPIRED", ex.apiError.code)
        }
}
