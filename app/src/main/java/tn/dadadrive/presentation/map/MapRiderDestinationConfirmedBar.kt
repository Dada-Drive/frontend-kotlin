package tn.dadadrive.presentation.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dadadrive.R
import tn.dadadrive.core.pricing.FareConfidence
import tn.dadadrive.core.pricing.RiderFareEstimate
import tn.dadadrive.core.theme.LocalAppColors
import tn.dadadrive.domain.models.ActiveRide
import tn.dadadrive.domain.models.DriverRatingsStats
import tn.dadadrive.domain.models.PassengerRideOffer
import tn.dadadrive.domain.models.RideRating
import tn.dadadrive.domain.models.RideStatus
import tn.dadadrive.presentation.components.BlackCloseIconButton
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatRideScheduledAtIsoForDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", Locale.getDefault()).format(zdt)
    } catch (_: Exception) {
        iso
    }
}

/** Bandeau réduit ; au tap, ré-affiche la carte trajet complète. */
@Composable
internal fun RiderDestinationRouteCardCollapsedPeek(
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onExpand() },
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .padding(bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(c.dragHandle, CircleShape),
            )
            Text(
                text = stringResource(R.string.map_enter_route),
                color = c.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        }
    }
}

/** Équivalent Swift : Presentation/Home/Componenets/DestinationConfirmedCard.swift */
@Composable
internal fun RiderDestinationConfirmedBar(
    pickupTitle: String,
    destinationTitle: String,
    intermediateStops: List<IntermediateStopDraft> = emptyList(),
    fareEstimate: RiderFareEstimate?,
    routeOptions: List<PassengerRouteOption>,
    selectedRouteIndex: Int,
    onSelectRoute: (Int) -> Unit,
    onChangeDestination: () -> Unit,
    onAddStopDuringSearch: () -> Unit,
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
    onSubmitRideRating: (rideId: String, score: Int, comment: String?) -> Unit,
) {
    val c = LocalAppColors.current
    val routeCardBg = Color.White
    val routeOnSurface = c.textPrimary
    val routeMuted = c.textSecondary
    val routePriceInk = c.textPrimary
    val routeSoftPanel = c.surfaceMuted
    val routeChipBorder = c.border
    val pickupDisplay = remember(pickupTitle) { formatAddressForDisplay(pickupTitle) }
    val destinationDisplay = remember(destinationTitle) { formatAddressForDisplay(destinationTitle) }
    val requestSuccessRideId = lastRequestedRide?.id
    val isScheduledRide = lastRequestedRide?.status == RideStatus.Scheduled
    val scheduledAtDisplay =
        remember(lastRequestedRide?.id, lastRequestedRide?.scheduledAt) {
            formatRideScheduledAtIsoForDisplay(lastRequestedRide?.scheduledAt)
        }
    val showSearchingDriversState =
        requestSuccessRideId != null &&
            !isRideMatched &&
            !isScheduledRide &&
            lastRequestedRide?.status != RideStatus.Completed &&
            incomingOffers.isEmpty() &&
            pickingOfferId == null
    var selectedScore by remember(requestSuccessRideId) { mutableStateOf(0) }
    var ratingComment by remember(requestSuccessRideId) { mutableStateOf("") }
    var showRatingThanks by remember(requestSuccessRideId) { mutableStateOf(false) }
    LaunchedEffect(rideRating?.id) {
        if (rideRating != null) showRatingThanks = true
    }
    val changeDestinationInteraction = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = routeCardBg,
        shadowElevation = 10.dp,
    ) {
        Column(
            Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (showSearchingDriversState) {
                SearchingDriversPanel(
                    pickupDisplay = pickupDisplay,
                    destinationDisplay = destinationDisplay,
                    intermediateStops = intermediateStops,
                    onAddStop = onAddStopDuringSearch,
                    onCancelRideRequest = onCancelRideRequest,
                )
                return@Column
            }
            if (requestSuccessRideId != null && isRideMatched && matchedOffer != null) {
                MatchedDriverCompactPanel(
                    matchedOffer = matchedOffer,
                    ride = lastRequestedRide,
                    onCancelRideRequest = onCancelRideRequest,
                )
                return@Column
            }
            if (requestSuccessRideId != null) {
                DriverOffersCompactPanel(
                    incomingOffers = incomingOffers,
                    isLoadingOffers = isLoadingOffers,
                    pickingOfferId = pickingOfferId,
                    onPickOffer = onPickOffer,
                    onDismissOffer = onDismissOffer,
                    onCancelRideRequest = onCancelRideRequest,
                )
                return@Column
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.map_enter_route),
                    color = routeOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                )
                BlackCloseIconButton(
                    onClick = onChangeDestination,
                    buttonSize = 30.dp,
                    iconSize = 16.dp,
                )
            }

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = routeSoftPanel,
            ) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                    PassengerRouteTimeline(
                        pickupDisplay = pickupDisplay,
                        destinationDisplay = destinationDisplay,
                        intermediateStops = intermediateStops,
                        routeOnSurface = routeOnSurface,
                        routeMuted = routeMuted,
                        includeAddStopRow = true,
                        onAddStopDuringSearch = onAddStopDuringSearch,
                        onChangeDestination = onChangeDestination,
                        changeDestinationInteraction = changeDestinationInteraction,
                        showDestinationChangeAction = true,
                    )
                }
            }

            if (routeOptions.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                ) {
                    routeOptions.forEachIndexed { index, option ->
                        val selected = index == selectedRouteIndex
                        Surface(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .clickable { onSelectRoute(index) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color.Black else Color.White,
                            border =
                                if (selected) {
                                    null
                                } else {
                                    BorderStroke(
                                        1.dp,
                                        routeChipBorder,
                                    )
                                },
                            shadowElevation = if (selected) 0.dp else 0.dp,
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(
                                    "Route ${index + 1}",
                                    color = if (selected) Color.White else routeOnSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                )
                                Text(
                                    String.format(Locale.US, "%d min - %.1f km", option.estimatedMinutes, option.distanceKm),
                                    color = if (selected) Color.White.copy(alpha = 0.88f) else routeMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
            if (fareEstimate != null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.map_fare_estimate_label),
                        color = routeMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = c.surfaceMuted,
                            border = BorderStroke(1.dp, c.border),
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.2f TND", fareEstimate.fareTnd),
                                color = routePriceInk,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Text(
                                text = String.format(Locale.US, "%d\u00A0min", fareEstimate.estimatedMinutes),
                                color = routeOnSurface,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                softWrap = false,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f\u00A0km", fareEstimate.distanceKm),
                                color = routeMuted,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                softWrap = false,
                            )
                        }
                    }
                }
                Text(
                    text =
                        when (fareEstimate.confidence) {
                            FareConfidence.CONFIRMED -> stringResource(R.string.map_fare_confirmed_note)
                            FareConfidence.NETWORK_FALLBACK -> stringResource(R.string.map_fare_network_fallback_note)
                            FareConfidence.ESTIMATED -> stringResource(R.string.map_fare_estimate_note)
                        },
                    color = routeMuted,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp).padding(top = 2.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.map_gps_fare_loading),
                    color = routeMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Button(
                onClick = onRequestRide,
                enabled = !requestInProgress && lastRequestedRide == null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    if (requestInProgress) stringResource(R.string.map_requesting_ride) else stringResource(R.string.map_validate_ride),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                )
            }
            requestSuccessRideId?.let {
                val confirmationLabel =
                    when {
                        isScheduledRide && scheduledAtDisplay.isNotBlank() ->
                            stringResource(R.string.map_ride_scheduled_confirmation, scheduledAtDisplay)
                        else -> stringResource(R.string.map_ride_request_sent)
                    }
                Text(
                    text = confirmationLabel,
                    color = c.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            if (requestSuccessRideId != null && isScheduledRide) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clickable(onClick = onCancelRideRequest),
                    shape = RoundedCornerShape(12.dp),
                    color = c.surface,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.map_cancel_order),
                            color = c.errorRed,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (requestSuccessRideId != null && lastRequestedRide?.status == RideStatus.Completed) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = c.surface,
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = c.surfaceMuted,
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.map_ride_finished_title),
                                        color = c.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 30.sp,
                                    )
                                    Text(
                                        text =
                                            stringResource(
                                                R.string.map_ride_finished_total,
                                                lastRequestedRide.finalFare ?: lastRequestedRide.calculatedFare,
                                            ),
                                        color = c.textSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp,
                                    )
                                }
                            }
                        }
                        if (isLoadingRideRating) {
                            Text(
                                text = stringResource(R.string.map_rate_loading),
                                color = c.textSecondary,
                                fontSize = 12.sp,
                            )
                        } else if (showRatingThanks) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = c.surfaceMuted,
                            ) {
                                Text(
                                    text = stringResource(R.string.map_rate_thanks),
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                )
                            }
                            Button(
                                onClick = onCancelRideRequest,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = c.textPrimary),
                            ) {
                                Text(stringResource(R.string.common_done), color = c.surface)
                            }
                        } else {
                            Text(
                                text =
                                    stringResource(
                                        R.string.map_rate_how_was_ride,
                                        matchedOffer?.driverName ?: stringResource(R.string.map_driver_fallback_name),
                                    ),
                                color = c.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                (1..5).forEach { score ->
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (score <= selectedScore) c.ratingYellow else c.surfaceMuted,
                                        modifier =
                                            Modifier
                                                .size(38.dp)
                                                .clickable { selectedScore = score },
                                    )
                                    if (score < 5) Spacer(Modifier.width(6.dp))
                                }
                            }
                            TextField(
                                value = ratingComment,
                                onValueChange = { ratingComment = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.map_rate_comment_placeholder),
                                        color = c.textHint,
                                    )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(92.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    TextFieldDefaults.colors(
                                        focusedContainerColor = c.surfaceMuted,
                                        unfocusedContainerColor = c.surfaceMuted,
                                        disabledContainerColor = c.surfaceMuted,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                    ),
                            )
                            Button(
                                onClick = {
                                    if (selectedScore in 1..5) {
                                        onSubmitRideRating(
                                            requestSuccessRideId,
                                            selectedScore,
                                            ratingComment.takeIf { it.isNotBlank() },
                                        )
                                    }
                                },
                                enabled = !isSubmittingRideRating && selectedScore in 1..5,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = c.textPrimary),
                                shape = RoundedCornerShape(999.dp),
                            ) {
                                Text(
                                    if (isSubmittingRideRating) {
                                        stringResource(R.string.map_rate_submitting)
                                    } else {
                                        stringResource(R.string.map_rate_submit)
                                    },
                                    color = c.surface,
                                )
                            }
                            TextButton(onClick = onCancelRideRequest, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(stringResource(R.string.welcome_skip), color = c.textSecondary, fontWeight = FontWeight.SemiBold)
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
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun DriverOffersCompactPanel(
    incomingOffers: List<PassengerRideOffer>,
    isLoadingOffers: Boolean,
    pickingOfferId: String?,
    onPickOffer: (String) -> Unit,
    onDismissOffer: (String) -> Unit,
    onCancelRideRequest: () -> Unit,
) {
    val c = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = c.surfaceElevated,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val driverCount = incomingOffers.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = pluralStringResource(R.plurals.map_drivers_available, driverCount, driverCount),
                        color = c.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )
                    Text(
                        text = stringResource(R.string.map_choose_driver),
                        color = c.textSecondary,
                        fontSize = 13.sp,
                    )
                }
                BlackCloseIconButton(onClick = onCancelRideRequest)
            }
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(incomingOffers, key = { it.id }) { offer ->
                    PassengerOfferCard(
                        offer = offer,
                        isAccepting = pickingOfferId == offer.id,
                        onRefuse = { onDismissOffer(offer.id) },
                        onAccept = { onPickOffer(offer.id) },
                        onOfferExpired = { onDismissOffer(offer.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchedDriverCompactPanel(
    matchedOffer: PassengerRideOffer,
    ride: ActiveRide?,
    onCancelRideRequest: () -> Unit,
) {
    val c = LocalAppColors.current
    val context = LocalContext.current
    val minutes = (ride?.estimatedMinutes ?: 1).coerceAtLeast(1)
    val distanceKm = ride?.distanceKm ?: 0.0
    val fare = ride?.finalFare ?: matchedOffer.offeredFare
    val driverName = matchedOffer.driverName ?: stringResource(R.string.map_driver_fallback_name)
    val vehicle = matchedOffer.vehicleLabel ?: stringResource(R.string.map_driver_vehicle_fallback)
    val driverPhone = matchedOffer.driverPhone?.trim().orEmpty()
    val phoneBadge = if (driverPhone.isNotBlank()) "123 TUN  $driverPhone" else "123 TUN  --------"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = c.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = c.surfaceMuted,
                ) {
                    Text(
                        text = stringResource(R.string.driver_status_accepted),
                        color = c.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.2f TND", fare),
                    color = c.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = c.surfaceMuted.copy(alpha = 0.45f),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = c.surface,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = driverName.take(1).uppercase(Locale.getDefault()),
                                    color = c.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = driverName,
                                color = c.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            )
                            Text(
                                text = vehicle,
                                color = c.textSecondary,
                                fontSize = 16.sp,
                            )
                            Spacer(Modifier.height(3.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = c.surface) {
                                Text(
                                    text = phoneBadge,
                                    color = c.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Surface(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .clickable {
                                        if (driverPhone.isNotBlank()) {
                                            val phoneUri = Uri.parse("tel:${Uri.encode(driverPhone)}")
                                            val canDirectCall =
                                                ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.CALL_PHONE,
                                                ) == PackageManager.PERMISSION_GRANTED
                                            val intent =
                                                if (canDirectCall) {
                                                    Intent(Intent.ACTION_CALL, phoneUri)
                                                } else {
                                                    Intent(Intent.ACTION_DIAL, phoneUri)
                                                }
                                            runCatching { context.startActivity(intent) }
                                        }
                                    },
                            shape = CircleShape,
                            color = c.textPrimary,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Call,
                                    contentDescription = stringResource(R.string.map_contact_driver),
                                    tint = c.surface,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = String.format(Locale.US, "%d min · %.1f km", minutes, distanceKm),
                            color = c.textSecondary,
                            fontSize = 14.sp,
                        )
                        Text(text = "")
                    }
                }
            }
        }
    }
}

private const val MAX_ROUTE_INTERMEDIATE_STOPS = 4

@Composable
private fun PassengerRouteTimeline(
    pickupDisplay: String,
    destinationDisplay: String,
    intermediateStops: List<IntermediateStopDraft>,
    routeOnSurface: Color,
    routeMuted: Color,
    includeAddStopRow: Boolean,
    onAddStopDuringSearch: () -> Unit,
    onChangeDestination: () -> Unit,
    changeDestinationInteraction: MutableInteractionSource,
    showDestinationChangeAction: Boolean,
    addressFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    hintFontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
) {
    val c = LocalAppColors.current
    Column(Modifier.fillMaxWidth()) {
        RouteTimelineLegRow(
            kind = RouteTimelineKind.Start,
            connectorUp = false,
            connectorDown = true,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.map_route_origin_hint),
                    color = routeMuted,
                    fontSize = hintFontSize,
                    modifier = Modifier.width(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = pickupDisplay,
                    color = routeOnSurface,
                    fontSize = addressFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        intermediateStops.forEach { draft ->
            RouteTimelineLegRow(
                kind = RouteTimelineKind.Stop,
                connectorUp = true,
                connectorDown = true,
            ) {
                Text(
                    text = formatAddressForDisplay(draft.label).ifBlank { "…" },
                    color = routeOnSurface,
                    fontSize = addressFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        if (includeAddStopRow && intermediateStops.size < MAX_ROUTE_INTERMEDIATE_STOPS) {
            RouteTimelineLegRow(
                kind = RouteTimelineKind.Stop,
                connectorUp = true,
                connectorDown = true,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = changeDestinationInteraction,
                            indication = null,
                            onClick = onAddStopDuringSearch,
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = c.ratingYellow,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.map_add_stop),
                        color = routeMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        RouteTimelineLegRow(
            kind = RouteTimelineKind.End,
            connectorUp = true,
            connectorDown = false,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.map_route_destination_hint),
                    color = routeMuted,
                    fontSize = hintFontSize,
                    modifier = Modifier.width(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = destinationDisplay,
                    color = routeOnSurface,
                    fontSize = addressFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (showDestinationChangeAction) {
                    Text(
                        text = stringResource(R.string.map_route_change),
                        color = c.textHint,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(c.surfaceMuted)
                                .clickable(
                                    interactionSource = changeDestinationInteraction,
                                    indication = null,
                                    onClick = onChangeDestination,
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchingDriversPanel(
    pickupDisplay: String,
    destinationDisplay: String,
    intermediateStops: List<IntermediateStopDraft>,
    onAddStop: () -> Unit,
    onCancelRideRequest: () -> Unit,
) {
    val c = LocalAppColors.current
    val searchingRouteInteraction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = c.primary,
                )
                Column {
                    Text(
                        text = stringResource(R.string.map_searching_drivers_title),
                        color = c.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = stringResource(R.string.map_searching_drivers_subtitle),
                        color = c.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(10.dp),
            color = c.surface,
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                PassengerRouteTimeline(
                    pickupDisplay = pickupDisplay,
                    destinationDisplay = destinationDisplay,
                    intermediateStops = intermediateStops,
                    routeOnSurface = c.textPrimary,
                    routeMuted = c.textSecondary,
                    includeAddStopRow = false,
                    onAddStopDuringSearch = onAddStop,
                    onChangeDestination = {},
                    changeDestinationInteraction = searchingRouteInteraction,
                    showDestinationChangeAction = false,
                    addressFontSize = 14.sp,
                    hintFontSize = 11.sp,
                )
            }
        }

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = onAddStop),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black,
        ) {
            Box(
                modifier = Modifier.padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.map_add_stop),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
            }
        }

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = onCancelRideRequest),
            shape = RoundedCornerShape(10.dp),
            color = c.errorRed.copy(alpha = 0.10f),
        ) {
            Box(
                modifier = Modifier.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.map_cancel_ride),
                    color = c.errorRed,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
