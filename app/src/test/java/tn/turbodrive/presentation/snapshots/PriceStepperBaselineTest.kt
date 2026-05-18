package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.components.designsystem.PriceStepper

/**
 * R-4.4 baseline : PriceStepper (4 visual states × light/dark).
 *
 * States : default, atMin (decrement disabled + boundary hint),
 *          atMax (increment disabled + boundary hint), disabled.
 */
@RunWith(JUnit4::class)
class PriceStepperBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun default_light() {
        paparazzi.snapshotLight {
            PriceStepper(
                value = 15.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun default_dark() {
        paparazzi.snapshotDark {
            PriceStepper(
                value = 15.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun atMin_light() {
        paparazzi.snapshotLight {
            PriceStepper(
                value = 5.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                boundaryHint = "Limite −1 TND",
            )
        }
    }

    @Test
    fun atMin_dark() {
        paparazzi.snapshotDark {
            PriceStepper(
                value = 5.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                boundaryHint = "Limite −1 TND",
            )
        }
    }

    @Test
    fun atMax_light() {
        paparazzi.snapshotLight {
            PriceStepper(
                value = 50.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                boundaryHint = "Limite +1 TND",
            )
        }
    }

    @Test
    fun atMax_dark() {
        paparazzi.snapshotDark {
            PriceStepper(
                value = 50.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                boundaryHint = "Limite +1 TND",
            )
        }
    }

    @Test
    fun disabled_light() {
        paparazzi.snapshotLight {
            PriceStepper(
                value = 15.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                enabled = false,
            )
        }
    }

    @Test
    fun disabled_dark() {
        paparazzi.snapshotDark {
            PriceStepper(
                value = 15.0,
                onValueChange = {},
                min = 5.0,
                max = 50.0,
                modifier = Modifier.padding(16.dp),
                enabled = false,
            )
        }
    }
}
