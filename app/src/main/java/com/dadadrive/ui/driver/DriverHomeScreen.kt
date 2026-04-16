// Équivalent Swift : Presentation/Driver/DriverHomeView.swift (+ feuilles AvailableRides / ActiveRide / toast)
package com.dadadrive.ui.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.util.Locale
import com.dadadrive.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.AvailableRide
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.ui.map.HereMapViewComposable
import com.dadadrive.ui.map.AppMapDisplayMode
import com.dadadrive.ui.map.MapFloatingControlsRow
import com.dadadrive.ui.map.MapViewModel
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors
import com.dadadrive.ui.wallet.WalletViewModel
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapMeasure
import kotlinx.coroutines.delay
import com.here.sdk.mapview.MapView as HereMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToColorSettings: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onLogout: () -> Unit,
    mapViewModel: MapViewModel = hiltViewModel(),
    driverViewModel: DriverViewModel = hiltViewModel(),
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by profileViewModel.user.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val locationHeadingDegrees by mapViewModel.locationHeadingDegrees.collectAsState()
    val isOnline by driverViewModel.isOnline.collectAsState()
    val isToggling by driverViewModel.isTogglingOnline.collectAsState()
    val availableRides by driverViewModel.availableRides.collectAsState()
    val isLoadingRides by driverViewModel.isLoadingRides.collectAsState()
    val showAvailable by driverViewModel.showAvailableRides.collectAsState()
    val activeRide by driverViewModel.activeRide.collectAsState()
    val showActive by driverViewModel.showActiveRide.collectAsState()
    val completeResult by driverViewModel.completeResult.collectAsState()
    val showCompleteToast by driverViewModel.showCompleteResult.collectAsState()
    val errorMsg by driverViewModel.errorMessage.collectAsState()
    val driverPreviewRoutes by mapViewModel.driverPreviewRouteGeometries.collectAsState()
    val wallet by walletViewModel.wallet.collectAsState()
    val walletLoading by walletViewModel.isLoading.collectAsState()

    var showDriverMenu by remember { mutableStateOf(false) }
    var showStatisticsSheet by remember { mutableStateOf(false) }
    var incomingRidePopup by remember { mutableStateOf<AvailableRide?>(null) }
    var lastAlertedRideId by remember { mutableStateOf<String?>(null) }
    var dismissedRideId by remember { mutableStateOf<String?>(null) }

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { r ->
        locationGranted = r[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            r[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) mapViewModel.startLocationUpdates()
    }
    LaunchedEffect(Unit) {
        if (locationGranted) mapViewModel.startLocationUpdates()
        else {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> {
                    profileViewModel.refresh()
                    if (locationGranted) mapViewModel.startLocationUpdates()
                }
                Lifecycle.Event.ON_PAUSE -> mapViewModel.stopLocationUpdates()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val mapViewRef = remember { mutableStateOf<HereMapView?>(null) }
    val sceneLoaded = remember { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }
    LaunchedEffect(currentLocation, sceneLoaded.value) {
        if (!hasFocused && sceneLoaded.value) {
            val target = currentLocation ?: GeoCoordinates(36.8065, 10.1815)
            mapViewRef.value?.camera?.lookAt(target, MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 15.0))
            if (currentLocation != null) hasFocused = true
        }
    }
    LaunchedEffect(currentLocation) {
        driverViewModel.updateDriverLocation(currentLocation)
    }
    LaunchedEffect(currentLocation, activeRide?.id) {
        mapViewModel.updateDriverPreviewRoutes(currentLocation, activeRide)
    }
    LaunchedEffect(isOnline, activeRide?.id, availableRides) {
        if (!isOnline || activeRide != null) {
            incomingRidePopup = null
            dismissedRideId = null
            return@LaunchedEffect
        }
        val firstRide = availableRides.firstOrNull()
        if (firstRide == null) {
            incomingRidePopup = null
            dismissedRideId = null
            return@LaunchedEffect
        }
        if (firstRide.id != dismissedRideId) {
            incomingRidePopup = firstRide
        }
        if (firstRide.id != lastAlertedRideId) {
            lastAlertedRideId = firstRide.id
            vibrateOnIncomingRide(context)
        }
    }

    val useDarkMap = isSystemInDarkTheme()
    var mapDisplayMode by remember { mutableStateOf(AppMapDisplayMode.NORMAL) }
    var showMapTypePicker by remember { mutableStateOf(false) }

    val avatarUrl = user?.profilePictureUri
    val initials = remember(user?.fullName) {
        val p = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
        when {
            p.size >= 2 -> "${p[0].first().uppercaseChar()}${p[1].first().uppercaseChar()}"
            p.size == 1 -> p[0].take(2).uppercase()
            else -> "?"
        }
    }
    val pickupPoint = activeRide?.let { GeoCoordinates(it.pickupLat, it.pickupLng) }
    val dropoffPoint = activeRide?.let { GeoCoordinates(it.dropoffLat, it.dropoffLng) }

    Box(Modifier.fillMaxSize()) {
        HereMapViewComposable(
            currentLocation = currentLocation,
            pickupPoint = pickupPoint,
            confirmedDestination = dropoffPoint,
            passengerRouteGeometries = driverPreviewRoutes,
            selectedPassengerRouteIndex = 0,
            sceneLoaded = sceneLoaded,
            useDarkMap = useDarkMap,
            mapDisplayMode = mapDisplayMode,
            profilePictureUri = avatarUrl,
            profileInitials = initials,
            useTaxiIconForUserMarker = true,
            userMarkerHeadingDegrees = locationHeadingDegrees,
            destinationPinColor = LocalAppColors.current.primary,
            onPickTargetUpdated = null,
            mapViewRef = mapViewRef
        )

        DriverTopOverlay(
            isOnline = isOnline,
            isToggling = isToggling,
            walletAmountText = walletViewModel.walletAmountCompact(),
            onToggleOnline = { driverViewModel.toggleOnlineStatus() },
            onMenuClick = { showDriverMenu = true },
            onWalletClick = onNavigateToWallet,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        MapFloatingControlsRow(
            showMapTypePicker = showMapTypePicker,
            mapDisplayMode = mapDisplayMode,
            onMapDisplayModeChange = { mapDisplayMode = it },
            onToggleMapTypePicker = { showMapTypePicker = !showMapTypePicker },
            onRecenterClick = {
                currentLocation?.let { loc ->
                    mapViewRef.value?.camera?.lookAt(loc, MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 16.0))
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            compact = true,
            anchorUnderStatusBar = false,
            rowVerticalAlignment = Alignment.CenterVertically
        )

        DriverHomeBottomPanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            isOnline = isOnline,
            availableCount = availableRides.size,
            onOpenRides = { driverViewModel.setShowAvailableRides(true) },
            onOpenWallet = onNavigateToWallet,
            onOpenStatistics = { showStatisticsSheet = true },
            onOpenSettings = onNavigateToColorSettings,
            earningsText = "0 DADA"
        )

        errorMsg?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 108.dp)
                    .padding(horizontal = 24.dp),
                color = LocalAppColors.current.errorRed.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(msg, color = LocalAppColors.current.onPrimary, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        if (showCompleteToast && completeResult != null) {
            CompleteResultOverlay(
                result = completeResult!!,
                onDismiss = { driverViewModel.dismissCompleteToast() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)
            )
        }

        if (showDriverMenu) {
            DriverSideMenuOverlay(
                userName = user?.fullName.orEmpty(),
                phone = user?.phoneNumber ?: user?.email.orEmpty(),
                walletAmountText = walletViewModel.walletAmountCompact(),
                ridesCount = activeRide?.let { 1 } ?: 0,
                avatarUrl = avatarUrl,
                initials = initials,
                onDismiss = { showDriverMenu = false },
                onMyRides = {
                    showDriverMenu = false
                    driverViewModel.setShowAvailableRides(true)
                },
                onWallet = {
                    showDriverMenu = false
                    onNavigateToWallet()
                },
                onStatistics = {
                    showDriverMenu = false
                    showStatisticsSheet = true
                },
                onEditProfile = {
                    showDriverMenu = false
                    onNavigateToEditProfile()
                },
                onLanguage = {},
                onHelp = {},
                onTerms = {},
                onLogout = {
                    showDriverMenu = false
                    profileViewModel.logout(onFinished = onLogout)
                }
            )
        }

        if (showAvailable) {
            ModalBottomSheet(
                onDismissRequest = { driverViewModel.setShowAvailableRides(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                AvailableRidesList(
                    rides = availableRides,
                    onAccept = { driverViewModel.acceptRide(it) },
                    onRefuse = { driverViewModel.refuseRide(it) },
                    onClose = { driverViewModel.setShowAvailableRides(false) }
                )
            }
        }

        if (showActive && activeRide != null) {
            ModalBottomSheet(
                onDismissRequest = { },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ActiveRideSheetContent(
                    ride = activeRide!!,
                    onStart = { driverViewModel.startRide() },
                    onComplete = { driverViewModel.completeRide() },
                    onCancel = { driverViewModel.cancelRide() }
                )
            }
        }
        incomingRidePopup?.let { ride ->
            IncomingRidePopup(
                ride = ride,
                onAccept = {
                    driverViewModel.acceptRide(ride)
                    dismissedRideId = null
                    incomingRidePopup = null
                },
                onRefuse = {
                    driverViewModel.refuseRide(ride)
                    dismissedRideId = ride.id
                    incomingRidePopup = null
                },
                onClose = {
                    dismissedRideId = ride.id
                    incomingRidePopup = null
                },
                onTimeout = {
                    dismissedRideId = ride.id
                    incomingRidePopup = null
                }
            )
        }

        if (showStatisticsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatisticsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                DriverStatisticsSheet(
                    totalRides = user?.let { activeRide?.let { 1 } ?: 0 } ?: 0,
                    walletBalance = wallet?.balance ?: 0.0,
                    onClose = { showStatisticsSheet = false }
                )
            }
        }
    }
}

private fun vibrateOnIncomingRide(context: Context) {
    runCatching {
        val pattern = longArrayOf(0L, 180L, 120L, 220L, 120L, 260L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = manager?.defaultVibrator
            if (vibrator?.hasVibrator() == true) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        }
    }
}

@Composable
private fun IncomingRidePopup(
    ride: AvailableRide,
    onAccept: () -> Unit,
    onRefuse: () -> Unit,
    onClose: () -> Unit,
    onTimeout: () -> Unit
) {
    val c = LocalAppColors.current
    val timeoutSeconds = 10
    var secondsLeft by remember(ride.id) { mutableStateOf(timeoutSeconds) }
    LaunchedEffect(ride.id) {
        while (secondsLeft > 0) {
            delay(1_000L)
            secondsLeft -= 1
        }
        onTimeout()
    }
    val progress = (secondsLeft.toFloat() / timeoutSeconds.toFloat()).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            color = c.surfaceElevated,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Nouvelle course",
                    color = c.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Pickup: ${ride.pickupAddress}",
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Dropoff: ${ride.dropoffAddress}",
                    color = c.textSecondary
                )
                val riderInfo = listOfNotNull(
                    ride.riderName?.takeIf { it.isNotBlank() },
                    ride.riderPhone?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (riderInfo.isNotBlank()) {
                    Text(
                        text = "Client: $riderInfo",
                        color = c.textSecondary,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.2f TND", ride.calculatedFare),
                    color = c.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = c.primary,
                    trackColor = c.surfaceMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onRefuse,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Refuser", color = c.errorRed, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        enabled = secondsLeft > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                    ) {
                        Text(
                            "Accepter (${secondsLeft}s)",
                            color = c.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverTopOverlay(
    isOnline: Boolean,
    isToggling: Boolean,
    walletAmountText: String,
    onToggleOnline: () -> Unit,
    onMenuClick: () -> Unit,
    onWalletClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = c.surfaceElevated,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onMenuClick),
                    shape = CircleShape,
                    color = c.surfaceMuted
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = c.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(c.primary, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                    Text("DadaDrive", color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    color = c.surfaceElevated.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(10.dp).background(c.primary, CircleShape))
                        Text(stringResource(R.string.driver_online_short), color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                        Surface(
                            modifier = Modifier
                                .size(width = 50.dp, height = 28.dp)
                                .clickable(enabled = !isToggling, onClick = onToggleOnline),
                            shape = RoundedCornerShape(999.dp),
                            color = if (isOnline) c.primary else c.surfaceMuted
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(3.dp),
                                contentAlignment = if (isOnline) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(Modifier.size(22.dp).background(Color.White, CircleShape))
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onWalletClick),
                    shape = RoundedCornerShape(999.dp),
                    color = c.surfaceElevated.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = c.primary, modifier = Modifier.size(16.dp))
                        Text("$walletAmountText DADA", color = c.textPrimary, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = c.textHint)
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverHomeBottomPanel(
    modifier: Modifier,
    isOnline: Boolean,
    availableCount: Int,
    onOpenRides: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit,
    earningsText: String
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = c.surfaceElevated,
        shadowElevation = 10.dp
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DriverQuickAction(Icons.Default.DirectionsCar, stringResource(R.string.driver_menu_my_rides), onClick = onOpenRides)
                DriverQuickAction(Icons.Default.AccountBalanceWallet, stringResource(R.string.wallet_title), onClick = onOpenWallet)
                DriverQuickAction(Icons.Default.BarChart, stringResource(R.string.driver_menu_statistics), onClick = onOpenStatistics)
                DriverQuickAction(Icons.Default.Settings, "Settings", onClick = onOpenSettings)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = c.surface
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.driver_todays_earnings), color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.driver_see_all), color = c.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(earningsText, color = c.primary, fontWeight = FontWeight.Black, fontSize = 34.sp)
                    Text(
                        text = if (isOnline) stringResource(R.string.driver_rides_count, availableCount) else stringResource(R.string.driver_offline),
                        color = c.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverQuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = c.surfaceMuted,
            modifier = Modifier.size(52.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = c.primary, modifier = Modifier.size(22.dp))
            }
        }
        Text(label, color = c.textSecondary, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun DriverSideMenuOverlay(
    userName: String,
    phone: String,
    walletAmountText: String,
    ridesCount: Int,
    avatarUrl: String?,
    initials: String,
    onDismiss: () -> Unit,
    onMyRides: () -> Unit,
    onWallet: () -> Unit,
    onStatistics: () -> Unit,
    onEditProfile: () -> Unit,
    onLanguage: () -> Unit,
    onHelp: () -> Unit,
    onTerms: () -> Unit,
    onLogout: () -> Unit
) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)).clickable(onClick = onDismiss)
        )
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.85f)
                .statusBarsPadding(),
            color = c.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(color = c.primary, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(shape = CircleShape, color = c.surface, modifier = Modifier.size(44.dp)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(model = avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(initials, color = c.textPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(phone, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            }
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(26.dp).clickable(onClick = onDismiss)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("💳 $walletAmountText DADA", color = Color.White, fontSize = 12.sp)
                            Text("🚗 $ridesCount rides", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                DriverMenuTile(Icons.Default.DirectionsCar, stringResource(R.string.driver_menu_my_rides), onMyRides)
                DriverMenuTile(Icons.Default.AccountBalanceWallet, stringResource(R.string.wallet_title), onWallet)
                DriverMenuTile(Icons.Default.BarChart, stringResource(R.string.driver_menu_statistics), onStatistics)
                Text(stringResource(R.string.driver_menu_account), color = c.textHint, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                DriverMenuTile(Icons.Default.Person, stringResource(R.string.menu_edit_profile), onEditProfile)
                DriverMenuTile(Icons.Default.Settings, stringResource(R.string.menu_language), onLanguage, trailing = "English")
                Text(stringResource(R.string.driver_menu_info), color = c.textHint, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                DriverMenuTile(Icons.Default.List, stringResource(R.string.menu_help_support), onHelp)
                DriverMenuTile(Icons.Default.List, stringResource(R.string.menu_terms_of_service), onTerms)
                DriverMenuTile(Icons.Default.PowerSettingsNew, stringResource(R.string.menu_log_out), onLogout, tint = c.errorRed, showChevron = false)
                Text(stringResource(R.string.app_version_label), color = c.textHint, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun DriverMenuTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: String? = null,
    tint: Color? = null,
    showChevron: Boolean = true
) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = c.surfaceMuted, modifier = Modifier.size(30.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint ?: c.textSecondary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.size(12.dp))
        Text(label, color = tint ?: c.textPrimary, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = c.textHint, fontSize = 12.sp)
        if (showChevron) Icon(Icons.Default.KeyboardArrowRight, null, tint = c.textHint)
    }
}

@Composable
private fun DriverStatisticsSheet(totalRides: Int, walletBalance: Double, onClose: () -> Unit) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.driver_stats_title), color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Icon(Icons.Default.Close, null, tint = c.textHint, modifier = Modifier.clickable(onClick = onClose))
        }
        Surface(shape = RoundedCornerShape(14.dp), color = c.surface) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Bolt, null, tint = c.primary, modifier = Modifier.size(14.dp))
                    Text(stringResource(R.string.driver_stats_session), color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                    Surface(shape = RoundedCornerShape(999.dp), color = c.primary.copy(alpha = 0.15f)) {
                        Text(stringResource(R.string.driver_stats_since, "13:09"), color = c.primary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriverStatCard("0.00", stringResource(R.string.driver_stats_earnings), "DADA", modifier = Modifier.weight(1f))
                    DriverStatCard("0", stringResource(R.string.driver_stats_rides), "", modifier = Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DriverStatCard("0.0", stringResource(R.string.driver_stats_km_driven), "km", modifier = Modifier.weight(1f))
                    DriverStatCard("34:06", stringResource(R.string.driver_stats_online_time), "", modifier = Modifier.weight(1f))
                }
                Surface(shape = RoundedCornerShape(10.dp), color = c.surfaceMuted) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.driver_stats_avg_fare), color = c.textSecondary)
                        Text("—", color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.driver_stats_commission_paid), color = c.textSecondary)
                        Text("0.00 DADA", color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Text(stringResource(R.string.driver_stats_all_time), color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DriverStatCard(totalRides.toString(), stringResource(R.string.driver_stats_total_rides), "", modifier = Modifier.weight(1f))
            DriverStatCard("—", stringResource(R.string.driver_stats_rating), "", modifier = Modifier.weight(1f))
            DriverStatCard(String.format(Locale.US, "%.0f", walletBalance), "Balance", "", modifier = Modifier.weight(1f))
        }
        Text(stringResource(R.string.driver_tips_title), color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        DriverTip(stringResource(R.string.driver_tip_peak))
        DriverTip(stringResource(R.string.driver_tip_busy))
        DriverTip(stringResource(R.string.driver_tip_longer))
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DriverStatCard(value: String, label: String, suffix: String, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = c.surface) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(value, color = c.textPrimary, fontWeight = FontWeight.Black, fontSize = 30.sp)
                if (suffix.isNotBlank()) Text(suffix, color = c.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text(label, color = c.textSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DriverTip(text: String) {
    val c = LocalAppColors.current
    Surface(shape = RoundedCornerShape(10.dp), color = c.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(6.dp).background(c.primary, CircleShape))
            Text(text, color = c.textPrimary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AvailableRidesList(
    rides: List<AvailableRide>,
    onAccept: (AvailableRide) -> Unit,
    onRefuse: (AvailableRide) -> Unit,
    onClose: () -> Unit
) {
    val c = LocalAppColors.current
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.driver_available_rides), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
            Text(stringResource(R.string.driver_close), color = c.primary, modifier = Modifier.clickable { onClose() }, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        rides.forEach { ride ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = c.surfaceMuted
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(ride.pickupAddress, color = c.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 2)
                    Text(stringResource(R.string.driver_dropoff_arrow, ride.dropoffAddress), color = c.textSecondary, fontSize = 12.sp, maxLines = 2)
                    val bookerContact = listOfNotNull(
                        ride.riderName?.takeIf { it.isNotBlank() },
                        ride.riderPhone?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ")
                    if (bookerContact.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.driver_booker_info, bookerContact),
                            color = c.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (ride.pickupForOther) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.driver_pickup_for_other_hint),
                            color = c.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val contact = listOfNotNull(
                            ride.passengerName?.takeIf { it.isNotBlank() },
                            ride.passengerPhone?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")
                        if (contact.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.driver_passenger_info, contact),
                                color = c.textPrimary,
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                    }
                    Text(stringResource(R.string.driver_fare_amount, ride.calculatedFare), color = c.primary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { onAccept(ride) }, colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                            Text(stringResource(R.string.driver_accept), color = c.onPrimary)
                        }
                        Button(onClick = { onRefuse(ride) }, colors = ButtonDefaults.buttonColors(containerColor = c.surfaceMuted)) {
                            Text(stringResource(R.string.driver_refuse), color = c.errorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveRideSheetContent(
    ride: ActiveRide,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val c = LocalAppColors.current
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text(stringResource(R.string.driver_active_ride), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        val bookerContact = listOfNotNull(
            ride.riderName?.takeIf { it.isNotBlank() },
            ride.riderPhone?.takeIf { it.isNotBlank() }
        ).joinToString(" · ")
        if (bookerContact.isNotEmpty()) {
            Text(
                text = stringResource(R.string.driver_booker_info, bookerContact),
                color = c.textSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(4.dp))
        }
        if (ride.pickupForOther) {
            val passengerContact = listOfNotNull(
                ride.passengerName?.takeIf { it.isNotBlank() },
                ride.passengerPhone?.takeIf { it.isNotBlank() }
            ).joinToString(" · ")
            if (passengerContact.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.driver_passenger_info, passengerContact),
                    color = c.textSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
            }
        }
        Text(stringResource(R.string.driver_pickup_line, ride.pickupAddress), color = c.textPrimary)
        Text(stringResource(R.string.driver_dropoff_line, ride.dropoffAddress), color = c.textPrimary)
        Text(
            stringResource(R.string.driver_fare_line, ride.finalFare ?: ride.calculatedFare),
            color = c.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(16.dp))
        when (ride.status) {
            RideStatus.Accepted -> {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                    Text(stringResource(R.string.driver_start_ride), color = c.onPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.driver_cancel_ride),
                    color = c.errorRed,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().clickable { onCancel() }.padding(12.dp)
                )
            }
            RideStatus.InProgress -> {
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                    Text(stringResource(R.string.driver_complete_ride), color = c.onPrimary)
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun CompleteResultOverlay(
    result: com.dadadrive.domain.model.CompleteRideResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(16.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.driver_ride_completed), fontWeight = FontWeight.Bold, color = c.textPrimary)
            Text(stringResource(R.string.driver_commission, result.commissionAmount), color = c.textSecondary, fontSize = 13.sp)
            Text(stringResource(R.string.driver_new_balance, result.newBalance), color = c.primary, fontWeight = FontWeight.Bold)
            result.warning?.let { Text(it, color = c.errorRed, fontSize = 12.sp) }
        }
    }
}
