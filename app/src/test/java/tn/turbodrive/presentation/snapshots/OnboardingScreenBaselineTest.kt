package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.onboarding.OnboardingScreenContent

/**
 * R-5.1 Session A baseline : OnboardingScreen.
 *
 * Captures page 0 (initial) with the pager dots in the active-first state.
 * No ViewModel — the screen takes a single completion callback.
 *
 * Locks redesign §S02 (turbodrive_redesign/screens-auth.jsx:33-81) :
 * - 3-page pager with active dot 24×8dp (was 28×8dp before R-5.1)
 * - 6dp gap between dots (was 8dp effective before R-5.1)
 */
@RunWith(JUnit4::class)
class OnboardingScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun onboardingScreen_light() {
        paparazzi.snapshotLight {
            OnboardingScreenContent(
                onCompleteIntro = {},
                onLastPageContinue = {},
            )
        }
    }

    @Test
    fun onboardingScreen_dark() {
        paparazzi.snapshotDark {
            OnboardingScreenContent(
                onCompleteIntro = {},
                onLastPageContinue = {},
            )
        }
    }
}
