package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.auth.OtpEntryPhaseContent

/**
 * R-5.1 Session B baseline : OTP entry phase (S06).
 *
 * Snapshots cover the visual states locked by the redesign spec
 * (`turbodrive_redesign/screens-auth.jsx:300-371`) :
 * - Empty cells (resend timer active)
 * - Partially filled cells (focus indicator on next)
 * - Error state (red border + errorSoft banner with alert-triangle icon)
 * - Resend link active (countdown elapsed)
 *
 * The composable is stateless (W2 extraction from PhoneScreen) so all states
 * are driven purely by parameters — no VM, no LaunchedEffects in scope.
 */
@RunWith(JUnit4::class)
class OtpEntryPhaseBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun otpEmpty_light() {
        paparazzi.snapshotLight {
            OtpEntryPhaseContent(
                displayPhone = "+216 20 123 456",
                otpCode = "",
                onOtpCodeChange = {},
                isError = false,
                errorMessage = null,
                resendCooldown = 45,
                onBack = {},
                onResend = {},
                onKeypadDigit = {},
                onKeypadBackspace = {},
            )
        }
    }

    @Test
    fun otpEmpty_dark() {
        paparazzi.snapshotDark {
            OtpEntryPhaseContent(
                displayPhone = "+216 20 123 456",
                otpCode = "",
                onOtpCodeChange = {},
                isError = false,
                errorMessage = null,
                resendCooldown = 45,
                onBack = {},
                onResend = {},
                onKeypadDigit = {},
                onKeypadBackspace = {},
            )
        }
    }

    @Test
    fun otpPartial_light() {
        paparazzi.snapshotLight {
            OtpEntryPhaseContent(
                displayPhone = "+216 20 123 456",
                otpCode = "123",
                onOtpCodeChange = {},
                isError = false,
                errorMessage = null,
                resendCooldown = 30,
                onBack = {},
                onResend = {},
                onKeypadDigit = {},
                onKeypadBackspace = {},
            )
        }
    }

    @Test
    fun otpError_light() {
        paparazzi.snapshotLight {
            OtpEntryPhaseContent(
                displayPhone = "+216 20 123 456",
                otpCode = "1234",
                onOtpCodeChange = {},
                isError = true,
                errorMessage = "Code incorrect. Réessayez.",
                resendCooldown = 20,
                onBack = {},
                onResend = {},
                onKeypadDigit = {},
                onKeypadBackspace = {},
            )
        }
    }

    @Test
    fun otpResendActive_light() {
        paparazzi.snapshotLight {
            OtpEntryPhaseContent(
                displayPhone = "+216 20 123 456",
                otpCode = "",
                onOtpCodeChange = {},
                isError = false,
                errorMessage = null,
                resendCooldown = 0,
                onBack = {},
                onResend = {},
                onKeypadDigit = {},
                onKeypadBackspace = {},
            )
        }
    }
}
