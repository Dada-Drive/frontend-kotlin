package tn.turbodrive.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import tn.turbodrive.data.network.IdempotencyKeyGenerator
import tn.turbodrive.data.network.annotation.Idempotent

class IdempotencyKeyInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var api: TestApi
    private lateinit var interceptor: IdempotencyKeyInterceptor

    /** Standard UUID v4 string: 8-4-4-4-12 hex with version digit `4` and variant `[89ab]`. */
    private val uuidV4Regex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        interceptor = IdempotencyKeyInterceptor(IdempotencyKeyGenerator())
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        api =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TestApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `annotated endpoint receives a UUID v4 idempotency-key header`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        api.annotatedPost().execute()

        val recorded: RecordedRequest = server.takeRequest()
        val key = recorded.getHeader(HEADER_NAME)
        assertTrue("Header should be present on @Idempotent endpoint", key != null)
        assertTrue("Header value should match UUID v4 format, got '$key'", uuidV4Regex.matches(key!!))
    }

    @Test
    fun `non-annotated endpoint has no idempotency-key header`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        api.plainGet().execute()

        val recorded = server.takeRequest()
        assertNull("Plain endpoint should not receive the header", recorded.getHeader(HEADER_NAME))
    }

    @Test
    fun `two successive annotated calls produce two different keys`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))

        api.annotatedPost().execute()
        api.annotatedPost().execute()

        val first = server.takeRequest().getHeader(HEADER_NAME)
        val second = server.takeRequest().getHeader(HEADER_NAME)
        assertNotEquals("Each invocation should mint a unique key", first, second)
    }

    @Test
    fun `pre-existing header is preserved (simulates RetryInterceptor replay)`() {
        // Pre-stamp the header BEFORE IdempotencyKeyInterceptor runs to simulate the case where
        // RetryInterceptor replays an already-keyed `initial` request.
        val preStampingInterceptor =
            Interceptor { chain ->
                val tagged =
                    chain.request().newBuilder()
                        .header(HEADER_NAME, "preset-key-do-not-touch")
                        .build()
                chain.proceed(tagged)
            }
        val client =
            OkHttpClient.Builder()
                .addInterceptor(preStampingInterceptor)
                .addInterceptor(interceptor)
                .build()
        val replayApi =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TestApi::class.java)

        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        replayApi.annotatedPost().execute()

        val key = server.takeRequest().getHeader(HEADER_NAME)
        assertEquals("preset-key-do-not-touch", key)
    }

    @Test
    fun `request with null Invocation tag passes through unchanged`() {
        // Direct OkHttp request (no Retrofit) -> no Invocation tag -> interceptor must no-op.
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val direct =
            Request.Builder()
                .url(server.url("/raw"))
                .get()
                .build()
        client.newCall(direct).execute().close()

        val recorded = server.takeRequest()
        assertNull("Non-Retrofit request must not receive the header", recorded.getHeader(HEADER_NAME))
    }

    private interface TestApi {
        @Idempotent
        @POST("annotated")
        fun annotatedPost(): retrofit2.Call<okhttp3.ResponseBody>

        @GET("plain")
        fun plainGet(): retrofit2.Call<okhttp3.ResponseBody>
    }

    private companion object {
        private const val HEADER_NAME = "idempotency-key"
    }
}
