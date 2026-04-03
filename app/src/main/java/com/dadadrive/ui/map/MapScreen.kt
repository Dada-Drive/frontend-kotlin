package com.dadadrive.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.dadadrive.R
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapFeatureModes
import com.here.sdk.mapview.MapFeatures
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker as HereMapMarker
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView as HereMapView
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToColorSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLocation       by viewModel.currentLocation.collectAsState()
    val currentAddress        by viewModel.currentAddress.collectAsState()
    val pickTargetGeo         by viewModel.pickTargetGeo.collectAsState()
    val pickTargetAddress     by viewModel.pickTargetAddress.collectAsState()
    val confirmedDestination  by viewModel.confirmedDestination.collectAsState()
    val destinationLabel      by viewModel.destinationLabel.collectAsState()
    val addressSearchResults  by viewModel.addressSearchResults.collectAsState()
    val addressSearchLoading  by viewModel.addressSearchLoading.collectAsState()
    val pickupSearchResults   by viewModel.pickupSearchResults.collectAsState()
    val pickupSearchLoading   by viewModel.pickupSearchLoading.collectAsState()
    val pickupOverrideLabel   by viewModel.pickupOverrideLabel.collectAsState()
    val riderFareEstimate     by viewModel.riderFareEstimate.collectAsState()
    val user                  by profileViewModel.user.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRouteSheet by remember { mutableStateOf(false) }
    var mapPickerMode  by remember { mutableStateOf(false) }
    val routeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var destinationFieldDraft by remember { mutableStateOf("") }
    var originFieldDraft by remember { mutableStateOf("") }

    LaunchedEffect(showRouteSheet) {
        if (showRouteSheet) {
            originFieldDraft = (pickupOverrideLabel ?: currentAddress).orEmpty()
            destinationFieldDraft = destinationLabel.orEmpty()
        }
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

        // Carte plein écran
        HereMapViewComposable(
            currentLocation      = currentLocation,
            confirmedDestination = if (mapPickerMode) null else confirmedDestination,
            sceneLoaded          = sceneLoaded,
            useDarkMap           = useDarkMap,
            mapDisplayMode       = mapDisplayMode,
            profilePictureUri    = user?.profilePictureUri,
            profileInitials      = profileInitials,
            onPickTargetUpdated  = if (mapPickerMode) viewModel::updatePickTarget else null,
            mapViewRef           = mapViewRef
        )

        if (mapPickerMode) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-132).dp)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    PickTargetAddressBubble(address = pickTargetAddress)
                }
                CenterPickupPinOverlay(primary = LocalAppColors.current.primary)
            }
        }

        if (!mapPickerMode) {
            MapHomeTopHeader(
                avatarUrl = avatarUrl,
                profileInitials = profileInitials,
                onProfileClick = { showProfileSheet = true },
                onSearchClick = { showRouteSheet = true },
                onNotificationsClick = { /* TODO: notifications */ },
                showOfflineStatusBadge = false,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (mapPickerMode) {
            IconButton(
                onClick = {
                    mapPickerMode = false
                    viewModel.resetPickerDraft()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Surface(shape = CircleShape, color = LocalAppColors.current.surfaceElevated) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = LocalAppColors.current.textPrimary,
                        modifier = Modifier.padding(10.dp).size(22.dp)
                    )
                }
            }
            PickerModeBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding(),
                canConfirm = pickTargetGeo != null,
                onTerminer = {
                    viewModel.confirmDestination()
                    mapPickerMode = false
                    viewModel.resetPickerDraft()
                    showRouteSheet = true
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
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
                        pickupTitle = pickupOverrideLabel ?: currentAddress ?: "Pickup location",
                        destinationTitle = destinationLabel.orEmpty(),
                        fareEstimate = riderFareEstimate,
                        onChangeDestination = {
                            viewModel.clearConfirmedDestination()
                            viewModel.clearAddressSearchResults()
                            viewModel.clearPickupSearchResults()
                            showRouteSheet = true
                        }
                    )
                }
                RiderBottomRouteEntryBar(
                    modifier = Modifier.fillMaxWidth(),
                    onOpenRouteSheet = { showRouteSheet = true }
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
            ModalBottomSheet(
                onDismissRequest = {
                    showRouteSheet = false
                    viewModel.clearPickupSearchResults()
                    viewModel.clearAddressSearchResults()
                },
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
                    destinationValue = destinationFieldDraft,
                    onDestinationChange = { text ->
                        destinationFieldDraft = text
                        viewModel.scheduleAddressSearch(text)
                    },
                    pickupResults = pickupSearchResults,
                    pickupSearchLoading = pickupSearchLoading,
                    destinationResults = addressSearchResults,
                    destinationSearchLoading = addressSearchLoading,
                    onPickupHit = { hit ->
                        viewModel.applyPickupOverride(hit)
                        originFieldDraft = hit.label
                    },
                    onDestinationHit = { hit ->
                        viewModel.applySearchDestination(hit)
                        destinationFieldDraft = hit.label
                        mapViewRef.value?.camera?.lookAt(
                            hit.coordinates,
                            MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 16.0)
                        )
                    },
                    onClose = {
                        showRouteSheet = false
                        viewModel.clearPickupSearchResults()
                        viewModel.clearAddressSearchResults()
                    },
                    onOpenMapPicker = {
                        showRouteSheet = false
                        viewModel.resetPickerDraft()
                        mapPickerMode = true
                    }
                )
            }
        }
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
                "Confirm pickup",
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

