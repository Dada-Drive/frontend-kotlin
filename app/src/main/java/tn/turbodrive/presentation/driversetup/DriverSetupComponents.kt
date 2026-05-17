package tn.turbodrive.presentation.driversetup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricCar
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.SportsMotorsports
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TimeToLeave
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.core.validation.DateParseResult
import tn.turbodrive.presentation.components.BlackCloseIconButton
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Locale

// ── Enums ────────────────────────────────────────────────────────────────────

internal enum class SetupStep { Personal, License, Vehicle }

internal enum class PhotoSlot {
    CinFront,
    CinBack,
    LicenseFront,
    LicenseBack,
    VehicleFront,
    VehicleSide,
    VehicleBack,
}

internal enum class DriverVehiclePreset(
    @StringRes val labelRes: Int,
    val apiSlug: String,
) {
    Citadine(R.string.driver_vehicle_cat_citadine, "citadine"),
    Compacte(R.string.driver_vehicle_cat_compacte, "compacte"),
    Berline(R.string.driver_vehicle_cat_berline, "berline"),
    Suv(R.string.driver_vehicle_cat_suv, "suv"),
    Coupe(R.string.driver_vehicle_cat_coupe, "coupe"),
    Monospace(R.string.driver_vehicle_cat_monospace, "monospace"),
    Utilitaire(R.string.driver_vehicle_cat_utilitaire, "utilitaire"),
    PickUp(R.string.driver_vehicle_cat_pickup, "pick_up"),
    Cabriolet(R.string.driver_vehicle_cat_cabriolet, "cabriolet"),
    Van(R.string.driver_vehicle_cat_van, "van"),
    Bus(R.string.driver_vehicle_cat_bus, "bus"),
    Minibus(R.string.driver_vehicle_cat_minibus, "minibus"),
    Remorquage(R.string.driver_vehicle_cat_remorquage, "remorquage"),
    Other(R.string.driver_vehicle_other, ""),
}

// Fichier dans `app/src/main/assets/` pour la grille « catégorie ». [Other] = icône [categoryIcon] uniquement.
// Ordre aligné sur les visuels fournis (Coupé, Berline, SUV, etc.).

/** Aligné visuel (texte intégré au JPG) ↔ [DriverVehiclePreset] : chaque fichier n’apparaît qu’une fois. */
internal fun DriverVehiclePreset.categoryImageAssetName(): String? =
    when (this) {
        DriverVehiclePreset.Citadine -> "289ee2ab-232a-4c42-a02e-0670d5d19462.jpg"
        DriverVehiclePreset.Compacte -> "fb5a191a-38f6-4db9-a678-a545f34d5ba4.jpg"
        DriverVehiclePreset.Berline -> "65d4f915-0977-4ee4-9e88-6a2b72bca998.jpg"
        DriverVehiclePreset.Suv -> "65ec50b0-3169-4b20-a390-04d80ce6d552.jpg"
        DriverVehiclePreset.Coupe -> "36b9593e-effb-48fe-843f-e83c83f3675e.jpg"
        DriverVehiclePreset.Monospace -> "f1e20f21-f513-4eec-91f5-08f32b12114c.jpg"
        DriverVehiclePreset.Utilitaire -> "cbe1e003-6ef4-46c9-ba52-3ebce90975aa.jpg"
        DriverVehiclePreset.PickUp -> "398bc6e0-8c48-4d51-85a9-6df516d791eb.jpg"
        DriverVehiclePreset.Cabriolet -> "e5819d7c-c633-4bf7-b796-86581a854dcc.jpg"
        DriverVehiclePreset.Van -> "ca2ad684-993a-4bc7-8957-71c03a79d0bd.jpg"
        DriverVehiclePreset.Bus -> "97180de8-dc0e-4302-9d1f-a89d31d2b1e8.jpg"
        DriverVehiclePreset.Minibus -> "e4f3c9ba-1ceb-4280-b3d8-077636361717.jpg"
        DriverVehiclePreset.Remorquage -> "9b9260a6-14ea-4007-8c2a-1a30e8b7fd60.jpg"
        DriverVehiclePreset.Other -> null
    }

