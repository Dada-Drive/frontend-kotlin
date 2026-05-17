package tn.dadadrive.presentation.error

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tn.dadadrive.core.theme.DadaDriveTheme
import tn.dadadrive.presentation.components.designsystem.DesignSnackbar
import tn.dadadrive.presentation.components.designsystem.DesignSnackbarVariant

/**
 * Snapshot coverage for the error snackbar in FR (LTR) and AR (RTL) — the two
 * primary locales for DadaDrive Tunisia. Validates that:
 * - The snackbar renders correctly with localized error copy.
 * - RTL flips icon/text/dismiss-button alignment without overflow.
 *
 * Strings are hardcoded with the exact wording currently shipped in
 * `values-fr/strings.xml` / `values-ar/strings.xml` for `error_insufficient_balance`
 * (see PresentableErrorMapper.stringResForBackendCode -> INSUFFICIENT_BALANCE).
 * Decoupling from `stringResource()` keeps the snapshot deterministic across
 * device locale configuration drifts.
 */
@RunWith(JUnit4::class)
class ErrorSnackbarPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "Theme.DadaDrive",
        )

    @Test
    fun errorSnackbar_fr_ltr() {
        paparazzi.snapshot {
            DadaDriveTheme(darkTheme = false) {
                DesignSnackbar(
                    title = "Solde insuffisant",
                    message = "Veuillez recharger votre portefeuille pour continuer.",
                    variant = DesignSnackbarVariant.InsufficientBalance,
                    onDismiss = {},
                )
            }
        }
    }

    @Test
    fun errorSnackbar_ar_rtl() {
        paparazzi.snapshot {
            DadaDriveTheme(darkTheme = false) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DesignSnackbar(
                        title = "الرصيد غير كافٍ",
                        message = "يرجى شحن محفظتك للمتابعة.",
                        variant = DesignSnackbarVariant.InsufficientBalance,
                        onDismiss = {},
                    )
                }
            }
        }
    }
}
