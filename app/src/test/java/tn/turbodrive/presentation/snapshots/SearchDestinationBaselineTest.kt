package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.riderhome.SearchDestinationContent

/**
 * R-5.4 baseline snapshots for S11 SearchDestinationContent.
 *
 * S11 — Route input bottom sheet: origin/destination fields + connecting line +
 * "Ajouter un arrêt" chip + schedule footer + map picker CTA.
 * 1 component × 2 themes = 2 baseline PNGs.
 */
@RunWith(JUnit4::class)
class SearchDestinationBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun s11_searchDestination_light() {
        paparazzi.snapshotLight {
            SearchDestinationContent(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "",
                onOriginChange = {},
                onDestinationChange = {},
                onAddStop = {},
                onScheduleClick = {},
                onMapPickerClick = {},
                onBack = {},
            )
        }
    }

    @Test
    fun s11_searchDestination_dark() {
        paparazzi.snapshotDark {
            SearchDestinationContent(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "",
                onOriginChange = {},
                onDestinationChange = {},
                onAddStop = {},
                onScheduleClick = {},
                onMapPickerClick = {},
                onBack = {},
            )
        }
    }
}
