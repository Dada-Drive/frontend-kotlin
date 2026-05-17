package tn.turbodrive.presentation.splash

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.turbodrive.R
import kotlinx.coroutines.delay
import tn.turbodrive.core.diagnostics.BootDiagnostics
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.branding.TurboBrandMark
import tn.turbodrive.presentation.branding.TurboSplashSpinner
import kotlin.math.max

private val SplashCream: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

private val SplashTaglineGray: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    LaunchedEffect(Unit) {
        val startMs = android.os.SystemClock.elapsedRealtime()
        BootDiagnostics.step("SplashScreen", "affichée, animation lancée")
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 600))
        val minVisibleMs = 300L
        val elapsed = android.os.SystemClock.elapsedRealtime() - startMs
        delay(max(0, minVisibleMs - elapsed))
        BootDiagnostics.step("SplashScreen", "fin délai → onSplashFinished()")
        onSplashFinished()
    }

    SplashScreenLayout(alpha = alpha.value)
}

@Composable
internal fun SplashScreenLayout(alpha: Float) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(SplashCream),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .alpha(alpha)
                    .padding(bottom = 56.dp),
        ) {
            TurboBrandMark(size = 88.dp)

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.brand_turbo_drive),
                color = Color.Black,
                style = AppTypography.displayMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                color = SplashTaglineGray,
                style = AppTypography.bodyM,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }

        TurboSplashSpinner(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp)
                    .alpha(alpha),
        )
    }
}
