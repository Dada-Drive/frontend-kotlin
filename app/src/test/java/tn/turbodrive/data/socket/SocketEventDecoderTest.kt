package tn.turbodrive.data.socket

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [SocketEventDecoder] — verifies typed parsing for the
 * backend event names defined in R-3.1, and graceful failure on unknown
 * events / malformed payloads.
 *
 * Payload casing is camelCase (matching the @SerialName values in
 * SocketEvent.kt). TODO Session B: realign with backend once §9 specced.
 */
class SocketEventDecoderTest {
    private val decoder = SocketEventDecoder()

    @Test
    fun `decodes ride new offer to typed RideNewOffer`() {
        val json =
            """
            {
              "rideId": "ride-123",
              "offerId": "offer-1",
              "driverId": "drv-9",
              "driverName": "Ali B.",
              "driverRating": 4.7,
              "vehicleType": "ECO",
              "offeredFare": 15.50
            }
            """.trimIndent()

        val result = decoder.decode("ride:new_offer", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.RideNewOffer)
        val event = result as SocketEvent.RideNewOffer
        assertEquals("ride-123", event.payload.rideId)
        assertEquals("offer-1", event.payload.offerId)
        assertEquals(15.50, event.payload.offeredFare, 0.001)
    }

    @Test
    fun `decodes shared passenger joined with nested fare updates`() {
        val json =
            """
            {
              "rideId": "ride-789",
              "passengerId": "p-1",
              "riderId": "u-2",
              "fareUpdates": [
                {"passengerId": "p-1", "newFare": 8.50},
                {"passengerId": "p-2", "newFare": 12.00}
              ],
              "seatsRemaining": 1
            }
            """.trimIndent()

        val result = decoder.decode("shared:passenger_joined", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.SharedPassengerJoined)
        val event = result as SocketEvent.SharedPassengerJoined
        assertEquals(2, event.payload.fareUpdates.size)
        assertEquals(1, event.payload.seatsRemaining)
        assertEquals(8.50, event.payload.fareUpdates[0].newFare, 0.001)
    }

    @Test
    fun `returns null for unknown event name`() {
        val result = decoder.decode("unknown:event", """{"foo":"bar"}""")
        assertNull(result)
    }

    @Test
    fun `returns null for malformed JSON without crashing`() {
        val result = decoder.decode("ride:accepted", "this is not json")
        assertNull(result)
    }

    @Test
    fun `tolerates unknown backend fields not modeled in payload`() {
        // Backend may add new fields (priority, experimentalFlag) before
        // the client models them. Decoder must still parse successfully.
        val json =
            """
            {
              "rideId": "ride-123",
              "riderId": "u-1",
              "riderName": "Sami",
              "pickupLat": 36.8065,
              "pickupLng": 10.1815,
              "pickupAddress": "Tunis",
              "dropoffAddress": "La Marsa",
              "priority": "high",
              "experimentalFlag": 42
            }
            """.trimIndent()

        val result = decoder.decode("ride:accepted", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.RideAccepted)
    }

    @Test
    fun `decodes wallet topup confirmed with new balance`() {
        val json =
            """
            {
              "transactionId": "txn-001",
              "amount": 50.00,
              "currency": "TND",
              "newBalance": 125.50,
              "confirmedAt": "2026-05-19T10:30:00Z"
            }
            """.trimIndent()

        val result = decoder.decode("wallet:topup_confirmed", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.WalletTopupConfirmed)
        val event = result as SocketEvent.WalletTopupConfirmed
        assertEquals(125.50, event.payload.newBalance, 0.001)
        assertEquals("TND", event.payload.currency)
    }

    @Test
    fun `decodes ride driver location update`() {
        val json =
            """
            {
              "driverId": "drv-9",
              "lat": 36.8065,
              "lng": 10.1815,
              "timestamp": "2026-05-19T10:30:00Z"
            }
            """.trimIndent()

        val result = decoder.decode("ride:driver_location", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.RideDriverLocation)
        val event = result as SocketEvent.RideDriverLocation
        assertEquals(36.8065, event.payload.lat, 0.0001)
        assertEquals(10.1815, event.payload.lng, 0.0001)
    }

    @Test
    fun `decodes negotiation proposed with optional message`() {
        val json =
            """
            {
              "rideId": "ride-123",
              "proposerId": "u-1",
              "proposedFare": 18.00,
              "message": "Trafic dense"
            }
            """.trimIndent()

        val result = decoder.decode("negotiate:propose", json)

        assertNotNull(result)
        assertTrue(result is SocketEvent.NegotiationProposed)
        val event = result as SocketEvent.NegotiationProposed
        assertEquals(18.00, event.payload.proposedFare, 0.001)
        assertEquals("Trafic dense", event.payload.message)
    }

    @Test
    fun `returns null when required field is missing`() {
        // rideId is non-nullable in RideAcceptedPayload — omit it
        val json =
            """
            {
              "riderId": "u-1",
              "riderName": "Sami",
              "pickupLat": 36.8,
              "pickupLng": 10.1,
              "pickupAddress": "X",
              "dropoffAddress": "Y"
            }
            """.trimIndent()

        val result = decoder.decode("ride:accepted", json)
        assertNull(result)
    }
}
