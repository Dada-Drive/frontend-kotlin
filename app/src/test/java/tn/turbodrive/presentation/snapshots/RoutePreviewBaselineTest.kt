package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.riderhome.RoutePreviewBottomSheet

/**
 * R-5.4 baseline snapshots for S12 RoutePreviewBottomSheet.
 *
 * S12 (plan) / S14 (JSX) — Route + fare estimate sheet:
 * route summary, category chip, fare estimate card, payment row, CTA.
 * 1 component × 2 themes = 2 baseline PNGs.
 */
@RunWith(JUnit4::class)
class RoutePreviewBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun s12_routePreview_light() {
        paparazzi.snapshotLight {
            RoutePreviewBottomSheet(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "Aéroport Tunis-Carthage",
                fareAmountTnd = "10",
                vehicleCategory = "Cours partagé",
                onRequestRide = {},
                onChangeCategory = {},
            )
        }
    }

    @Test
    fun s12_routePreview_dark() {
        paparazzi.snapshotDark {
            RoutePreviewBottomSheet(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "Aéroport Tunis-Carthage",
                fareAmountTnd = "10",
                vehicleCategory = "Cours partagé",
                onRequestRide = {},
                onChangeCategory = {},
            )
        }
    }
}
