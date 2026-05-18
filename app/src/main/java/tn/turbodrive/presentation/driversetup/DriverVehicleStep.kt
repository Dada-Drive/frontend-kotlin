package tn.turbodrive.presentation.driversetup

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.components.BlackCloseIconButton

@Composable
internal fun DriverVehicleStep(
    vehicleFrontBmp: Bitmap?,
    vehicleSideBmp: Bitmap?,
    vehicleBackBmp: Bitmap?,
    vehicleMake: String,
    vehicleModel: String,
    vehicleYear: String,
    selectedColorSlug: String,
    customColorText: String,
    plateInput: String,
    vehiclePreset: DriverVehiclePreset,
    customVehicleType: String,
    seatInput: String,
    onVehicleFrontClick: () -> Unit,
    onVehicleSideClick: () -> Unit,
    onVehicleBackClick: () -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onColorSlugChange: (String) -> Unit,
    onCustomColorChange: (String) -> Unit,
    onPlateInputChange: (String) -> Unit,
    onPresetChange: (DriverVehiclePreset) -> Unit,
    onCustomTypeChange: (String) -> Unit,
    onSeatChange: (String) -> Unit,
    titleFontSize: TextUnit,
) {
    val context = LocalContext.current
    val otherPresetSelected = vehiclePreset == DriverVehiclePreset.Other
    val otherColorSelected = selectedColorSlug == "other"
    val selectedColorDef = vehicleColors.firstOrNull { it.apiValue == selectedColorSlug }
    val selectedColorLabel =
        when {
            otherColorSelected -> customColorText.ifBlank { stringResource(R.string.vehicle_color_other) }
            selectedColorDef != null -> stringResource(selectedColorDef.nameRes)
            else -> ""
        }

    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        VehicleColorPickerSheet(
            selectedColorSlug = selectedColorSlug,
            customColorText = customColorText,
            onColorSelected = {
                onColorSlugChange(it)
                showColorPicker = false
            },
            onCustomColorChange = onCustomColorChange,
            onDismiss = { showColorPicker = false },
        )
    }

    Text(
        text = stringResource(R.string.driver_vehicle_title),
        color = OnboardingTitle,
        fontSize = titleFontSize,
        fontWeight = FontWeight.Bold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.driver_vehicle_subtitle),
        color = OnboardingSubtitle,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    )
    Spacer(Modifier.height(24.dp))

    // ── Vehicle photos ────────────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        UploadPlaceholderCard(
            stringResource(R.string.driver_vehicle_picture_label),
            vehicleFrontBmp,
            onVehicleFrontClick,
            Modifier.weight(1f),
        )
        UploadPlaceholderCard(stringResource(R.string.driver_vehicle_side_label), vehicleSideBmp, onVehicleSideClick, Modifier.weight(1f))
        UploadPlaceholderCard(stringResource(R.string.driver_vehicle_back_label), vehicleBackBmp, onVehicleBackClick, Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))

    // ── Make / Model (saisie libre) ─────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OnboardingSmallField(
            label = stringResource(R.string.driver_field_make),
            value = vehicleMake,
            onValueChange = onMakeChange,
            placeholder = stringResource(R.string.driver_placeholder_make),
            modifier = Modifier.weight(1f),
        )
        OnboardingSmallField(
            label = stringResource(R.string.driver_field_model),
            value = vehicleModel,
            onValueChange = onModelChange,
            placeholder = stringResource(R.string.driver_placeholder_model),
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(14.dp))

    // ── Year / Seats ──────────────────────────────────────────────────────────
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OnboardingSmallField(
            label = stringResource(R.string.driver_field_year),
            value = vehicleYear,
            onValueChange = { onYearChange(it.filter { ch -> ch.isDigit() }.take(4)) },
            placeholder = stringResource(R.string.driver_placeholder_year),
            modifier = Modifier.weight(1f),
        )
        OnboardingSmallField(
            label = stringResource(R.string.driver_seats_label),
            value = seatInput,
            onValueChange = { onSeatChange(it.filter { ch -> ch.isDigit() }.take(2)) },
            placeholder = stringResource(R.string.driver_seats_hint),
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(14.dp))

    // ── Vehicle category grid ─────────────────────────────────────────────────
    val categoryVisualHeight = 78.dp
    Text(stringResource(R.string.driver_vehicle_type_label), color = OnboardingTitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))

    val presetGrid = DriverVehiclePreset.entries.toList()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        presetGrid.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { preset ->
                    val selected = vehiclePreset == preset
                    val assetName = preset.categoryImageAssetName()
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) MonoPrimary.copy(alpha = 0.08f) else OnboardingFieldBg)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MonoPrimary else LocalAppColors.current.outlineLight,
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable { onPresetChange(preset) }
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            if (assetName != null) {
                                AsyncImage(
                                    model =
                                        ImageRequest.Builder(context)
                                            .data("file:///android_asset/$assetName")
                                            .crossfade(200)
                                            .build(),
                                    contentDescription = stringResource(preset.labelRes),
                                    contentScale = ContentScale.Fit,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(categoryVisualHeight)
                                            .alpha(if (selected) 1f else 0.88f)
                                            .clip(RoundedCornerShape(8.dp)),
                                )
                            } else {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(categoryVisualHeight)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = if (selected) MonoPrimary.copy(alpha = 0.35f) else LocalAppColors.current.border,
                                                shape = RoundedCornerShape(8.dp),
                                            ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = preset.categoryIcon(),
                                        contentDescription = stringResource(preset.labelRes),
                                        modifier = Modifier.size(36.dp),
                                        tint = if (selected) MonoPrimary else OnboardingLabel.copy(alpha = 0.75f),
                                    )
                                }
                            }
                        }
                        if (selected) {
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .background(MonoPrimary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(painterResource(AppIcon.check), null, tint = MonoOnPrimary, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }

    if (otherPresetSelected) {
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = customVehicleType,
            onValueChange = onCustomTypeChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.driver_vehicle_custom_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors(),
        )
    }

    Spacer(Modifier.height(24.dp))

    // ── Color picker (before plate) + saisie « autre » sur l’écran principal ─
    OnboardingPickerField(
        label = stringResource(R.string.driver_field_color),
        value = selectedColorLabel,
        placeholder = stringResource(R.string.driver_placeholder_color),
        onClick = { showColorPicker = true },
    )
    if (otherColorSelected) {
        Spacer(Modifier.height(10.dp))
        OnboardingSmallField(
            label = stringResource(R.string.vehicle_color_other),
            value = customColorText,
            onValueChange = onCustomColorChange,
            placeholder = stringResource(R.string.driver_placeholder_color),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(14.dp))

    // ── Plate single field (TUN normalized by default) ───────────────────────
    OnboardingSmallField(
        label = stringResource(R.string.driver_plate_label),
        value = plateInput,
        onValueChange = { input ->
            onPlateInputChange(input.uppercase().filter { ch -> ch.isLetterOrDigit() || ch == ' ' }.take(16))
        },
        placeholder = stringResource(R.string.driver_plate_hint),
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(24.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleColorPickerSheet(
    selectedColorSlug: String,
    customColorText: String,
    onColorSelected: (String) -> Unit,
    onCustomColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val otherSelected = selectedColorSlug == "other"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = OnboardingPageBg,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.vehicle_color_picker_title),
                    color = OnboardingTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                BlackCloseIconButton(
                    onClick = onDismiss,
                    buttonSize = 32.dp,
                    iconSize = 18.dp,
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(vehicleColors) { colorDef ->
                    val selected = selectedColorSlug == colorDef.apiValue
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onColorSelected(colorDef.apiValue) }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorDef.hex))
                                    .border(1.dp, LocalAppColors.current.border, CircleShape),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(colorDef.nameRes),
                            color = if (selected) MonoPrimary else OnboardingTitle,
                            fontSize = 18.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    HorizontalDivider(color = LocalAppColors.current.surfaceMuted)
                }
                item {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onColorSelected("other") }
                                .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(AppIcon.edit),
                            contentDescription = null,
                            tint = if (otherSelected) MonoPrimary else OnboardingLabel,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.vehicle_color_other),
                            color = if (otherSelected) MonoPrimary else OnboardingTitle,
                            fontSize = 18.sp,
                            fontWeight = if (otherSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