internal fun DriverVehiclePreset.categoryIcon(): ImageVector =
    when (this) {
        DriverVehiclePreset.Citadine -> Icons.Outlined.ElectricCar
        DriverVehiclePreset.Compacte -> Icons.Outlined.DirectionsCar
        DriverVehiclePreset.Berline -> Icons.Outlined.TimeToLeave
        DriverVehiclePreset.Suv -> Icons.Outlined.Terrain
        DriverVehiclePreset.Coupe -> Icons.Outlined.SportsMotorsports
        DriverVehiclePreset.Monospace -> Icons.Outlined.AirportShuttle
        DriverVehiclePreset.Utilitaire -> Icons.Outlined.LocalShipping
        DriverVehiclePreset.PickUp -> Icons.Outlined.Build
        DriverVehiclePreset.Cabriolet -> Icons.Outlined.DirectionsCar
        DriverVehiclePreset.Van -> Icons.Outlined.AirportShuttle
        DriverVehiclePreset.Bus -> Icons.Outlined.DirectionsBus
        DriverVehiclePreset.Minibus -> Icons.Outlined.AirportShuttle
        DriverVehiclePreset.Remorquage -> Icons.Outlined.Build
        DriverVehiclePreset.Other -> Icons.Filled.Edit
    }

// ── Colors ───────────────────────────────────────────────────────────────────
// Theme tokens accessed via composable getters (LocalAppColors).
// Mono* are stable design constants (pure black/white) — kept as raw Color values.

internal val OnboardingPageBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

internal val TurboSetupSuccessGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.successGreen

internal val TurboSetupSuccessBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.successContainer

internal val OnboardingFieldBg = Color.White

internal val OnboardingTitle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

internal val OnboardingSubtitle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

internal val OnboardingLabel: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

internal val OnboardingStepInactiveCircle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.border

internal val OnboardingStepInactiveNumber: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

internal val MonoPrimary = Color.Black
internal val MonoOnPrimary = Color.White

internal val MonoDisabled: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.outlineLight

// ── TextField colors ─────────────────────────────────────────────────────────

@Composable
internal fun onboardingFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = OnboardingTitle,
        unfocusedTextColor = OnboardingTitle,
        focusedContainerColor = OnboardingFieldBg,
        unfocusedContainerColor = OnboardingFieldBg,
        disabledContainerColor = OnboardingFieldBg,
        focusedBorderColor = MonoPrimary,
        unfocusedBorderColor = LocalAppColors.current.border,
        cursorColor = MonoPrimary,
    )

// ── Shared field composables ──────────────────────────────────────────────────

@Composable
internal fun OnboardingSmallField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = OnboardingLabel.copy(alpha = 0.65f)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(),
        )
    }
}

@Composable
internal fun OnboardingLargeField(
    label: String,
    value: String,
    onValueChange: ((String) -> Unit)?,
    placeholder: String,
    onClick: (() -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange?.invoke(it) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            placeholder = { Text(placeholder, color = OnboardingLabel.copy(alpha = 0.65f)) },
            readOnly = onValueChange == null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(),
        )
    }
}

// ── Picker field (read-only + tap to open sheet) ──────────────────────────────

@Composable
internal fun OnboardingPickerField(
    label: String,
    value: String,
    placeholder: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(modifier) {
        Text(label, color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder, color = OnboardingLabel.copy(alpha = 0.65f)) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        tint = if (enabled) OnboardingLabel else OnboardingLabel.copy(alpha = 0.4f),
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = onboardingFieldColors(),
            )
            if (enabled) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onClick,
                            ),
                )
            }
        }
    }
}

// ── Date saisie manuelle JJMMAAAA (8 chiffres) ───────────────────────────────

