package com.dadadrive.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors

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
    val user                  by profileViewModel.user.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRouteSheet by remember { mutableStateOf(false) }
    var mapPickerMode  by remember { mutableStateOf(false) }
    val routeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var destinationFieldDraft by remember { mutableStateOf("") }

    LaunchedEffect(destinationLabel) {
        destinationFieldDraft = destinationLabel.orEmpty()
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
            val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
            val pulse1 by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.8f,
                animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
                label = "pulse1"
            )
            val pulse1Alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f, targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
                label = "pulse1alpha"
            )
            val pulse2 by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 2.2f,
                animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing, delayMillis = 800), RepeatMode.Restart),
                label = "pulse2"
            )
            val pulse2Alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f, targetValue = 0f,
                animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing, delayMillis = 800), RepeatMode.Restart),
                label = "pulse2alpha"
            )

            val green    = Color(0xFF80C000)
            val dotSize  = 46.dp
            val totalSize = 100.dp

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 8.dp)
                    .size(totalSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize + 8.dp)
                        .scale(pulse2)
                        .background(Color.Transparent, CircleShape)
                        .border(2.dp, green.copy(alpha = pulse2Alpha), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(dotSize + 8.dp)
                        .scale(pulse1)
                        .background(Color.Transparent, CircleShape)
                        .border(2.dp, green.copy(alpha = pulse1Alpha), CircleShape)
                )
                Box(modifier = Modifier.size(dotSize + 4.dp).background(green, CircleShape))
                Box(modifier = Modifier.size(dotSize + 2.dp).background(Color.White, CircleShape))
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(Color(0xFF262629))
                        .clickable { showProfileSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Photo de profil",
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
                        .padding(end = 22.dp, bottom = 22.dp)
                        .size(13.dp)
                        .background(green, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xCC5B5B5B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color(0xFF8BCF26), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "DadaDrive",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp, top = 10.dp)
                    .size(52.dp),
                shape = CircleShape,
                color = Color(0xCC5B5B5B)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
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
            BottomSearchBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding(),
                onWhereToClick = { showRouteSheet = true }
            )
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
                    profileViewModel.logout()
                    onLogout()
                }
            )
        }

        if (showRouteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRouteSheet = false },
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
                    originAddress = currentAddress,
                    destinationValue = destinationFieldDraft,
                    onDestinationValueChange = { v ->
                        destinationFieldDraft = v
                        viewModel.updateDestinationLabelInput(v)
                    },
                    onClose = { showRouteSheet = false },
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
private fun PickTargetAddressBubble(address: String?, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val label = address?.takeIf { it.isNotBlank() } ?: "…"
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = c.primary,
        shadowElevation = 8.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = c.onPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PickerModeBottomBar(
    modifier: Modifier = Modifier,
    canConfirm: Boolean,
    onTerminer: () -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onTerminer,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = c.primary,
                disabledContainerColor = c.primaryDisabled,
                contentColor = c.onPrimary,
                disabledContentColor = c.onPrimary.copy(alpha = 0.5f)
            )
        ) {
            Text("Terminer", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }
    }
}

@Composable
private fun RouteItinerarySheetContent(
    originAddress: String?,
    destinationValue: String,
    onDestinationValueChange: (String) -> Unit,
    onClose: () -> Unit,
    onOpenMapPicker: () -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Indiquer votre itinéraire",
                color = c.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = c.textLabel)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = originAddress ?: "Localisation en cours…",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("De", color = c.textSecondary, fontSize = 13.sp) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(c.primary, CircleShape)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = c.outlineLight,
                unfocusedBorderColor = c.border,
                focusedTextColor     = c.textPrimary,
                unfocusedTextColor   = c.textPrimary,
                disabledTextColor    = c.textPrimary,
                focusedContainerColor   = c.surfaceMuted,
                unfocusedContainerColor = c.surfaceMuted
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = destinationValue,
            onValueChange = onDestinationValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("À", color = c.textSecondary, fontSize = 13.sp) },
            placeholder = { Text("Où allez-vous ?", color = c.greyHint) },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = c.textSecondary, modifier = Modifier.size(22.dp))
            },
            trailingIcon = {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onOpenMapPicker() },
                    shape = RoundedCornerShape(8.dp),
                    color = c.surfaceElevated,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        MapPickerMiniIcon(tint = c.locationMarkerBlue)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            maxLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = c.primary,
                unfocusedBorderColor = c.border,
                focusedTextColor     = c.textPrimary,
                unfocusedTextColor   = c.textPrimary,
                focusedContainerColor   = c.surfaceElevated,
                unfocusedContainerColor = c.surfaceElevated
            )
        )
    }
}

