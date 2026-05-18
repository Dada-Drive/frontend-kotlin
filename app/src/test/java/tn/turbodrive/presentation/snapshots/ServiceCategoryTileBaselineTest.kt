package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.presentation.components.designsystem.ServiceCategoryTile

/**
 * R-4.4 baseline : ServiceCategoryTile (3 visual states × light/dark).
 *
 * States : default, selected, disabled.
 */
@RunWith(JUnit4::class)
class ServiceCategoryTileBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun default_light() {
        paparazzi.snapshotLight {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun default_dark() {
        paparazzi.snapshotDark {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun selected_light() {
        paparazzi.snapshotLight {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = true,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun selected_dark() {
        paparazzi.snapshotDark {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = true,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun disabled_light() {
        paparazzi.snapshotLight {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
                modifier = Modifier.padding(16.dp),
                enabled = false,
            )
        }
    }

    @Test
    fun disabled_dark() {
        paparazzi.snapshotDark {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
                modifier = Modifier.padding(16.dp),
                enabled = false,
            )
        }
    }
}
