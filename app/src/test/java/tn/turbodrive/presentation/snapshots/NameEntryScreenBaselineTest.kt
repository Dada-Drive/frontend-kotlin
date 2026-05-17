package tn.turbodrive.presentation.snapshots

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.auth.NameEntryScreen
import tn.turbodrive.presentation.auth.NameEntryViewModel
import tn.turbodrive.presentation.common.ScreenState

/**
 * R-4.1 baseline : NameEntryScreen with a stubbed [NameEntryViewModel].
 *
 * The VM is mocked via mockk so we can render the happy-path "Idle" state
 * (no loading spinner, no error banner). Real coroutines never fire.
 */
@RunWith(JUnit4::class)
class NameEntryScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    private fun stubViewModel(): NameEntryViewModel {
        val vm = mockk<NameEntryViewModel>(relaxed = true)
        every { vm.state } returns MutableStateFlow(ScreenState.Idle)
        return vm
    }

    @Test
    fun nameEntryScreen_light() {
        paparazzi.snapshotLight {
            NameEntryScreen(onContinue = {}, onBack = {}, viewModel = stubViewModel())
        }
    }

    @Test
    fun nameEntryScreen_dark() {
        paparazzi.snapshotDark {
            NameEntryScreen(onContinue = {}, onBack = {}, viewModel = stubViewModel())
        }
    }
}
