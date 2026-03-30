package com.dadadrive.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.view.ViewGroup
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.LongPressListener
import com.here.sdk.gestures.TapListener
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
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// MapScreen
// ─────────────────────────────────────────────────────────────────────────────

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
    val currentLocation  by viewModel.currentLocation.collectAsState()
    val locationAccuracy by viewModel.locationAccuracy.collectAsState()
    val currentAddress   by viewModel.currentAddress.collectAsState()
    val isTracking       by viewModel.isTracking.collectAsState()
    val markers          by viewModel.markers.collectAsState()
    val tappedPoint      by viewModel.tappedPoint.collectAsState()
    val user             by profileViewModel.user.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Permissions ──────────────────────────────────────────────────────────

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

    // Demande les permissions au premier affichage
    LaunchedEffect(Unit) {
        if (locationPermissionGranted) {
            viewModel.startLocationUpdates()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Arrête le tracking GPS quand l'écran est quitté, rafraîchit le profil au retour
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

    // ── Carte ─────────────────────────────────────────────────────────────────

    val mapViewRef    = remember { mutableStateOf<HereMapView?>(null) }
    val sceneLoaded   = remember { mutableStateOf(false) }
    // Premier centrage automatique sur la position GPS (attend que la scène ET la position soient prêtes)
    var hasInitiallyFocused by remember { mutableStateOf(false) }

    LaunchedEffect(currentLocation, sceneLoaded.value) {
        if (!hasInitiallyFocused && sceneLoaded.value && currentLocation != null) {
            mapViewRef.value?.camera?.lookAt(
                currentLocation!!,
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 15.0)
            )
            hasInitiallyFocused = true
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    // systemBarsPadding + imePadding : zone de layout stable (largeur/hauteur > 0) pour la MapView
    // et watermark HERE ; les overlays sont dans cette zone sous encoches / barres système.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {

        // Carte HERE : LITE_DAY / LITE_NIGHT selon le mode système (cohérent avec la barre d’état)
        val useDarkMap = isSystemInDarkTheme()
        HereMapViewComposable(
            markers          = markers,
            currentLocation  = currentLocation,
            locationAccuracy = locationAccuracy,
            sceneLoaded      = sceneLoaded,
            useDarkMap       = useDarkMap,
            onMapTapped      = { viewModel.onMapTapped(it) },
            onLongPress      = { point ->
                viewModel.addMarker(
                    position = point,
                    title    = String.format(Locale.US, "%.4f, %.4f", point.latitude, point.longitude)
                )
            },
            mapViewRef = mapViewRef
        )

        // ── Avatar profil — haut gauche ───────────────────────────────────────
        val avatarUrl = user?.profilePictureUri
        val initials  = remember(user?.fullName) {
            val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
            when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "?"
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(LocalAppColors.current.surfaceElevated)
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
                Text(initials, color = LocalAppColors.current.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // ── Indicateur GPS actif — haut droit ─────────────────────────────────
        if (isTracking) {
            GpsActiveIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 8.dp)
            )
        }

        // ── Barre de recherche + adresse — bas ───────────────────────────────
        BottomSearchBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentAddress   = currentAddress,
            locationAccuracy = locationAccuracy
        )

        // ── Card coordonnées tapées (animée) ──────────────────────────────────
        AnimatedVisibility(
            visible  = tappedPoint != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 108.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit  = slideOutVertically { it } + fadeOut()
        ) {
            tappedPoint?.let { point ->
                TappedPointCard(
                    point     = point,
                    onDismiss = { viewModel.dismissTappedPoint() },
                    onAddMarker = {
                        viewModel.addMarker(
                            position = point,
                            title    = String.format(Locale.US, "%.4f, %.4f", point.latitude, point.longitude)
                        )
                        viewModel.dismissTappedPoint()
                    }
                )
            }
        }

        // ── Profile bottom sheet ───────────────────────────────────────────────
        if (showProfileSheet) {
            ProfileBottomSheet(
                sheetState = sheetState,
                user       = user,
                onDismiss  = { showProfileSheet = false },
                onEditProfile = { showProfileSheet = false; onNavigateToEditProfile() },
                onColorSettings = { showProfileSheet = false; onNavigateToColorSettings() },
                onLogout = {
                    showProfileSheet = false
                    profileViewModel.logout()
                    onLogout()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HereMapViewComposable — carte + marqueur position bleu
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Composable HERE Maps avec :
 * - Marqueur bleu à la position GPS de l'utilisateur
 * - Marqueurs personnalisés (DadaMarker) placés par l'utilisateur
 * - Gestures : tap (coordonnées) + appui long (épingle)
 */
@Composable
private fun HereMapViewComposable(
    markers: List<DadaMarker>,
    currentLocation: GeoCoordinates?,
    locationAccuracy: Float?,
    sceneLoaded: MutableState<Boolean>,
    useDarkMap: Boolean,
    onMapTapped: (GeoCoordinates) -> Unit,
    onLongPress: (GeoCoordinates) -> Unit,
    mapViewRef: MutableState<HereMapView?>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current

    var mapError by remember { mutableStateOf<String?>(null) }

    // Cache des marqueurs utilisateur HERE
    val hereUserMarkers = remember { mutableListOf<HereMapMarker>() }
    // Marqueur bleu de position actuelle (un seul à la fois)
    val locationMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }

    // ── Images des marqueurs (créées une seule fois) ───────────────────────────

    // Marqueur bleu — position utilisateur (cercle bleu avec contour blanc)
    val appColorsMap = LocalAppColors.current
    val locationMarkerImage: MapImage = remember(
        appColorsMap.locationMarkerBlue,
        appColorsMap.locationCirclePrecision,
        appColorsMap.onPrimary
    ) {
        MapImageFactory.fromBitmap(
            createLocationMarkerBitmap(
                haloArgb = appColorsMap.locationCirclePrecision.toArgb(),
                coreArgb = appColorsMap.locationMarkerBlue.toArgb(),
                ringArgb = appColorsMap.onPrimary.toArgb()
            )
        )
    }
    // Pins utilisateur — couleur vive + lisible sur fond clair ou sombre
    val pinMarkerImage: MapImage = remember(appColorsMap.onPrimary) {
        MapImageFactory.fromBitmap(
            createUserPinMarkerBitmap(ringArgb = appColorsMap.onPrimary.toArgb())
        )
    }

    // ── MapView ────────────────────────────────────────────────────────────────

    val mapView = remember {
        HereMapView(context).also { it.onCreate(null) }
    }
    SideEffect { mapViewRef.value = mapView }

    // Après rotation / changement de taille : forcer un nouveau layout (watermark HERE, surface).
    LaunchedEffect(configuration.screenWidthDp, configuration.screenHeightDp, configuration.orientation) {
        mapView.post {
            mapView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mapView.requestLayout()
        }
    }

    // Cycle de vie
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                // onDestroy : uniquement dans onDispose pour éviter un double appel (HERE / rendu).
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

    // Thème de carte HERE aligné clair/sombre + rechargement si le mode système change
    LaunchedEffect(mapView, mapScheme) {
        sceneLoaded.value = false
        mapError = null
        mapView.mapScene.loadScene(mapScheme) { error ->
            if (error != null) {
                mapError = error.name
                return@loadScene
            }
            runCatching {
                mapView.mapScene.enableFeatures(
                    mapOf(
                        MapFeatures.EXTRUDED_BUILDINGS to MapFeatureModes.EXTRUDED_BUILDINGS_ALL,
                        MapFeatures.BUILDING_FOOTPRINTS to MapFeatureModes.BUILDING_FOOTPRINTS_ALL,
                        MapFeatures.SHADOWS to MapFeatureModes.SHADOWS_ALL,
                        MapFeatures.ROAD_EXIT_LABELS to MapFeatureModes.ROAD_EXIT_LABELS_ALL,
                        MapFeatures.AMBIENT_OCCLUSION to MapFeatureModes.AMBIENT_OCCLUSION_ALL
                    )
                )
            }
            sceneLoaded.value = true
            mapView.gestures.tapListener = TapListener { origin ->
                mapView.viewToGeoCoordinates(origin)?.let { onMapTapped(it) }
            }
            mapView.gestures.longPressListener = LongPressListener { state, origin ->
                if (state == GestureState.BEGIN) {
                    mapView.viewToGeoCoordinates(origin)?.let { onLongPress(it) }
                }
            }
        }
    }

    // Mise à jour du marqueur bleu quand la position GPS ou la scène change
    LaunchedEffect(currentLocation, sceneLoaded.value) {
        if (!sceneLoaded.value) return@LaunchedEffect

        // Retirer l'ancien marqueur de position
        locationMarkerRef.value?.let { mapView.mapScene.removeMapMarker(it) }

        // Placer le nouveau marqueur bleu automatiquement
        currentLocation?.let { loc ->
            val marker = HereMapMarker(loc, locationMarkerImage)
            mapView.mapScene.addMapMarker(marker)
            locationMarkerRef.value = marker
        }
    }

    // Mise à jour des marqueurs utilisateur
    LaunchedEffect(markers, sceneLoaded.value) {
        if (!sceneLoaded.value) return@LaunchedEffect
        hereUserMarkers.forEach { mapView.mapScene.removeMapMarker(it) }
        hereUserMarkers.clear()
        markers.forEach { dadaMarker ->
            val m = HereMapMarker(dadaMarker.position, pinMarkerImage)
            mapView.mapScene.addMapMarker(m)
            hereUserMarkers.add(m)
        }
    }

    if (mapError != null) {
        MapLoadErrorContent(message = mapError!!)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = {
                    mapView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bitmap du marqueur bleu (créé programmatiquement)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Couleurs issues de [AppColorScheme] (localisation / marqueur).
 */
private fun createLocationMarkerBitmap(
    haloArgb: Int,
    coreArgb: Int,
    ringArgb: Int
): Bitmap {
    val size = 72
    val cx = size / 2f
    val cy = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val haloStrong = boostArgbAlpha(haloArgb, 1.35f)

    // Anneau précision (plus opaque pour mieux se détacher du fond carte)
    canvas.drawCircle(cx, cy, cx - 1f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = haloStrong
        style = Paint.Style.FILL
    })

    // Bande blanche épaisse entre halo et cœur bleu
    canvas.drawCircle(cx, cy, 29f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.FILL
    })

    canvas.drawCircle(cx, cy, 20f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = coreArgb
        style = Paint.Style.FILL
    })

    // Contour du disque central (contraste supplémentaire)
    canvas.drawCircle(cx, cy, 20f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.STROKE
        strokeWidth = 4f
    })

    canvas.drawCircle(cx, cy, 9f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.FILL
    })

    return bitmap
}

/** Pin placé par l'utilisateur : orange vif + anneau blanc épais. */
private fun createUserPinMarkerBitmap(
    coreArgb: Int = 0xFFE65100.toInt(),
    ringArgb: Int
): Bitmap {
    val size = 64
    val cx = size / 2f
    val cy = size / 2f
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawCircle(cx, cy, cx - 1f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x40000000 or (coreArgb and 0x00FFFFFF)
        style = Paint.Style.FILL
    })
    canvas.drawCircle(cx, cy, 22f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.FILL
    })
    canvas.drawCircle(cx, cy, 16f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = coreArgb
        style = Paint.Style.FILL
    })
    canvas.drawCircle(cx, cy, 16f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
    })
    canvas.drawCircle(cx, cy, 6f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ringArgb
        style = Paint.Style.FILL
    })
    return bitmap
}

