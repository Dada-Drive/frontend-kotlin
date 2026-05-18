package tn.turbodrive.presentation.snapshots

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.components.designsystem.RideOffer
import tn.turbodrive.presentation.components.designsystem.StackedOffersList

/**
 * R-4.4 baseline : StackedOffersList (4 visual states × light/dark).
 *
 * States : empty, single offer, three offers, five offers.
 * Each offer's [validityRemainingMs] is fixed so [LinearProgressTimer]
 * shows a deterministic frame (the progressOverride path is taken via
 * the ratio validityRemainingMs / 30_000).
 */
@RunWith(JUnit4::class)
class StackedOffersListBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    private val sampleOffers =
        listOf(
            RideOffer(
                id = "1",
                pickupAddress = "Av. Habib Bourguiba, Tunis",
                dropoffAddress = "Aéroport Tunis-Carthage",
                distanceKm = 8.4,
                estimatedMinutes = 18,
                fare = 22.50,
                validityRemainingMs = 28_000,
            ),
            RideOffer(
                id = "2",
                pickupAddress = "La Marsa, Sidi Bou Said",
                dropoffAddress = "Centre-ville Tunis",
                distanceKm = 14.2,
                estimatedMinutes = 27,
                fare = 35.00,
                validityRemainingMs = 14_000,
            ),
            RideOffer(
                id = "3",
                pickupAddress = "Lac 1, Tunis",
                dropoffAddress = "Cité Ennasr, Ariana",
                distanceKm = 6.1,
                estimatedMinutes = 12,
                fare = 17.00,
                validityRemainingMs = 4_000,
            ),
            RideOffer(
                id = "4",
                pickupAddress = "El Menzah 9, Tunis",
                dropoffAddress = "Megrine Sud",
                distanceKm = 21.5,
                estimatedMinutes = 35,
                fare = 48.00,
                validityRemainingMs = 20_000,
            ),
            RideOffer(
                id = "5",
                pickupAddress = "Bardo, Tunis",
                dropoffAddress = "Hammam-Lif",
                distanceKm = 19.0,
                estimatedMinutes = 30,
                fare = 40.00,
                validityRemainingMs = 10_000,
            ),
        )

    @Test
    fun empty_light() {
        paparazzi.snapshotLight {
            StackedOffersList(offers = emptyList(), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun empty_dark() {
        paparazzi.snapshotDark {
            StackedOffersList(offers = emptyList(), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun single_light() {
        paparazzi.snapshotLight {
            StackedOffersList(offers = sampleOffers.take(1), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun single_dark() {
        paparazzi.snapshotDark {
            StackedOffersList(offers = sampleOffers.take(1), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun three_light() {
        paparazzi.snapshotLight {
            StackedOffersList(offers = sampleOffers.take(3), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun three_dark() {
        paparazzi.snapshotDark {
            StackedOffersList(offers = sampleOffers.take(3), onAccept = {}, onReject = {})
        }
    }

    @Test
    fun five_light() {
        paparazzi.snapshotLight {
            StackedOffersList(offers = sampleOffers, onAccept = {}, onReject = {})
        }
    }

    @Test
    fun five_dark() {
        paparazzi.snapshotDark {
            StackedOffersList(offers = sampleOffers, onAccept = {}, onReject = {})
        }
    }
}
