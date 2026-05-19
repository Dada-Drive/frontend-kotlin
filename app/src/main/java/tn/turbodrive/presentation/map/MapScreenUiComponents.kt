package tn.turbodrive.presentation.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.here.sdk.core.Point2D
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.common.ScreenState
import tn.turbodrive.presentation.riderhome.RouteItinerarySheetContent
import kotlin.math.roundToInt
import com.here.sdk.mapview.MapView as HereMapView

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
    onOpenIntermediateStopMapPicker: (index: Int, append: Boolean) -> Unit,
) {
    val ridePickupNow by viewModel.ridePickupNow.collectAsState()
    val rideForMe by viewModel.rideForMe.collectAsState()
    val rideScheduledAtEpochMs by viewModel.rideScheduledAtEpochMs.collectAsState()
    val passengerBookingName by viewModel.passengerBookingName.collectAsState()
    val passengerBookingPhone by viewModel.passengerBookingPhone.collectAsState()
    val intermediateStopDrafts by viewModel.intermediateStopDrafts.collectAsState()
    val poiState by viewModel.poiState.collectAsState()
    val pickupSearchState by viewModel.pickupSearchState.collectAsState()
    val addressSearchState by viewModel.addressSearchState.collectAsState()
    val pickupSearchResults = (pickupSearchState as? ScreenState.Loaded)?.value.orEmpty()
    val pickupSearchLoading = pickupSearchState is ScreenState.Loading
    val addressSearchResults = (addressSearchState as? ScreenState.Loaded)?.value.orEmpty()
    val addressSearchLoading = addressSearchState is ScreenState.Loading
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(poiState) {
        val msg = (poiState as? ScreenState.Error)?.error?.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar("Erreur POI: $msg")
        viewModel.consumePoiSearchError()
    }

    var originFieldDraft by remember(fieldResetSession) {
        mutableStateOf(initialOrigin)
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
                    .padding(top = 6.dp, bottom = 2.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(LocalAppColors.current.dragHandle, CircleShape),
            )
        },
    ) {
        RouteItinerarySheetContent(
            originValue = originFieldDraft,
            onOriginChange = { text ->
                originFieldDraft = text
                if (text.isBlank()) {
                    viewModel.clearPickupOverride()
                } else if (detectCategory(text) == null) {
                    viewModel.schedulePickupSearch(text)
                } else {
                    viewModel.clearPickupSearchResults()
                }
                viewModel.onSearchQueryChanged(text, PoiSearchField.PICKUP)
            },
            intermediateStops = intermediateStopDrafts,
            finalDestination = finalDestinationDraft,
            onIntermediateStopChange = { index, value ->
                viewModel.updateIntermediateStopLabel(index, value)
            },
            onAddIntermediateStop = {
                if (intermediateStopDrafts.size < 4) {
                    viewModel.addIntermediateStopDraft()
                }
            },
            onRemoveIntermediateStop = { index ->
                viewModel.removeIntermediateStopDraft(index)
            },
            onFinalDestinationChange = { value ->
                finalDestinationDraft = value
                if (detectCategory(value) == null) {
                    viewModel.scheduleAddressSearch(value)
                } else {
                    viewModel.clearAddressSearchResults()
                }
                viewModel.onSearchQueryChanged(value, PoiSearchField.DROPOFF)
            },
            onClose = onDismiss,
            onOpenPickupMapPicker = onOpenPickupMapPicker,
            onOpenDestinationMapPicker = onOpenDestinationMapPicker,
            onOpenIntermediateStopMapPicker = onOpenIntermediateStopMapPicker,
            pickupNow = ridePickupNow,
            onPickupNowToggle = { viewModel.setRidePickupNow(!ridePickupNow) },
            forMe = rideForMe,
            onForMeToggle = { viewModel.setRideForMe(!rideForMe) },
            scheduledAtEpochMs = rideScheduledAtEpochMs,
            onScheduledAtChosen = { viewModel.setRideScheduledAtEpochMs(it) },
            passengerName = passengerBookingName,
            onPassengerNameChange = viewModel::setPassengerBookingName,
            passengerPhone = passengerBookingPhone,
            onPassengerPhoneChange = viewModel::setPassengerBookingPhone,
            sheetState = routeSheetState,
            pickupSearchResults = pickupSearchResults,
            pickupSearchLoading = pickupSearchLoading,
            addressSearchResults = addressSearchResults,
            addressSearchLoading = addressSearchLoading,
            onPickupSuggestionPick = { hit ->
                originFieldDraft = hit.label
                viewModel.applyPickupOverride(hit)
                focusManager.clearFocus()
            },
            onDestinationSuggestionPick = { hit ->
                finalDestinationDraft = hit.label
                viewModel.applySearchDestination(hit)
                focusManager.clearFocus()
                onDismiss()
            },
            onIntermediateSuggestionPick = { index, hit ->
                viewModel.applyIntermediateStopHit(index, hit)
                focusManager.clearFocus()
            },
        )
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
internal fun PickerModeBottomBar(
    modifier: Modifier = Modifier,
    canConfirm: Boolean,
    onTerminer: () -> Unit,
) {
    val c = LocalAppColors.current
    val gradientColors =
        if (canConfirm) {
            listOf(
                lerp(c.primary, Color.White, 0.12f),
                c.primary,
                lerp(c.primary, Color.Black, 0.15f),
            )
        } else {
            listOf(c.primaryDisabled, c.primaryDisabled, c.primaryDisabled)
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.horizontalGradient(gradientColors))
                    .clickable(enabled = canConfirm, onClick = onTerminer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.map_confirm_pickup_action),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = c.onPrimary.copy(alpha = if (canConfirm) 1f else 0.5f),
            )
        }
    }
}

