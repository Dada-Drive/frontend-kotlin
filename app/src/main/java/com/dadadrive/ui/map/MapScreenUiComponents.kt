package com.dadadrive.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import com.dadadrive.R
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapView as HereMapView
import com.dadadrive.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PassengerRouteSearchBottomSheet(
    viewModel: MapViewModel,
    mapViewRef: MutableState<HereMapView?>,
    routeSheetState: SheetState,
    fieldResetSession: Int,
    initialOrigin: String,
    initialDestination: String,
    onDismiss: () -> Unit,
    onOpenPickupMapPicker: () -> Unit,
    onOpenDestinationMapPicker: () -> Unit,
) {
    val addressSearchResults by viewModel.addressSearchResults.collectAsState()
    val addressSearchLoading by viewModel.addressSearchLoading.collectAsState()
    val pickupSearchResults by viewModel.pickupSearchResults.collectAsState()
    val pickupSearchLoading by viewModel.pickupSearchLoading.collectAsState()
    val ridePickupNow by viewModel.ridePickupNow.collectAsState()
    val rideForMe by viewModel.rideForMe.collectAsState()
    val rideScheduledAtEpochMs by viewModel.rideScheduledAtEpochMs.collectAsState()
    val passengerBookingName by viewModel.passengerBookingName.collectAsState()
    val passengerBookingPhone by viewModel.passengerBookingPhone.collectAsState()

    var originFieldDraft by remember(fieldResetSession) {
        mutableStateOf(initialOrigin)
    }
    var intermediateStopDrafts by remember(fieldResetSession) {
        mutableStateOf<List<String>>(emptyList())
    }
    var finalDestinationDraft by remember(fieldResetSession) {
        mutableStateOf(initialDestination)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = routeSheetState,
        containerColor = LocalAppColors.current.surfaceElevated,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(LocalAppColors.current.dragHandle, CircleShape)
            )
        }
    ) {
        RouteItinerarySheetContent(
            originValue = originFieldDraft,
            onOriginChange = { text ->
                originFieldDraft = text
                if (text.isBlank()) viewModel.clearPickupOverride()
                else viewModel.schedulePickupSearch(text)
            },
            intermediateStops = intermediateStopDrafts,
            finalDestination = finalDestinationDraft,
            onIntermediateStopChange = { index, value ->
                intermediateStopDrafts = intermediateStopDrafts.toMutableList().apply {
                    this[index] = value
                }
                viewModel.scheduleAddressSearch(value)
            },
            onAddIntermediateStop = {
                if (intermediateStopDrafts.size < 4) {
                    intermediateStopDrafts = intermediateStopDrafts + ""
                    viewModel.scheduleAddressSearch("")
                }
            },
            onRemoveIntermediateStop = { index ->
                intermediateStopDrafts = intermediateStopDrafts.toMutableList().apply {
                    removeAt(index)
                }
            },
            onFinalDestinationChange = { value ->
                finalDestinationDraft = value
                viewModel.scheduleAddressSearch(value)
            },
            pickupResults = pickupSearchResults,
            pickupSearchLoading = pickupSearchLoading,
            destinationResults = addressSearchResults,
            destinationSearchLoading = addressSearchLoading,
            onPickupHit = { hit ->
                viewModel.applyPickupOverride(hit)
                originFieldDraft = hit.label
            },
            onIntermediateStopHit = { index, hit ->
                intermediateStopDrafts = intermediateStopDrafts.toMutableList().apply {
                    this[index] = hit.label
                }
            },
            onFinalDestinationHit = { hit ->
                finalDestinationDraft = hit.label
                viewModel.applySearchDestination(hit)
                mapViewRef.value?.camera?.lookAt(
                    hit.coordinates,
                    MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 16.0)
                )
            },
            onClose = onDismiss,
            onOpenPickupMapPicker = onOpenPickupMapPicker,
            onOpenDestinationMapPicker = onOpenDestinationMapPicker,
            pickupNow = ridePickupNow,
            onPickupNowToggle = { viewModel.setRidePickupNow(!ridePickupNow) },
            forMe = rideForMe,
            onForMeToggle = { viewModel.setRideForMe(!rideForMe) },
            scheduledAtEpochMs = rideScheduledAtEpochMs,
            onScheduledAtChosen = { viewModel.setRideScheduledAtEpochMs(it) },
            passengerName = passengerBookingName,
            onPassengerNameChange = viewModel::setPassengerBookingName,
            passengerPhone = passengerBookingPhone,
            onPassengerPhoneChange = viewModel::setPassengerBookingPhone
        )
    }
}