/** Saisie date : 8 chiffres [jjmmaaaa] sans séparateurs — évite le curseur qui saute (reformatage _). */
@Composable
internal fun UnderscoreDateTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    fieldModifier: Modifier = Modifier.fillMaxWidth(),
) {
    Text(label, color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    val digits = value.filter { it.isDigit() }.take(8)
    OutlinedTextField(
        value = digits,
        onValueChange = { onValueChange(it.filter { ch -> ch.isDigit() }.take(8)) },
        modifier = fieldModifier,
        placeholder = { Text(placeholder, color = OnboardingLabel.copy(alpha = 0.65f)) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = onboardingFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

// ── Upload card (Turbo driver registration style) ───────────────────────────

private fun Modifier.dashedPhotoPlaceholderBorder(
    strokeWidth: androidx.compose.ui.unit.Dp,
    color: Color,
    corner: androidx.compose.ui.unit.Dp,
) = drawBehind {
    val sw = strokeWidth.toPx()
    val half = sw / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(half, half),
        size = Size(size.width - sw, size.height - sw),
        cornerRadius = CornerRadius(corner.toPx(), corner.toPx()),
        style =
            Stroke(
                width = sw,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(11f, 9f), 0f),
            ),
    )
}

@Composable
internal fun UploadPlaceholderCard(
    label: String,
    bitmap: Bitmap?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val corner = 16.dp
    Column(modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.12f)
                    .clip(RoundedCornerShape(corner))
                    .background(if (bitmap != null) TurboSetupSuccessBg else Color.White)
                    .then(
                        if (bitmap == null) {
                            Modifier.dashedPhotoPlaceholderBorder(1.5.dp, LocalAppColors.current.outlineLight, corner)
                        } else {
                            Modifier.border(2.dp, TurboSetupSuccessGreen, RoundedCornerShape(corner))
                        },
                    )
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(corner)),
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(TurboSetupSuccessGreen.copy(alpha = 0.18f)),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TurboSetupSuccessGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.driver_photo_captured),
                        color = TurboSetupSuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = stringResource(R.string.driver_photo_take),
                        modifier = Modifier.size(30.dp),
                        tint = OnboardingLabel,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.driver_upload_tap_to_capture),
                        color = OnboardingSubtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = OnboardingTitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun DriverSetupPrimaryFooter(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(999.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MonoPrimary,
                contentColor = MonoOnPrimary,
                disabledContainerColor = MonoDisabled,
                disabledContentColor = MonoOnPrimary.copy(alpha = 0.45f),
            ),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MonoOnPrimary, strokeWidth = 2.dp)
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ── Step bottom bar ───────────────────────────────────────────────────────────

@Composable
internal fun StepBottomBar(
    current: Int,
    total: Int,
    canGoNext: Boolean,
    showBack: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    brandPrimary: Color,
    brandOnPrimary: Color,
    brandPrimaryDisabled: Color,
    loading: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.driver_step_counter, current, total),
            color = OnboardingTitle,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(LocalAppColors.current.outlineLight, RoundedCornerShape(8.dp)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(current / total.toFloat())
                        .height(4.dp)
                        .background(brandPrimary, RoundedCornerShape(8.dp)),
            )
        }
        if (showBack) {
            Button(
                onClick = onBack,
                modifier = Modifier.height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = OnboardingFieldBg,
                        contentColor = OnboardingTitle,
                    ),
                border = BorderStroke(1.dp, LocalAppColors.current.outlineLight),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(18.dp))
            }
        }
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = brandPrimary,
                    contentColor = brandOnPrimary,
                    disabledContainerColor = brandPrimaryDisabled,
                    disabledContentColor = brandOnPrimary.copy(alpha = 0.5f),
                ),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = brandOnPrimary, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.driver_next), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.size(4.dp))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Stepper ───────────────────────────────────────────────────────────────────

@Composable
internal fun DriverSetupStepper(step: SetupStep) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        StepperColumn(
            1,
            stringResource(R.string.driver_tab_personal),
            isActive = step == SetupStep.Personal,
            isComplete = step != SetupStep.Personal,
        )
        StepDivider(step != SetupStep.Personal)
        StepperColumn(
            2,
            stringResource(R.string.driver_tab_license),
            isActive = step == SetupStep.License,
            isComplete = step == SetupStep.Vehicle,
        )
        StepDivider(step == SetupStep.Vehicle)
        StepperColumn(
            3,
            stringResource(R.string.driver_tab_vehicle),
            isActive = step == SetupStep.Vehicle,
            isComplete = false,
        )
    }
}

