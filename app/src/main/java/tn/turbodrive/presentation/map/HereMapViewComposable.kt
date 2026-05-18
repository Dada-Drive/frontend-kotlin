package tn.turbodrive.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.GeoBox
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoOrientationUpdate
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Point2D
import com.here.sdk.gestures.GestureState
import com.here.sdk.gestures.PinchRotateListener
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapFeatureModes
import com.here.sdk.mapview.MapFeatures
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.RenderSize
import com.turbodrive.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.core.theme.MapColorTokens
import tn.turbodrive.domain.models.NearbyTaxi
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import com.here.sdk.core.Color as HereColor
import com.here.sdk.mapview.MapMarker as HereMapMarker
import com.here.sdk.mapview.MapPolyline as HereMapPolyline
import com.here.sdk.mapview.MapView as HereMapView

@OptIn(FlowPreview::class)
@Composable
internal fun HereMapViewComposable(
    currentLocation: GeoCoordinates?,
    nearbyTaxis: List<NearbyTaxi> = emptyList(),
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
    /**
     * Bolt/inDrive-style navigation follow: when true, the camera continuously tracks
     * the interpolated taxi position with `bearing = heading` (map rotates so the
     * vehicle points up on screen) and a slight tilt for a 3D driving feel. Enabled
     * only when [useTaxiIconForUserMarker] is also true.
     */
    navigationFollow: Boolean = false,
    onUserLocationScreenPointUpdated: ((Point2D?) -> Unit)? = null,
    destinationPinColor: Color = LocalAppColors.current.errorRed,
    mainRouteColor: Color = LocalAppColors.current.primary,
    /**
     * When true, [passengerRouteGeometries] are sequential legs of one trip (e.g. driver→pickup
     * then pickup→destination), not alternative route choices. Index 0 uses [mainRouteColor],
     * index 1 uses [secondLegRouteColor]; both use the primary line width.
     */
    sequentialRouteLegs: Boolean = false,
    secondLegRouteColor: Color = MapColorTokens.routeSecondLeg,
    /**
     * Rider-entered intermediate stops. Each entry includes its on-route validity; invalid
     * stops render as a yellow pin with a red forbidden overlay (same base design as the
     * pickup/destination teardrop pins for visual consistency).
     */
    intermediateStops: List<IntermediateStopMarker> = emptyList(),
    onPickTargetUpdated: ((GeoCoordinates) -> Unit)?,
    mapViewRef: MutableState<HereMapView?>,
    fitRouteToBoundsRequestId: Int = 0,
    /** Fired once when the user starts a pinch / rotate on the map (typical zoom gesture). */
    onPinchRotateBegin: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val composeScope = rememberCoroutineScope()

    var mapError by remember { mutableStateOf<String?>(null) }
    var scenePolylineEpoch by remember { mutableIntStateOf(0) }
    var lastAppliedFitRouteRequestId by remember { mutableIntStateOf(0) }
    val mapViewDimensionsReady = remember { mutableStateOf(false) }
    val locationMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }
    val nearbyTaxiMarkers = remember { mutableStateListOf<HereMapMarker>() }
    val pickupMarkerRef = remember { mutableStateOf<HereMapMarker?>(null) }
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

    /**
     * Heading derived from the interpolated dot's motion across 60 fps frames. Takes
     * precedence over [userMarkerHeadingDegrees] for rotation so the taxi icon always
     * points in the direction the *visible* dot is actually moving — eliminates the
     * drift/slide effect caused by applying raw GPS bearing while the smoothed position
     * is still catching up to the real fix.
     */
    var displayedMotionHeadingDeg by remember { mutableStateOf<Float?>(null) }

    val appColorsMap = LocalAppColors.current
    val pickupDestPinBlackArgb = 0xFF000000.toInt()
    val pickupPinImage: MapImage =
        remember(pickupDestPinBlackArgb) {
            MapImageFactory.fromBitmap(
                createLabeledPushPinBitmap(
                    colorArgb = pickupDestPinBlackArgb,
                    label = "Départ",
                ),
            )
        }
    val destinationPinImage: MapImage =
        remember(pickupDestPinBlackArgb) {
            MapImageFactory.fromBitmap(
                createLabeledPushPinBitmap(
                    colorArgb = pickupDestPinBlackArgb,
                    label = "Arrivée",
                ),
            )
        }
    val intermediateStopValidColorArgb = STOP_PIN_YELLOW_ARGB
    val intermediateStopPinImage: MapImage =
        remember(intermediateStopValidColorArgb) {
            MapImageFactory.fromBitmap(createPushPinBitmap(intermediateStopValidColorArgb))
        }
    val intermediateStopForbiddenPinImage: MapImage =
        remember(intermediateStopValidColorArgb) {
            MapImageFactory.fromBitmap(createForbiddenPushPinBitmap(intermediateStopValidColorArgb))
        }
    val intermediateStopMarkers = remember { mutableStateListOf<HereMapMarker>() }
    val nearbyTaxiFallbackArgb = 0xFF7CCB00.toInt()
    var nearbyTaxiMarkerImage by remember(nearbyTaxiFallbackArgb) {
        mutableStateOf(MapImageFactory.fromBitmap(createPushPinBitmap(nearbyTaxiFallbackArgb)))
    }
    val pickupPinAnchor = remember { Anchor2D(0.5, teardropPickupPinAnchorYNormalized()) }

    val mapView =
        remember {
            HereMapView(context).also {
                it.onCreate(null)
                it.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
            }
        }
    SideEffect { mapViewRef.value = mapView }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
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
            if (error != null) {
                mapError = error.name
                return@loadScene
            }
            runCatching {
                mapView.mapScene.enableFeatures(
                    mapOf(
                        MapFeatures.ROAD_EXIT_LABELS to MapFeatureModes.ROAD_EXIT_LABELS_ALL,
                        MapFeatures.TRAFFIC_FLOW to MapFeatureModes.TRAFFIC_FLOW_WITH_FREE_FLOW,
                        MapFeatures.TRAFFIC_INCIDENTS to MapFeatureModes.TRAFFIC_INCIDENTS_ALL,
                    ),
                )
            }
            mapView.camera.lookAt(
                GeoCoordinates(36.8065, 10.1815),
                MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, 14.0),
            )
            // HERE may invoke this callback off the main thread; hop to Main so Compose sees
            // sceneLoaded + epoch in sync (re-runs polyline LaunchedEffects if geometries arrived early).
            composeScope.launch(Dispatchers.Main.immediate) {
                sceneLoaded.value = true
                scenePolylineEpoch++
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, onPinchRotateBegin) {
        if (!sceneLoaded.value) {
            onDispose { }
        } else {
            mapView.gestures.pinchRotateListener =
                if (onPinchRotateBegin != null) {
                    val cb = onPinchRotateBegin
                    PinchRotateListener { state, _, _, _, _ ->
                        if (state == GestureState.BEGIN) cb()
                    }
                } else {
                    null
                }
            onDispose {
                mapView.gestures.pinchRotateListener = null
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, onPickTargetUpdated) {
        if (!sceneLoaded.value || onPickTargetUpdated == null) {
            onDispose { }
        } else {
            val cb = onPickTargetUpdated
            val listener =
                object : MapCameraListener {
                    override fun onMapCameraUpdated(state: MapCamera.State) {
                        val width = mapView.width
                        val height = mapView.height
                        if (width <= 0 || height <= 0) return
                        mapView.viewToGeoCoordinates(Point2D(width / 2.0, height / 2.0))?.let(cb)
                    }
                }
            mapView.camera.addListener(listener)
            mapView.post {
                val width = mapView.width
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
            val listener =
                object : MapCameraListener {
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
    // Each frame we also derive a motion heading from the actual step of the *displayed* dot
    // (circular low-pass on unit vector) — this is what drives the icon rotation, so the nose
    // of the car tracks the visible trajectory with zero lag instead of jumping ahead.
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
                taxiSmoothHolder.hasMotionHeading = false
            } else {
                val prevLat = taxiSmoothHolder.lat
                val prevLng = taxiSmoothHolder.lng
                val dLatTgt = tgt.latitude - prevLat
                val dLngTgt = tgt.longitude - prevLng
                val jump = approxDeltaMeters(dLatTgt, dLngTgt, prevLat)
                if (jump > TAXI_SNAP_JUMP_METERS) {
                    taxiSmoothHolder.lat = tgt.latitude
                    taxiSmoothHolder.lng = tgt.longitude
                    taxiSmoothHolder.hasMotionHeading = false
                    displayedMotionHeadingDeg = null
                } else {
                    taxiSmoothHolder.lat = prevLat + dLatTgt * TAXI_POSITION_LERP
                    taxiSmoothHolder.lng = prevLng + dLngTgt * TAXI_POSITION_LERP

                    val stepLat = taxiSmoothHolder.lat - prevLat
                    val stepLng = taxiSmoothHolder.lng - prevLng
                    val stepMeters = approxDeltaMeters(stepLat, stepLng, prevLat)
                    if (stepMeters >= TAXI_MOTION_HEADING_MIN_STEP_METERS) {
                        // North (lat) = cos component, East (lng*cos(lat)) = sin component,
                        // so atan2(east, north) gives a bearing clockwise from true north.
                        val midLatRad = prevLat * PI / 180.0
                        val east = stepLng * cos(midLatRad)
                        val north = stepLat
                        val invLen = 1.0 / hypot(east, north)
                        val stepSin = east * invLen
                        val stepCos = north * invLen
                        if (!taxiSmoothHolder.hasMotionHeading) {
                            taxiSmoothHolder.motionSinAcc = stepSin
                            taxiSmoothHolder.motionCosAcc = stepCos
                            taxiSmoothHolder.hasMotionHeading = true
                        } else {
                            val a = TAXI_MOTION_HEADING_SMOOTHING
                            taxiSmoothHolder.motionSinAcc =
                                taxiSmoothHolder.motionSinAcc * (1.0 - a) + stepSin * a
                            taxiSmoothHolder.motionCosAcc =
                                taxiSmoothHolder.motionCosAcc * (1.0 - a) + stepCos * a
                        }
                        val bearingRad =
                            kotlin.math.atan2(
                                taxiSmoothHolder.motionSinAcc,
                                taxiSmoothHolder.motionCosAcc,
                            )
                        var bearingDeg = (bearingRad * 180.0 / PI).toFloat()
                        if (bearingDeg < 0f) bearingDeg += 360f
                        displayedMotionHeadingDeg = bearingDeg
                    }
                }
            }
            val g = GeoCoordinates(taxiSmoothHolder.lat, taxiSmoothHolder.lng)
            setMarkerCoordinates(marker, g)
            displayedMarkerLocationRef.value = g

            // Navigation follow: drive the camera each frame so the map pans to track
            // the interpolated taxi and rotates to match the heading. Gated on having
            // a heading so we don't force "north-up" when the driver is stationary.
            if (navigationFollow) {
                val headingForCamera = displayedMotionHeadingDeg ?: userMarkerHeadingDegrees
                if (headingForCamera != null) {
                    runCatching {
                        mapView.camera.lookAt(
                            g,
                            GeoOrientationUpdate(
                                headingForCamera.toDouble(),
                                NAV_CAMERA_TILT_DEG,
                            ),
                            MapMeasure(MapMeasure.Kind.ZOOM_LEVEL, NAV_CAMERA_ZOOM_LEVEL),
                        )
                    }
                }
            }
        }
    }

    // Rotation: prefer heading derived from the interpolated dot's own motion (computed in
    // the 60 fps loop above). Falls back to [userMarkerHeadingDegrees] (route-snap / GPS bearing)
    // only when the dot is essentially stationary — keeps the icon oriented correctly at stops.
    // Debounce + skip tiny changes, because frequent setImage caused flicker (billboard + bitmap swap).
    LaunchedEffect(sceneLoaded.value, useTaxiIconForUserMarker) {
        if (!sceneLoaded.value || !useTaxiIconForUserMarker) return@LaunchedEffect
        snapshotFlow {
            Triple(mapCameraBearingDeg, displayedMotionHeadingDeg, userMarkerHeadingDegrees)
        }
            .debounce(TAXI_ROTATION_DEBOUNCE_MS)
            .collectLatest { (cam, motionHead, propHead) ->
                val marker = locationMarkerRef.value ?: return@collectLatest
                val base = taxiBaseBitmap ?: return@collectLatest
                val h = motionHead ?: propHead ?: return@collectLatest
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

    LaunchedEffect(sceneLoaded.value) {
        if (!sceneLoaded.value) return@LaunchedEffect
        val bitmap = withContext(Dispatchers.IO) { loadTaxiMarkerBitmap(context) }
        nearbyTaxiMarkerImage = MapImageFactory.fromBitmap(bitmap)
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
        useTaxiIconForUserMarker,
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        val loc = currentLocation
        if (loc == null) {
            taxiSmoothHolder.initialized = false
            taxiSmoothHolder.hasMotionHeading = false
            displayedMotionHeadingDeg = null
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
            taxiSmoothHolder.hasMotionHeading = false
            displayedMotionHeadingDeg = null
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
        val userLocationMarkerArgb =
            if (useTaxiIconForUserMarker) {
                primaryArgb
            } else {
                0xFF000000.toInt()
            }
        if (useTaxiIconForUserMarker && taxiBaseBitmap == null) {
            taxiBaseBitmap = withContext(Dispatchers.IO) { loadTaxiMarkerBitmap(context) }
        }
        val initialHeading = displayedMotionHeadingDeg ?: userMarkerHeadingDegrees ?: 0f
        val bmp =
            withContext(Dispatchers.IO) {
                if (useTaxiIconForUserMarker) {
                    val base = taxiBaseBitmap
                    if (base != null) {
                        rotateTaxiBitmap(
                            base,
                            initialHeading,
                            mapCameraBearingDeg,
                        )
                    } else {
                        loadTaxiMarkerBitmap(context)
                            ?: createUserLocationMarkerBitmap(null, profileInitials, userLocationMarkerArgb)
                    }
                } else {
                    val avatar = loadProfilePhotoBitmap(context, profilePictureUri)
                    try {
                        createUserLocationMarkerBitmap(avatar, profileInitials, userLocationMarkerArgb)
                    } finally {
                        // `loadProfilePhotoBitmap` renvoie une copie (pas le bitmap du cache Coil).
                        avatar?.recycle()
                    }
                }
            }
        withContext(Dispatchers.Main) {
            if (!sceneLoaded.value ||
                currentLocation?.latitude != loc.latitude ||
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

    LaunchedEffect(
        intermediateStops,
        sceneLoaded.value,
        intermediateStopPinImage,
        intermediateStopForbiddenPinImage,
        pickupPinAnchor,
    ) {
        if (!sceneLoaded.value) {
            return@LaunchedEffect
        }
        // Diffing per-marker here is possible but the list is tiny (≤4); recreate is simpler
        // and keeps ordering consistent with [intermediateStops] so indices match the sheet.
        intermediateStopMarkers.forEach { runCatching { mapView.mapScene.removeMapMarker(it) } }
        intermediateStopMarkers.clear()
        intermediateStops.forEach { stop ->
            runCatching {
                val image = if (stop.isValid) intermediateStopPinImage else intermediateStopForbiddenPinImage
                val marker = HereMapMarker(stop.coordinates, image, pickupPinAnchor)
                mapView.mapScene.addMapMarker(marker)
                intermediateStopMarkers.add(marker)
            }
        }
    }

    LaunchedEffect(sceneLoaded.value, nearbyTaxis, nearbyTaxiMarkerImage) {
        if (!sceneLoaded.value) return@LaunchedEffect
        nearbyTaxiMarkers.forEach { runCatching { mapView.mapScene.removeMapMarker(it) } }
        nearbyTaxiMarkers.clear()
        nearbyTaxis.forEach { taxi ->
            runCatching {
                val marker =
                    HereMapMarker(
                        GeoCoordinates(taxi.latitude, taxi.longitude),
                        nearbyTaxiMarkerImage,
                        Anchor2D(0.5, 0.5),
                    )
                mapView.mapScene.addMapMarker(marker)
                nearbyTaxiMarkers.add(marker)
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value, currentLocation, onUserLocationScreenPointUpdated) {
        if (!sceneLoaded.value || onUserLocationScreenPointUpdated == null) {
            onDispose { }
        } else {
            val listener =
                object : MapCameraListener {
                    override fun onMapCameraUpdated(state: MapCamera.State) {
                        val loc = currentLocation
                        if (loc == null) {
                            onUserLocationScreenPointUpdated(null)
                        } else {
                            onUserLocationScreenPointUpdated(runCatching { mapView.geoToViewCoordinates(loc) }.getOrNull())
                        }
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
    val snakeAnimator = remember(mapView) { SnakeAnimator(mapView) }

    LaunchedEffect(
        passengerRouteGeometries,
        selectedPassengerRouteIndex,
        sceneLoaded.value,
        scenePolylineEpoch,
        mainRouteColor,
        sequentialRouteLegs,
        secondLegRouteColor,
    ) {
        if (!sceneLoaded.value) return@LaunchedEffect
        routePolylines.forEach { mapView.mapScene.removeMapPolyline(it) }
        routePolylines.clear()
        if (passengerRouteGeometries.isEmpty()) return@LaunchedEffect
        val density = mapView.resources.displayMetrics.density
        val primaryRgb = mainRouteColor
        val primaryLine = HereColor.valueOf(primaryRgb.red, primaryRgb.green, primaryRgb.blue, 0.95f)
        val secondRgb = secondLegRouteColor
        val secondLine = HereColor.valueOf(secondRgb.red, secondRgb.green, secondRgb.blue, 0.95f)
        val primaryOutline = HereColor.valueOf(1f, 1f, 1f, 0.65f)
        val altLine = HereColor.valueOf(0.60f, 0.62f, 0.68f, 0.50f)
        val altOutline = HereColor.valueOf(1f, 1f, 1f, 0.15f)
        passengerRouteGeometries.forEachIndexed { index, geometry ->
            val isPrimaryStyle =
                when {
                    sequentialRouteLegs -> index == 0 || index == 1
                    else -> index == selectedPassengerRouteIndex
                }
            val lineWidthDp = if (isPrimaryStyle) 5.0 else 3.0
            val outlineWidthDp = if (isPrimaryStyle) 7.5 else 5.0
            val coreWidthPx = lineWidthDp * density
            val haloWidthPx = outlineWidthDp * density
            val coreWidth =
                MapMeasureDependentRenderSize(
                    RenderSize.Unit.PIXELS,
                    coreWidthPx,
                )
            val haloWidth =
                MapMeasureDependentRenderSize(
                    RenderSize.Unit.PIXELS,
                    haloWidthPx,
                )
            val fill =
                when {
                    sequentialRouteLegs && index == 0 -> primaryLine
                    sequentialRouteLegs && index == 1 -> secondLine
                    sequentialRouteLegs -> altLine
                    index == selectedPassengerRouteIndex -> primaryLine
                    else -> altLine
                }
            val outline =
                when {
                    sequentialRouteLegs && (index == 0 || index == 1) -> primaryOutline
                    index == selectedPassengerRouteIndex -> primaryOutline
                    else -> altOutline
                }
            runCatching {
                val representation =
                    HereMapPolyline.SolidRepresentation(
                        coreWidth,
                        fill,
                        haloWidth,
                        outline,
                        LineCap.ROUND,
                    )
                val poly =
                    HereMapPolyline(geometry, representation).also {
                        it.drawOrder =
                            when {
                                sequentialRouteLegs && index == 0 -> 3
                                sequentialRouteLegs && index == 1 -> 4
                                index == selectedPassengerRouteIndex -> 3
                                else -> 1
                            }
                    }
                mapView.mapScene.addMapPolyline(poly)
                routePolylines.add(poly)
            }
        }
    }

    LaunchedEffect(
        sceneLoaded.value,
        passengerRouteGeometries,
        selectedPassengerRouteIndex,
        sequentialRouteLegs,
    ) {
        if (!sceneLoaded.value) {
            snakeAnimator.stopSnakeAnimation()
            return@LaunchedEffect
        }
        val snakeRouteIndex = if (sequentialRouteLegs) 0 else selectedPassengerRouteIndex
        val geometry = passengerRouteGeometries.getOrNull(snakeRouteIndex)
        if (geometry == null || geometry.vertices.size < 2) {
            snakeAnimator.stopSnakeAnimation()
            return@LaunchedEffect
        }
        snakeAnimator.setupSnakeAnimation(geometry.vertices)
    }

    LaunchedEffect(fitRouteToBoundsRequestId) {
        if (fitRouteToBoundsRequestId == 0) return@LaunchedEffect
        if (passengerRouteGeometries.isEmpty()) return@LaunchedEffect
        val mapView = mapViewRef.value ?: return@LaunchedEffect
        val principalGeometry =
            passengerRouteGeometries.getOrNull(selectedPassengerRouteIndex)
                ?: passengerRouteGeometries.first()
        val principalPoints = principalGeometry.vertices
        if (principalPoints.size < 2) return@LaunchedEffect
        val principalMinLat = principalPoints.minOf { it.latitude }
        val principalMaxLat = principalPoints.maxOf { it.latitude }
        val principalMinLng = principalPoints.minOf { it.longitude }
        val principalMaxLng = principalPoints.maxOf { it.longitude }

        // Single-step framing avoids the "double jump" (destination snap then route snap)
        // and feels smoother right after destination confirmation / route selection.
        val latPad = (principalMaxLat - principalMinLat).coerceAtLeast(0.0012) * 0.22
        val lngPad = (principalMaxLng - principalMinLng).coerceAtLeast(0.0012) * 0.16
        val adjustedBox =
            GeoBox(
                GeoCoordinates(principalMinLat - latPad, principalMinLng - lngPad),
                GeoCoordinates(principalMaxLat + latPad, principalMaxLng + lngPad),
            )

        mapView.camera.lookAt(
            adjustedBox,
            GeoOrientationUpdate(0.0, 0.0),
        )
    }

    val trafficPolylines = remember { mutableStateListOf<HereMapPolyline>() }
    LaunchedEffect(passengerTrafficSpans, sceneLoaded.value, scenePolylineEpoch) {
        if (!sceneLoaded.value) return@LaunchedEffect
        trafficPolylines.forEach { mapView.mapScene.removeMapPolyline(it) }
        trafficPolylines.clear()
        if (passengerTrafficSpans.isEmpty()) return@LaunchedEffect
        val trafficDensity = mapView.resources.displayMetrics.density
        passengerTrafficSpans.forEach { span ->
            val color =
                when {
                    span.jamFactor < 4.0 -> return@forEach
                    span.jamFactor < 7.0 -> HereColor.valueOf(1f, 0.84f, 0.0f, 0.60f)
                    span.jamFactor < 9.0 -> HereColor.valueOf(1f, 0.45f, 0.1f, 0.65f)
                    else -> HereColor.valueOf(0.9f, 0.1f, 0.1f, 0.70f)
                }
            val trafficWidthPx = 3.0 * trafficDensity
            val width =
                MapMeasureDependentRenderSize(
                    RenderSize.Unit.PIXELS,
                    trafficWidthPx,
                )
            runCatching {
                val representation =
                    HereMapPolyline.SolidRepresentation(
                        width,
                        color,
                        LineCap.ROUND,
                    )
                val poly =
                    HereMapPolyline(span.geometry, representation).also {
                        it.drawOrder = 4
                    }
                mapView.mapScene.addMapPolyline(poly)
                trafficPolylines.add(poly)
            }
        }
    }

    DisposableEffect(mapView, sceneLoaded.value) {
        onDispose {
            snakeAnimator.stopSnakeAnimation()
            nearbyTaxiMarkers.forEach { runCatching { mapView.mapScene.removeMapMarker(it) } }
            nearbyTaxiMarkers.clear()
            intermediateStopMarkers.forEach { runCatching { mapView.mapScene.removeMapMarker(it) } }
            intermediateStopMarkers.clear()
            routePolylines.forEach { runCatching { mapView.mapScene.removeMapPolyline(it) } }
            routePolylines.clear()
            trafficPolylines.forEach { runCatching { mapView.mapScene.removeMapPolyline(it) } }
            trafficPolylines.clear()
        }
    }

    // Sélection pickup / destination : pas d’inertie après le relâchement — la carte reste sur le point visé.
    DisposableEffect(mapView, sceneLoaded.value, onPickTargetUpdated) {
        val pickerMode = sceneLoaded.value && onPickTargetUpdated != null
        if (!pickerMode) {
            onDispose { }
        } else {
            val listener =
                View.OnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL,
                        -> {
                            v.post {
                                runCatching { mapView.camera.cancelAnimations() }
                            }
                        }
                    }
                    false
                }
            mapView.setOnTouchListener(listener)
            onDispose { mapView.setOnTouchListener(null) }
        }
    }

    if (mapError != null) {
        MapLoadErrorContent(message = mapError!!)
    } else {
        AndroidView(
            factory = {
                mapView.apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                }
            },
            update = { view ->
                view.layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
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
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
internal fun MapLoadErrorContent(message: String) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(c.darkSurface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(painterResource(AppIcon.alertTriangle), null, tint = c.errorRed, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.map_load_error),
                color = c.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(message, color = c.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

private suspend fun loadProfilePhotoBitmap(
    context: Context,
    url: String?,
): Bitmap? {
    if (url.isNullOrBlank()) return null
    return try {
        val request =
            ImageRequest.Builder(context)
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

private suspend fun loadTaxiMarkerBitmap(context: Context): Bitmap =
    withContext(Dispatchers.IO) {
        val fromAsset =
            runCatching {
                context.assets.open("taxi-icon.png").use { stream ->
                    BitmapFactory.decodeStream(stream)?.let { decoded ->
                        val scaled = Bitmap.createScaledBitmap(decoded, 85, 85, true)
                        if (scaled !== decoded) decoded.recycle()
                        scaled
                    }
                }
            }.getOrNull()
        fromAsset ?: createPushPinBitmap(0xFF7CCB00.toInt())
    }

private fun setMarkerCoordinates(
    marker: HereMapMarker,
    coordinates: GeoCoordinates,
) {
    runCatching {
        marker.javaClass
            .getMethod("setCoordinates", GeoCoordinates::class.java)
            .invoke(marker, coordinates)
    }
}

private const val TAXI_SMOOTH_FRAME_MS = 16L
private const val TAXI_POSITION_LERP = 0.28
private const val TAXI_SNAP_JUMP_METERS = 75.0
private const val TAXI_ROTATION_DEBOUNCE_MS = 45L

/**
 * Fixed zoom held by the navigation-follow camera. 17.5 matches Bolt/inDrive: close
 * enough to read street names, wide enough to see the next 2–3 intersections ahead.
 */
private const val NAV_CAMERA_ZOOM_LEVEL = 17.5

/**
 * Camera pitch (deg from horizontal). 45° gives the classic 3D driving perspective
 * without occluding the far field — same ballpark as Google Maps / Bolt.
 */
private const val NAV_CAMERA_TILT_DEG = 45.0

/**
 * Minimum on-screen displacement of the interpolated dot between two 60 fps frames
 * before we refresh the motion-derived heading. Below this, the step is pure GPS noise /
 * lerp residue — keeping the previous heading avoids jitter when stationary.
 */
private const val TAXI_MOTION_HEADING_MIN_STEP_METERS = 0.12

/**
 * Exponential smoothing weight (new-sample) applied to the unit vector of the motion
 * heading, once per frame (16 ms). 0.30 gives a ~50 ms time constant — fast enough to
 * track turns, slow enough to absorb single-frame noise.
 */
private const val TAXI_MOTION_HEADING_SMOOTHING = 0.30

private class TaxiSmoothHolder {
    var initialized: Boolean = false
    var lat: Double = 0.0
    var lng: Double = 0.0

    /**
     * Low-pass filtered unit vector of the *displayed* dot's motion, used to orient
     * the taxi icon so its nose always points in the direction the visible dot is
     * moving. Decoupled from raw GPS bearing on purpose: rotation thus stays in sync
     * with the interpolated position instead of jumping ahead during turns.
     */
    var motionSinAcc: Double = 0.0
    var motionCosAcc: Double = 0.0
    var hasMotionHeading: Boolean = false
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

private fun absShortestAngleDiffDeg(
    a: Float,
    b: Float,
): Float {
    var d = (a - b) % 360f
    if (d < 0f) d += 360f
    if (d > 180f) d = 360f - d
    return kotlin.math.abs(d)
}

/** ~mètres entre deux deltas lat/lng (suffisant pour détecter un saut / reprise GPS). */
private fun approxDeltaMeters(
    dLat: Double,
    dLng: Double,
    midLat: Double,
): Double {
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
    mapCameraBearingNorthClockwiseDeg: Float,
): Bitmap {
    val deg =
        vehicleHeadingNorthClockwiseDeg - mapCameraBearingNorthClockwiseDeg +
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
    mapCameraBearingNorthClockwiseDeg: Float,
) {
    val rotated = rotateTaxiBitmap(taxiBase, vehicleHeadingNorthClockwiseDeg, mapCameraBearingNorthClockwiseDeg)
    marker.setImage(MapImageFactory.fromBitmap(rotated))
}
