package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.auth.AuthState
import tn.turbodrive.presentation.auth.WelcomeScreen

/**
 * R-4.1 baseline : WelcomeScreen.
 *
 * No ViewModel : the screen takes 4 plain callbacks + an `AuthState`.
 * Snapshot uses `AuthState.Idle` (initial state, no error/cooldown banner).
 */
@RunWith(JUnit4::class)
class WelcomeScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun welcomeScreen_light() {
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
    }

    @Test
    fun welcomeScreen_dark() {
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
    }

    /**
     * R-5.1 : verrouille la couleur du border de l'error badge (1dp solid `error`,
     * pas `errorSoft` qui le rendait invisible). Cf. screens-auth.jsx:126-137.
     */
    @Test
    fun welcomeScreen_errorBanner_light() {
        paparazzi.snapshotLight {
            WelcomeScreen(
                onPhoneClick = {},
                onGoogleClick = {},
                onFacebookClick = {},
                onSkipClick = {},
                authState = AuthState.Error("Connexion impossible. Réessayez."),
                googleCooldownSeconds = 0,
            )
        }
    }
}
