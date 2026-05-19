package tn.turbodrive.presentation.snapshots

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.map.AppMapDisplayMode
import tn.turbodrive.presentation.map.MapHomeTopHeader
import tn.turbodrive.presentation.map.MapSideFloatingControls
import tn.turbodrive.presentation.map.MapTypePickerPanel
import tn.turbodrive.presentation.map.PickupPinOverlay

/**
 * R-5.3 baseline snapshots for S32–S35 map components.
 *
 * S32 MapHomeTopHeader  — user avatar + brand header chrome
 * S33 MapSideFloatingControls — driver map layer + recenter FABs
 * S34 MapTypePickerPanel — style picker (Normal / Satellite / Hybrid)
 * S35 PickupPinOverlay   — destination teardrop pin + confirm bar
 *
 * 4 components × 2 themes = 8 baseline PNGs.
 */
@RunWith(JUnit4::class)
class MapComponentsBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    // ── S32 MapHomeTopHeader ────────────────────────────────────────────────

    @Test
    fun s32_mapHomeTopHeader_light() {
        paparazzi.snapshotLight {
            MapHomeTopHeader(
                avatarUrl = null,
                onProfileClick = {},
                walletAmountText = "12.50",
            )
        }
    }

    @Test
    fun s32_mapHomeTopHeader_dark() {
        paparazzi.snapshotDark {
            MapHomeTopHeader(
                avatarUrl = null,
                onProfileClick = {},
                walletAmountText = "12.50",
            )
        }
    }

    // ── S33 MapSideFloatingControls ─────────────────────────────────────────

    @Test
    fun s33_mapSideFloatingControls_light() {
        paparazzi.snapshotLight {
            MapSideFloatingControls(
                onLayersClick = {},
                onRecenterClick = {},
                recenterIconTint = Color.Black,
                compact = true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun s33_mapSideFloatingControls_dark() {
        paparazzi.snapshotDark {
            MapSideFloatingControls(
                onLayersClick = {},
                onRecenterClick = {},
                recenterIconTint = Color.White,
                compact = true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    // ── S34 MapTypePickerPanel ──────────────────────────────────────────────

    @Test
    fun s34_mapTypePickerPanel_light() {
        paparazzi.snapshotLight {
            MapTypePickerPanel(
                selected = AppMapDisplayMode.NORMAL,
                onSelect = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Test
    fun s34_mapTypePickerPanel_dark() {
        paparazzi.snapshotDark {
            MapTypePickerPanel(
                selected = AppMapDisplayMode.SATELLITE,
                onSelect = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    // ── S35 PickupPinOverlay ────────────────────────────────────────────────

    @Test
    fun s35_pickupPinOverlay_light() {
        paparazzi.snapshotLight {
            PickupPinOverlay(
                address = "Avenue Habib Bourguiba, Tunis",
                isLoading = false,
                onConfirm = {},
                isDestination = true,
                modifier =
                    Modifier
                        .size(width = 360.dp, height = 420.dp)
                        .wrapContentSize(),
            )
        }
    }

    @Test
    fun s35_pickupPinOverlay_dark() {
        paparazzi.snapshotDark {
            PickupPinOverlay(
                address = "Avenue Habib Bourguiba, Tunis",
                isLoading = false,
                onConfirm = {},
                isDestination = true,
                modifier =
                    Modifier
                        .size(width = 360.dp, height = 420.dp)
                        .wrapContentSize(),
            )
        }
    }
}
