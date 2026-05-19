package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.riderhome.RiderHomeBottomSheet

/**
 * R-5.4 baseline snapshots for S10 RiderHomeBottomSheet.
 *
 * HereMapViewComposable does not render in JVM Paparazzi tests (requires Android device).
 * RiderHomeBottomSheet is the extractable overlay composable of RiderHomeScreen (S10 idle state).
 * 1 component × 2 themes = 2 baseline PNGs.
 */
@RunWith(JUnit4::class)
class RiderHomeBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun s10_riderHomeBottomSheet_light() {
        paparazzi.snapshotLight {
            RiderHomeBottomSheet(
                firstName = "Ahmed",
                onSearchClick = {},
                onScheduleClick = {},
                onShortcutClick = {},
            )
        }
    }

    @Test
    fun s10_riderHomeBottomSheet_dark() {
        paparazzi.snapshotDark {
            RiderHomeBottomSheet(
                firstName = "Ahmed",
                onSearchClick = {},
                onScheduleClick = {},
                onShortcutClick = {},
            )
        }
    }
}
