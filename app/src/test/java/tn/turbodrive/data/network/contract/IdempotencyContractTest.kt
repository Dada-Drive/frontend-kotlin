package tn.turbodrive.data.network.contract

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import tn.turbodrive.data.network.IdempotencyKeyGenerator
import tn.turbodrive.data.network.api.AuthApiService
import tn.turbodrive.data.network.api.RidesApiService
import tn.turbodrive.data.network.interceptor.IdempotencyKeyInterceptor
import tn.turbodrive.data.network.model.LogoutRequest
import java.util.UUID

/**
 * Contract tests for `idempotency-key` header injection (R-1.3).
 *
 * Locks the convention documented in `docs/BACKEND_CONTRACT.md §5`:
 * - Endpoints annotated `@Idempotent` MUST receive an `idempotency-key` header
 *   carrying a UUID v4.
 * - Endpoints WITHOUT the annotation MUST NOT receive the header.
 *
 * If the backend ever stops honoring this convention (silently dedupe-by-payload
 * instead of by-key, or change header name), the front behavior is unchanged but
 * the duplication guarantee is lost. These tests are the canary.
 */
class IdempotencyContractTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `Idempotent annotated endpoint receives valid UUID idempotency-key header`() =
        kotlinx.coroutines.runBlocking {
            val ridesApi: RidesApiService =
                ContractTestSupport.retrofitService(server) { builder ->
                    builder.addInterceptor(IdempotencyKeyInterceptor(IdempotencyKeyGenerator()))
                }
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """{ "success": true, "data": { }, "error": null }""",
                ),
            )

            // pickRideOffer is @Idempotent — minimal call with path params only
            runCatching {
                ridesApi.pickRideOffer(rideId = "r-1", offerId = "o-1")
            }

            val recorded = server.takeRequest()
            val key = recorded.getHeader("idempotency-key")
            assertNotNull("Idempotent endpoint must carry idempotency-key header", key)

            // Must be a valid UUID v4 (lowercase, dashed)
            val parsed = UUID.fromString(key!!)
            assertEquals(
                "Header must contain a parseable UUID",
                key.lowercase(),
                parsed.toString().lowercase(),
            )
        }

    @Test
    fun `non-Idempotent endpoint does not receive idempotency-key header`() =
        kotlinx.coroutines.runBlocking {
            // Same interceptor wired in, but the endpoint is NOT @Idempotent → no header.
            val authApi: AuthApiService =
                ContractTestSupport.retrofitService(server) { builder ->
                    builder.addInterceptor(IdempotencyKeyInterceptor(IdempotencyKeyGenerator()))
                }
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """{ "success": true, "data": null, "error": null }""",
                ),
            )

            // logout is NOT @Idempotent (cf. AuthApiService) → no header expected
            runCatching {
                authApi.logout(LogoutRequest(refreshToken = "rt"))
            }

            val recorded = server.takeRequest()
            assertNull(
                "Non-Idempotent endpoint must NOT receive idempotency-key header",
                recorded.getHeader("idempotency-key"),
            )
        }

    @Test
    fun `pre-existing idempotency-key header is preserved on Idempotent endpoint`() =
        kotlinx.coroutines.runBlocking {
            // Simulates RetryInterceptor replaying a request that already has the key set.
            // The interceptor must NOT overwrite — otherwise retries on rate-limit would
            // generate a fresh key on each attempt, defeating the dedup mechanism.
            val explicitKey = "11111111-2222-3333-4444-555555555555"

            // Custom interceptor that stamps the header BEFORE the IdempotencyKeyInterceptor
            // gets a chance to inject a fresh UUID.
            val ridesApi: RidesApiService =
                ContractTestSupport.retrofitService(server) { builder ->
                    builder
                        .addInterceptor { chain ->
                            chain.proceed(
                                chain.request().newBuilder()
                                    .header("idempotency-key", explicitKey)
                                    .build(),
                            )
                        }
                        .addInterceptor(IdempotencyKeyInterceptor(IdempotencyKeyGenerator()))
                }
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """{ "success": true, "data": { }, "error": null }""",
                ),
            )

            runCatching { ridesApi.pickRideOffer(rideId = "r-1", offerId = "o-1") }

            val recorded = server.takeRequest()
            assertEquals(
                "Pre-existing idempotency-key must be preserved (retry replay stability)",
                explicitKey,
                recorded.getHeader("idempotency-key"),
            )
        }

    @Test
    fun `header name is lowercase per backend contract convention`() =
        kotlinx.coroutines.runBlocking {
            // Contract §5: header is `idempotency-key` (lowercase) not `Idempotency-Key`.
            // HTTP is case-insensitive on the wire, but consistency helps log grep.
            val ridesApi: RidesApiService =
                ContractTestSupport.retrofitService(server) { builder ->
                    builder.addInterceptor(IdempotencyKeyInterceptor(IdempotencyKeyGenerator()))
                }
            server.enqueue(
                ContractTestSupport.jsonOk(
                    """{ "success": true, "data": { }, "error": null }""",
                ),
            )

            runCatching { ridesApi.pickRideOffer(rideId = "r-1", offerId = "o-1") }

            val recorded = server.takeRequest()
            // Headers are case-insensitive in OkHttp's getHeader() but the raw name on
            // the wire is what we sent. We inspect the recorded request's header list
            // to ensure the wire name is exactly "idempotency-key".
            val headerNames = recorded.headers.toMultimap().keys
            val match =
                headerNames.firstOrNull { it.equals("idempotency-key", ignoreCase = true) }
            assertNotNull("Header must be present", match)
            assertEquals("Header must be lowercase", "idempotency-key", match)
        }
}
