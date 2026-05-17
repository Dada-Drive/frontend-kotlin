package tn.turbodrive.presentation.splash

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.core.theme.TurboDriveTheme

@RunWith(JUnit4::class)
class SplashScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.TurboDrive",
        )

    @Test
    fun splashScreen_light() {
        paparazzi.snapshot {
            TurboDriveTheme(darkTheme = false) {
                SplashScreenLayout(alpha = 1f)
            }
        }
    }
}
