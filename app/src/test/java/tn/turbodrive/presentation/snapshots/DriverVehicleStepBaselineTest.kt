package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.driversetup.DriverVehiclePreset
import tn.turbodrive.presentation.driversetup.DriverVehicleStep

/**
 * R-5.2 baseline : DriverVehicleStep (step 3 of driver onboarding).
 *
 * No OCR for vehicle V1 (carte_grise + assurance deferred to a later
 * phase). Locks the 3-slot photo grid + make/model/year fields + plate +
 * service category preset row in their empty state.
 */
@RunWith(JUnit4::class)
class DriverVehicleStepBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun driverVehicleStepEmpty_light() {
        paparazzi.snapshotLight {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            ) {
                DriverVehicleStep(
                    vehicleFrontBmp = null,
                    vehicleSideBmp = null,
                    vehicleBackBmp = null,
                    vehicleMake = "",
                    vehicleModel = "",
                    vehicleYear = "",
                    selectedColorSlug = "",
                    customColorText = "",
                    plateInput = "",
                    vehiclePreset = DriverVehiclePreset.Citadine,
                    customVehicleType = "",
                    seatInput = "",
                    onVehicleFrontClick = {},
                    onVehicleSideClick = {},
                    onVehicleBackClick = {},
                    onMakeChange = {},
                    onModelChange = {},
                    onYearChange = {},
                    onColorSlugChange = {},
                    onCustomColorChange = {},
                    onPlateInputChange = {},
                    onPresetChange = {},
                    onCustomTypeChange = {},
                    onSeatChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }

    @Test
    fun driverVehicleStepEmpty_dark() {
        paparazzi.snapshotDark {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            ) {
                DriverVehicleStep(
                    vehicleFrontBmp = null,
                    vehicleSideBmp = null,
                    vehicleBackBmp = null,
                    vehicleMake = "",
                    vehicleModel = "",
                    vehicleYear = "",
                    selectedColorSlug = "",
                    customColorText = "",
                    plateInput = "",
                    vehiclePreset = DriverVehiclePreset.Citadine,
                    customVehicleType = "",
                    seatInput = "",
                    onVehicleFrontClick = {},
                    onVehicleSideClick = {},
                    onVehicleBackClick = {},
                    onMakeChange = {},
                    onModelChange = {},
                    onYearChange = {},
                    onColorSlugChange = {},
                    onCustomColorChange = {},
                    onPlateInputChange = {},
                    onPresetChange = {},
                    onCustomTypeChange = {},
                    onSeatChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }
}
