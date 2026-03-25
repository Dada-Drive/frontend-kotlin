package com.dadadrive.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.ui.theme.Black
import com.dadadrive.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 800))
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha.value)
        ) {
            DadaDriveLogoIcon(
                modifier = Modifier.size(120.dp),
                foreground = White,
                cutout = Black
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "DADA DRIVE",
                color = White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your ride, your way",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DadaDriveLogoIcon(
    modifier: Modifier = Modifier,
    foreground: Color = White,
    cutout: Color = Black
) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val w = size.width
        val h = size.height

        // Outer D shape
        val dPath = Path().apply {
            moveTo(w * 0.12f, h * 0.06f)
            lineTo(w * 0.52f, h * 0.06f)
            cubicTo(w * 0.90f, h * 0.06f, w * 0.96f, h * 0.28f, w * 0.96f, h * 0.50f)
            cubicTo(w * 0.96f, h * 0.72f, w * 0.90f, h * 0.94f, w * 0.52f, h * 0.94f)
            lineTo(w * 0.12f, h * 0.94f)
            close()
        }
        drawPath(dPath, color = foreground)

        // Lightning bolt cutout through the D
        val boltPath = Path().apply {
            moveTo(w * 0.63f, h * 0.06f)
            lineTo(w * 0.38f, h * 0.46f)
            lineTo(w * 0.52f, h * 0.46f)
            lineTo(w * 0.28f, h * 0.94f)
            lineTo(w * 0.40f, h * 0.94f)
            lineTo(w * 0.64f, h * 0.54f)
            lineTo(w * 0.50f, h * 0.54f)
            lineTo(w * 0.75f, h * 0.06f)
            close()
        }
        drawPath(boltPath, color = cutout)
    }
}
