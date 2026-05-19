package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.riderhome.SchedulePickerContent

/**
 * R-5.4 baseline snapshots for S13 SchedulePickerContent.
 *
 * S13 — Schedule picker: clock icon header, static wheel (day/hour/min/ampm),
 * selected time chip, confirm CTA. 1 component × 2 themes = 2 baseline PNGs.
 */
@RunWith(JUnit4::class)
class SchedulePickerBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun s13_schedulePicker_light() {
        paparazzi.snapshotLight {
            SchedulePickerContent(
                selectedLabel = "Aujourd'hui à 14:30",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }

    @Test
    fun s13_schedulePicker_dark() {
        paparazzi.snapshotDark {
            SchedulePickerContent(
                selectedLabel = "Aujourd'hui à 14:30",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
