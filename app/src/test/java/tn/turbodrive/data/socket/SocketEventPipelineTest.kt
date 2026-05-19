package tn.turbodrive.data.socket

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration test for the SocketEventManager → ViewModel pipeline.
 *
 * Verifies that events emitted on the underlying SocketService flow
 * propagate through the SocketEventManager façade and reach a
 * collector — and that consumer-side scope filters (per VM) correctly
 * accept in-scope events and ignore out-of-scope ones.
 *
 * A real Socket.IO → JSON → decoder roundtrip is covered by
 * [SocketEventDecoderTest]. A real Netty Socket.IO server test lands
 * in R-3.6 (Session B).
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SocketEventPipelineTest {
    private val decoder = SocketEventDecoder()

    @Test
    fun `three events emitted upstream reach the manager events flow in order`() =
        runTest(UnconfinedTestDispatcher()) {
            val upstream =
                MutableSharedFlow<SocketEvent>(
                    replay = 0,
                    extraBufferCapacity = 8,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )
            val socketService =
                mockk<SocketService>(relaxed = true) {
                    every { events } returns upstream
                }
            val manager = SocketEventManager(socketService)

            val received = mutableListOf<SocketEvent>()
            val job =
                launch {
                    manager.events.collect { received += it }
                }

            val e1 = decoder.decode("ride:accepted", RIDE_ACCEPTED_JSON)
            val e2 = decoder.decode("wallet:topup_confirmed", WALLET_TOPUP_JSON)
            val e3 = decoder.decode("notification:new", NOTIFICATION_JSON)

            assertNotNull(e1)
            assertNotNull(e2)
            assertNotNull(e3)

            upstream.emit(e1!!)
            upstream.emit(e2!!)
            upstream.emit(e3!!)

            assertEquals(3, received.size)
            assertTrue(received[0] is SocketEvent.RideAccepted)
            assertTrue(received[1] is SocketEvent.WalletTopupConfirmed)
            assertTrue(received[2] is SocketEvent.NotificationNew)
            job.cancel()
        }

    @Test
    fun `wallet-scoped filter accepts wallet events and ignores ride events`() {
        // Verifies the VM-side scoping pattern: a consumer that only cares
        // about wallet:* should not store unrelated events. Mirrors the
        // `handleSocketEvent` whitelist in WalletViewModel.
        val ride = decoder.decode("ride:accepted", RIDE_ACCEPTED_JSON)
        val wallet = decoder.decode("wallet:topup_confirmed", WALLET_TOPUP_JSON)
        val tx = decoder.decode("wallet:transaction_new", WALLET_TX_JSON)

        val seen = mutableListOf<SocketEvent>()
        listOf(ride!!, wallet!!, tx!!).forEach { event ->
            if (event is SocketEvent.WalletTopupConfirmed || event is SocketEvent.WalletTransactionNew) {
                seen += event
            }
        }

        assertEquals(2, seen.size)
        assertTrue(seen[0] is SocketEvent.WalletTopupConfirmed)
        assertTrue(seen[1] is SocketEvent.WalletTransactionNew)
    }

    @Test
    fun `forwarding preserves event identity end to end`() =
        runTest(UnconfinedTestDispatcher()) {
            val upstream =
                MutableSharedFlow<SocketEvent>(
                    replay = 0,
                    extraBufferCapacity = 8,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )
            val socketService =
                mockk<SocketService>(relaxed = true) {
                    every { events } returns upstream
                }
            val manager = SocketEventManager(socketService)

            var received: SocketEvent? = null
            val job =
                launch {
                    manager.events.collect { received = it }
                }

            val emitted = decoder.decode("ride:driver_location", DRIVER_LOCATION_JSON)
            assertNotNull(emitted)
            upstream.emit(emitted!!)

            assertNotNull(received)
            assertTrue(received === emitted) // same reference, no copying
            job.cancel()
        }

    @Test
    fun `manager events flow tolerates collector absence — no replay`() =
        runTest(UnconfinedTestDispatcher()) {
            val upstream =
                MutableSharedFlow<SocketEvent>(
                    replay = 0,
                    extraBufferCapacity = 8,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )
            val socketService =
                mockk<SocketService>(relaxed = true) {
                    every { events } returns upstream
                }
            val manager = SocketEventManager(socketService)

            // Emit before any collector subscribes — should not be replayed.
            val e1 = decoder.decode("ride:accepted", RIDE_ACCEPTED_JSON)!!
            upstream.emit(e1)

            var received: SocketEvent? = null
            val job =
                launch {
                    manager.events.collect { received = it }
                }

            // Give the collector a chance to attach; no historical event delivered.
            assertNull(received)
            job.cancel()
        }

    private companion object {
        const val RIDE_ACCEPTED_JSON = """
            {
              "rideId": "r-1",
              "riderId": "u-1",
              "riderName": "Sami",
              "pickupLat": 36.8065,
              "pickupLng": 10.1815,
              "pickupAddress": "Tunis",
              "dropoffAddress": "La Marsa"
            }
        """

        const val WALLET_TOPUP_JSON = """
            {
              "transactionId": "t-1",
              "amount": 50.0,
              "currency": "TND",
              "newBalance": 125.5,
              "confirmedAt": "2026-05-19T10:00:00Z"
            }
        """

        const val WALLET_TX_JSON = """
            {
              "transactionId": "t-2",
              "transactionType": "RIDE_FARE",
              "amount": -8.5,
              "currency": "TND",
              "newBalance": 117.0,
              "createdAt": "2026-05-19T10:05:00Z"
            }
        """

        const val NOTIFICATION_JSON = """
            {
              "id": "n-1",
              "type": "PROMO",
              "title": "Hello",
              "body": "Test",
              "createdAt": "2026-05-19T10:00:00Z"
            }
        """

        const val DRIVER_LOCATION_JSON = """
            {
              "driverId": "d-1",
              "lat": 36.8,
              "lng": 10.18,
              "timestamp": "2026-05-19T10:00:00Z"
            }
        """
    }
}
