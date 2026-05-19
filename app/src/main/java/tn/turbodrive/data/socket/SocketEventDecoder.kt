package tn.turbodrive.data.socket

import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decodes raw Socket.IO event payloads into typed [SocketEvent] instances.
 *
 * Returns `null` on parse failure or unknown event name. Malformed payloads
 * are logged at WARN and **never crash** the app: the backend may add new
 * events or fields ahead of the client, and the app must keep running.
 */
@Singleton
class SocketEventDecoder
    @Inject
    constructor() {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }

        /**
         * Decode a raw payload into a typed [SocketEvent].
         *
         * @param eventName backend event name, e.g. `"ride:accepted"`.
         * @param payloadJson raw JSON string from `socket.io args[0].toString()`.
         * @return typed event, or `null` if the event name is unknown or the
         *   payload fails to parse.
         */
        @Suppress("CyclomaticComplexMethod") // dispatch table: 21 event names
        fun decode(
            eventName: String,
            payloadJson: String,
        ): SocketEvent? =
            try {
                when (eventName) {
                    "ride:new_request" -> SocketEvent.RideNewRequest(json.decodeFromString(payloadJson))
                    "ride:new_offer" -> SocketEvent.RideNewOffer(json.decodeFromString(payloadJson))
                    "ride:accepted" -> SocketEvent.RideAccepted(json.decodeFromString(payloadJson))
                    "ride:offer_rejected" -> SocketEvent.RideOfferRejected(json.decodeFromString(payloadJson))
                    "ride:driver_arrived" -> SocketEvent.RideDriverArrived(json.decodeFromString(payloadJson))
                    "ride:status_changed" -> SocketEvent.RideStatusChanged(json.decodeFromString(payloadJson))
                    "ride:completed" -> SocketEvent.RideCompleted(json.decodeFromString(payloadJson))
                    "ride:cancelled" -> SocketEvent.RideCancelled(json.decodeFromString(payloadJson))
                    "ride:driver_location" -> SocketEvent.RideDriverLocation(json.decodeFromString(payloadJson))

                    "negotiate:propose" -> SocketEvent.NegotiationProposed(json.decodeFromString(payloadJson))
                    "negotiate:accept" -> SocketEvent.NegotiationAccepted(json.decodeFromString(payloadJson))
                    "negotiate:counter" -> SocketEvent.NegotiationCountered(json.decodeFromString(payloadJson))
                    "negotiate:reject" -> SocketEvent.NegotiationRejected(json.decodeFromString(payloadJson))

                    "wallet:topup_confirmed" -> SocketEvent.WalletTopupConfirmed(json.decodeFromString(payloadJson))
                    "wallet:transaction_new" -> SocketEvent.WalletTransactionNew(json.decodeFromString(payloadJson))

                    "notification:new" -> SocketEvent.NotificationNew(json.decodeFromString(payloadJson))

                    "shared:passenger_joined" -> SocketEvent.SharedPassengerJoined(json.decodeFromString(payloadJson))
                    "shared:passenger_left" -> SocketEvent.SharedPassengerLeft(json.decodeFromString(payloadJson))
                    "shared:passenger_picked_up" -> SocketEvent.SharedPassengerPickedUp(json.decodeFromString(payloadJson))
                    "shared:passenger_dropped_off" -> SocketEvent.SharedPassengerDroppedOff(json.decodeFromString(payloadJson))
                    "shared:ride_completed" -> SocketEvent.SharedRideCompleted(json.decodeFromString(payloadJson))

                    else -> {
                        warn("Unknown event name: $eventName")
                        null
                    }
                }
            } catch (e: SerializationException) {
                warn("Failed to parse $eventName: ${e.message}")
                null
            } catch (e: IllegalArgumentException) {
                warn("Invalid payload for $eventName: ${e.message}")
                null
            }

        /**
         * `android.util.Log` is native — calling it in JVM unit tests throws
         * `UnsatisfiedLinkError`. `runCatching` swallows that while keeping
         * the real Android log in production.
         */
        private fun warn(msg: String) {
            runCatching { Log.w(TAG, msg) }
        }

        private companion object {
            const val TAG = "SocketEventDecoder"
        }
    }
