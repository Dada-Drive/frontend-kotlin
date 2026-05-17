package tn.turbodrive.presentation.snapshots

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.domain.models.WalletInfo
import tn.turbodrive.domain.models.WalletTransaction
import tn.turbodrive.presentation.wallet.WalletScreen
import tn.turbodrive.presentation.wallet.WalletViewModel

/**
 * R-4.1 baseline : WalletScreen with a stubbed [WalletViewModel].
 *
 * Renders the happy-path "Loaded" state : a wallet with balance + 3
 * representative transactions (top-up, ride payment, refund) so the snapshot
 * shows the full UI tree (badge, balance display, transactions list).
 */
@RunWith(JUnit4::class)
class WalletScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    private val sampleWallet =
        WalletInfo(
            id = "w_baseline",
            ownerId = "u_baseline",
            balance = 142.50,
            status = "active",
        )

    private val sampleTransactions =
        listOf(
            WalletTransaction(
                id = "t1",
                type = "topup",
                amount = 50.0,
                status = "completed",
                note = "Recharge wallet",
                createdAt = "2026-05-15",
            ),
            WalletTransaction(
                id = "t2",
                type = "ride_payment",
                amount = -12.30,
                status = "completed",
                note = "Course Tunis → La Marsa",
                createdAt = "2026-05-14",
            ),
            WalletTransaction(
                id = "t3",
                type = "refund",
                amount = 8.00,
                status = "completed",
                note = "Annulation",
                createdAt = "2026-05-12",
            ),
        )

    private fun stubViewModel(): WalletViewModel {
        val vm = mockk<WalletViewModel>(relaxed = true)
        every { vm.wallet } returns MutableStateFlow(sampleWallet)
        every { vm.transactions } returns MutableStateFlow(sampleTransactions)
        every { vm.isLoading } returns MutableStateFlow(false)
        every { vm.errorMessage } returns MutableStateFlow(null)
        return vm
    }

    @Test
    fun walletScreen_light() {
        paparazzi.snapshotLight {
            WalletScreen(onBack = {}, viewModel = stubViewModel())
        }
    }

    @Test
    fun walletScreen_dark() {
        paparazzi.snapshotDark {
            WalletScreen(onBack = {}, viewModel = stubViewModel())
        }
    }
}
