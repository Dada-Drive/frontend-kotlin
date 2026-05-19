package tn.turbodrive.data.socket

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import io.socket.client.IO
import io.socket.client.Socket
import org.junit.AfterClass
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * E2E integration tests verifying the Socket.IO client ↔ Netty server handshake,
 * event delivery over the wire, and [SocketEventDecoder] decoding of server-emitted
 * payloads. [SocketService] is bypassed because its URL is compile-time (BuildConfig);
 * the raw [IO] client points directly to the in-process Netty server.
 *
 * Three scenarios (R-3.6):
 * 1. TCP handshake — client connects → EVENT_CONNECT fires.
 * 2. Event round-trip — server broadcasts ride:accepted after client connects → decoder produces RideAccepted.
 * 3. Disconnect — client calls disconnect() → EVENT_DISCONNECT fires.
 *
 * netty-socketio 2.0.0 has a known issue where EIO4 polling sessions become stale after
 * custom-namespace acknowledgment packets, causing EVENT_CONNECT to never fire on the
 * client side. The tests therefore use the default namespace ("/"); the Socket.IO lifecycle
 * behaviour (connect/event/disconnect) is identical regardless of namespace.
 *
 * Server starts once for the whole class (avoids port-rebind delays between tests).
 */
class SocketLifecycleIntegrationTest {
    companion object {
        private const val PORT = 18_943

        private lateinit var server: SocketIOServer

        private val ridePayload =
            mapOf(
                "rideId" to "r-e2e",
                "riderId" to "u-1",
                "riderName" to "Sami",
                "pickupLat" to 36.8065,
                "pickupLng" to 10.1815,
                "pickupAddress" to "Tunis",
                "dropoffAddress" to "La Marsa",
            )

        @BeforeClass
        @JvmStatic
        fun startServer() {
            val config = Configuration()
            config.setPort(PORT) // explicit setter: avoids Kotlin shadowing of outer `port` variable
            config.hostname = "127.0.0.1" // explicit IPv4 — avoids macOS localhost→::1 mismatch
            server = SocketIOServer(config)
            server.addConnectListener { /* accept all clients */ }
            server.start()
            Thread.sleep(300) // give Netty event loop time to bind before first test
        }

        @AfterClass
        @JvmStatic
        fun stopServer() {
            server.stop()
        }
    }

    private val decoder = SocketEventDecoder()

    @Test
    fun `client connects and EVENT_CONNECT fires`() {
        val latch = CountDownLatch(1)
        val socket = buildSocket()

        socket.on(Socket.EVENT_CONNECT) { latch.countDown() }
        socket.connect()

        assertTrue("connect timeout", latch.await(5, TimeUnit.SECONDS))
        socket.disconnect()
    }

    @Test
    fun `server broadcasts ride-accepted after connect and decoder produces RideAccepted`() {
        val connectLatch = CountDownLatch(1)
        val eventLatch = CountDownLatch(1)
        val receivedEvent = AtomicReference<SocketEvent?>()
        val socket = buildSocket()

        socket.on(Socket.EVENT_CONNECT) { connectLatch.countDown() }
        socket.on("ride:accepted") { args ->
            receivedEvent.set(decoder.decode("ride:accepted", args[0].toString()))
            eventLatch.countDown()
        }
        socket.connect()

        // Wait for connection, then have server broadcast the event.
        assertTrue("connect timeout", connectLatch.await(5, TimeUnit.SECONDS))
        server.broadcastOperations.sendEvent("ride:accepted", ridePayload)

        assertTrue("event timeout", eventLatch.await(5, TimeUnit.SECONDS))
        assertNotNull(receivedEvent.get())
        assertTrue(receivedEvent.get() is SocketEvent.RideAccepted)
        socket.disconnect()
    }

    @Test
    fun `client disconnects and EVENT_DISCONNECT fires`() {
        val connectLatch = CountDownLatch(1)
        val disconnectLatch = CountDownLatch(1)
        val socket = buildSocket()

        socket.on(Socket.EVENT_CONNECT) { connectLatch.countDown() }
        socket.on(Socket.EVENT_DISCONNECT) { disconnectLatch.countDown() }
        socket.connect()

        assertTrue("connect timeout", connectLatch.await(5, TimeUnit.SECONDS))
        socket.disconnect()
        assertTrue("disconnect timeout", disconnectLatch.await(5, TimeUnit.SECONDS))
    }

    private fun buildSocket(): Socket =
        IO.socket(
            "http://127.0.0.1:$PORT",
            IO.Options().apply {
                // Polling only: netty-socketio 2.0 + socket.io-client 2.1 WebSocket upgrade
                // fails with EngineIOException in JVM tests (Netty HTTP-upgrade header mismatch).
                transports = arrayOf("polling")
                reconnection = false
                timeout = 4_000
            },
        )
}
