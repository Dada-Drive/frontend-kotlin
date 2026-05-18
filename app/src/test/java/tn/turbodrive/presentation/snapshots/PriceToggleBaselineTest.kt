package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.components.designsystem.PriceToggle

/**
 * R-4.4 baseline : PriceToggle (3 visual states × light/dark).
 *
 * States : twoOptionsDefault (TND selected), twoOptionsSwapped (USD selected),
 *          threeOptionsDisabled (3 options, disabled).
 */
@RunWith(JUnit4::class)
class PriceToggleBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun twoOptionsDefault_light() {
        paparazzi.snapshotLight {
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "TND",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
            )
        }
    }

    @Test
    fun twoOptionsDefault_dark() {
        paparazzi.snapshotDark {
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "TND",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
            )
        }
    }

    @Test
    fun twoOptionsSwapped_light() {
        paparazzi.snapshotLight {
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "USD",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
            )
        }
    }

    @Test
    fun twoOptionsSwapped_dark() {
        paparazzi.snapshotDark {
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "USD",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
            )
        }
    }

    @Test
    fun threeOptionsDisabled_light() {
        paparazzi.snapshotLight {
            PriceToggle(
                options = listOf("Cash", "Wallet", "Card"),
                selected = "Wallet",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
                enabled = false,
            )
        }
    }

    @Test
    fun threeOptionsDisabled_dark() {
        paparazzi.snapshotDark {
            PriceToggle(
                options = listOf("Cash", "Wallet", "Card"),
                selected = "Wallet",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.padding(16.dp).wrapContentWidth(),
                enabled = false,
            )
        }
    }
}
