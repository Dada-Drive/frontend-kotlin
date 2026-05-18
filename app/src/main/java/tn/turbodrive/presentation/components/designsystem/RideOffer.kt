package tn.turbodrive.presentation.components.designsystem

/**
 * Ride offer data from the driver matching service — R-4.4.
 *
 * [validityRemainingMs] drives the [LinearProgressTimer] bar shown at the
 * bottom of each OfferCard in [StackedOffersList]. Default 30s matches
 * the redesign spec (turbodrive_redesign/screens-rider.jsx).
 */
data class RideOffer(
    val id: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val fare: Double,
    val validityRemainingMs: Long = 30_000L,
)