@Composable
internal fun HereMapViewComposable(
    currentLocation: GeoCoordinates?,
    confirmedDestination: GeoCoordinates?,
    sceneLoaded: MutableState<Boolean>,
    useDarkMap: Boolean,
    mapDisplayMode: AppMapDisplayMode = AppMapDisplayMode.NORMAL,
    profilePictureUri: String?,
    profileInitials: String,
    onPickTargetUpdated: ((GeoCoordinates) -> Unit)?,
    mapViewRef: MutableState<HereMapView?>
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapError by remember { mutableStateOf<String?>(null) }
    val mapViewDimensionsReady = remember { mutableStateOf(false) }
    val locationMarkerRef    = remember { mutableStateOf<HereMapMarker?>(null) }
    val destinationMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }

    val appColorsMap = LocalAppColors.current
    val destinationPinImage: MapImage = remember(appColorsMap.errorRed) {
        MapImageFactory.fromBitmap(createTeardropPickupLocationBitmap(appColorsMap.errorRed.toArgb()))
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
                    mapOf(MapFeatures.ROAD_EXIT_LABELS to MapFeatureModes.ROAD_EXIT_LABELS_ALL)
                )
            }
            mapView.camera.lookAt(
                GeoCoordinates(36.8065, 10.1815),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 14.0)
            )
            sceneLoaded.value = true
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

    LaunchedEffect(
        currentLocation,
        sceneLoaded.value,
        profilePictureUri,
        profileInitials,
        appColorsMap.primary
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        val loc = currentLocation
        if (loc == null) {
            locationMarkerRef.value?.let { mapView.mapScene.removeMapMarker(it) }
            locationMarkerRef.value = null
            return@LaunchedEffect
        }
        val primaryArgb = appColorsMap.primary.toArgb()
        val bmp = withContext(Dispatchers.IO) {
            val avatar = loadProfilePhotoBitmap(context, profilePictureUri)
            try {
                createUserLocationMarkerBitmap(avatar, profileInitials, primaryArgb)
            } finally {
                // `loadProfilePhotoBitmap` renvoie une copie (pas le bitmap du cache Coil).
                avatar?.recycle()
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
            val marker = HereMapMarker(loc, MapImageFactory.fromBitmap(bmp), Anchor2D(0.5, 1.0))
            mapView.mapScene.addMapMarker(marker)
            locationMarkerRef.value = marker
        }
    }

    LaunchedEffect(confirmedDestination, sceneLoaded.value, destinationPinImage, pickupPinAnchor) {
        if (!sceneLoaded.value) return@LaunchedEffect
        destinationMarkerRef.value?.let { mapView.mapScene.removeMapMarker(it) }
        destinationMarkerRef.value = null
        val dest = confirmedDestination ?: return@LaunchedEffect
        val m = HereMapMarker(dest, destinationPinImage, pickupPinAnchor)
        mapView.mapScene.addMapMarker(m)
        destinationMarkerRef.value = m
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
                fun markReadyIfSized() {
                    if (view.width > 0 && view.height > 0 && !mapViewDimensionsReady.value) {
                        mapViewDimensionsReady.value = true
                    }
                }
                markReadyIfSized()
                view.post { markReadyIfSized() }
            },
            modifier = Modifier.fillMaxSize()
        )
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
                "Erreur de chargement de la carte",
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

/** Équivalent Swift : Presentation/Home/Componenets/DestinationConfirmedCard.swift */
@Composable
private fun RiderDestinationConfirmedBar(
    pickupTitle: String,
    destinationTitle: String,
    fareEstimate: RiderFareEstimate?,
    onChangeDestination: () -> Unit
) {
    val c = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = c.surfaceElevated,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
            Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("From", color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(40.dp))
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
                    Text("To", color = c.textSecondary, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                    Text(destinationTitle, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                TextButton(onClick = onChangeDestination) {
                    Text("Change", color = c.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = c.dividerGrey)
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
                    text = "Estimation : même formule que le serveur (base + km + minutes, minimum appliqué).",
                    color = c.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 6.dp)
                )
            } else {
                Text(
                    text = "Position GPS en cours pour estimer distance et prix…",
                    color = c.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: ride request (Swift idem) */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("Request Ride", color = c.onPrimary, fontWeight = FontWeight.Bold)
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
                        sheetAvatarUrl, "Photo de profil",
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
            val roleLabel = if (user?.role == "driver") "Driver" else "Passenger"
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
            ProfileMenuItem(Icons.Default.Edit,    stringResource(R.string.menu_edit_profile),     onClick = onEditProfile)
            ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.menu_appearance_colors), onClick = onColorSettings)
            ProfileMenuItem(Icons.Default.Search,  stringResource(R.string.menu_help_support),     onClick = {})
            ProfileMenuItem(Icons.Default.Info,      stringResource(R.string.menu_terms_of_service), onClick = {})
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(4.dp))
            ProfileMenuItem(Icons.Default.ExitToApp, "Log Out", tint = c.errorRed, onClick = onLogout)
            Spacer(Modifier.height(12.dp))
            Text("DadaDrive v1.0", color = c.textTertiary, fontSize = 11.sp)
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