@Composable
private fun MapPickerMiniIcon(tint: Color) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE8E8E8))
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = "Choisir sur la carte",
            tint = tint,
            modifier = Modifier
                .align(Alignment.Center)
                .size(18.dp)
        )
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
private fun HereMapViewComposable(
    currentLocation: GeoCoordinates?,
    confirmedDestination: GeoCoordinates?,
    sceneLoaded: MutableState<Boolean>,
    useDarkMap: Boolean,
    profilePictureUri: String?,
    profileInitials: String,
    onPickTargetUpdated: ((GeoCoordinates) -> Unit)?,
    mapViewRef: MutableState<HereMapView?>
) {
    val context       = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var mapError by remember { mutableStateOf<String?>(null) }
    val locationMarkerRef    = remember { mutableStateOf<HereMapMarker?>(null) }
    val destinationMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }

    val appColorsMap = LocalAppColors.current
    val destinationPinImage: MapImage = remember(appColorsMap.primary) {
        MapImageFactory.fromBitmap(createTeardropPickupLocationBitmap(appColorsMap.primary.toArgb()))
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

    val mapScheme = if (useDarkMap) MapScheme.LITE_NIGHT else MapScheme.LITE_DAY

    LaunchedEffect(mapView, mapScheme) {
        sceneLoaded.value = false
        mapError = null
        mapView.mapScene.loadScene(mapScheme) { error ->
            if (error != null) { mapError = error.name; return@loadScene }
            runCatching {
                mapView.mapScene.enableFeatures(
                    mapOf(MapFeatures.ROAD_EXIT_LABELS to MapFeatureModes.ROAD_EXIT_LABELS_ALL)
                )
            }
            // ✅ FIX : Centrer sur Tunis immédiatement après chargement de la scène
            // Évite le "globe" dézoomé en attendant le GPS
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
        appColorsMap.primary,
        appColorsMap.darkSurface
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        val loc = currentLocation
        if (loc == null) {
            locationMarkerRef.value?.let { mapView.mapScene.removeMapMarker(it) }
            locationMarkerRef.value = null
            return@LaunchedEffect
        }
        val primaryArgb     = appColorsMap.primary.toArgb()
        val placeholderArgb = appColorsMap.darkSurface.toArgb()
        val bmp = withContext(Dispatchers.IO) {
            val avatar = loadProfilePhotoBitmap(context, profilePictureUri)
            try {
                createUserLocationMarkerBitmap(avatar, profileInitials, primaryArgb, placeholderArgb)
            } finally {
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
            val marker = HereMapMarker(loc, MapImageFactory.fromBitmap(bmp), Anchor2D(0.5, 0.5))
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
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MapLoadErrorContent(message: String) {
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

@Composable
private fun BottomSearchBar(
    modifier: Modifier = Modifier,
    onWhereToClick: () -> Unit
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onWhereToClick() },
            shape           = RoundedCornerShape(32.dp),
            color           = c.surfaceElevated,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = c.textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Where to?", color = c.textSecondary, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = c.surfaceMuted, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, "Historique", tint = c.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickDestinationChip("Home",  Icons.Default.Home,  Modifier.weight(1f))
            QuickDestinationChip("Work",  Icons.Default.Place, Modifier.weight(1f))
            QuickDestinationChip("Saved", Icons.Default.Star,  Modifier.weight(1f))
        }
    }
}

@Composable
private fun QuickDestinationChip(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        modifier        = modifier.clickable { },
        shape           = RoundedCornerShape(32.dp),
        color           = c.surfaceElevated,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = c.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileBottomSheet(
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
                Icon(Icons.Default.Close, null, tint = c.textLabel)
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
            ProfileMenuItem(Icons.Default.Edit,      "Edit Profile",      onClick = onEditProfile)
            ProfileMenuItem(Icons.Default.Settings,  "Thème de couleurs", onClick = onColorSettings)
            ProfileMenuItem(Icons.Default.Search,    "Help & Support",    onClick = {})
            ProfileMenuItem(Icons.Default.Info,      "Terms of Service",  onClick = {})
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
            is SuccessResult -> result.drawable.toBitmap()
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ProfileMenuItem(
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