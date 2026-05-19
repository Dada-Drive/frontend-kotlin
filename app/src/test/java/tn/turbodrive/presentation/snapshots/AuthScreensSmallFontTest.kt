package tn.turbodrive.presentation.snapshots

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.auth.AuthState
import tn.turbodrive.presentation.auth.NameEntryScreen
import tn.turbodrive.presentation.auth.NameEntryViewModel
import tn.turbodrive.presentation.auth.OtpEntryPhaseContent
import tn.turbodrive.presentation.auth.WelcomeScreen
import tn.turbodrive.presentation.common.ScreenState
import tn.turbodrive.presentation.onboarding.OnboardingScreenContent
import tn.turbodrive.presentation.role.RoleSelectionScreen
import tn.turbodrive.presentation.role.RoleViewModel
import tn.turbodrive.presentation.splash.SplashScreenLayout

/**
 * R-5.1 Session B — accessibility fontScale 0.85 (small) for 6 auth screens × 2 themes.
 *
 * Locks the dense-UI rendering for users who prefer the Android system "Small" font
 * preference. Each screen has both light and dark variants → 12 snapshots in this class.
 *
 * Pair with [AuthScreensLargeFontTest] (fontScale 1.3) to complete the 3-scale matrix.
 * The default scale (1.0) lives in each screen's individual `*BaselineTest` class.
 */
@RunWith(JUnit4::class)
class AuthScreensSmallFontTest {
    private val smallFont = 0.85f

    @get:Rule
    val paparazzi = createPaparazzi(fontScale = smallFont)

    private fun stubNameVm(): NameEntryViewModel {
        val vm = mockk<NameEntryViewModel>(relaxed = true)
        every { vm.state } returns MutableStateFlow(ScreenState.Idle)
        return vm
    }

    private fun stubRoleVm(): RoleViewModel {
        val vm = mockk<RoleViewModel>(relaxed = true)
        every { vm.state } returns MutableStateFlow(RoleViewModel.RoleState.Idle)
        return vm
    }

    @Test
    fun splash_light_smallFont() = paparazzi.snapshotLight { SplashScreenLayout(alpha = 1f) }

    @Test
    fun splash_dark_smallFont() = paparazzi.snapshotDark { SplashScreenLayout(alpha = 1f) }

    @Test
    fun welcome_light_smallFont() =
        paparazzi.snapshotLight {
            WelcomeScreen(
                onPhoneClick = {},
                onGoogleClick = {},
                onFacebookClick = {},
                onSkipClick = {},
                authState = AuthState.Idle,
                googleCooldownSeconds = 0,
            )
        }

    @Test
    fun welcome_dark_smallFont() =
        paparazzi.snapshotDark {
            WelcomeScreen(
                onPhoneClick = {},
                onGoogleClick = {},
                onFacebookClick = {},
                onSkipClick = {},
                authState = AuthState.Idle,
                googleCooldownSeconds = 0,
            )
        }

    @Test
    fun onboarding_light_smallFont() =
        paparazzi.snapshotLight {
            OnboardingScreenContent(onCompleteIntro = {}, onLastPageContinue = {})
        }

    @Test
    fun onboarding_dark_smallFont() =
        paparazzi.snapshotDark {
            OnboardingScreenContent(onCompleteIntro = {}, onLastPageContinue = {})
        }

    @Test
    fun otp_light_smallFont() =
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

    @Test
    fun otp_dark_smallFont() =
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

    @Test
    fun nameEntry_light_smallFont() =
        paparazzi.snapshotLight {
            NameEntryScreen(onContinue = {}, onBack = {}, viewModel = stubNameVm())
        }

    @Test
    fun nameEntry_dark_smallFont() =
        paparazzi.snapshotDark {
            NameEntryScreen(onContinue = {}, onBack = {}, viewModel = stubNameVm())
        }

    @Test
    fun roleSelection_light_smallFont() =
        paparazzi.snapshotLight {
            RoleSelectionScreen(onSuccess = {}, onBack = {}, viewModel = stubRoleVm())
        }

    @Test
    fun roleSelection_dark_smallFont() =
        paparazzi.snapshotDark {
            RoleSelectionScreen(onSuccess = {}, onBack = {}, viewModel = stubRoleVm())
        }
}
