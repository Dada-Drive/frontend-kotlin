package com.dadadrive.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.DriverRatingsStats
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideRating
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.ui.theme.LocalAppColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatRideScheduledAtIsoForDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("EEE d MMM Â· HH:mm", Locale.getDefault()).format(zdt)
    } catch (_: Exception) {
        iso
    }
}

/** Ã‰quivalent Swift : Presentation/Home/Componenets/DestinationConfirmedCard.swift */
@Composable
internal fun RiderDestinationConfirmedBar(
    pickupTitle: String,
    destinationTitle: String,
    fareEstimate: RiderFareEstimate?,
    routeOptions: List<PassengerRouteOption>,
    selectedRouteIndex: Int,
    onSelectRoute: (Int) -> Unit,
    onChangeDestination: () -> Unit,
    onRequestRide: () -> Unit,
    onCancelRideRequest: () -> Unit,
    onPickOffer: (String) -> Unit,
    onDismissOffer: (String) -> Unit,
    requestInProgress: Boolean,
    requestError: String?,
    lastRequestedRide: ActiveRide?,
    incomingOffers: List<PassengerRideOffer>,
    isLoadingOffers: Boolean,
    pickingOfferId: String?,
    matchedOffer: PassengerRideOffer?,
    isRideMatched: Boolean,
    scheduledRides: List<ActiveRide>,
    isLoadingScheduledRides: Boolean,
    scheduledRidesError: String?,
    rideRating: RideRating?,
    isLoadingRideRating: Boolean,
    rideRatingError: String?,
    isSubmittingRideRating: Boolean,
    submitRideRatingError: String?,
    driverRatingsStats: DriverRatingsStats,
    onSubmitRideRating: (rideId: String, score: Int, comment: String?) -> Unit
) {
    val c = LocalAppColors.current
    val requestSuccessRideId = lastRequestedRide?.id
    val isScheduledRide = lastRequestedRide?.status == RideStatus.Scheduled
    val scheduledAtDisplay = remember(lastRequestedRide?.id, lastRequestedRide?.scheduledAt) {
        formatRideScheduledAtIsoForDisplay(lastRequestedRide?.scheduledAt)
    }
    var selectedScore by remember(requestSuccessRideId) { mutableStateOf(5) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = c.surfaceElevated,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.map_route_origin_hint), color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                Text(pickupTitle, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.map_route_destination_hint), color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                    Text(destinationTitle, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                TextButton(onClick = onChangeDestination) {
                    Text(stringResource(R.string.map_route_change), color = c.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = c.dividerGrey)
            if (routeOptions.size > 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 12.dp)) {
                    routeOptions.forEachIndexed { index, option ->
                        val selected = index == selectedRouteIndex
                        Surface(
                            modifier = Modifier.clickable { onSelectRoute(index) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) c.primary else c.surface,
                            shadowElevation = if (selected) 1.dp else 0.dp
                        ) {
                            Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    stringResource(R.string.map_route_option, index + 1),
                                    color = if (selected) c.onPrimary else c.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    String.format(Locale.US, "%d min Â· %.1f km", option.estimatedMinutes, option.distanceKm),
                                    color = if (selected) c.onPrimary.copy(alpha = 0.85f) else c.textHint,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = c.dividerGrey)
            }
            if (fareEstimate != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.1f km Â· %d min",
                                fareEstimate.distanceKm,
                                fareEstimate.estimatedMinutes
                            ),
                            color = c.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.US, "%.2f TND", fareEstimate.fareTnd),
                            color = c.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.map_fare_estimate_note),
                    color = c.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 6.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.map_gps_fare_loading),
                    color = c.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRequestRide,
                enabled = !requestInProgress && lastRequestedRide == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    if (requestInProgress) stringResource(R.string.map_requesting_ride) else stringResource(R.string.map_request_ride),
                    color = c.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            requestSuccessRideId?.let {
                val confirmationLabel = when {
                    isScheduledRide && scheduledAtDisplay.isNotBlank() ->
                        stringResource(R.string.map_ride_scheduled_confirmation, scheduledAtDisplay)
                    else -> stringResource(R.string.map_ride_request_sent)
                }
                Text(
                    text = confirmationLabel,
                    color = c.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            if (requestSuccessRideId != null && isRideMatched && matchedOffer != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = c.surface
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.map_driver_arrival_eta),
                            color = c.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = matchedOffer.vehicleLabel ?: stringResource(R.string.map_driver_vehicle_fallback),
                            color = c.textSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "â˜… %.1f (%d ratings)",
                                driverRatingsStats.avgRating,
                                driverRatingsStats.totalRatings
                            ),
                            color = c.textSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = matchedOffer.driverName ?: stringResource(R.string.map_driver_fallback_name),
                                color = c.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.map_contact_driver),
                                color = c.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.map_safety),
                                color = c.textPrimary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = c.surfaceMuted
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = stringResource(R.string.map_payment_cash),
                                    color = c.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable(onClick = onCancelRideRequest),
                    shape = RoundedCornerShape(12.dp),
                    color = c.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.map_cancel_ride),
                            color = c.errorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (requestSuccessRideId != null && isScheduledRide) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable(onClick = onCancelRideRequest),
                    shape = RoundedCornerShape(12.dp),
                    color = c.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.map_cancel_order),
                            color = c.errorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (requestSuccessRideId != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = c.surface
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.map_choose_driver),
                            color = c.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onCancelRideRequest) {
                            Text(
                                text = stringResource(R.string.map_cancel_order),
                                color = c.errorRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (isLoadingOffers && incomingOffers.isEmpty()) {
                            Text(
                                text = stringResource(R.string.map_waiting_driver_offers),
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(incomingOffers, key = { it.id }) { offer ->
                                    PassengerOfferCard(
                                        offer = offer,
                                        isAccepting = pickingOfferId == offer.id,
                                        onRefuse = { onDismissOffer(offer.id) },
                                        onAccept = { onPickOffer(offer.id) }
                                    )
                                }
                            }
                            if (incomingOffers.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.map_waiting_driver_offers),
                                    color = c.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            if (requestSuccessRideId != null && lastRequestedRide?.status == RideStatus.Completed) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = c.surface
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.map_rate_ride_title),
                            color = c.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLoadingRideRating) {
                            Text(
                                text = stringResource(R.string.map_rate_loading),
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else if (rideRating != null) {
                            val ratingCommentSuffix = rideRating.comment?.let { " Â· $it" }.orEmpty()
                            Text(
                                text = stringResource(
                                    R.string.map_rate_your_rating,
                                    rideRating.score,
                                    ratingCommentSuffix
                                ),
                                color = c.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..5).forEach { score ->
                                    Surface(
                                        modifier = Modifier.clickable { selectedScore = score },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (selectedScore == score) c.primary else c.surfaceMuted
                                    ) {
                                        Text(
                                            text = "$score★",
                                            color = if (selectedScore == score) c.onPrimary else c.textPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Button(
                                onClick = { onSubmitRideRating(requestSuccessRideId, selectedScore, null) },
                                enabled = !isSubmittingRideRating,
                                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (isSubmittingRideRating) {
                                        stringResource(R.string.map_rate_submitting)
                                    } else {
                                        stringResource(R.string.map_rate_submit)
                                    },
                                    color = c.onPrimary
                                )
                            }
                        }
                        rideRatingError?.let { Text(it, color = c.errorRed, fontSize = 11.sp) }
                        submitRideRatingError?.let { Text(it, color = c.errorRed, fontSize = 11.sp) }
                    }
                }
            }
            requestError?.let {
                Text(
                    text = it,
                    color = c.errorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            if (requestSuccessRideId == null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = c.surface
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.map_scheduled_rides_title),
                            color = c.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isLoadingScheduledRides) {
                            Text(
                                text = stringResource(R.string.common_loading),
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else if (scheduledRides.isEmpty()) {
                            Text(
                                text = stringResource(R.string.map_no_upcoming_scheduled_rides),
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            scheduledRides.take(3).forEachIndexed { idx, ride ->
                                val whenText = formatRideScheduledAtIsoForDisplay(ride.scheduledAt)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = whenText.ifBlank {
                                            stringResource(R.string.map_scheduled_label)
                                        },
                                        color = c.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.map_from_address,
                                            ride.pickupAddress
                                        ),
                                        color = c.textSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.map_to_address,
                                            ride.dropoffAddress
                                        ),
                                        color = c.textSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                if (idx < scheduledRides.take(3).lastIndex) {
                                    HorizontalDivider(color = c.dividerGrey.copy(alpha = 0.4f))
                                }
                            }
                        }
                        scheduledRidesError?.let { err ->
                            Text(
                                text = err,
                                color = c.errorRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

