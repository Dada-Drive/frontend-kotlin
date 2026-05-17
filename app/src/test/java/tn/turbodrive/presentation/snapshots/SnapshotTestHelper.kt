package tn.turbodrive.presentation.snapshots

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import tn.turbodrive.core.theme.TurboDriveSchemeDark
import tn.turbodrive.core.theme.TurboDriveSchemeLight
import tn.turbodrive.core.theme.TurboDriveTheme

/**
 * R-4.1 baseline snapshot helper.
 * Paparazzi rule factory + light/dark wrappers using TurboDriveTheme.
 *
 * Naming convention : test classes use `*BaselineTest` suffix and test methods
 * use `<screen>_light` / `<screen>_dark` so the generated PNGs sort naturally
 * in `app/src/test/snapshots/images/`. These files form the **baseline v1**
 * that will be compared against future v2 design changes in R-4.5.
 */
internal fun createPaparazzi(): Paparazzi =
    Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "Theme.TurboDrive",
    )

internal fun Paparazzi.snapshotLight(content: @Composable () -> Unit) {
    snapshot {
        TurboDriveTheme(darkTheme = false, appColors = TurboDriveSchemeLight) {
            content()
        }
    }
}

internal fun Paparazzi.snapshotDark(content: @Composable () -> Unit) {
    snapshot {
        TurboDriveTheme(darkTheme = true, appColors = TurboDriveSchemeDark) {
            content()
        }
    }
}
