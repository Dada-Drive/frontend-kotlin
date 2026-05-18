package tn.turbodrive.presentation.driversetup

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.core.validation.DateParseResult
import tn.turbodrive.presentation.common.ScreenState

@Composable
fun DriverSetupScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit,
    viewModel: DriverSetupViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isCompact = screenWidthDp < 360
    val hPad = if (isCompact) 14.dp else 24.dp
    val vPad = if (isCompact) 12.dp else 16.dp
    val titleFontSize = if (isCompact) 24.sp else 28.sp

    val state by viewModel.state.collectAsState()
    val loading: Boolean =
        when (state) {
            ScreenState.Loading -> true
            ScreenState.Idle, is ScreenState.Loaded, is ScreenState.Error -> false
        }
    val vmError: String? =
        when (val s = state) {
            is ScreenState.Error -> s.error.message
            ScreenState.Idle, ScreenState.Loading, is ScreenState.Loaded -> null
        }

    var step by remember { mutableStateOf(SetupStep.Personal) }

    var cinFrontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var cinBackBmp by remember { mutableStateOf<Bitmap?>(null) }
    var cinNumber by remember { mutableStateOf("") }
    var cinDeliveredAt by remember { mutableStateOf("") }

    var licenseFrontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var licenseBackBmp by remember { mutableStateOf<Bitmap?>(null) }
    var licenseSuffix by remember { mutableStateOf("") }
    var licenseIssueInput by remember { mutableStateOf("") }
    var licenseExpiryInput by remember { mutableStateOf("") }
    var licenseCategories by remember { mutableStateOf(setOf('B')) }

    var vehicleFrontBmp by remember { mutableStateOf<Bitmap?>(null) }
    var vehicleSideBmp by remember { mutableStateOf<Bitmap?>(null) }
    var vehicleBackBmp by remember { mutableStateOf<Bitmap?>(null) }
    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var vehicleColorSlug by remember { mutableStateOf("") }
    var customColorText by remember { mutableStateOf("") }
    var plateInput by remember { mutableStateOf("") }
    var vehiclePreset by remember { mutableStateOf(DriverVehiclePreset.Citadine) }
    var customVehicleType by remember { mutableStateOf("") }
    var seatInput by remember { mutableStateOf("") }

    var activeSlot by remember { mutableStateOf<PhotoSlot?>(null) }
    var showSourcePicker by remember { mutableStateOf(false) }

    var submitError by remember { mutableStateOf<Int?>(null) }

    fun applyBitmap(bmp: Bitmap) =
        when (activeSlot) {
            PhotoSlot.CinFront -> cinFrontBmp = bmp
            PhotoSlot.CinBack -> cinBackBmp = bmp
            PhotoSlot.LicenseFront -> licenseFrontBmp = bmp
            PhotoSlot.LicenseBack -> licenseBackBmp = bmp
            PhotoSlot.VehicleFront -> vehicleFrontBmp = bmp
            PhotoSlot.VehicleSide -> vehicleSideBmp = bmp
            PhotoSlot.VehicleBack -> vehicleBackBmp = bmp
            null -> {}
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { context.uriToBitmap(it)?.let { bmp -> applyBitmap(bmp) } }
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
            bmp?.let { applyBitmap(it) }
        }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) cameraLauncher.launch(null)
        }

    fun openPhotoFor(slot: PhotoSlot) {
        activeSlot = slot
        showSourcePicker = true
    }

    if (showSourcePicker) {
        PhotoSourcePickerDialog(
            onCamera = {
                showSourcePicker = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onGallery = {
                showSourcePicker = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = { showSourcePicker = false },
        )
    }

    val personalOk =
        cinFrontBmp != null && cinBackBmp != null &&
            cinNumber.trim().isNotEmpty() && isValidPastOrPresentDate(cinDeliveredAt.trim())

    val licenseOk =
        licenseFrontBmp != null && licenseBackBmp != null &&
            licenseSuffix.trim().isNotEmpty() &&
            isValidPastOrPresentDate(licenseIssueInput.trim()) &&
            isValidFutureDate(licenseExpiryInput.trim()) &&
            licenseCategories.contains('B')

    val yearInt = vehicleYear.toIntOrNull() ?: 0
    val normalizedPlate =
        remember(plateInput) {
            val cleaned = plateInput.trim().replace(Regex("\\s+"), " ")
            val source = if (Regex("(?i)\\bTUN\\b").containsMatchIn(cleaned)) cleaned else cleaned
            val match = Regex("(\\d{1,3})\\s*(?:[Tt][Uu][Nn]\\s*)?(\\S+)").find(source)
            match?.let {
                val digits = it.groupValues[1].padStart(3, '0')
                val serial = it.groupValues[2].uppercase()
                "$digits TUN $serial"
            }
        }
    val resolvedVehicleType =
        if (vehiclePreset == DriverVehiclePreset.Other) {
            customVehicleType.trim()
        } else {
            vehiclePreset.apiSlug
        }
    val typeOk =
        resolvedVehicleType.isNotEmpty() &&
            (vehiclePreset != DriverVehiclePreset.Other || customVehicleType.trim().isNotEmpty())
    val resolvedColor = if (vehicleColorSlug == "other") customColorText.trim() else vehicleColorSlug
    val colorOk = resolvedColor.isNotEmpty() && (vehicleColorSlug != "other" || customColorText.trim().length >= 2)
    val seatsInt = seatInput.toIntOrNull()
    val vehicleOk =
        vehicleFrontBmp != null && vehicleSideBmp != null && vehicleBackBmp != null &&
            normalizedPlate != null && vehicleMake.trim().isNotEmpty() &&
            vehicleModel.trim().isNotEmpty() && colorOk &&
            yearInt >= 1990 && typeOk && seatsInt != null && seatsInt in 1..99

    val footerEnabled =
        when (step) {
            SetupStep.Personal -> personalOk
            SetupStep.License -> licenseOk
            SetupStep.Vehicle -> vehicleOk
        } && !loading

    val footerText =
        when (step) {
            SetupStep.Vehicle -> stringResource(R.string.driver_submit)
            else -> stringResource(R.string.driver_next)
        }

    fun onHeaderBack() {
        step =
            when {
                step == SetupStep.License -> SetupStep.Personal
                step == SetupStep.Vehicle -> SetupStep.License
                else -> {
                    onBack()
                    return
                }
            }
    }

    fun onHeaderClose() {
        onBack()
    }

    @Suppress("ReturnCount") // Guard ladder over 9 nullable fields; each branch surfaces a distinct localized error.
    fun onFooterClick() {
        when (step) {
            SetupStep.Personal -> if (personalOk) step = SetupStep.License
            SetupStep.License -> if (licenseOk) step = SetupStep.Vehicle
            SetupStep.Vehicle -> {
                if (!personalOk || !licenseOk || !vehicleOk) {
                    submitError = R.string.driver_setup_error_missing_fields
                    return
                }
                val plate =
                    normalizedPlate ?: run {
                        submitError = R.string.driver_setup_error_plate
                        return
                    }
                val cinIso =
                    when (val r = parseUnderscoreDate(cinDeliveredAt.trim())) {
                        is DateParseResult.Valid -> r.iso
                        DateParseResult.Invalid -> {
                            submitError = R.string.driver_setup_error_cin_date
                            return
                        }
                    }
                val licIso =
                    when (val r = parseUnderscoreDate(licenseExpiryInput.trim())) {
                        is DateParseResult.Valid -> r.iso
                        DateParseResult.Invalid -> {
                            submitError = R.string.driver_setup_error_license_expiry
                            return
                        }
                    }
                val cinFront =
                    cinFrontBmp ?: run {
                        submitError = R.string.driver_setup_error_cin_front
                        return
                    }
                val cinBack =
                    cinBackBmp ?: run {
                        submitError = R.string.driver_setup_error_cin_back
                        return
                    }
                val licFront =
                    licenseFrontBmp ?: run {
                        submitError = R.string.driver_setup_error_license_front
                        return
                    }
                val licBack =
                    licenseBackBmp ?: run {
                        submitError = R.string.driver_setup_error_license_back
                        return
                    }
                val vFront =
                    vehicleFrontBmp ?: run {
                        submitError = R.string.driver_setup_error_vehicle_front
                        return
                    }
                val vSide =
                    vehicleSideBmp ?: run {
                        submitError = R.string.driver_setup_error_vehicle_side
                        return
                    }
                val vBack =
                    vehicleBackBmp ?: run {
                        submitError = R.string.driver_setup_error_vehicle_back
                        return
                    }
                val seats = requireNotNull(seatsInt) { "seatsInt non-null per vehicleOk" }
                submitError = null
                viewModel.submitDriverSetup(
                    fullLicenseNumber = "TN-${licenseSuffix.trim()}",
                    licenseExpiry = licIso,
                    cin = cinNumber.trim(),
                    cinDeliveredAt = cinIso,
                    cinPhotoFront = cinFront.toBase64(),
                    cinPhotoBack = cinBack.toBase64(),
                    licensePhotoFront = licFront.toBase64(),
                    licensePhotoBack = licBack.toBase64(),
                    make = vehicleMake.trim(),
                    model = vehicleModel.trim(),
                    year = yearInt,
                    plateNumber = plate,
                    color = resolvedColor,
                    vehicleType = resolvedVehicleType,
                    seats = seats,
                    photoFront = vFront.toBase64(),
                    photoSide = vSide.toBase64(),
                    photoBack = vBack.toBase64(),
                    onComplete = onComplete,
                )
            }
        }
    }

    fun toggleLicenseCategory(cat: Char) {
        val s = licenseCategories.toMutableSet()
        if (s.contains(cat)) {
            if (cat == 'B' && s.size == 1) return
            s.remove(cat)
        } else {
            s.add(cat)
        }
        licenseCategories = s.toSet()
    }

    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(OnboardingPageBg)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad, vertical = vPad),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, LocalAppColors.current.border),
                    shadowElevation = 2.dp,
                    onClick = { onHeaderBack() },
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(AppIcon.arrowLeft),
                            contentDescription = stringResource(R.string.driver_back),
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.driver_registration_title),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = OnboardingTitle,
                    maxLines = 1,
                )
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, LocalAppColors.current.border),
                    shadowElevation = 2.dp,
                    onClick = { onHeaderClose() },
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(AppIcon.close),
                            contentDescription = stringResource(R.string.driver_close),
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            DriverSetupStepper(step = step)
            Spacer(Modifier.height(8.dp))

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = hPad)
                        .padding(bottom = 8.dp),
            ) {
                when (step) {
                    SetupStep.Personal ->
                        DriverPersonalStep(
                            cinFrontBmp = cinFrontBmp,
                            cinBackBmp = cinBackBmp,
                            cinNumber = cinNumber,
                            cinDeliveredAt = cinDeliveredAt,
                            onCinFrontClick = { openPhotoFor(PhotoSlot.CinFront) },
                            onCinBackClick = { openPhotoFor(PhotoSlot.CinBack) },
                            onCinNumberChange = { cinNumber = it },
                            onCinDateChange = { cinDeliveredAt = it },
                            titleFontSize = titleFontSize,
                        )
                    SetupStep.License ->
                        DriverLicenseStep(
                            licenseFrontBmp = licenseFrontBmp,
                            licenseBackBmp = licenseBackBmp,
                            licenseSuffix = licenseSuffix,
                            licenseIssueInput = licenseIssueInput,
                            licenseExpiryInput = licenseExpiryInput,
                            licenseCategories = licenseCategories,
                            onLicenseFrontClick = { openPhotoFor(PhotoSlot.LicenseFront) },
                            onLicenseBackClick = { openPhotoFor(PhotoSlot.LicenseBack) },
                            onSuffixChange = { licenseSuffix = it },
                            onIssueChange = { licenseIssueInput = it },
                            onExpiryChange = { licenseExpiryInput = it },
                            onLicenseCategoryToggle = { toggleLicenseCategory(it) },
                            titleFontSize = titleFontSize,
                        )
                    SetupStep.Vehicle ->
                        DriverVehicleStep(
                            vehicleFrontBmp = vehicleFrontBmp,
                            vehicleSideBmp = vehicleSideBmp,
                            vehicleBackBmp = vehicleBackBmp,
                            vehicleMake = vehicleMake,
                            vehicleModel = vehicleModel,
                            vehicleYear = vehicleYear,
                            selectedColorSlug = vehicleColorSlug,
                            customColorText = customColorText,
                            plateInput = plateInput,
                            vehiclePreset = vehiclePreset,
                            customVehicleType = customVehicleType,
                            seatInput = seatInput,
                            onVehicleFrontClick = { openPhotoFor(PhotoSlot.VehicleFront) },
                            onVehicleSideClick = { openPhotoFor(PhotoSlot.VehicleSide) },
                            onVehicleBackClick = { openPhotoFor(PhotoSlot.VehicleBack) },
                            onMakeChange = { vehicleMake = it },
                            onModelChange = { vehicleModel = it },
                            onYearChange = { vehicleYear = it },
                            onColorSlugChange = { vehicleColorSlug = it },
                            onCustomColorChange = { customColorText = it },
                            onPlateInputChange = { plateInput = it },
                            onPresetChange = { vehiclePreset = it },
                            onCustomTypeChange = { customVehicleType = it },
                            onSeatChange = { seatInput = it },
                            titleFontSize = titleFontSize,
                        )
                }
            }

            DriverSetupPrimaryFooter(
                text = footerText,
                enabled = footerEnabled,
                loading = loading && step == SetupStep.Vehicle,
                onClick = { onFooterClick() },
                modifier =
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = hPad, vertical = 12.dp),
            )
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            DriverErrorSnackbar(
                message = vmError,
                onDismiss = { viewModel.dismissError() },
            )
            if (vmError != null && submitError != null) {
                Spacer(Modifier.height(8.dp))
            }
            DriverErrorSnackbar(
                message = submitError?.let { stringResource(it) },
                onDismiss = { submitError = null },
            )
        }
    }
}
