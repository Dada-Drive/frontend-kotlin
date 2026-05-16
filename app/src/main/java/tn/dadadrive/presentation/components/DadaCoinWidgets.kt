package tn.dadadrive.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.dadadrive.core.theme.LocalAppColors

enum class DadaCoinMetal {
    Silver,
    Gold,
}

@Composable
fun AnimatedDadaCoinIcon(
    size: Dp,
    metal: DadaCoinMetal,
    modifier: Modifier = Modifier,
) {
    val base = when (metal) {
        DadaCoinMetal.Silver -> Color(0xFFC0C0C0)
        DadaCoinMetal.Gold -> Color(0xFFD4AF37)
    }
    val transition = rememberInfiniteTransition(label = "dada_coin")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rot",
    )
    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation)
            .background(base, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "D",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.42f).sp,
        )
    }
}

@Composable
fun FullScreenCoinIntroOverlay(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedDadaCoinIcon(size = 120.dp, metal = DadaCoinMetal.Gold)
        Text(
            text = "DADA",
            color = c.onPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp),
        )
    }
}
