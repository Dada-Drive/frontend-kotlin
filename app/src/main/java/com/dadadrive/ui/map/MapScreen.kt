package com.dadadrive.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.PI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.dadadrive.R
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.Color as HereColor
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapFeatureModes
import com.here.sdk.mapview.MapFeatures
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker as HereMapMarker
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline as HereMapPolyline
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView as HereMapView
import com.here.sdk.mapview.RenderSize
import com.dadadrive.core.language.AppLanguage
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.ui.language.LanguageViewModel
import com.dadadrive.ui.language.localizedDisplayName
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors
import com.dadadrive.ui.wallet.WalletViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToColorSettings: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLocation       by viewModel.currentLocation.collectAsState()
    val currentAddress        by viewModel.currentAddress.collectAsState()
    val pickTargetGeo         by viewModel.pickTargetGeo.collectAsState()
    val pickTargetAddress     by viewModel.pickTargetAddress.collectAsState()
    val confirmedDestination  by viewModel.confirmedDestination.collectAsState()
    val destinationLabel      by viewModel.destinationLabel.collectAsState()
    val pickupOverrideLabel   by viewModel.pickupOverrideLabel.collectAsState()
    val pickupOverrideGeo     by viewModel.pickupOverrideGeo.collectAsState()
    val riderFareEstimate     by viewModel.riderFareEstimate.collectAsState()
    val passengerRouteGeometries by viewModel.passengerRouteGeometries.collectAsState()
    val passengerTrafficSpans by viewModel.passengerTrafficSpans.collectAsState()
    val passengerRouteOptions by viewModel.passengerRouteOptions.collectAsState()
    val selectedPassengerRouteIndex by viewModel.selectedPassengerRouteIndex.collectAsState()
    val isRequestingRide by viewModel.isRequestingRide.collectAsState()
    val rideRequestError by viewModel.rideRequestError.collectAsState()
    val lastRequestedRide by viewModel.lastRequestedRide.collectAsState()
    val incomingRideOffers by viewModel.incomingRideOffers.collectAsState()
    val isLoadingRideOffers by viewModel.isLoadingRideOffers.collectAsState()
    val pickingOfferId by viewModel.pickingOfferId.collectAsState()
    val matchedRideOffer by viewModel.matchedRideOffer.collectAsState()
    val isRideMatched by viewModel.isRideMatched.collectAsState()
    val scheduledRides by viewModel.scheduledRides.collectAsState()
    val isLoadingScheduledRides by viewModel.isLoadingScheduledRides.collectAsState()
    val scheduledRidesError by viewModel.scheduledRidesError.collectAsState()
    val rideRating by viewModel.rideRating.collectAsState()
    val isLoadingRideRating by viewModel.isLoadingRideRating.collectAsState()
    val rideRatingError by viewModel.rideRatingError.collectAsState()
    val isSubmittingRideRating by viewModel.isSubmittingRideRating.collectAsState()
    val submitRideRatingError by viewModel.submitRideRatingError.collectAsState()
    val driverRatingsStats by viewModel.driverRatingsStats.collectAsState()
    val user                  by profileViewModel.user.collectAsState()
    val wallet by walletViewModel.wallet.collectAsState()
    val walletLoading by walletViewModel.isLoading.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRouteSheet by remember { mutableStateOf(false) }
    var routeSheetSession by remember { mutableIntStateOf(0) }
    var mapPickerMode  by remember { mutableStateOf(false) }
    var pickerIsDestination by remember { mutableStateOf(false) }
    val routeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val openRouteSheet: () -> Unit = {
        routeSheetSession++
        showRouteSheet = true
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        if (granted) viewModel.startLocationUpdates()
    }

    LaunchedEffect(Unit) {
        if (locationPermissionGranted) viewModel.startLocationUpdates()
        else permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    profileViewModel.refresh()
                    if (locationPermissionGranted) viewModel.startLocationUpdates()
                    viewModel.fetchScheduledRides()
                }
                Lifecycle.Event.ON_PAUSE -> viewModel.stopLocationUpdates()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val mapViewRef  = remember { mutableStateOf<HereMapView?>(null) }
    val sceneLoaded = remember { mutableStateOf(false) }
    var hasInitiallyFocused by remember { mutableStateOf(false) }

    LaunchedEffect(lastRequestedRide?.id) {
        lastRequestedRide?.id?.let { viewModel.fetchRideRating(it) }
    }

    // ✅ FIX : Position Tunis par défaut si GPS pas encore disponible
    LaunchedEffect(currentLocation, sceneLoaded.value) {
        if (!hasInitiallyFocused && sceneLoaded.value) {
            val target = currentLocation ?: GeoCoordinates(36.8065, 10.1815)
            mapViewRef.value?.camera?.lookAt(
                target,
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 15.0)
            )
            if (currentLocation != null) {
                hasInitiallyFocused = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        val useDarkMap = isSystemInDarkTheme()
        var mapDisplayMode by remember { mutableStateOf(AppMapDisplayMode.NORMAL) }
        var showMapTypePicker by remember { mutableStateOf(false) }

        val avatarUrl = user?.profilePictureUri
        val profileInitials = remember(user?.fullName) {
            val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
            when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "?"
            }
        }
        val userLocationScreenPoint = remember { mutableStateOf<Point2D?>(null) }

        // Carte plein écran
        HereMapViewComposable(
            currentLocation      = currentLocation,
            pickupPoint          = if (mapPickerMode) {
                null
            } else {
                pickupOverrideGeo ?: if (confirmedDestination != null) currentLocation else null
            },
            confirmedDestination = if (mapPickerMode) null else confirmedDestination,
            passengerRouteGeometries = if (mapPickerMode) emptyList() else passengerRouteGeometries,
            passengerTrafficSpans = if (mapPickerMode) emptyList() else passengerTrafficSpans,
            selectedPassengerRouteIndex = selectedPassengerRouteIndex,
            sceneLoaded          = sceneLoaded,
            useDarkMap           = useDarkMap,
            mapDisplayMode       = mapDisplayMode,
            profilePictureUri    = user?.profilePictureUri,
            profileInitials      = profileInitials,
            showBuiltInUserMarker = false,
            onUserLocationScreenPointUpdated = { point -> userLocationScreenPoint.value = point },
            onPickTargetUpdated  = if (mapPickerMode) viewModel::updatePickTarget else null,
            mapViewRef           = mapViewRef
        )
        UserLocationOverlay(
            avatarUrl = avatarUrl,
            profileInitials = profileInitials,
            position = userLocationScreenPoint.value,
            showPickupCallout = !mapPickerMode && !showRouteSheet && confirmedDestination == null,
            onOpenRouteSheet = openRouteSheet,
            modifier = Modifier.fillMaxSize()
        )

        if (mapPickerMode) {
            PickupPinOverlay(
                address = pickTargetAddress,
                isLoading = pickTargetGeo != null && pickTargetAddress.isNullOrBlank(),
                onConfirm = {
                    if (pickerIsDestination) {
                        viewModel.confirmDestination()
                        mapPickerMode = false
                        showRouteSheet = false
                    } else {
                        viewModel.confirmPickupOverrideFromPicker()
                        mapPickerMode = false
                        viewModel.resetPickerDraft()
                        openRouteSheet()
                    }
                },
                confirmButtonText = if (pickerIsDestination) {
                    stringResource(R.string.map_confirm_destination_button)
                } else {
                    stringResource(R.string.map_confirm_pickup_button)
                },
                isDestination = pickerIsDestination,
                onCancel = if (pickerIsDestination) {
                    {
                        mapPickerMode = false
                        viewModel.resetPickerDraft()
                        openRouteSheet()
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!mapPickerMode) {
            MapHomeTopHeader(
                avatarUrl = avatarUrl,
                profileInitials = profileInitials,
                onProfileClick = { showProfileSheet = true },
                onSearchClick = openRouteSheet,
                onNotificationsClick = { /* TODO: notifications */ },
                onWalletClick = onNavigateToWallet,
                walletAmountText = walletViewModel.walletAmountCompact(),
                isWalletLoading = walletLoading && wallet == null,
                showOfflineStatusBadge = false,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (!mapPickerMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .then(if (!showRouteSheet) Modifier.imePadding() else Modifier)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MapFloatingControlsRow(
                    showMapTypePicker = showMapTypePicker,
                    mapDisplayMode = mapDisplayMode,
                    onMapDisplayModeChange = { mapDisplayMode = it },
                    onToggleMapTypePicker = { showMapTypePicker = !showMapTypePicker },
                    onRecenterClick = {
                        currentLocation?.let { loc ->
                            mapViewRef.value?.camera?.lookAt(
                                loc,
                                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 16.0)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    compact = true,
                    anchorUnderStatusBar = false
                )
                // Équivalent Swift : DestinationConfirmedCard.swift — Request Ride = TODO
                if (confirmedDestination != null) {
                    RiderDestinationConfirmedBar(
                        pickupTitle = pickupOverrideLabel ?: currentAddress ?: stringResource(R.string.map_pickup_location),
                        destinationTitle = destinationLabel.orEmpty(),
                        fareEstimate = riderFareEstimate,
                        routeOptions = passengerRouteOptions,
                        selectedRouteIndex = selectedPassengerRouteIndex,
                        onSelectRoute = { idx -> viewModel.selectPassengerRoute(idx) },
                        onChangeDestination = {
                            viewModel.clearConfirmedDestination()
                            viewModel.clearAddressSearchResults()
                            viewModel.clearPickupSearchResults()
                            openRouteSheet()
                        },
                        onRequestRide = { viewModel.requestRide() },
                        onCancelRideRequest = { viewModel.cancelRequestedRide() },
                        onPickOffer = { offerId -> viewModel.pickRideOffer(offerId) },
                        onDismissOffer = { offerId -> viewModel.dismissIncomingOffer(offerId) },
                        requestInProgress = isRequestingRide,
                        requestError = rideRequestError,
                        lastRequestedRide = lastRequestedRide,
                        incomingOffers = incomingRideOffers,
                        isLoadingOffers = isLoadingRideOffers,
                        pickingOfferId = pickingOfferId,
                        matchedOffer = matchedRideOffer,
                        isRideMatched = isRideMatched,
                        scheduledRides = scheduledRides,
                        isLoadingScheduledRides = isLoadingScheduledRides,
                        scheduledRidesError = scheduledRidesError,
                        rideRating = rideRating,
                        isLoadingRideRating = isLoadingRideRating,
                        rideRatingError = rideRatingError,
                        isSubmittingRideRating = isSubmittingRideRating,
                        submitRideRatingError = submitRideRatingError,
                        driverRatingsStats = driverRatingsStats,
                        onSubmitRideRating = { rideId, score, comment ->
                            viewModel.submitRideRating(rideId, score, comment)
                        }
                    )
                }
                RiderBottomRouteEntryBar(
                    modifier = Modifier.fillMaxWidth(),
                    onOpenRouteSheet = openRouteSheet
                )
            }
        }

        if (showProfileSheet) {
            ProfileBottomSheet(
                sheetState    = sheetState,
                user          = user,
                onDismiss     = { showProfileSheet = false },
                onEditProfile = { showProfileSheet = false; onNavigateToEditProfile() },
                onColorSettings = { showProfileSheet = false; onNavigateToColorSettings() },
                onLogout = {
                    showProfileSheet = false
                    profileViewModel.logout(onFinished = onLogout)
                }
            )
        }

        if (showRouteSheet) {
            PassengerRouteSearchBottomSheet(
                viewModel = viewModel,
                mapViewRef = mapViewRef,
                routeSheetState = routeSheetState,
                fieldResetSession = routeSheetSession,
                initialOrigin = (pickupOverrideLabel ?: currentAddress).orEmpty(),
                initialDestination = destinationLabel.orEmpty(),
                onDismiss = {
                    showRouteSheet = false
                    viewModel.clearPickupSearchResults()
                    viewModel.clearAddressSearchResults()
                },
                onOpenPickupMapPicker = {
                    showRouteSheet = false
                    pickerIsDestination = false
                    viewModel.resetPickerDraft()
                    mapPickerMode = true
                },
                onOpenDestinationMapPicker = {
                    showRouteSheet = false
                    pickerIsDestination = true
                    viewModel.resetPickerDraft()
                    mapPickerMode = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerRouteSearchBottomSheet(
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

// ─────────────────────────────────────────────────────────────────────────────
// Composables privés
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PickerModeBottomBar(
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
private fun CenterPickupPinOverlay(primary: Color) {
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

@OptIn(FlowPreview::class)
@Composable
internal fun HereMapViewComposable(
    currentLocation: GeoCoordinates?,
    pickupPoint: GeoCoordinates? = null,
    confirmedDestination: GeoCoordinates?,
    passengerRouteGeometries: List<GeoPolyline> = emptyList(),
    passengerTrafficSpans: List<PassengerTrafficSpan> = emptyList(),
    selectedPassengerRouteIndex: Int = 0,
    sceneLoaded: MutableState<Boolean>,
    useDarkMap: Boolean,
    mapDisplayMode: AppMapDisplayMode = AppMapDisplayMode.NORMAL,
    profilePictureUri: String?,
    profileInitials: String,
    showBuiltInUserMarker: Boolean = true,
    useTaxiIconForUserMarker: Boolean = false,
    /** Clockwise from true north (0–360°). Applied when [useTaxiIconForUserMarker] is true. */
    userMarkerHeadingDegrees: Float? = null,
    onUserLocationScreenPointUpdated: ((Point2D?) -> Unit)? = null,
    destinationPinColor: Color = LocalAppColors.current.errorRed,
    onPickTargetUpdated: ((GeoCoordinates) -> Unit)?,
    mapViewRef: MutableState<HereMapView?>
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val composeScope = rememberCoroutineScope()

    var mapError by remember { mutableStateOf<String?>(null) }
    var scenePolylineEpoch by remember { mutableIntStateOf(0) }
    val mapViewDimensionsReady = remember { mutableStateOf(false) }
    val locationMarkerRef    = remember { mutableStateOf<HereMapMarker?>(null) }
    val pickupMarkerRef      = remember { mutableStateOf<HereMapMarker?>(null) }
    val destinationMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }
    val displayedMarkerLocationRef = remember { mutableStateOf<GeoCoordinates?>(null) }
    /** Unrotated taxi asset; heading applied via [rotateTaxiBitmap] + [HereMapMarker.setImage]. */
    var taxiBaseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val taxiSmoothHolder = remember { TaxiSmoothHolder() }
    val taxiRotCache = remember { TaxiRotationCache() }
    /**
     * Map camera bearing (deg, clockwise from north). HERE 2D markers are screen-aligned billboards;
     * taxi rotation must be (vehicle heading − this) so the car stays aligned with the road when the user rotates the map.
     */
    var mapCameraBearingDeg by remember { mutableStateOf(0f) }

    val appColorsMap = LocalAppColors.current
    val pickupPinImage: MapImage = remember(appColorsMap.primary) {
        MapImageFactory.fromBitmap(createPushPinBitmap(appColorsMap.primary.toArgb()))
    }
    val destinationPinImage: MapImage = remember(destinationPinColor) {
        MapImageFactory.fromBitmap(createPushPinBitmap(destinationPinColor.toArgb()))
    }
    val pickupPinAnchor = remember { Anchor2D(0.5, teardropPickupPinAnchorYNormalized()) }

    val mapView = remember {
        HereMapView(context).also {
            it.onCreate(null)
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    SideEffect { mapViewRef.value = mapView }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    val mapScheme = mapDisplayMode.toHereMapScheme(useDarkMap)

    // Charger la scène seulement après layout (évite hsdk-WatermarkProcessor sans dimensions).
    LaunchedEffect(mapViewDimensionsReady.value, mapScheme) {
        if (!mapViewDimensionsReady.value) return@LaunchedEffect
        sceneLoaded.value = false
        mapError = null
        mapView.mapScene.loadScene(mapScheme) { error ->
            if (error != null) { mapError = error.name; return@loadScene }
            runCatching {
                mapView.mapScene.enableFeatures(
                    mapOf(
                        MapFeatures.ROAD_EXIT_LABELS to MapFeatureModes.ROAD_EXIT_LABELS_ALL,
                        MapFeatures.TRAFFIC_FLOW to MapFeatureModes.TRAFFIC_FLOW_WITH_FREE_FLOW,
                        MapFeatures.TRAFFIC_INCIDENTS to MapFeatureModes.TRAFFIC_INCIDENTS_ALL
                    )
                )
            }
            mapView.camera.lookAt(
                GeoCoordinates(36.8065, 10.1815),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 14.0)
            )
            // HERE may invoke this callback off the main thread; hop to Main so Compose sees
            // sceneLoaded + epoch in sync (re-runs polyline LaunchedEffects if geometries arrived early).
            composeScope.launch(Dispatchers.Main.immediate) {
                sceneLoaded.value = true
                scenePolylineEpoch++
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, onPickTargetUpdated) {
        if (!sceneLoaded.value || onPickTargetUpdated == null) {
            onDispose { }
        } else {
            val cb = onPickTargetUpdated
            val listener = object : MapCameraListener {
                override fun onMapCameraUpdated(state: MapCamera.State) {
                    val width  = mapView.width
                    val height = mapView.height
                    if (width <= 0 || height <= 0) return
                    mapView.viewToGeoCoordinates(Point2D(width / 2.0, height / 2.0))?.let(cb)
                }
            }
            mapView.camera.addListener(listener)
            mapView.post {
                val width  = mapView.width
                val height = mapView.height
                if (width > 0 && height > 0) {
                    mapView.viewToGeoCoordinates(Point2D(width / 2.0, height / 2.0))?.let(cb)
                }
            }
            onDispose { mapView.camera.removeListener(listener) }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, useTaxiIconForUserMarker) {
        if (!sceneLoaded.value || !useTaxiIconForUserMarker) {
            mapCameraBearingDeg = 0f
            onDispose { }
        } else {
            fun readBearing(state: MapCamera.State) {
                val b = runCatching { state.orientationAtTarget.bearing }.getOrNull() ?: return
                mapCameraBearingDeg = b.toFloat()
            }
            val listener = object : MapCameraListener {
                override fun onMapCameraUpdated(state: MapCamera.State) {
                    readBearing(state)
                }
            }
            mapView.camera.addListener(listener)
            mapView.post {
                runCatching { readBearing(mapView.camera.state) }
            }
            onDispose { mapView.camera.removeListener(listener) }
        }
    }

    val latestDriverLocation = rememberUpdatedState(currentLocation)

    // Position: interpolation ~60fps (dead-reckoning style) instead of stepping + cancelling each GPS tick.
    LaunchedEffect(sceneLoaded.value, useTaxiIconForUserMarker) {
        if (!sceneLoaded.value || !useTaxiIconForUserMarker) return@LaunchedEffect
        while (isActive) {
            delay(TAXI_SMOOTH_FRAME_MS)
            val marker = locationMarkerRef.value ?: continue
            val tgt = latestDriverLocation.value ?: continue
            if (!taxiSmoothHolder.initialized) {
                taxiSmoothHolder.lat = tgt.latitude
                taxiSmoothHolder.lng = tgt.longitude
                taxiSmoothHolder.initialized = true
            } else {
                val dLat = tgt.latitude - taxiSmoothHolder.lat
                val dLng = tgt.longitude - taxiSmoothHolder.lng
                val jump = approxDeltaMeters(dLat, dLng, taxiSmoothHolder.lat)
                if (jump > TAXI_SNAP_JUMP_METERS) {
                    taxiSmoothHolder.lat = tgt.latitude
                    taxiSmoothHolder.lng = tgt.longitude
                } else {
                    taxiSmoothHolder.lat += dLat * TAXI_POSITION_LERP
                    taxiSmoothHolder.lng += dLng * TAXI_POSITION_LERP
                }
            }
            val g = GeoCoordinates(taxiSmoothHolder.lat, taxiSmoothHolder.lng)
            setMarkerCoordinates(marker, g)
            displayedMarkerLocationRef.value = g
        }
    }

    // Rotation: debounce + skip tiny changes — frequent setImage caused visible flicker (billboard + bitmap swap).
    LaunchedEffect(sceneLoaded.value, useTaxiIconForUserMarker) {
        if (!sceneLoaded.value || !useTaxiIconForUserMarker) return@LaunchedEffect
        snapshotFlow { mapCameraBearingDeg to userMarkerHeadingDegrees }
            .debounce(TAXI_ROTATION_DEBOUNCE_MS)
            .collectLatest { (cam, head) ->
                val marker = locationMarkerRef.value ?: return@collectLatest
                val base = taxiBaseBitmap ?: return@collectLatest
                val h = head ?: return@collectLatest
                val visualDeg = h - cam + TAXI_MARKER_ROTATION_OFFSET_DEG
                if (taxiRotCache.shouldSkip(visualDeg)) return@collectLatest
                setTaxiMarkerHeading(marker, base, h, cam)
                taxiRotCache.onApplied(visualDeg)
            }
    }

    LaunchedEffect(sceneLoaded.value, useTaxiIconForUserMarker) {
        if (!sceneLoaded.value || !useTaxiIconForUserMarker || taxiBaseBitmap != null) return@LaunchedEffect
        taxiBaseBitmap = withContext(Dispatchers.IO) { loadTaxiMarkerBitmap(context) }
    }

    LaunchedEffect(
        currentLocation,
        userMarkerHeadingDegrees,
        taxiBaseBitmap,
        sceneLoaded.value,
        profilePictureUri,
        profileInitials,
        appColorsMap.primary,
        showBuiltInUserMarker,
        useTaxiIconForUserMarker
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        val loc = currentLocation
        if (loc == null) {
            taxiSmoothHolder.initialized = false
            taxiRotCache.reset()
            locationMarkerRef.value?.let { runCatching { mapView.mapScene.removeMapMarker(it) } }
            locationMarkerRef.value = null
            displayedMarkerLocationRef.value = null
            onUserLocationScreenPointUpdated?.invoke(null)
            return@LaunchedEffect
        }
        onUserLocationScreenPointUpdated?.invoke(runCatching { mapView.geoToViewCoordinates(loc) }.getOrNull())
        if (!showBuiltInUserMarker) {
            taxiSmoothHolder.initialized = false
            taxiRotCache.reset()
            locationMarkerRef.value?.let { runCatching { mapView.mapScene.removeMapMarker(it) } }
            locationMarkerRef.value = null
            displayedMarkerLocationRef.value = null
            return@LaunchedEffect
        }
        if (useTaxiIconForUserMarker && locationMarkerRef.value != null) {
            return@LaunchedEffect
        }
        val primaryArgb = appColorsMap.primary.toArgb()
        if (useTaxiIconForUserMarker && taxiBaseBitmap == null) {
            taxiBaseBitmap = withContext(Dispatchers.IO) { loadTaxiMarkerBitmap(context) }
        }
        val bmp = withContext(Dispatchers.IO) {
            if (useTaxiIconForUserMarker) {
                val base = taxiBaseBitmap
                if (base != null) {
                    rotateTaxiBitmap(
                        base,
                        userMarkerHeadingDegrees ?: 0f,
                        mapCameraBearingDeg
                    )
                } else {
                    loadTaxiMarkerBitmap(context)
                        ?: createUserLocationMarkerBitmap(null, profileInitials, primaryArgb)
                }
            } else {
                val avatar = loadProfilePhotoBitmap(context, profilePictureUri)
                try {
                    createUserLocationMarkerBitmap(avatar, profileInitials, primaryArgb)
                } finally {
                    // `loadProfilePhotoBitmap` renvoie une copie (pas le bitmap du cache Coil).
                    avatar?.recycle()
                }
            }
        }
        withContext(Dispatchers.Main) {
            if (!sceneLoaded.value ||
                currentLocation?.latitude  != loc.latitude ||
                currentLocation?.longitude != loc.longitude
            ) {
                bmp.recycle()
                return@withContext
            }
            locationMarkerRef.value?.let { mapView.mapScene.removeMapMarker(it) }
            val markerAnchor = if (useTaxiIconForUserMarker) Anchor2D(0.5, 0.5) else Anchor2D(0.5, 1.0)
            val marker = HereMapMarker(loc, MapImageFactory.fromBitmap(bmp), markerAnchor)
            mapView.mapScene.addMapMarker(marker)
            locationMarkerRef.value = marker
            displayedMarkerLocationRef.value = loc
            if (useTaxiIconForUserMarker) {
                taxiSmoothHolder.initialized = true
                taxiSmoothHolder.lat = loc.latitude
                taxiSmoothHolder.lng = loc.longitude
                taxiRotCache.reset()
            }
        }
    }

    LaunchedEffect(confirmedDestination, sceneLoaded.value, destinationPinImage, pickupPinAnchor) {
        if (!sceneLoaded.value) return@LaunchedEffect
        destinationMarkerRef.value?.let { runCatching { mapView.mapScene.removeMapMarker(it) } }
        destinationMarkerRef.value = null
        val dest = confirmedDestination ?: return@LaunchedEffect
        runCatching {
            val m = HereMapMarker(dest, destinationPinImage, pickupPinAnchor)
            mapView.mapScene.addMapMarker(m)
            destinationMarkerRef.value = m
        }.onFailure {
            destinationMarkerRef.value = null
        }
    }

    LaunchedEffect(pickupPoint, sceneLoaded.value, pickupPinImage, pickupPinAnchor) {
        if (!sceneLoaded.value) return@LaunchedEffect
        pickupMarkerRef.value?.let { runCatching { mapView.mapScene.removeMapMarker(it) } }
        pickupMarkerRef.value = null
        val pickup = pickupPoint ?: return@LaunchedEffect
        runCatching {
            val m = HereMapMarker(pickup, pickupPinImage, pickupPinAnchor)
            mapView.mapScene.addMapMarker(m)
            pickupMarkerRef.value = m
        }.onFailure {
            pickupMarkerRef.value = null
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, currentLocation, onUserLocationScreenPointUpdated) {
        if (!sceneLoaded.value || onUserLocationScreenPointUpdated == null) {
            onDispose { }
        } else {
            val listener = object : MapCameraListener {
                override fun onMapCameraUpdated(state: MapCamera.State) {
                    val loc = currentLocation
                    if (loc == null) onUserLocationScreenPointUpdated(null)
                    else onUserLocationScreenPointUpdated(runCatching { mapView.geoToViewCoordinates(loc) }.getOrNull())
                }
            }
            mapView.camera.addListener(listener)
            mapView.post {
                val loc = currentLocation
                if (loc != null) {
                    onUserLocationScreenPointUpdated(runCatching { mapView.geoToViewCoordinates(loc) }.getOrNull())
                }
            }
            onDispose { mapView.camera.removeListener(listener) }
        }
    }

    val routePolylines = remember { mutableStateListOf<HereMapPolyline>() }
    LaunchedEffect(
        passengerRouteGeometries,
        selectedPassengerRouteIndex,
        sceneLoaded.value,
        scenePolylineEpoch,
        appColorsMap.primary
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        routePolylines.forEach { mapView.mapScene.removeMapPolyline(it) }
        routePolylines.clear()
        if (passengerRouteGeometries.isEmpty()) return@LaunchedEffect
        val density = mapView.resources.displayMetrics.density
        val primaryRgb = appColorsMap.primary
        val primaryLine = HereColor.valueOf(primaryRgb.red, primaryRgb.green, primaryRgb.blue, 0.95f)
        val primaryOutline = HereColor.valueOf(1f, 1f, 1f, 0.65f)
        val altLine = HereColor.valueOf(0.60f, 0.62f, 0.68f, 0.50f)
        val altOutline = HereColor.valueOf(1f, 1f, 1f, 0.15f)
        passengerRouteGeometries.forEachIndexed { index, geometry ->
            val isMain = index == selectedPassengerRouteIndex
            val lineWidthDp = if (isMain) 5.0 else 3.0
            val outlineWidthDp = if (isMain) 7.5 else 5.0
            val coreWidthPx = lineWidthDp * density
            val haloWidthPx = outlineWidthDp * density
            val coreWidth = MapMeasureDependentRenderSize(
                RenderSize.Unit.PIXELS,
                coreWidthPx
            )
            val haloWidth = MapMeasureDependentRenderSize(
                RenderSize.Unit.PIXELS,
                haloWidthPx
            )
            val fill = if (isMain) primaryLine else altLine
            val outline = if (isMain) primaryOutline else altOutline
            runCatching {
                val representation = HereMapPolyline.SolidRepresentation(
                    coreWidth,
                    fill,
                    haloWidth,
                    outline,
                    LineCap.ROUND
                )
                val poly = HereMapPolyline(geometry, representation).also {
                    it.drawOrder = if (isMain) 2 else 1
                }
                mapView.mapScene.addMapPolyline(poly)
                routePolylines.add(poly)
            }
        }
    }

    val trafficPolylines = remember { mutableStateListOf<HereMapPolyline>() }
    LaunchedEffect(passengerTrafficSpans, sceneLoaded.value, scenePolylineEpoch) {
        if (!sceneLoaded.value) return@LaunchedEffect
        trafficPolylines.forEach { mapView.mapScene.removeMapPolyline(it) }
        trafficPolylines.clear()
        if (passengerTrafficSpans.isEmpty()) return@LaunchedEffect
        val trafficDensity = mapView.resources.displayMetrics.density
        passengerTrafficSpans.forEach { span ->
            val color = when {
                span.jamFactor < 4.0 -> return@forEach
                span.jamFactor < 7.0 -> HereColor.valueOf(1f, 0.84f, 0.0f, 0.60f)
                span.jamFactor < 9.0 -> HereColor.valueOf(1f, 0.45f, 0.1f, 0.65f)
                else -> HereColor.valueOf(0.9f, 0.1f, 0.1f, 0.70f)
            }
            val trafficWidthPx = 3.0 * trafficDensity
            val width = MapMeasureDependentRenderSize(
                RenderSize.Unit.PIXELS,
                trafficWidthPx
            )
            runCatching {
                val representation = HereMapPolyline.SolidRepresentation(
                    width,
                    color,
                    LineCap.ROUND
                )
                val poly = HereMapPolyline(span.geometry, representation).also {
                    it.drawOrder = 3
                }
                mapView.mapScene.addMapPolyline(poly)
                trafficPolylines.add(poly)
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value) {
        onDispose {
            routePolylines.forEach { runCatching { mapView.mapScene.removeMapPolyline(it) } }
            routePolylines.clear()
            trafficPolylines.forEach { runCatching { mapView.mapScene.removeMapPolyline(it) } }
            trafficPolylines.clear()
        }
    }

    if (mapError != null) {
        MapLoadErrorContent(message = mapError!!)
    } else {
        AndroidView(
            factory = {
                mapView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                if (view.width > 0 && view.height > 0) {
                    if (!mapViewDimensionsReady.value) mapViewDimensionsReady.value = true
                } else {
                    view.post {
                        if (view.width > 0 && view.height > 0 && !mapViewDimensionsReady.value) {
                            mapViewDimensionsReady.value = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun UserLocationOverlay(
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
    val gapPx = with(density) { 10.dp.toPx() }
    // Keep the callout visually attached to the user marker.
    val calloutAnchorPx = with(density) { 58.dp.toPx() }
    val calloutWidthPx = with(density) { 288.dp.toPx() }
    val routeInteractive = showPickupCallout

    Box(modifier = modifier) {
        if (showPickupCallout) {
            Surface(
                modifier = Modifier
                    .width(288.dp)
                    .wrapContentHeight()
                    .offset {
                        IntOffset(
                            (position.x - calloutWidthPx / 2f).toInt(),
                            (position.y - markerHalfPx - gapPx - calloutAnchorPx).toInt()
                        )
                    }
                    .clickable { onOpenRouteSheet() },
                shape = RoundedCornerShape(14.dp),
                color = c.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.map_pickup_callout_title),
                            color = c.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.map_route_origin_placeholder),
                            color = c.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = c.textPrimary,
                        modifier = Modifier.size(22.dp)
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

@Composable
internal fun MapLoadErrorContent(message: String) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(c.darkSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Warning, null, tint = c.errorRed, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.map_load_error),
                color = c.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun formatRideScheduledAtIsoForDisplay(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(iso)
        val zdt = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", Locale.getDefault()).format(zdt)
    } catch (_: Exception) {
        iso
    }
}

/** Équivalent Swift : Presentation/Home/Componenets/DestinationConfirmedCard.swift */
@Composable
private fun RiderDestinationConfirmedBar(
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
    rideRating: com.dadadrive.domain.model.RideRating?,
    isLoadingRideRating: Boolean,
    rideRatingError: String?,
    isSubmittingRideRating: Boolean,
    submitRideRatingError: String?,
    driverRatingsStats: com.dadadrive.domain.model.DriverRatingsStats,
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
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
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
                                    String.format(Locale.US, "%d min · %.1f km", option.estimatedMinutes, option.distanceKm),
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
                                "%.1f km · %d min",
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
                                "★ %.1f (%d ratings)",
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
                            text = "Rate this ride",
                            color = c.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLoadingRideRating) {
                            Text("Loading rating...", color = c.textSecondary, fontSize = 12.sp)
                        } else if (rideRating != null) {
                            Text(
                                text = "Your rating: ${rideRating.score}/5${rideRating.comment?.let { " · $it" } ?: ""}",
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
                                    if (isSubmittingRideRating) "Submitting..." else "Submit rating",
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
                            text = "Scheduled rides",
                            color = c.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (isLoadingScheduledRides) {
                            Text(
                                text = "Loading...",
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else if (scheduledRides.isEmpty()) {
                            Text(
                                text = "No upcoming scheduled rides",
                                color = c.textSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            scheduledRides.take(3).forEachIndexed { idx, ride ->
                                val whenText = formatRideScheduledAtIsoForDisplay(ride.scheduledAt)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = whenText.ifBlank { "Scheduled" },
                                        color = c.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "From: ${ride.pickupAddress}",
                                        color = c.textSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "To: ${ride.dropoffAddress}",
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

@Composable
private fun PassengerOfferCard(
    offer: PassengerRideOffer,
    isAccepting: Boolean,
    onRefuse: () -> Unit,
    onAccept: () -> Unit
) {
    val c = LocalAppColors.current
    var localAcceptProgress by remember(offer.id) { mutableStateOf(0f) }
    var localRunning by remember(offer.id) { mutableStateOf(false) }
    val displayedProgress by animateFloatAsState(
        targetValue = localAcceptProgress,
        animationSpec = tween(durationMillis = 120),
        label = "accept_progress"
    )
    val acceptProgressColor = lerp(c.surfaceMuted, c.primary, displayedProgress.coerceIn(0f, 1f))
    val acceptEnabled = !isAccepting && !localRunning
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = c.surfaceMuted
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                text = String.format(Locale.US, "%.2f TND", offer.offeredFare),
                color = c.textPrimary,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = listOfNotNull(
                    offer.driverName,
                    offer.driverRating?.let { "★ ${String.format(Locale.US, "%.1f", it)}" },
                    offer.totalRides?.let { "$it rides" }
                ).joinToString(" "),
                color = c.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            offer.vehicleLabel?.let {
                Text(
                    text = it,
                    color = c.textSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onRefuse),
                    shape = RoundedCornerShape(10.dp),
                    color = c.surface
                ) {
                    Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.driver_refuse), color = c.textSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = acceptEnabled) {
                            if (localRunning) return@clickable
                            localRunning = true
                            localAcceptProgress = 0f
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = acceptProgressColor
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAccepting || localRunning) {
                                stringResource(R.string.map_accepting_with_progress, (displayedProgress * 100f).toInt())
                            } else {
                                stringResource(R.string.driver_accept)
                            },
                            color = c.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (localRunning && !isAccepting) {
                LaunchedEffect(offer.id, localRunning) {
                    val totalMs = 2200L
                    val stepMs = 100L
                    val steps = (totalMs / stepMs).toInt()
                    for (i in 1..steps) {
                        localAcceptProgress = i.toFloat() / steps.toFloat()
                        delay(stepMs)
                    }
                    onAccept()
                    localRunning = false
                    localAcceptProgress = 0f
                }
            }
        }
    }
}

/** Barre du bas : ouvre la feuille « Enter your route » (champs From / To + carte). */
@Composable
private fun RiderBottomRouteEntryBar(
    onOpenRouteSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier
            .padding(bottom = 20.dp)
            .clickable { onOpenRouteSheet() },
        shape = RoundedCornerShape(32.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = c.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.map_where_to),
                color = c.textSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileBottomSheet(
    sheetState: SheetState,
    user: com.dadadrive.domain.model.User?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onColorSettings: () -> Unit = {},
    onLogout: () -> Unit
) {
    val c = LocalAppColors.current
    val activity = LocalActivity.current
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val selectedLang by languageViewModel.selected.collectAsState()
    var languagePickerExpanded by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = c.surfaceElevated,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(c.dragHandle, CircleShape)
            )
        }
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = c.textHint)
            }
        }
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sheetAvatarUrl = user?.profilePictureUri
            val sheetInitials = run {
                val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
                when {
                    parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                    parts.size == 1 -> parts[0].take(2).uppercase()
                    else -> "?"
                }
            }
            Box(
                modifier         = Modifier.size(72.dp).clip(CircleShape).background(c.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                if (!sheetAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        sheetAvatarUrl, stringResource(R.string.cd_photo_profile),
                        Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(sheetInitials, color = c.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(user?.fullName ?: "", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(user?.phoneNumber ?: user?.email ?: "", color = c.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val primary   = c.primary
            val roleLabel = stringResource(
                if (user?.role == "driver") R.string.role_label_driver else R.string.role_label_passenger
            )
            Box(
                modifier = Modifier
                    .background(primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(roleLabel, color = primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(8.dp))
            ProfileMenuItem(Icons.Default.Edit, stringResource(R.string.menu_edit_profile), onClick = onEditProfile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { languagePickerExpanded = !languagePickerExpanded }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Language, stringResource(R.string.menu_language), tint = c.textPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.menu_language), color = c.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    "${selectedLang.flag} ${selectedLang.localizedDisplayName()}",
                    color = c.textHint,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (languagePickerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = c.textHint,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(
                visible = languagePickerExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    AppLanguage.menuChoices.forEachIndexed { index, lang ->
                        val isLast = index == AppLanguage.menuChoices.lastIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    languageViewModel.selectLanguage(lang)
                                    languagePickerExpanded = false
                                    activity?.recreate()
                                }
                                .background(
                                    if (selectedLang == lang) c.primary.copy(alpha = 0.08f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.flag, fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(lang.localizedDisplayName(), color = c.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            if (selectedLang == lang) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (!isLast) HorizontalDivider(color = c.dividerGrey, modifier = Modifier.padding(start = 40.dp))
                    }
                    HorizontalDivider(
                        color = c.dividerGrey,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    )
                    TextButton(
                        onClick = {
                            languageViewModel.followDeviceLanguage()
                            languagePickerExpanded = false
                            activity?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.menu_language_follow_device),
                            color = c.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.menu_appearance_colors), onClick = onColorSettings)
            ProfileMenuItem(Icons.Default.Search,  stringResource(R.string.menu_help_support),     onClick = {})
            ProfileMenuItem(Icons.Default.Info,      stringResource(R.string.menu_terms_of_service), onClick = {})
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(4.dp))
            ProfileMenuItem(Icons.Default.ExitToApp, stringResource(R.string.menu_log_out), tint = c.errorRed, onClick = onLogout)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_version_label), color = c.textTertiary, fontSize = 11.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

private suspend fun loadProfilePhotoBitmap(context: Context, url: String?): Bitmap? {
    if (url.isNullOrBlank()) return null
    return try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(512, 512)
            .allowHardware(false)
            .build()
        when (val result = context.imageLoader.execute(request)) {
            is SuccessResult -> {
                val src = result.drawable.toBitmap()
                val cfg = src.config ?: Bitmap.Config.ARGB_8888
                // Copie dédiée au marqueur : évite toute aliasing avec le cache Coil / AsyncImage.
                src.copy(cfg, false)
            }
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private suspend fun loadTaxiMarkerBitmap(context: Context): Bitmap? = withContext(Dispatchers.IO) {
    runCatching {
        context.assets.open("taxi-icon.png").use { stream ->
            val decoded = BitmapFactory.decodeStream(stream) ?: return@runCatching null
            val scaled = Bitmap.createScaledBitmap(decoded, 85, 85, true)
            if (scaled !== decoded) decoded.recycle()
            scaled
        }
    }.getOrNull()
}

private fun setMarkerCoordinates(marker: HereMapMarker, coordinates: GeoCoordinates) {
    runCatching {
        marker.javaClass
            .getMethod("setCoordinates", GeoCoordinates::class.java)
            .invoke(marker, coordinates)
    }
}

private const val TAXI_SMOOTH_FRAME_MS = 16L
private const val TAXI_POSITION_LERP = 0.20
private const val TAXI_SNAP_JUMP_METERS = 75.0
private const val TAXI_ROTATION_DEBOUNCE_MS = 45L

private class TaxiSmoothHolder {
    var initialized: Boolean = false
    var lat: Double = 0.0
    var lng: Double = 0.0
}

private class TaxiRotationCache {
    private var lastAppliedVisualDeg: Float = Float.NaN
    fun reset() {
        lastAppliedVisualDeg = Float.NaN
    }
    fun shouldSkip(visualDeg: Float): Boolean {
        if (lastAppliedVisualDeg.isNaN()) return false
        return absShortestAngleDiffDeg(lastAppliedVisualDeg, visualDeg) < 0.7f
    }
    fun onApplied(visualDeg: Float) {
        lastAppliedVisualDeg = visualDeg
    }
}

private fun absShortestAngleDiffDeg(a: Float, b: Float): Float {
    var d = (a - b) % 360f
    if (d < 0f) d += 360f
    if (d > 180f) d = 360f - d
    return kotlin.math.abs(d)
}

/** ~mètres entre deux deltas lat/lng (suffisant pour détecter un saut / reprise GPS). */
private fun approxDeltaMeters(dLat: Double, dLng: Double, midLat: Double): Double {
    val mLat = dLat * 111_000.0
    val mLng = dLng * 111_000.0 * cos(midLat * PI / 180.0)
    return hypot(mLat, mLng)
}

/** PNG taxi: front vers le haut = nord géographique. Ajuster seulement si l’asset est dessiné autrement. */
private const val TAXI_MARKER_ROTATION_OFFSET_DEG = 0f

/**
 * [vehicleHeadingNorthClockwiseDeg]: GPS cap. [mapCameraBearingNorthClockwiseDeg]: rotation actuelle de la carte (HERE).
 * Les marqueurs 2D sont souvent des billboards écran : la texture doit être tournée de (véhicule − carte).
 */
private fun rotateTaxiBitmap(
    base: Bitmap,
    vehicleHeadingNorthClockwiseDeg: Float,
    mapCameraBearingNorthClockwiseDeg: Float
): Bitmap {
    val deg = vehicleHeadingNorthClockwiseDeg - mapCameraBearingNorthClockwiseDeg +
        TAXI_MARKER_ROTATION_OFFSET_DEG
    val m = Matrix()
    m.postRotate(deg, base.width / 2f, base.height / 2f)
    return Bitmap.createBitmap(base, 0, 0, base.width, base.height, m, true)
}

/** HERE Explore MapMarker has no setRotation; rotate the bitmap and swap the image. */
private fun setTaxiMarkerHeading(
    marker: HereMapMarker,
    taxiBase: Bitmap,
    vehicleHeadingNorthClockwiseDeg: Float,
    mapCameraBearingNorthClockwiseDeg: Float
) {
    val rotated = rotateTaxiBitmap(taxiBase, vehicleHeadingNorthClockwiseDeg, mapCameraBearingNorthClockwiseDeg)
    marker.setImage(MapImageFactory.fromBitmap(rotated))
}

@Composable
internal fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val resolved = tint ?: LocalAppColors.current.textPrimary
    val chevron  = LocalAppColors.current.textTertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = resolved, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = resolved, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = chevron, modifier = Modifier.size(18.dp))
    }
}
