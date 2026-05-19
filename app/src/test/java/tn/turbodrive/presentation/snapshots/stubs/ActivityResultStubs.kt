package tn.turbodrive.presentation.snapshots.stubs

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.app.ActivityOptionsCompat

/**
 * Paparazzi has no Activity / Lifecycle, so any composable that calls
 * `rememberLauncherForActivityResult` crashes with
 * "No ActivityResultRegistryOwner was provided".
 *
 * This helper installs a no-op [ActivityResultRegistryOwner] in the
 * composition local tree. Launchers compile and `remember` happily ;
 * calling `.launch()` is a silent no-op (which is fine — snapshots
 * never trigger user gestures).
 */
@Composable
internal fun ProvideStubActivityResultRegistry(content: @Composable () -> Unit) {
    val noopRegistry =
        object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?,
            ) {
                // No-op : snapshots never trigger user gestures.
            }
        }
    val owner =
        object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = noopRegistry
        }
    CompositionLocalProvider(
        LocalActivityResultRegistryOwner provides owner,
    ) {
        content()
    }
}