@Composable
internal fun CenterPickupPinOverlay(primary: Color) {
    val layout = remember { teardropPickupPinLayout() }
    val pinBitmap = remember(primary) { createTeardropPickupLocationBitmap(primary.toArgb()) }
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        if (w <= 0f || h <= 0f) return@BoxWithConstraints
        val left = ((w - layout.bitmapWidth) / 2f).roundToInt()
        val top = ((h / 2f) - layout.tipYFromTop).roundToInt()
        val imgW = (layout.bitmapWidth / density.density).dp
        val imgH = (layout.bitmapHeight / density.density).dp
        Image(
            bitmap = pinBitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier =
                Modifier
                    .offset { IntOffset(left, top) }
                    .size(imgW, imgH),
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
    onUserPinTap: (() -> Unit)? = null,
    pinColor: Color,
    modifier: Modifier = Modifier,
    showBroadcastPulse: Boolean = false,
) {
    if (position == null) return
    val c = LocalAppColors.current
    val density = LocalDensity.current

    // Teardrop geometry — chosen so the circular avatar fits snugly inside the head and
    // the tail tapers down to the coordinate point (Google-Maps style, matches reference).
    // Tuned slim so the marker doesn't visually dominate the map.
    val pinWidth = 46.dp
    val pinHeight = 70.dp
    val headDiameter = 42.dp // outer teardrop circle diameter
    val avatarSize = 32.dp // inner photo circle (white ring = 3dp around it)
    val routeInteractive = showPickupCallout
    val pinInteractive = onUserPinTap != null || routeInteractive
    val markerHalfPx = with(density) { (pinWidth / 2).toPx() }
    val avatarCenterXPx = position.x.toFloat()
    // Anchor = tip at `position`; avatar center sits at head center (headDiameter/2 from top).
    val avatarCenterYPx =
        position.y.toFloat() -
            with(density) { (pinHeight - headDiameter / 2).toPx() }

    Box(modifier = modifier) {
        if (showBroadcastPulse) {
            BroadcastPulseRings(
                centerXPx = avatarCenterXPx,
                centerYPx = avatarCenterYPx,
                color = c.primary,
            )
        }

        Box(
            modifier =
                Modifier
                    .zIndex(1f)
                    .offset {
                        IntOffset(
                            x = (position.x - markerHalfPx).toInt(),
                            y = (position.y - with(density) { pinHeight.toPx() }).toInt(),
                        )
                    }
                    .then(
                        if (pinInteractive) {
                            Modifier.clickable {
                                onUserPinTap?.invoke() ?: onOpenRouteSheet()
                            }
                        } else {
                            Modifier
                        },
                    )
                    .size(width = pinWidth, height = pinHeight),
        ) {
            // Single filled teardrop background (smooth shoulder → tip, no visible step
            // between the head and the tail as in the reference screenshot).
            TeardropShape(
                color = pinColor,
                headDiameterDp = headDiameter,
                modifier = Modifier.fillMaxSize(),
            )
            // Avatar centered inside the head, with a thin white ring so the photo
            // reads as "pressed into" the pin without making it look bold.
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (headDiameter - avatarSize) / 2 - 1.5.dp)
                        .size(avatarSize + 3.dp)
                        .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(avatarSize)
                            .clip(CircleShape)
                            .background(c.darkInput),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            profileInitials,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Paints a Google-Maps-style teardrop as two overlapping filled shapes:
 *   1. A full circle at the top (the "head") of diameter [headDiameterDp].
 *   2. A tail path starting at two tangent points on the lower sides of the head,
 *      curving down to meet at a single tip at the bottom-center via quadratic
 *      beziers — so the shoulders transition smoothly into the tail without any
 *      visible step (matches the reference screenshot).
 * Both shapes use the same solid color so the union paints as one teardrop.
 */
@Composable
private fun TeardropShape(
    color: Color,
    headDiameterDp: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val headDiameter = with(density) { headDiameterDp.toPx() }
        val headRadius = headDiameter / 2f
        val cx = w / 2f
        val headCy = headRadius

        // Shoulder = point on the circle where the tail starts. Angle is measured from
        // the vertical (straight-down) axis at the head center. A *smaller* angle pulls
        // the shoulders inward + downward, so the tail base is narrow: this is what
        // gives the pin its arrow-like silhouette (instead of a round, "pregnant"
        // teardrop).
        val tangentAngleRad = Math.toRadians(42.0).toFloat()
        val shoulderDx = headRadius * kotlin.math.sin(tangentAngleRad)
        val shoulderDy = headRadius * kotlin.math.cos(tangentAngleRad)
        val leftShoulderX = cx - shoulderDx
        val rightShoulderX = cx + shoulderDx
        val shoulderY = headCy + shoulderDy

        val tipX = cx
        val tipY = h

        // 1. Solid head circle.
        drawCircle(
            color = color,
            radius = headRadius,
            center = androidx.compose.ui.geometry.Offset(cx, headCy),
        )

        // 2. Arrow-shaped tail: each side bows *inward* toward the centerline via a
        //    quadratic whose control point sits close to cx — so the sides look
        //    concave (arrow point) rather than convex (water drop).
        val tail =
            androidx.compose.ui.graphics.Path().apply {
                moveTo(leftShoulderX, shoulderY)
                quadraticTo(
                    cx - headRadius * 0.08f,
                    shoulderY + (tipY - shoulderY) * 0.70f,
                    tipX,
                    tipY,
                )
                quadraticTo(
                    cx + headRadius * 0.08f,
                    shoulderY + (tipY - shoulderY) * 0.70f,
                    rightShoulderX,
                    shoulderY,
                )
                close()
            }
        drawPath(path = tail, color = color)
    }
}

@Composable
private fun BroadcastPulseRings(
    centerXPx: Float,
    centerYPx: Float,
    color: Color,
) {
    val transition = rememberInfiniteTransition(label = "broadcast_pulse")
    val maxRadiusDp = 140.dp
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { maxRadiusDp.toPx() }

    val progress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "p1",
    )
    val progress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(700),
            ),
        label = "p2",
    )
    val progress3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(1400),
            ),
        label = "p3",
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        fun drawRing(progress: Float) {
            val radius = maxRadiusPx * progress
            val alpha = (1f - progress).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = 0.22f * alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerXPx, centerYPx),
            )
            drawCircle(
                color = color.copy(alpha = 0.55f * alpha),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerXPx, centerYPx),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = with(density) { 2.dp.toPx() }),
            )
        }
        drawRing(progress1)
        drawRing(progress2)
        drawRing(progress3)
    }
}

@Composable
internal fun PickupCenterHintOverlay(
    onOpenRouteSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Box(modifier = modifier) {
        CenterPickupPinOverlay(primary = Color.Black)
        Surface(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = (-74).dp)
                    .width(200.dp)
                    .wrapContentHeight()
                    .clickable { onOpenRouteSheet() },
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
            shadowElevation = 8.dp,
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.map_pickup_callout_title),
                        color = c.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 13.sp,
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = stringResource(R.string.map_route_origin_placeholder),
                        color = c.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp,
                    )
                }
                Icon(
                    painter = painterResource(AppIcon.chevronRight),
                    contentDescription = null,
                    tint = c.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
