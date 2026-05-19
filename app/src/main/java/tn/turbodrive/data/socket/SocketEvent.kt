package tn.turbodrive.data.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =====================================================================
// INBOUND payloads (server → client)
// camelCase via @SerialName (TODO Session B: confirm backend casing)
// =====================================================================

@Serializable
data class RideNewRequestPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("pickupLat") val pickupLat: Double,
    @SerialName("pickupLng") val pickupLng: Double,
    @SerialName("pickupAddress") val pickupAddress: String,
    @SerialName("dropoffAddress") val dropoffAddress: String,
    @SerialName("vehicleType") val vehicleType: String,
    @SerialName("calculatedFare") val calculatedFare: Double,
    @SerialName("riderName") val riderName: String,
)

@Serializable
data class RideNewOfferPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("offerId") val offerId: String,
    @SerialName("driverId") val driverId: String,
    @SerialName("driverName") val driverName: String,
    @SerialName("driverRating") val driverRating: Double,
    @SerialName("vehicleType") val vehicleType: String,
    @SerialName("offeredFare") val offeredFare: Double,
)

@Serializable
data class RideAcceptedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("riderId") val riderId: String,
    @SerialName("riderName") val riderName: String,
    @SerialName("pickupLat") val pickupLat: Double,
    @SerialName("pickupLng") val pickupLng: Double,
    @SerialName("pickupAddress") val pickupAddress: String,
    @SerialName("dropoffAddress") val dropoffAddress: String,
)

@Serializable
data class RideOfferRejectedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("offerId") val offerId: String,
)

@Serializable
data class RideDriverArrivedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("arrivedAt") val arrivedAt: String,
)

@Serializable
data class RideStatusChangedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("status") val status: String,
    @SerialName("timestamp") val timestamp: String,
)

@Serializable
data class RideCompletedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("status") val status: String,
    @SerialName("completedAt") val completedAt: String,
)

@Serializable
data class RideCancelledPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("cancelledBy") val cancelledBy: String,
    @SerialName("cancelReason") val cancelReason: String? = null,
)

@Serializable
data class RideDriverLocationPayload(
    @SerialName("driverId") val driverId: String,
    val lat: Double,
    val lng: Double,
    @SerialName("timestamp") val timestamp: String,
)

// ---------- Negotiation (TODO backend: validate event names + schemas) ----------

@Serializable
data class NegotiationProposedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("proposerId") val proposerId: String,
    @SerialName("proposedFare") val proposedFare: Double,
    val message: String? = null,
    @SerialName("expiresAt") val expiresAt: String? = null,
)

@Serializable
data class NegotiationAcceptedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("acceptedBy") val acceptedBy: String,
    @SerialName("agreedFare") val agreedFare: Double,
)

@Serializable
data class NegotiationCounteredPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("counterBy") val counterBy: String,
    @SerialName("counterFare") val counterFare: Double,
    val message: String? = null,
)

@Serializable
data class NegotiationRejectedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("rejectedBy") val rejectedBy: String,
    val reason: String? = null,
)

// ---------- Wallet (TODO backend: validate event names + schemas) ----------

@Serializable
data class WalletTopupConfirmedPayload(
    @SerialName("transactionId") val transactionId: String,
    val amount: Double,
    val currency: String,
    @SerialName("newBalance") val newBalance: Double,
    @SerialName("confirmedAt") val confirmedAt: String,
)

@Serializable
data class WalletTransactionNewPayload(
    @SerialName("transactionId") val transactionId: String,
    @SerialName("transactionType") val transactionType: String,
    val amount: Double,
    val currency: String,
    @SerialName("newBalance") val newBalance: Double,
    @SerialName("createdAt") val createdAt: String,
)

// ---------- Notification (TODO backend: validate) ----------

@Serializable
data class NotificationNewPayload(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    @SerialName("createdAt") val createdAt: String,
)

// ---------- Shared rides (backend V1 in dada-api; TODO confirm exact schemas) ----------

@Serializable
data class SharedFareUpdate(
    @SerialName("passengerId") val passengerId: String,
    @SerialName("newFare") val newFare: Double,
)

@Serializable
data class SharedPassengerJoinedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("passengerId") val passengerId: String,
    @SerialName("riderId") val riderId: String,
    @SerialName("fareUpdates") val fareUpdates: List<SharedFareUpdate> = emptyList(),
    @SerialName("seatsRemaining") val seatsRemaining: Int,
)

@Serializable
data class SharedPassengerLeftPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("passengerId") val passengerId: String,
    @SerialName("riderId") val riderId: String,
    @SerialName("seatsRestored") val seatsRestored: Int,
    @SerialName("fareUpdates") val fareUpdates: List<SharedFareUpdate> = emptyList(),
)

@Serializable
data class SharedPassengerPickedUpPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("passengerId") val passengerId: String,
    @SerialName("pickupOrder") val pickupOrder: Int,
)

@Serializable
data class SharedPassengerDroppedOffPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("passengerId") val passengerId: String,
    @SerialName("dropoffOrder") val dropoffOrder: Int,
    @SerialName("finalFare") val finalFare: Double,
)

@Serializable
data class SharedRideCompletedPayload(
    @SerialName("rideId") val rideId: String,
    @SerialName("completedAt") val completedAt: String,
)

