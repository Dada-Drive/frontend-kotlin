package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.driversetup.DriverLicenseStep
import tn.turbodrive.presentation.snapshots.stubs.ProvideStubActivityResultRegistry

/**
 * R-5.2 baseline : DriverLicenseStep (step 2 of driver onboarding).
 *
 * Locks the layout of the license front (OCR-capable) + back placeholders
 * and the 4-button category row. Photos and OCR progress remain null —
 * empty default state of the form.
 */
@RunWith(JUnit4::class)
class DriverLicenseStepBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Composable
    private fun ScrollableScope(content: @Composable () -> Unit) {
        ProvideStubActivityResultRegistry {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            ) {
                content()
            }
        }
    }

    @Test
    fun driverLicenseStepEmpty_light() {
        paparazzi.snapshotLight {
            ScrollableScope {
                DriverLicenseStep(
                    licenseFrontBmp = null,
                    licenseBackBmp = null,
                    licenseSuffix = "",
                    licenseIssueInput = "",
                    licenseExpiryInput = "",
                    licenseCategories = setOf('B'),
                    permisOcrProgress = null,
                    onLicenseFrontFileCaptured = { _, _ -> },
                    onLicenseFrontRetake = {},
                    onLicenseBackClick = {},
                    onSuffixChange = {},
                    onIssueChange = {},
                    onExpiryChange = {},
                    onLicenseCategoryToggle = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }

    @Test
    fun driverLicenseStepEmpty_dark() {
        paparazzi.snapshotDark {
            ScrollableScope {
                DriverLicenseStep(
                    licenseFrontBmp = null,
                    licenseBackBmp = null,
                    licenseSuffix = "",
                    licenseIssueInput = "",
                    licenseExpiryInput = "",
                    licenseCategories = setOf('B'),
                    permisOcrProgress = null,
                    onLicenseFrontFileCaptured = { _, _ -> },
                    onLicenseFrontRetake = {},
                    onLicenseBackClick = {},
                    onSuffixChange = {},
                    onIssueChange = {},
                    onExpiryChange = {},
                    onLicenseCategoryToggle = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }
}
