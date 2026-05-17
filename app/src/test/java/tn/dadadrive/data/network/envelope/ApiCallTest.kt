package tn.dadadrive.data.network.envelope

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Tests unitaires de [unwrap] sur `Response<ApiResponse<T>>`.
 *
 * On utilise les factories `Response.success(...)` / `Response.error(...)` directement plutôt
 * que MockWebServer : on teste la logique de l'extension, pas Retrofit lui-même.
 */
class ApiCallTest {
    private data class Payload(val id: String, val name: String)

    @Test
    fun `unwrap returns success when backend envelope success=true with data`() {
        val payload = Payload(id = "u1", name = "Alice")
        val response = Response.success(ApiResponse(success = true, data = payload, error = null))

        val result = response.unwrap()

        assertTrue(result.isSuccess)
        assertEquals(payload, result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `unwrap returns success Unit when backend envelope success=true with null data`() {
        val response: Response<ApiResponse<Unit>> =
            Response.success(ApiResponse(success = true, data = null, error = null))

        val result = response.unwrap()

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `unwrap returns failure with BackendException when backend envelope success=false`() {
        val apiError = ApiError(code = "INVALID_OTP", message = "Code incorrect ou expiré.")
        val response: Response<ApiResponse<Payload>> =
            Response.success(ApiResponse(success = false, data = null, error = apiError))

        val result = response.unwrap()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is BackendException)
        val be = ex as BackendException
        assertEquals("INVALID_OTP", be.apiError.code)
        assertEquals("Code incorrect ou expiré.", be.apiError.message)
        assertEquals(200, be.httpCode)
    }

    @Test
    fun `unwrap returns failure with HTTP_401 when response is not successful`() {
        val errorBody = "Unauthorized".toResponseBody("text/plain".toMediaType())
        val response: Response<ApiResponse<Payload>> = Response.error(401, errorBody)

        val result = response.unwrap()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is BackendException)
        val be = ex as BackendException
        assertEquals("HTTP_401", be.apiError.code)
        assertEquals(401, be.httpCode)
    }

    @Test
    fun `unwrap returns failure with EMPTY_BODY when body is null`() {
        // Response.success(code, null) construit un Response 204 No Content avec body null.
        val response: Response<ApiResponse<Payload>> =
            Response.success(204, null as ApiResponse<Payload>?)

        val result = response.unwrap()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is BackendException)
        val be = ex as BackendException
        assertEquals("EMPTY_BODY", be.apiError.code)
        assertEquals(204, be.httpCode)
    }

    // R-1.4 -- extended cases

    @Test
    fun `unwrap with success=true and null data routes through Unit branch`() {
        // Ambiguous backend response: success without a data payload.
        // Caller typing as ApiResponse<Unit> accepts this as a "completed without payload" case
        // (logout, delete, etc.).
        val response: Response<ApiResponse<Unit>> =
            Response.success(ApiResponse(success = true, data = null, error = null))

        val result = response.unwrap()

        assertTrue("Success without data should resolve to Unit success", result.isSuccess)
        assertEquals(Unit, result.getOrNull())
    }

    @Test
    fun `unwrap on HTTP 200 with success=false envelope propagates backend code`() {
        // The whole point of the envelope: HTTP 200 + {success:false, error:{...}} is a
        // business failure surface, distinct from HTTP-level failures. Retrofit only allows
        // 2xx codes in Response.success() so 4xx/5xx are tested via Response.error() above.
        val apiError = ApiError(code = "VALIDATION_ERROR", message = "field 'email' is required")
        val response: Response<ApiResponse<Payload>> =
            Response.success(ApiResponse<Payload>(success = false, data = null, error = apiError))

        val result = response.unwrap()

        assertTrue(result.isFailure)
        val be = result.exceptionOrNull() as BackendException
        assertEquals("VALIDATION_ERROR", be.apiError.code)
        assertEquals(200, be.httpCode)
    }

    @Test
    fun `unwrap on HTTP 500 with empty error body returns BackendException HTTP_500`() {
        val errorBody = "".toResponseBody("application/json".toMediaType())
        val response: Response<ApiResponse<Payload>> = Response.error(500, errorBody)

        val result = response.unwrap()

        assertTrue(result.isFailure)
        val be = result.exceptionOrNull() as BackendException
        assertEquals("HTTP_500", be.apiError.code)
        assertEquals(500, be.httpCode)
    }
}