// =====================================================================
// OUTBOUND payloads (client → server)
// =====================================================================

@Serializable
data class LocationUpdatePayload(
    val lat: Double,
    val lng: Double,
)

@Serializable
data class DriverStatusPayload(
    @SerialName("isOnline") val isOnline: Boolean,
)

// =====================================================================
// Sealed SocketEvent — typed INBOUND events (R-3.1)
//
// Each typed subclass corresponds to one backend event name. Synthetic
// events (Connected / Disconnected / ResyncCompleted) carry no payload
// and represent lifecycle transitions emitted by SocketService.
// =====================================================================

sealed class SocketEvent {
    // ---------- Ride lifecycle (9) ----------

    /** Backend: `ride:new_request` — driver receives a new ride request. */
    data class RideNewRequest(val payload: RideNewRequestPayload) : SocketEvent()

    /** Backend: `ride:new_offer` — rider receives a driver offer. */
    data class RideNewOffer(val payload: RideNewOfferPayload) : SocketEvent()

    /** Backend: `ride:accepted` — driver's offer was picked by the rider. */
    data class RideAccepted(val payload: RideAcceptedPayload) : SocketEvent()

    /** Backend: `ride:offer_rejected` — a driver offer was rejected. */
    data class RideOfferRejected(val payload: RideOfferRejectedPayload) : SocketEvent()

    /** Backend: `ride:driver_arrived` — driver reached pickup. */
    data class RideDriverArrived(val payload: RideDriverArrivedPayload) : SocketEvent()

    /** Backend: `ride:status_changed` — generic ride status transition. */
    data class RideStatusChanged(val payload: RideStatusChangedPayload) : SocketEvent()

    /** Backend: `ride:completed`. */
    data class RideCompleted(val payload: RideCompletedPayload) : SocketEvent()

    /** Backend: `ride:cancelled`. */
    data class RideCancelled(val payload: RideCancelledPayload) : SocketEvent()

    /** Backend: `ride:driver_location` — driver GPS update (~5s frequency). */
    data class RideDriverLocation(val payload: RideDriverLocationPayload) : SocketEvent()

    // ---------- Negotiation (4) — UI deferred to R-5.5 ----------

    /** Backend: `negotiate:propose`. */
    data class NegotiationProposed(val payload: NegotiationProposedPayload) : SocketEvent()

    /** Backend: `negotiate:accept`. */
    data class NegotiationAccepted(val payload: NegotiationAcceptedPayload) : SocketEvent()

    /** Backend: `negotiate:counter`. */
    data class NegotiationCountered(val payload: NegotiationCounteredPayload) : SocketEvent()

    /** Backend: `negotiate:reject`. */
    data class NegotiationRejected(val payload: NegotiationRejectedPayload) : SocketEvent()

    // ---------- Wallet (2) ----------

    /** Backend: `wallet:topup_confirmed`. */
    data class WalletTopupConfirmed(val payload: WalletTopupConfirmedPayload) : SocketEvent()

    /** Backend: `wallet:transaction_new`. */
    data class WalletTransactionNew(val payload: WalletTransactionNewPayload) : SocketEvent()

    // ---------- Notification (1) ----------

    /** Backend: `notification:new`. */
    data class NotificationNew(val payload: NotificationNewPayload) : SocketEvent()

    // ---------- Shared rides (5) — backend V1 in dada-api ----------

    /** Backend: `shared:passenger_joined`. */
    data class SharedPassengerJoined(val payload: SharedPassengerJoinedPayload) : SocketEvent()

    /** Backend: `shared:passenger_left`. */
    data class SharedPassengerLeft(val payload: SharedPassengerLeftPayload) : SocketEvent()

    /** Backend: `shared:passenger_picked_up`. */
    data class SharedPassengerPickedUp(val payload: SharedPassengerPickedUpPayload) : SocketEvent()

    /** Backend: `shared:passenger_dropped_off`. */
    data class SharedPassengerDroppedOff(val payload: SharedPassengerDroppedOffPayload) : SocketEvent()

    /** Backend: `shared:ride_completed`. */
    data class SharedRideCompleted(val payload: SharedRideCompletedPayload) : SocketEvent()

    // ---------- Synthetic lifecycle (3) — emitted by SocketService ----------

    /** Emitted after the low-level Socket.IO CONNECT event. Triggers resync (R-3.3, Session B). */
    object Connected : SocketEvent()

    /** Emitted on Socket.IO DISCONNECT. */
    object Disconnected : SocketEvent()

    /** Emitted by ResyncOnReconnectUseCase once full-state resync completes (R-3.3, Session B). */
    object ResyncCompleted : SocketEvent()
}

// =====================================================================
// Sealed SocketClientEvent — typed OUTBOUND events (client → server)
// Kept intact from existing code; carries the event name + payload.
// =====================================================================

sealed class SocketClientEvent {
    abstract val eventName: String

    data class LocationUpdate(val payload: LocationUpdatePayload) : SocketClientEvent() {
        override val eventName: String = "location:update"
    }

    data class DriverStatus(val payload: DriverStatusPayload) : SocketClientEvent() {
        override val eventName: String = "driver:status"
    }
}
