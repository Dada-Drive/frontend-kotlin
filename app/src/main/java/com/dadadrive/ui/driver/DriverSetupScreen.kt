// Équivalent Swift : Presentation/Driver/DriverSetupView.swift
package com.dadadrive.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.R
import com.dadadrive.domain.model.VehicleType
import com.dadadrive.ui.theme.LocalAppColors
import java.util.Calendar

private enum class SetupStep { License, Vehicle }

private val OnboardingPageBg = Color(0xFFF2F2F2)
private val OnboardingFieldBg = Color.White
private val OnboardingTitle = Color(0xFF111111)
private val OnboardingSubtitle = Color(0xFF616161)
private val OnboardingLabel = Color(0xFF757575)
private val OnboardingStepInactiveCircle = Color(0xFFE0E0E0)
private val OnboardingStepInactiveNumber = Color(0xFF757575)

@Composable
private fun VehicleType.localizedLabel(): String = stringResource(
    when (this) {
        VehicleType.Economy -> R.string.driver_vehicle_economy
        VehicleType.Comfort -> R.string.driver_vehicle_comfort
        VehicleType.Xl -> R.string.driver_vehicle_xl
        VehicleType.Van -> R.string.driver_vehicle_van
    }
)

@Composable
fun DriverSetupScreen(
    onComplete: () -> Unit,
    viewModel: DriverSetupViewModel = hiltViewModel()
) {
    val c = LocalAppColors.current
    val loading by viewModel.loading.collectAsState()
    val vmError by viewModel.error.collectAsState()

    var step by remember { mutableStateOf(SetupStep.License) }

    var licenseSuffix by remember { mutableStateOf("") }
    var expY by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR) + 1) }
    var expM by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var expD by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    var viewCalY by remember { mutableIntStateOf(expY) }
    var viewCalM by remember { mutableIntStateOf(expM) }

    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var plateInput by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf(VehicleType.Economy) }

    val fullLicense = "TN-${licenseSuffix.trim()}"
    val licenseExpiry = String.format("%04d-%02d-%02d", expY, expM, expD)
    val licenseOk = licenseSuffix.trim().isNotEmpty() && run {
        val cal = Calendar.getInstance()
        cal.set(expY, expM - 1, expD, 23, 59, 59)
        cal.timeInMillis > System.currentTimeMillis()
    }
    val yearInt = vehicleYear.toIntOrNull() ?: 0
    val normalizedPlate = remember(plateInput) {
        val m = Regex("(\\d{3})\\s*[Tt][Uu][Nn]\\s*(\\S+)", RegexOption.IGNORE_CASE)
            .find(plateInput.trim())
        m?.let { "${it.groupValues[1]} TUN ${it.groupValues[2]}" }
    }
    val plateOk = normalizedPlate != null &&
        vehicleMake.trim().isNotEmpty() &&
        vehicleModel.trim().isNotEmpty() &&
        vehicleColor.trim().isNotEmpty() &&
        yearInt >= 1990

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingPageBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        DriverSetupStepper(step = step, brandPrimary = c.primary)

        Spacer(Modifier.height(24.dp))

        when (step) {
            SetupStep.License -> {
                Text(
                    text = stringResource(R.string.driver_license_title),
                    color = OnboardingTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.driver_license_subtitle),
                    color = OnboardingSubtitle,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(28.dp))

                Text(stringResource(R.string.driver_license_number_label), color = OnboardingLabel, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = licenseSuffix,
                    onValueChange = { licenseSuffix = it },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("TN-", color = OnboardingTitle, fontWeight = FontWeight.Medium) },
                    placeholder = { Text(stringResource(R.string.driver_license_suffix_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = onboardingFieldColors()
                )

                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.driver_license_expiry_label), color = OnboardingLabel, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                LicenseExpiryCalendar(
                    viewYear = viewCalY,
                    viewMonth = viewCalM,
                    selectedY = expY,
                    selectedM = expM,
                    selectedD = expD,
                    brandPrimary = c.primary,
                    onPrevMonth = {
                        if (viewCalM == 1) {
                            viewCalM = 12
                            viewCalY--
                        } else {
                            viewCalM--
                        }
                    },
                    onNextMonth = {
                        if (viewCalM == 12) {
                            viewCalM = 1
                            viewCalY++
                        } else {
                            viewCalM++
                        }
                    },
                    onSelectDay = { y, m, d ->
                        expY = y
                        expM = m
                        expD = d
                    }
                )

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { step = SetupStep.Vehicle },
                    enabled = licenseOk && !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.primary,
                        contentColor = c.onPrimary,
                        disabledContainerColor = c.primaryDisabled,
                        disabledContentColor = c.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(stringResource(R.string.auth_continue), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            SetupStep.Vehicle -> {
                Text(
                    text = stringResource(R.string.driver_vehicle_title),
                    color = OnboardingTitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.driver_vehicle_subtitle),
                    color = OnboardingSubtitle,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OnboardingSmallField(
                        label = stringResource(R.string.driver_field_make),
                        value = vehicleMake,
                        onValueChange = { vehicleMake = it },
                        placeholder = stringResource(R.string.driver_placeholder_make),
                        modifier = Modifier.weight(1f)
                    )
                    OnboardingSmallField(
                        label = stringResource(R.string.driver_field_model),
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        placeholder = stringResource(R.string.driver_placeholder_model),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OnboardingSmallField(
                        label = stringResource(R.string.driver_field_year),
                        value = vehicleYear,
                        onValueChange = { vehicleYear = it.filter { ch -> ch.isDigit() }.take(4) },
                        placeholder = stringResource(R.string.driver_placeholder_year),
                        modifier = Modifier.weight(1f)
                    )
                    OnboardingSmallField(
                        label = stringResource(R.string.driver_field_color),
                        value = vehicleColor,
                        onValueChange = { vehicleColor = it },
                        placeholder = stringResource(R.string.driver_placeholder_color),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.driver_plate_label), color = OnboardingLabel, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = plateInput,
                    onValueChange = { plateInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.driver_plate_hint), color = OnboardingLabel.copy(alpha = 0.65f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = onboardingFieldColors()
                )

                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.driver_vehicle_type_label), color = OnboardingLabel, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.entries.forEach { t ->
                        val selected = vehicleType == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) c.primary else OnboardingFieldBg)
                                .then(
                                    if (!selected) Modifier.border(
                                        1.dp,
                                        Color(0xFFD0D0D0),
                                        RoundedCornerShape(20.dp)
                                    ) else Modifier
                                )
                                .clickable { vehicleType = t }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t.localizedLabel(),
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) c.onPrimary else OnboardingTitle,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                vmError?.let {
                    Text(
                        text = it,
                        color = c.errorRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { step = SetupStep.License },
                        modifier = Modifier
                            .weight(0.38f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnboardingFieldBg,
                            contentColor = OnboardingTitle
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD0D0D0))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(stringResource(R.string.driver_back), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    Button(
                        onClick = {
                            val plate = normalizedPlate ?: return@Button
                            viewModel.submitDriverSetup(
                                fullLicenseNumber = fullLicense,
                                licenseExpiry = licenseExpiry,
                                make = vehicleMake.trim(),
                                model = vehicleModel.trim(),
                                year = yearInt,
                                plateNumber = plate,
                                color = vehicleColor.trim(),
                                vehicleType = vehicleType,
                                onComplete = onComplete
                            )
                        },
                        enabled = plateOk && !loading,
                        modifier = Modifier
                            .weight(0.62f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = c.primary,
                            contentColor = c.onPrimary,
                            disabledContainerColor = c.primaryDisabled,
                            disabledContentColor = c.onPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = c.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.driver_submit), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun onboardingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OnboardingTitle,
    unfocusedTextColor = OnboardingTitle,
    focusedContainerColor = OnboardingFieldBg,
    unfocusedContainerColor = OnboardingFieldBg,
    disabledContainerColor = OnboardingFieldBg,
    focusedBorderColor = Color(0xFFCCCCCC),
    unfocusedBorderColor = Color(0xFFE0E0E0),
    cursorColor = OnboardingTitle
)

@Composable
private fun OnboardingSmallField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, color = OnboardingLabel, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = OnboardingLabel.copy(alpha = 0.65f)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = onboardingFieldColors()
        )
    }
}

@Composable
private fun DriverSetupStepper(step: SetupStep, brandPrimary: Color) {
    val labelLicense = stringResource(R.string.driver_tab_license)
    val labelVehicle = stringResource(R.string.driver_tab_vehicle)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperColumn(
            stepIndex = 1,
            label = labelLicense,
            isActive = step == SetupStep.License,
            isComplete = step == SetupStep.Vehicle,
            brandPrimary = brandPrimary
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(2.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(1.dp))
                .align(Alignment.CenterVertically)
        )
        StepperColumn(
            stepIndex = 2,
            label = labelVehicle,
            isActive = step == SetupStep.Vehicle,
            isComplete = false,
            brandPrimary = brandPrimary
        )
    }
}