@Composable
internal fun PickerModeBottomBar(
    modifier: Modifier = Modifier,
    canConfirm: Boolean,
    onTerminer: () -> Unit
) {
    val c = LocalAppColors.current
    val gradientColors = if (canConfirm) {
        listOf(
            lerp(c.primary, Color.White, 0.12f),
            c.primary,
            lerp(c.primary, Color.Black, 0.15f)
        )
    } else {
        listOf(c.primaryDisabled, c.primaryDisabled, c.primaryDisabled)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Brush.horizontalGradient(gradientColors))
                .clickable(enabled = canConfirm, onClick = onTerminer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.map_confirm_pickup_action),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = c.onPrimary.copy(alpha = if (canConfirm) 1f else 0.5f)
            )
        }
    }
}

@Composable
internal fun CenterPickupPinOverlay(primary: Color) {
    val layout    = remember { teardropPickupPinLayout() }
    val pinBitmap = remember(primary) { createTeardropPickupLocationBitmap(primary.toArgb()) }
    val density   = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        if (w <= 0f || h <= 0f) return@BoxWithConstraints
        val left = ((w - layout.bitmapWidth) / 2f).roundToInt()
        val top  = ((h / 2f) - layout.tipYFromTop).roundToInt()
        val imgW = (layout.bitmapWidth  / density.density).dp
        val imgH = (layout.bitmapHeight / density.density).dp
        Image(
            bitmap = pinBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset { IntOffset(left, top) }
                .size(imgW, imgH)
        )
    }
}

@Composable
internal fun UserLocationOverlay(
    avatarUrl: String?,
    profileInitials: String,
    position: Point2D?,
    showPickupCallout: Boolean,
    onOpenRouteSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (position == null) return
    val c = LocalAppColors.current
    val density = LocalDensity.current
    val size = 46.dp
    val pulse = rememberInfiniteTransition(label = "user_location_pulse")
    val p1 by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "user_p1"
    )
    val p2 by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, delayMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "user_p2"
    )

    val markerHalfPx = with(density) { 23.dp.toPx() }
    val gapPx = with(density) { 2.dp.toPx() }
    // Keep the callout visually attached to the user marker.
    val calloutAnchorPx = with(density) { 50.dp.toPx() }
    val calloutWidthPx = with(density) { 200.dp.toPx() }
    val routeInteractive = showPickupCallout

    Box(modifier = modifier) {
        if (showPickupCallout) {
            Surface(
                modifier = Modifier
                    .width(200.dp)
                    .wrapContentHeight()
                    .offset {
                        IntOffset(
                            (position.x - calloutWidthPx / 2f).toInt(),
                            (position.y - markerHalfPx - gapPx - calloutAnchorPx).toInt()
                        )
                    }
                    .clickable { onOpenRouteSheet() },
                shape = RoundedCornerShape(12.dp),
                color = c.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.map_pickup_callout_title),
                            color = c.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 13.sp
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = stringResource(R.string.map_route_origin_placeholder),
                            color = c.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = c.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (position.x - 23.0).toInt(),
                        y = (position.y - 23.0).toInt()
                    )
                }
                .then(
                    if (routeInteractive) Modifier.clickable { onOpenRouteSheet() }
                    else Modifier
                )
                .size(size),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size + 16.dp + (30.dp * p2))
                    .border(2.dp, c.primary.copy(alpha = (0.22f - p2 * 0.22f).coerceAtLeast(0f)), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(size + 8.dp + (18.dp * p1))
                    .border(2.5.dp, c.primary.copy(alpha = (0.45f - p1 * 0.45f).coerceAtLeast(0f)), CircleShape)
            )
            Box(modifier = Modifier.size(size + 4.dp).background(c.primary, CircleShape))
            Box(modifier = Modifier.size(size + 2.dp).background(Color.White, CircleShape))
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(c.darkInput),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(profileInitials, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(13.dp)
                    .background(c.primary, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}