private fun boostArgbAlpha(argb: Int, factor: Float): Int {
    val a = ((argb ushr 24 and 0xFF) * factor).toInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
}

// ─────────────────────────────────────────────────────────────────────────────
// Composants UI
// ─────────────────────────────────────────────────────────────────────────────

/** Pastille animée indiquant que le suivi GPS est actif. */
@Composable
private fun GpsActiveIndicator(modifier: Modifier = Modifier) {
    val primary = LocalAppColors.current.primary
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gps_scale"
    )
    Box(
        modifier = modifier
            .size(36.dp)
            .scale(scale)
            .background(primary.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "GPS actif",
            tint = primary,
            modifier = Modifier.size(18.dp)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Warning, null, tint = c.errorRed, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Erreur de chargement de la carte", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(message, color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}


@Composable
private fun TappedPointCard(
    point: GeoCoordinates,
    onDismiss: () -> Unit,
    onAddMarker: () -> Unit
) {
    val c = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = c.surfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Coordonnées sélectionnées", color = c.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = c.textLabel)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Lat : ${String.format(Locale.US, "%.6f", point.latitude)}", color = c.textSecondary, fontSize = 13.sp)
            Text("Lng : ${String.format(Locale.US, "%.6f", point.longitude)}", color = c.textSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAddMarker,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LocalAppColors.current.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Épingler ce lieu", color = c.onPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Barre de recherche + adresse
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomSearchBar(
    modifier: Modifier = Modifier,
    currentAddress: String? = null,
    locationAccuracy: Float? = null
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Carte adresse — affichée quand une position GPS est disponible
        if (currentAddress != null) {
            AddressCard(address = currentAddress, accuracy = locationAccuracy)
        }

        // Barre de recherche "Where to?"
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = c.surfaceElevated,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
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

        // Raccourcis rapides
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickDestinationChip("Home",  Icons.Default.Home,  Modifier.weight(1f))
            QuickDestinationChip("Work",  Icons.Default.Place,  Modifier.weight(1f))
            QuickDestinationChip("Saved", Icons.Default.Star,  Modifier.weight(1f))
        }
    }
}

/**
 * Carte compacte affichant l'adresse actuelle et la précision GPS.
 */
@Composable
private fun AddressCard(address: String, accuracy: Float?) {
    val c = LocalAppColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = c.surfaceOverlaySemi,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = c.locationMarkerBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address,
                    color = c.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (accuracy != null) {
                    Text(
                        text = "Précision : ±${accuracy.toInt()} m",
                        color = c.greyHint,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickDestinationChip(label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(32.dp),
        color = c.surfaceElevated,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, label, tint = c.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

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
        sheetState = sheetState,
        containerColor = c.surfaceElevated,
        dragHandle = {
            Box(
                Modifier.padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(c.dragHandle, CircleShape)
            )
        }
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)) {
                Icon(Icons.Default.Close, null, tint = c.textLabel)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sheetAvatarUrl = user?.profilePictureUri
            val sheetInitials  = run {
                val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
                when {
                    parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                    parts.size == 1 -> parts[0].take(2).uppercase()
                    else -> "?"
                }
            }
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(c.surfaceMuted), contentAlignment = Alignment.Center) {
                if (!sheetAvatarUrl.isNullOrBlank()) {
                    AsyncImage(sheetAvatarUrl, "Photo de profil", Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Text(sheetInitials, color = c.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(user?.fullName ?: "", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(user?.phoneNumber ?: user?.email ?: "", color = c.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val primary = c.primary
            val roleLabel = if (user?.role == "driver") "Driver" else "Passenger"
            Box(modifier = Modifier.background(primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(roleLabel, color = primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(8.dp))
            ProfileMenuItem(Icons.Default.Edit, "Edit Profile", onClick = onEditProfile)
            ProfileMenuItem(Icons.Default.Settings, "Thème de couleurs", onClick = onColorSettings)
            ProfileMenuItem(Icons.Default.Search, "Help & Support", onClick = {})
            ProfileMenuItem(Icons.Default.Info, "Terms of Service", onClick = {})
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

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val resolved = tint ?: LocalAppColors.current.textPrimary
    val chevron = LocalAppColors.current.textTertiary
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = resolved, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = resolved, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = chevron, modifier = Modifier.size(18.dp))
    }
}
