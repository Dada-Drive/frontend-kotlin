package tn.dadadrive.presentation.branding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.dadadrive.core.theme.LocalAppColors

private val TurboAccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.successGreen

/**
 * Logo carré noir, « T » blanc et accent vert (identité maquette TurboDrive).
 */
@Composable
fun TurboBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerFraction: Float = 0.22f,
) {
    val corner = size * cornerFraction
    val accentGreen = TurboAccentGreen
    Box(
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(corner))
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "T",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.44f).sp,
            letterSpacing = (-0.5).sp,
        )
        Canvas(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = size * 0.1f, bottom = size * 0.12f)
                .size(size * 0.28f),
        ) {
            val w = this.size.width
            val h = this.size.height
            val path =
                Path().apply {
                    moveTo(w * 0.1f, h * 0.55f)
                    lineTo(w * 0.95f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.95f)
                    close()
                }
            drawPath(path, accentGreen)
        }
    }
}

@Composable
fun TurboSplashSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    val c = LocalAppColors.current
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = c.textPrimary,
        trackColor = c.border,
        strokeWidth = 2.5.dp,
    )
}

/** Petite marque pour barres d’app (onboarding, etc.). */
@Composable
fun TurboBrandMarkSmall(modifier: Modifier = Modifier) {
    TurboBrandMark(modifier = modifier, size = 36.dp, cornerFraction = 0.24f)
}
