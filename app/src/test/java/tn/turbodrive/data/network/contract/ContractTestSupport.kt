package tn.turbodrive.data.network.contract

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Shared helpers for contract tests in [tn.turbodrive.data.network.contract].
 *
 * Goal: every test in this package spins up a [MockWebServer], wires a real Retrofit
 * instance with the production Gson config (default builder, no field-naming policy),
 * and exercises an [Api service] against responses that match the spec in
 * `docs/BACKEND_CONTRACT.md`.
 *
 * Adding tests here = locking the contract from the frontend side. When the real
 * backend is implemented, these tests double as compliance fixtures: any deviation
 * (renamed field, different envelope shape, drifted error code) will break the test
 * and pinpoint the divergence.
 */
internal object ContractTestSupport {
    /** Production Gson config — matches `NetworkModule.provideGson()`. */
    fun productionGson(): Gson = GsonBuilder().create()

    /**
     * Builds a Retrofit instance pointing at [server], pre-configured with the same
     * Gson converter as production. Pass a freshly-started [MockWebServer].
     */
    inline fun <reified T> retrofitService(
        server: MockWebServer,
        gson: Gson = productionGson(),
        clientCustomizer: (OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    ): T {
        val client = clientCustomizer(OkHttpClient.Builder()).build()
        val retrofit =
            Retrofit.Builder()
                .baseUrl(server.url("/api/v1/"))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        return retrofit.create(T::class.java)
    }

    /** Builds a 200 OK [MockResponse] with `application/json` content type. */
    fun jsonOk(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)

    /** Builds a non-2xx [MockResponse] with a JSON body (envelope `success=false`). */
    fun jsonError(
        httpCode: Int,
        body: String,
    ): MockResponse =
        MockResponse()
            .setResponseCode(httpCode)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
