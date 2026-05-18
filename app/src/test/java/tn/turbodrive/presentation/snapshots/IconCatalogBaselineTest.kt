package tn.turbodrive.presentation.snapshots

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors

/**
 * R-4.3 baseline : sample of the AppIcon registry.
 *
 * Renders a 4-column grid of 15 representative icons (Navigation, Map, Actions,
 * Status, Wallet, User). Goal : detect vector-drawable regressions when the
 * design system v2 is fine-tuned in R-4.5 (color/tint changes, stroke updates).
 *
 * Brand icons (Google/Facebook/etc.) are omitted -- they are rendered with
 * fixed brand colors and don't follow theme tint, so light/dark variants
 * would be identical.
 */
@RunWith(JUnit4::class)
class IconCatalogBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun iconCatalog_light() {
        paparazzi.snapshotLight {
            IconCatalogSample()
        }
    }

    @Test
    fun iconCatalog_dark() {
        paparazzi.snapshotDark {
            IconCatalogSample()
        }
    }
}

private data class IconSample(
    val name: String,
    @DrawableRes val res: Int,
)

private val sampleIcons =
    listOf(
        IconSample("arrowLeft", AppIcon.arrowLeft),
        IconSample("chevronRight", AppIcon.chevronRight),
        IconSample("close", AppIcon.close),
        IconSample("menu", AppIcon.menu),
        IconSample("mapPin", AppIcon.mapPin),
        IconSample("navigation", AppIcon.navigation),
        IconSample("car", AppIcon.car),
        IconSample("user", AppIcon.user),
        IconSample("wallet", AppIcon.wallet),
        IconSample("bell", AppIcon.bell),
        IconSample("settings", AppIcon.settings),
        IconSample("check", AppIcon.check),
        IconSample("alertTriangle", AppIcon.alertTriangle),
        IconSample("search", AppIcon.search),
        IconSample("phone", AppIcon.phone),
    )

@Composable
private fun IconCatalogSample() {
    val c = LocalAppColors.current
    Surface(color = c.surface, modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(sampleIcons) { sample ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(sample.res),
                        contentDescription = sample.name,
                        tint = c.textPrimary,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = sample.name, color = c.textSecondary)
                }
            }
        }
    }
}
