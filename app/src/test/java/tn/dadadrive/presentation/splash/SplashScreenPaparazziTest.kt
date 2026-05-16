package tn.dadadrive.presentation.splash

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.dadadrive.core.theme.DadaDriveTheme

@RunWith(JUnit4::class)
class SplashScreenPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "Theme.DadaDrive",
    )

    @Test
    fun splashScreen_light() {
        paparazzi.snapshot {
            DadaDriveTheme(darkTheme = false) {
                SplashScreenLayout(alpha = 1f)
            }
        }
    }
}
