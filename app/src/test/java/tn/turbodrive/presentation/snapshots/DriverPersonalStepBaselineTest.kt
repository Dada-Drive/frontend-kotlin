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
import tn.turbodrive.domain.models.OcrDocType
import tn.turbodrive.domain.models.OcrFields
import tn.turbodrive.domain.models.OcrLifecycle
import tn.turbodrive.domain.models.OcrPollResult
import tn.turbodrive.domain.models.OcrUploadedDocument
import tn.turbodrive.domain.usecases.driver.OcrProgress
import tn.turbodrive.presentation.driversetup.DriverPersonalStep
import tn.turbodrive.presentation.snapshots.stubs.ProvideStubActivityResultRegistry

/**
 * R-5.2 baseline : DriverPersonalStep (step 1 of driver onboarding).
 *
 * Renders the stateless composable directly — no VM, no Hilt — so the
 * snapshot focuses on visual regression of the CIN capture + form layout.
 * Includes light variants where the OCR banner is mounted, locking
 * the Processing and Ready visual states.
 */
@RunWith(JUnit4::class)
class DriverPersonalStepBaselineTest {
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
    fun driverPersonalStepEmpty_light() {
        paparazzi.snapshotLight {
            ScrollableScope {
                DriverPersonalStep(
                    cinFrontBmp = null,
                    cinBackBmp = null,
                    cinNumber = "",
                    cinDeliveredAt = "",
                    cinOcrProgress = null,
                    onCinFrontFileCaptured = { _, _ -> },
                    onCinFrontRetake = {},
                    onCinBackClick = {},
                    onCinNumberChange = {},
                    onCinDateChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }

    @Test
    fun driverPersonalStepEmpty_dark() {
        paparazzi.snapshotDark {
            ScrollableScope {
                DriverPersonalStep(
                    cinFrontBmp = null,
                    cinBackBmp = null,
                    cinNumber = "",
                    cinDeliveredAt = "",
                    cinOcrProgress = null,
                    onCinFrontFileCaptured = { _, _ -> },
                    onCinFrontRetake = {},
                    onCinBackClick = {},
                    onCinNumberChange = {},
                    onCinDateChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }

    @Test
    fun driverPersonalStepOcrProcessing_light() {
        paparazzi.snapshotLight {
            ScrollableScope {
                DriverPersonalStep(
                    cinFrontBmp = null,
                    cinBackBmp = null,
                    cinNumber = "",
                    cinDeliveredAt = "",
                    cinOcrProgress =
                        OcrProgress.Processing(
                            uploaded =
                                OcrUploadedDocument(
                                    id = "doc-1",
                                    url = null,
                                    docType = OcrDocType.Cin,
                                    status = OcrLifecycle.Processing,
                                ),
                        ),
                    onCinFrontFileCaptured = { _, _ -> },
                    onCinFrontRetake = {},
                    onCinBackClick = {},
                    onCinNumberChange = {},
                    onCinDateChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }

    @Test
    fun driverPersonalStepOcrReady_light() {
        paparazzi.snapshotLight {
            ScrollableScope {
                DriverPersonalStep(
                    cinFrontBmp = null,
                    cinBackBmp = null,
                    cinNumber = "09876543",
                    cinDeliveredAt = "",
                    cinOcrProgress =
                        OcrProgress.Ready(
                            result =
                                OcrPollResult(
                                    documentId = "doc-1",
                                    docType = OcrDocType.Cin,
                                    status = OcrLifecycle.Ready,
                                    fields = OcrFields(numeroCin = "09876543"),
                                    confidence = 0.92,
                                    rejectionReason = null,
                                ),
                        ),
                    onCinFrontFileCaptured = { _, _ -> },
                    onCinFrontRetake = {},
                    onCinBackClick = {},
                    onCinNumberChange = {},
                    onCinDateChange = {},
                    titleFontSize = 28.sp,
                )
            }
        }
    }
}
