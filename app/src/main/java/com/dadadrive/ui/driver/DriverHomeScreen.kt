// Équivalent Swift : Presentation/Driver/DriverHomeView.swift (+ feuilles AvailableRides / ActiveRide / toast)
package com.dadadrive.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
import com.dadadrive.ui.map.MapHomeTopHeader
import com.dadadrive.ui.map.ProfileBottomSheet
import com.dadadrive.ui.map.MapViewModel
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapView as HereMapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToColorSettings: () -> Unit,
    onLogout: () -> Unit,
    mapViewModel: MapViewModel = hiltViewModel(),
    driverViewModel: DriverViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val user by profileViewModel.user.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
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

    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    Box(Modifier.fillMaxSize()) {
        HereMapViewComposable(
            currentLocation = currentLocation,
            confirmedDestination = null,
            sceneLoaded = sceneLoaded,
            useDarkMap = useDarkMap,
            mapDisplayMode = mapDisplayMode,
            profilePictureUri = avatarUrl,
            profileInitials = initials,
            onPickTargetUpdated = null,
            mapViewRef = mapViewRef
        )

        MapHomeTopHeader(
            avatarUrl = avatarUrl,
            profileInitials = initials,
            onProfileClick = { showProfileSheet = true },
            onSearchClick = { /* TODO: recherche globale */ },
            onNotificationsClick = { /* TODO: centre notifications */ },
            showOfflineStatusBadge = !isOnline,
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
            modifier = Modifier.align(Alignment.TopEnd),
            compact = true,
            anchorUnderStatusBar = true,
            rowVerticalAlignment = Alignment.Top
        )

        DriverBottomCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            isOnline = isOnline,
            isToggling = isToggling,
            availableCount = availableRides.size,
            isLoadingRides = isLoadingRides,
            onToggleOnline = { driverViewModel.toggleOnlineStatus() },
            onOpenRides = { driverViewModel.setShowAvailableRides(true) }
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

        if (showProfileSheet) {
            ProfileBottomSheet(
                sheetState = sheetState,
                user = user,
                onDismiss = { showProfileSheet = false },
                onEditProfile = { showProfileSheet = false; onNavigateToEditProfile() },
                onColorSettings = { showProfileSheet = false; onNavigateToColorSettings() },
                onLogout = {
                    showProfileSheet = false
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
    }
}

@Composable
private fun DriverBottomCard(
    modifier: Modifier,
    isOnline: Boolean,
    isToggling: Boolean,
    availableCount: Int,
    isLoadingRides: Boolean,
    onToggleOnline: () -> Unit,
    onOpenRides: () -> Unit
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = c.surfaceElevated,
        shadowElevation = 12.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (isOnline) "You're Online" else "You're Offline",
                        color = c.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isOnline) "Looking for ride requests..." else "Go online to start accepting rides",
                        color = c.textSecondary,
                        fontSize = 13.sp
                    )
                }
                Button(
                    onClick = onToggleOnline,
                    enabled = !isToggling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOnline) c.surfaceMuted else c.primary,
                        contentColor = if (isOnline) c.textPrimary else c.onPrimary
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    if (isToggling) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = c.primary)
                    } else {
                        Text(if (isOnline) "Go Offline" else "Go Online", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            if (isOnline) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = c.dividerGrey)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = availableCount > 0, onClick = onOpenRides)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, null, tint = if (availableCount > 0) c.primary else c.textSecondary)
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (availableCount == 0) "No rides available yet" else "$availableCount ride(s) available",
                            color = if (availableCount > 0) c.primary else c.textSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (isLoadingRides) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
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
            Text("Available rides", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
            Text("Close", color = c.primary, modifier = Modifier.clickable { onClose() }, fontWeight = FontWeight.SemiBold)
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
                    Text("→ ${ride.dropoffAddress}", color = c.textSecondary, fontSize = 12.sp, maxLines = 2)
                    Text("${String.format("%.2f", ride.calculatedFare)} TND", color = c.primary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { onAccept(ride) }, colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                            Text("Accept", color = c.onPrimary)
                        }
                        Button(onClick = { onRefuse(ride) }, colors = ButtonDefaults.buttonColors(containerColor = c.surfaceMuted)) {
                            Text("Refuse", color = c.errorRed)
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
        Text("Active Ride", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        Text("Pickup: ${ride.pickupAddress}", color = c.textPrimary)
        Text("Dropoff: ${ride.dropoffAddress}", color = c.textPrimary)
        Text(
            "Fare: ${String.format("%.2f", ride.finalFare ?: ride.calculatedFare)} TND",
            color = c.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(16.dp))
        when (ride.status) {
            RideStatus.Accepted -> {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                    Text("Start Ride", color = c.onPrimary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Cancel Ride", color = c.errorRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth().clickable { onCancel() }.padding(12.dp))
            }
            RideStatus.InProgress -> {
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = c.primary)) {
                    Text("Complete Ride", color = c.onPrimary)
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
            Text("Ride completed", fontWeight = FontWeight.Bold, color = c.textPrimary)
            Text("Commission: ${result.commissionAmount}", color = c.textSecondary, fontSize = 13.sp)
            Text("New balance: ${result.newBalance}", color = c.primary, fontWeight = FontWeight.Bold)
            result.warning?.let { Text(it, color = c.errorRed, fontSize = 12.sp) }
        }
    }
}
