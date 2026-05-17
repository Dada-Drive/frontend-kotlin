package tn.dadadrive.data.network.error

import android.content.Context
import com.dadadrive.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import tn.dadadrive.data.network.envelope.ApiError
import tn.dadadrive.data.network.envelope.BackendException
import tn.dadadrive.domain.models.BackendErrorCode
import tn.dadadrive.domain.models.ErrorCategory
import java.io.IOException
import java.net.SocketTimeoutException

class PresentableErrorMapperTest {
    private lateinit var context: Context
    private lateinit var mapper: PresentableErrorMapper

    @Before
    fun setUp() {
        context = mockk()
        // Default stub : any getString returns a deterministic placeholder so we can
        // assert routing without owning the real resources.
        every { context.getString(any()) } answers { "string#${firstArg<Int>()}" }
        mapper = PresentableErrorMapper(context)
    }

    @Test
    fun `fromThrowable BackendException with known code resolves localized message`() {
        val ex = BackendException(ApiError(code = "RIDE_NOT_FOUND", message = "raw"), httpCode = 404)

        val result = mapper.fromThrowable(ex)

        assertEquals(BackendErrorCode.RIDE_NOT_FOUND, result.code)
        assertEquals(ErrorCategory.NotFound, result.category)
        assertFalse(result.isRetryable)
        verify { context.getString(R.string.error_ride_not_found) }
    }

    @Test
    fun `fromThrowable BackendException with unknown code falls back to HTTP message`() {
        val ex = BackendException(ApiError(code = "WEIRD_FUTURE_CODE", message = "raw"), httpCode = 500)

        val result = mapper.fromThrowable(ex)

        // UNKNOWN -> stringResForBackendCode returns null -> falls back to fromHttp(500) -> error_server_error
        verify { context.getString(R.string.error_server_error) }
        assertEquals(ErrorCategory.Server, result.category)
        assertTrue(result.isRetryable)
    }

    @Test
    fun `fromThrowable BackendException with TOKEN_EXPIRED is intentionally skipped`() {
        // TokenAuthenticator handles refresh upstream -- mapper must NOT produce a user-facing message
        // tied to TOKEN_EXPIRED. It falls through to fromHttp generic handling.
        val ex = BackendException(ApiError(code = "TOKEN_EXPIRED", message = "raw"), httpCode = 401)

        val result = mapper.fromThrowable(ex)

        // fromBackend returns null for TOKEN_EXPIRED -> fromHttp(401) -> error_unauthorized
        verify { context.getString(R.string.error_unauthorized) }
        assertEquals(ErrorCategory.Auth, result.category)
        // The typed code is not propagated for skipped codes (fromHttp path doesn't set it).
        assertNull(result.code)
    }

    @Test
    fun `fromThrowable BackendException with 429 propagates retryAfter null when missing`() {
        val ex = BackendException(ApiError(code = "RATE_LIMITED", message = "raw"), httpCode = 429)

        val result = mapper.fromThrowable(ex)

        assertEquals(BackendErrorCode.RATE_LIMITED, result.code)
        assertEquals(ErrorCategory.RateLimit, result.category)
        assertTrue(result.isRetryable)
        // BackendException doesn't carry Retry-After header -- retryAfterSeconds stays null
        assertNull(result.retryAfterSeconds)
    }

    @Test
    fun `fromThrowable IOException routes to network category`() {
        val result = mapper.fromThrowable(IOException("offline"))

        assertEquals(ErrorCategory.Network, result.category)
        assertTrue(result.isRetryable)
        verify { context.getString(R.string.error_network) }
    }

    @Test
    fun `fromThrowable SocketTimeoutException routes to network category`() {
        val result = mapper.fromThrowable(SocketTimeoutException("slow"))

        assertEquals(ErrorCategory.Network, result.category)
        assertTrue(result.isRetryable)
        verify { context.getString(R.string.error_network) }
    }

    @Test
    fun `fromThrowable generic exception falls back to error_generic`() {
        val result = mapper.fromThrowable(RuntimeException("boom"))

        assertEquals(ErrorCategory.Server, result.category)
        assertFalse(result.isRetryable)
        verify { context.getString(R.string.error_generic) }
    }

    /**
     * Exhaustiveness guard : iterating over the enum proves every code (except UNKNOWN)
     * routes through a non-null @StringRes lookup, otherwise this test fails at runtime.
     * Acts as a runtime complement to the compile-time `when` exhaustiveness check.
     */
    @Test
    fun `all non-UNKNOWN codes propagate typed code via BackendException path`() {
        BackendErrorCode.entries
            .filter { it != BackendErrorCode.UNKNOWN }
            .forEach { code ->
                val ex = BackendException(ApiError(code = code.name, message = "raw"), httpCode = 400)
                val result = mapper.fromThrowable(ex)
                assertEquals(
                    "Expected code $code to propagate through BackendException path",
                    code,
                    result.code,
                )
            }
    }

    // R-1.4 -- extended cases

    @Test
    fun `fromHttpException parses error code from JSON body and routes to localized message`() {
        // Body contains the legacy envelope {success:false, error:{code, message}} -- proves
        // PresentableErrorMapper.parseErrorCodeFromBody() correctly extracts the code from a
        // raw HttpException (not yet a BackendException).
        val body =
            """{"success":false,"error":{"code":"INSUFFICIENT_BALANCE","message":"low"}}"""
                .toResponseBody("application/json".toMediaType())
        val httpResponse: Response<Any> = Response.error(409, body)
        val httpEx = HttpException(httpResponse)

        val result = mapper.fromThrowable(httpEx)

        assertEquals(BackendErrorCode.INSUFFICIENT_BALANCE, result.code)
        assertEquals(ErrorCategory.BusinessRule, result.category)
        verify { context.getString(R.string.error_insufficient_balance) }
    }

    @Test
    fun `fromHttp 429 with Retry-After header propagates retryAfterSeconds`() {
        val result = mapper.fromHttp(httpCode = 429, backendErrorCode = null, retryAfterHeader = "120")

        assertEquals(ErrorCategory.RateLimit, result.category)
        assertTrue(result.isRetryable)
        assertNotNull(result.retryAfterSeconds)
        assertEquals(120, result.retryAfterSeconds)
        verify { context.getString(R.string.error_rate_limited) }
    }

    @Test
    fun `fromHttp 429 without Retry-After header leaves retryAfterSeconds null`() {
        val result = mapper.fromHttp(httpCode = 429, backendErrorCode = null, retryAfterHeader = null)

        assertEquals(ErrorCategory.RateLimit, result.category)
        assertNull(result.retryAfterSeconds)
    }
}