@Composable
private fun RowScope.StepDivider(done: Boolean) {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(2.dp)
                .background(if (done) TurboSetupSuccessGreen else LocalAppColors.current.border, RoundedCornerShape(1.dp)),
    )
}

@Composable
private fun StepperColumn(
    stepIndex: Int,
    label: String,
    isActive: Boolean,
    isComplete: Boolean,
) {
    val circleColor =
        when {
            isComplete -> TurboSetupSuccessGreen
            isActive -> MonoPrimary
            else -> OnboardingStepInactiveCircle
        }
    val numberColor =
        when {
            isComplete -> Color.White
            isActive -> Color.White
            else -> OnboardingStepInactiveNumber
        }
    val labelColor =
        when {
            isComplete -> TurboSetupSuccessGreen
            isActive -> MonoPrimary
            else -> OnboardingStepInactiveNumber
        }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(circleColor),
            contentAlignment = Alignment.Center,
        ) {
            if (isComplete) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    "$stepIndex",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = numberColor,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (isActive || isComplete) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Photo source dialog ───────────────────────────────────────────────────────

@Composable
internal fun PhotoSourcePickerDialog(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.driver_photo_dialog_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = OnboardingTitle,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PhotoDialogRow(Icons.Default.CameraAlt, stringResource(R.string.driver_photo_take), onCamera)
                PhotoDialogRow(Icons.Default.PhotoLibrary, stringResource(R.string.driver_photo_gallery), onGallery)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = OnboardingSubtitle, fontSize = 14.sp)
            }
        },
        containerColor = OnboardingPageBg,
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun PhotoDialogRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OnboardingFieldBg)
                .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = OnboardingTitle, modifier = Modifier.size(22.dp))
        Text(label, color = OnboardingTitle, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

internal fun contentOnPrimaryForBrand(primary: Color): Color = if (primary.luminance() > 0.5f) Color.Black else Color.White

internal fun isValidFutureDate(input: String): Boolean {
    val iso = resolveDateToIsoString(input) ?: return false
    return runCatching { LocalDate.parse(iso).isAfter(LocalDate.now()) }.getOrDefault(false)
}

internal fun isValidPastOrPresentDate(input: String): Boolean {
    val iso = resolveDateToIsoString(input) ?: return false
    return runCatching {
        val d = LocalDate.parse(iso)
        !d.isAfter(LocalDate.now())
    }.getOrDefault(false)
}

internal fun Context.uriToBitmap(uri: Uri): Bitmap? =
    runCatching {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream.close()
        val scale = minOf(256f / original.width, 256f / original.height, 1f)
        if (scale < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
        } else {
            original
        }
    }.getOrNull()

internal fun Bitmap.toBase64(): String {
    var working = this
    var quality = 45
    var encoded: ByteArray

    while (true) {
        val out = ByteArrayOutputStream()
        working.compress(Bitmap.CompressFormat.JPEG, quality, out)
        encoded = out.toByteArray()
        if (encoded.size <= 1100 || (working.width <= 72 && working.height <= 72 && quality <= 25)) {
            break
        }
        val nextW = (working.width * 0.78f).toInt().coerceAtLeast(72)
        val nextH = (working.height * 0.78f).toInt().coerceAtLeast(72)
        if (nextW == working.width && nextH == working.height) {
            quality = (quality - 5).coerceAtLeast(25)
        } else {
            working = Bitmap.createScaledBitmap(working, nextW, nextH, true)
            quality = (quality - 3).coerceAtLeast(25)
        }
    }
    return Base64.encodeToString(encoded, Base64.NO_WRAP)
}

/**
 * Saisie JJMMAAAA (8 chiffres) → [yyyy-MM-dd] pour l'API, ou null si date invalide.
 */
internal fun underscoreDateToIso(u: String): String? {
    val digits = u.filter { it.isDigit() }
    if (digits.length != 8) return null
    val day = digits.substring(0, 2).toIntOrNull() ?: return null
    val month = digits.substring(2, 4).toIntOrNull() ?: return null
    val year = digits.substring(4, 8).toIntOrNull() ?: return null
    return runCatching {
        val ld = LocalDate.of(year, month, day)
        String.format(Locale.US, "%04d-%02d-%02d", ld.year, ld.monthValue, ld.dayOfMonth)
    }.getOrNull()
}

internal fun parseUnderscoreDate(u: String): DateParseResult =
    underscoreDateToIso(u)?.let { DateParseResult.Valid(it) } ?: DateParseResult.Invalid

private val IsoDatePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")

private fun resolveDateToIsoString(input: String): String? {
    val t = input.trim()
    if (t.filter { it.isDigit() }.length == 8) return underscoreDateToIso(t)
    if (IsoDatePattern.matches(t)) {
        return runCatching { LocalDate.parse(t).toString() }.getOrNull()
    }
    return null
}

// ── Vehicle color palette ─────────────────────────────────────────────────────

internal data class VehicleColorDef(
    @StringRes val nameRes: Int,
    val hex: Long,
    val apiValue: String,
)

internal val vehicleColors =
    listOf(
        VehicleColorDef(R.string.vehicle_color_white, 0xFFFFFFFF, "white"),
        VehicleColorDef(R.string.vehicle_color_black, 0xFF1A1A1A, "black"),
        VehicleColorDef(R.string.vehicle_color_grey, 0xFF9E9E9E, "grey"),
        VehicleColorDef(R.string.vehicle_color_dark_grey, 0xFF424242, "dark_grey"),
        VehicleColorDef(R.string.vehicle_color_silver, 0xFFC0C0C0, "silver"),
        VehicleColorDef(R.string.vehicle_color_blue, 0xFF1565C0, "blue"),
        VehicleColorDef(R.string.vehicle_color_light_blue, 0xFF42A5F5, "light_blue"),
        VehicleColorDef(R.string.vehicle_color_red, 0xFFC62828, "red"),
        VehicleColorDef(R.string.vehicle_color_burgundy, 0xFF6D1A36, "burgundy"),
        VehicleColorDef(R.string.vehicle_color_green, 0xFF2E7D32, "green"),
        VehicleColorDef(R.string.vehicle_color_orange, 0xFFE65100, "orange"),
        VehicleColorDef(R.string.vehicle_color_brown, 0xFF5D4037, "brown"),
        VehicleColorDef(R.string.vehicle_color_beige, 0xFFD7C49E, "beige"),
        VehicleColorDef(R.string.vehicle_color_gold, 0xFFB8860B, "gold"),
        VehicleColorDef(R.string.vehicle_color_bronze, 0xFFCD7F32, "bronze"),
        VehicleColorDef(R.string.vehicle_color_pink, 0xFFF48FB1, "pink"),
        VehicleColorDef(R.string.vehicle_color_purple, 0xFF6A1B9A, "purple"),
        VehicleColorDef(R.string.vehicle_color_yellow, 0xFFF9A825, "yellow"),
    )

// ── Vehicle make/model picker bottom sheet ────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VehiclePickerSheet(
    title: String,
    items: List<String>,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered =
        remember(query, items) {
            if (query.isBlank()) items else items.filter { it.contains(query, ignoreCase = true) }
        }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OnboardingPageBg,
    ) {
        Column(
            modifier =
                androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnboardingTitle)
                BlackCloseIconButton(
                    onClick = onDismiss,
                    buttonSize = 32.dp,
                    iconSize = 16.dp,
                )
            }
            Spacer(androidx.compose.ui.Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.vehicle_search_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = OnboardingLabel) },
                singleLine = true,
                colors = onboardingFieldColors(),
            )
            Spacer(androidx.compose.ui.Modifier.height(8.dp))
            if (isLoading) {
                Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = androidx.compose.ui.Modifier.size(32.dp))
                }
            } else {
                LazyColumn(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                    items(filtered) { item ->
                        Text(
                            item,
                            modifier =
                                androidx.compose.ui.Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(item)
                                        query = ""
                                    }
                                    .padding(horizontal = 4.dp, vertical = 14.dp),
                            color = OnboardingTitle,
                            fontSize = 15.sp,
                        )
                        HorizontalDivider(color = LocalAppColors.current.surfaceMuted)
                    }
                }
            }
        }
    }
}
