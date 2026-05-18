package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.components.designsystem.LinearProgressTimer

/**
 * R-4.4 baseline : LinearProgressTimer (3 visual states × light/dark).
 *
 * All tests use [progressOverride] to freeze the timer so Paparazzi can
 * capture a deterministic frame.
 *
 * States : start (100% full, green), mid (50%, orange), end (5%, red).
 */
@RunWith(JUnit4::class)
class LinearProgressTimerBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun start_light() {
        paparazzi.snapshotLight {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 1.0f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }

    @Test
    fun start_dark() {
        paparazzi.snapshotDark {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 1.0f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }

    @Test
    fun mid_light() {
        paparazzi.snapshotLight {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 0.5f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }

    @Test
    fun mid_dark() {
        paparazzi.snapshotDark {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 0.5f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }

    @Test
    fun end_light() {
        paparazzi.snapshotLight {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 0.05f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }

    @Test
    fun end_dark() {
        paparazzi.snapshotDark {
            LinearProgressTimer(
                durationMs = 30_000,
                progressOverride = 0.05f,
                modifier = Modifier.width(300.dp).padding(16.dp),
            )
        }
    }
}
