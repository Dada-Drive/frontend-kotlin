package tn.turbodrive.presentation.snapshots

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.turbodrive.presentation.role.RoleSelectionScreen
import tn.turbodrive.presentation.role.RoleViewModel

/**
 * R-4.1 baseline : RoleSelectionScreen with a stubbed [RoleViewModel].
 *
 * Renders the initial state where neither role is selected.
 */
@RunWith(JUnit4::class)
class RoleSelectionScreenBaselineTest {
    @get:Rule
    val paparazzi = createPaparazzi()

    private fun stubViewModel(): RoleViewModel {
        val vm = mockk<RoleViewModel>(relaxed = true)
        every { vm.state } returns MutableStateFlow(RoleViewModel.RoleState.Idle)
        return vm
    }

    @Test
    fun roleSelectionScreen_light() {
        paparazzi.snapshotLight {
            RoleSelectionScreen(onSuccess = {}, onBack = {}, viewModel = stubViewModel())
        }
    }

    @Test
    fun roleSelectionScreen_dark() {
        paparazzi.snapshotDark {
            RoleSelectionScreen(onSuccess = {}, onBack = {}, viewModel = stubViewModel())
        }
    }
}
