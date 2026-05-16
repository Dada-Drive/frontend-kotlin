package tn.dadadrive.presentation.notifications

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DriverOfferOpenRequest(
    val rideId: String?
)

object DriverNotificationBridge {
    const val EXTRA_OPEN_DRIVER_OFFER = "open_driver_offer"
    const val EXTRA_RIDE_ID = "ride_id"

    private val _openOfferRequest = MutableStateFlow<DriverOfferOpenRequest?>(null)
    val openOfferRequest: StateFlow<DriverOfferOpenRequest?> = _openOfferRequest.asStateFlow()

    fun publishFromIntent(intent: Intent?) {
        if (intent == null) return
        val shouldOpenOffer = intent.getBooleanExtra(EXTRA_OPEN_DRIVER_OFFER, false)
        if (!shouldOpenOffer) return
        val rideId = intent.getStringExtra(EXTRA_RIDE_ID)
        _openOfferRequest.value = DriverOfferOpenRequest(rideId = rideId)
    }

    fun clearOpenOfferRequest() {
        _openOfferRequest.value = null
    }
}
