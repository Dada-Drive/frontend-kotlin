package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.splash.SplashScreenLayout

/**
 * R-4.1 baseline : SplashScreen.
 *
 * The stateless `SplashScreenLayout(alpha)` composable is snapshotted with
 * `alpha = 1f` (fully shown) for both themes. The animated splash sequence
 * is not relevant for visual baseline — only the rest state matters.
 */
@RunWith(JUnit4::class)
class SplashScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun splashScreen_light() {
        paparazzi.snapshotLight {
            SplashScreenLayout(alpha = 1f)
        }
    }

    @Test
    fun splashScreen_dark() {
        paparazzi.snapshotDark {
            SplashScreenLayout(alpha = 1f)
        }
    }
}
