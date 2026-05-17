package tn.turbodrive.core.designsystem.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Reduced motion per design-system.md §9.5 — [Settings.Global.ANIMATOR_DURATION_SCALE] == 0.
 * Read once per composition; not reactive to mid-session setting changes.
 */
fun Context.isReducedMotionEnabled(): Boolean =
    runCatching {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }.getOrDefault(false)

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context.applicationContext) { context.isReducedMotionEnabled() }
}
