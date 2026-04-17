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
                address = pickTargetAddress.orEmpty(),
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