@Composable
private fun StepperColumn(
    stepIndex: Int,
    label: String,
    isActive: Boolean,
    isComplete: Boolean,
    brandPrimary: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isComplete || isActive -> brandPrimary
                        else -> OnboardingStepInactiveCircle
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isComplete -> Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = contentOnPrimaryForBrand(brandPrimary),
                    modifier = Modifier.size(20.dp)
                )
                else -> Text(
                    text = "$stepIndex",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isActive) contentOnPrimaryForBrand(brandPrimary) else OnboardingStepInactiveNumber
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isActive || isComplete) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isActive || isComplete -> brandPrimary
                else -> OnboardingStepInactiveNumber
            }
        )
    }
}

private fun contentOnPrimaryForBrand(primary: Color): Color =
    if (primary.luminance() > 0.5f) Color.Black else Color.White

@Composable
private fun LicenseExpiryCalendar(
    viewYear: Int,
    viewMonth: Int,
    selectedY: Int,
    selectedM: Int,
    selectedD: Int,
    brandPrimary: Color,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (y: Int, m: Int, d: Int) -> Unit
) {
    val monthName = Calendar.getInstance().apply { set(viewYear, viewMonth - 1, 1) }
        .getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OnboardingFieldBg)
            .border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_prev_month),
                    tint = brandPrimary
                )
            }
            Text(
                text = "$monthName $viewYear",
                fontWeight = FontWeight.SemiBold,
                color = OnboardingTitle,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_month),
                    tint = brandPrimary
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        val weekdays = stringArrayResource(R.array.calendar_weekdays_short)
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = OnboardingLabel,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        val daysInMonth = Calendar.getInstance().run {
            set(viewYear, viewMonth - 1, 1)
            getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        val firstDow = Calendar.getInstance().run {
            set(viewYear, viewMonth - 1, 1)
            // Monday = 0 … Sunday = 6
            (get(Calendar.DAY_OF_WEEK) + 5) % 7
        }

        val cells = firstDow + daysInMonth
        val rows = (cells + 6) / 7

        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val idx = row * 7 + col
                    val dayNum = idx - firstDow + 1
                    if (dayNum < 1 || dayNum > daysInMonth) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val selectable = isFutureOrTodayEnd(viewYear, viewMonth, dayNum)
                        val selected =
                            viewYear == selectedY && viewMonth == selectedM && dayNum == selectedD
                        val contentColor = when {
                            selected -> contentOnPrimaryForBrand(brandPrimary)
                            selectable -> OnboardingTitle
                            else -> OnboardingLabel.copy(alpha = 0.35f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selected) brandPrimary.copy(alpha = 0.85f) else Color.Transparent
                                )
                                .clickable(enabled = selectable) {
                                    onSelectDay(viewYear, viewMonth, dayNum)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isFutureOrTodayEnd(y: Int, m: Int, d: Int): Boolean {
    val cal = Calendar.getInstance()
    cal.set(y, m - 1, d, 23, 59, 59)
    return cal.timeInMillis > System.currentTimeMillis()
}
