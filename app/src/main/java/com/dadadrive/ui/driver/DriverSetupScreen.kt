// Équivalent Swift : Presentation/Driver/DriverSetupView.swift
package com.dadadrive.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.domain.model.VehicleType
import com.dadadrive.ui.theme.LocalAppColors
import java.util.Calendar

private enum class SetupStep { License, Vehicle }

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

    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var plateSuffix by remember { mutableStateOf("") }
    var plateLetters by remember { mutableStateOf("") }
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
    val plateOk = plateSuffix.length == 3 && plateSuffix.all { it.isDigit() } &&
        plateLetters.isNotBlank() &&
        vehicleMake.trim().isNotEmpty() && vehicleModel.trim().isNotEmpty() &&
        vehicleColor.trim().isNotEmpty() && yearInt >= 1990
    val fullPlate = "${plateSuffix.trim()} TUN ${plateLetters.trim()}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.darkSurface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            if (step == SetupStep.License) "Driver license" else "Your vehicle",
            color = c.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        when (step) {
            SetupStep.License -> {
                Text("License number", color = c.textSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = licenseSuffix,
                    onValueChange = { licenseSuffix = it },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("TN-") },
                    label = { Text("Suffix") },
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expY.toString(),
                        onValueChange = { it.toIntOrNull()?.let { y -> expY = y } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Year") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expM.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> expM = m.coerceIn(1, 12) } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Month") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expD.toString(),
                        onValueChange = { it.toIntOrNull()?.let { d -> expD = d.coerceIn(1, 31) } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Day") },
                        singleLine = true
                    )
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { step = SetupStep.Vehicle },
                    enabled = licenseOk && !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    Text("Next", color = c.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            SetupStep.Vehicle -> {
                OutlinedTextField(
                    value = vehicleMake,
                    onValueChange = { vehicleMake = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Make") }
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { vehicleModel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Model") }
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vehicleYear,
                    onValueChange = { vehicleYear = it.filter { ch -> ch.isDigit() }.take(4) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Year") }
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = plateSuffix,
                        onValueChange = { plateSuffix = it.filter { ch -> ch.isDigit() }.take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("3 digits") }
                    )
                    OutlinedTextField(
                        value = plateLetters,
                        onValueChange = { plateLetters = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Letters") }
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vehicleColor,
                    onValueChange = { vehicleColor = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Color") }
                )
                Spacer(Modifier.height(12.dp))
                Text("Vehicle type", color = c.textSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.entries.forEach { t ->
                        FilterChip(
                            selected = vehicleType == t,
                            onClick = { vehicleType = t },
                            label = { Text(t.name, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.submitDriverSetup(
                            fullLicenseNumber = fullLicense,
                            licenseExpiry = licenseExpiry,
                            make = vehicleMake.trim(),
                            model = vehicleModel.trim(),
                            year = yearInt,
                            plateNumber = fullPlate,
                            color = vehicleColor.trim(),
                            vehicleType = vehicleType,
                            onComplete = onComplete
                        )
                    },
                    enabled = plateOk && !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp),
                            color = c.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Submit", color = c.onPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        vmError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = c.errorRed, fontSize = 13.sp)
        }
    }
}
