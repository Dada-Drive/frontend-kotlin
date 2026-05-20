package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.riderhome.WaitingForDriverContent

/**
 * R-5.4 baseline snapshots for S14 WaitingForDriverContent.
 *
 * S14 — Waiting for drivers: search pulse indicator, route summary card,
 * "Voir les chauffeurs" CTA, cancel action. 1 component × 2 themes = 2 PNGs.
 */
@RunWith(JUnit4::class)
class WaitingDriverBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    @Test
    fun s14_waitingForDriver_light() {
        paparazzi.snapshotLight {
            WaitingForDriverContent(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "Aéroport Tunis-Carthage",
                onViewDrivers = {},
                onCancel = {},
            )
        }
    }

    @Test
    fun s14_waitingForDriver_dark() {
        paparazzi.snapshotDark {
            WaitingForDriverContent(
                originText = "Av. Habib Bourguiba, Tunis",
                destinationText = "Aéroport Tunis-Carthage",
                onViewDrivers = {},
                onCancel = {},
            )
        }
    }
}
