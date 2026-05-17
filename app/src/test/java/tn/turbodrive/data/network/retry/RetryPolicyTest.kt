package tn.turbodrive.data.network.retry

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetryPolicyTest {
    @Test
    fun `no retry POST without idempotency on 500`() {
        val json = "{}".toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url("https://example.com/rides").post(json).build()
        val resp = response(req, 500)
        assertFalse(RetryPolicy.shouldRetryServerError(0, resp, req))
    }

    @Test
    fun `retry POST with idempotency on 500`() {
        val json = "{}".toRequestBody("application/json".toMediaType())
        val req =
            Request.Builder().url("https://example.com/rides")
                .header("idempotency-key", "abc")
                .post(json)
                .build()
        val resp = response(req, 500)
        assertTrue(RetryPolicy.shouldRetryServerError(0, resp, req))
        assertFalse(RetryPolicy.shouldRetryServerError(3, resp, req))
    }

    @Test
    fun `retry GET on 500`() {
        val req = Request.Builder().url("https://example.com/health").get().build()
        val resp = response(req, 500)
        assertTrue(RetryPolicy.shouldRetryServerError(0, resp, req))
    }

    @Test
    fun `429 triggers retry flag`() {
        val req = Request.Builder().url("https://example.com/").get().build()
        val resp = response(req, 429)
        assertTrue(RetryPolicy.shouldRetryRateLimit(resp))
    }

    @Test
    fun `retry after header parses to ms`() {
        val req = Request.Builder().url("https://example.com/").get().build()
        val resp = response(req, 429, mapOf("Retry-After" to "3"))
        assertEquals(3000L, RetryPolicy.rateLimitWaitMs(resp))
    }

    private fun response(
        request: Request,
        code: Int,
        headers: Map<String, String> = emptyMap(),
    ): Response {
        val hb = okhttp3.Headers.Builder()
        headers.forEach { (k, v) -> hb.add(k, v) }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("m")
            .headers(hb.build())
            .build()
    }
}
