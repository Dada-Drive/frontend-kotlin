package com.dadadrive.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.stringResource
import com.dadadrive.R
import com.dadadrive.ui.theme.AppColor
import com.dadadrive.ui.theme.AppTypography
import com.dadadrive.ui.theme.AppRadius
import com.dadadrive.ui.theme.AppSpacing
import com.dadadrive.ui.theme.lighter
import com.dadadrive.ui.theme.darker

@Composable
fun WelcomeScreen(
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit,
    authState: AuthState = AuthState.Idle
) {
    val isLoading = authState is AuthState.Loading
    val error = (authState as? AuthState.Error)?.message

    val transition = rememberInfiniteTransition(label = "welcomeAmbient")
    val orb1 by transition.animateFloat(
        initialValue = -280f,
        targetValue = -258f,
        animationSpec = infiniteRepeatable(tween(6000), RepeatMode.Reverse),
        label = "orb1"
    )
    val orb2 by transition.animateFloat(
        initialValue = 60f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "orb2"
    )
    val orb3 by transition.animateFloat(
        initialValue = 340f,
        targetValue = 354f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "orb3"
    )
    val glow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.background)
    ) {
        Orb(440.dp, 220.dp, 120.dp, orb1.dp, AppColor.green.copy(alpha = 0.22f), 8.dp)
        Orb(320.dp, 160.dp, (-140).dp, orb2.dp, AppColor.green.copy(alpha = 0.12f), 12.dp)
        Orb(200.dp, 100.dp, 130.dp, orb3.dp, AppColor.green.copy(alpha = 0.15f), 6.dp)
        GridOverlay()

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(1f))
            LogoSection(glow = glow)
            Spacer(Modifier.weight(1f))
            BottomSection(
                isLoading = isLoading,
                error = error,
                onPhoneClick = onPhoneClick,
                onGoogleClick = onGoogleClick
            )
        }
    }
}

@Composable
private fun Orb(
    size: androidx.compose.ui.unit.Dp,
    radius: androidx.compose.ui.unit.Dp,
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    color: Color,
    blur: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    radius = radius.value * 2.2f
                )
            )
            .blur(blur)
    )
}

@Composable
private fun GridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 36.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = AppColor.green.copy(alpha = 0.045f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = AppColor.green.copy(alpha = 0.045f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            y += step
        }
    }
}

@Composable
private fun LogoSection(glow: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(AppColor.green.copy(alpha = glow * 0.35f), CircleShape)
                    .blur(16.dp)
            )
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AppColor.green.copy(alpha = 0.8f), AppColor.green.copy(alpha = 0.2f))
                        ),
                        shape = CircleShape
                    )
                    .padding(1.5.dp)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AppColor.green.lighter(0.15f),
                                AppColor.green,
                                AppColor.green.darker(0.2f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🚗",
                    fontSize = 28.sp,
                    modifier = Modifier.semantics { contentDescription = "DadaDrive logo" }
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.l))
        Text(
            text = buildAnnotatedString {
                append("Dada")
                withStyle(SpanStyle(color = AppColor.green.copy(alpha = 0.85f))) { append("Drive") }
            },
            color = AppColor.textPrimary,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(AppSpacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CapsuleDash()
            Text(
                "Your ride, your price.",
                color = AppColor.textHint,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            CapsuleDash()
        }
    }
}

@Composable
private fun CapsuleDash() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(width = 24.dp, height = 2.dp)
            .background(AppColor.green, RoundedCornerShape(999.dp))
    )
}

@Composable
private fun BottomSection(
    isLoading: Boolean,
    error: String?,
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column {
        Column(
            modifier = Modifier
                .padding(horizontal = AppSpacing.l)
                .background(
                    AppColor.surface.copy(alpha = 0.55f),
                    RoundedCornerShape(28.dp)
                )
                .padding(AppSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
        ) {
            AuthButton(style = AuthBtnStyle.Google, isLoading = isLoading, onClick = onGoogleClick)
            DisabledAppleButton()
            DividerOr()
            AuthButton(style = AuthBtnStyle.Phone, isLoading = false, onClick = onPhoneClick)

            if (!error.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColor.error.copy(alpha = 0.1f), RoundedCornerShape(AppRadius.m))
                        .padding(AppSpacing.m),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Warning, null, tint = AppColor.error, modifier = Modifier.size(14.dp))
                    Text(error, color = AppColor.error, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Text(
            text = legalText(),
            color = AppColor.textHint.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.l)
                .fillMaxWidth()
        )
        Spacer(Modifier.height(AppSpacing.xxxl))
    }
}

private fun legalText(): AnnotatedString = buildAnnotatedString {
    append("By continuing, you agree to our ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Terms of Service") }
    append(" and ")
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
    append(".")
}

private enum class AuthBtnStyle { Google, Phone }

@Composable
private fun AuthButton(style: AuthBtnStyle, isLoading: Boolean, onClick: () -> Unit) {
    val isGoogle = style == AuthBtnStyle.Google
    val buttonBg = if (isGoogle) AppColor.surface else AppColor.green
    val labelColor = if (isGoogle) AppColor.textPrimary else Color.Black
    val iconBg = if (isGoogle) Color(0xFFEA4335).copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.18f)
    val iconFg = if (isGoogle) Color(0xFFEA4335) else Color.Black
    val arrowColor = if (isGoogle) AppColor.textHint else Color.Black.copy(alpha = 0.5f)

    Button(
        onClick = onClick,
        enabled = !(isGoogle && isLoading),
        shape = RoundedCornerShape(AppRadius.full),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBg,
            contentColor = labelColor,
            disabledContainerColor = buttonBg,
            disabledContentColor = labelColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isGoogle && isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = iconFg, modifier = Modifier.size(14.dp))
                } else {
                    if (isGoogle) {
                        Text(
                            text = "G",
                            color = iconFg,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { contentDescription = "Google sign in" }
                        )
                    }
                    if (!isGoogle) Icon(Icons.Default.Phone, null, tint = iconFg, modifier = Modifier.size(15.dp))
                }
            }
            Spacer(Modifier.size(AppSpacing.m))
            Text(
                text = when {
                    isGoogle && isLoading -> "Signing in..."
                    isGoogle -> "Continue with Google"
                    else -> "Continue with Phone"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            if (!(isGoogle && isLoading)) {
                Icon(Icons.Default.ArrowForward, null, tint = arrowColor, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun DisabledAppleButton() {
    Box {
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(AppRadius.full),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White,
                disabledContentColor = Color.Black
            )
        ) { Text(stringResource(R.string.welcome_continue_with_apple), fontWeight = FontWeight.SemiBold) }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(AppRadius.full))
                .background(AppColor.background.copy(alpha = 0.6f)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                "Coming Soon",
                color = AppColor.textHint,
                style = AppTypography.labelS,
                modifier = Modifier
                    .padding(end = AppSpacing.m)
                    .background(AppColor.surface, RoundedCornerShape(AppRadius.full))
                    .padding(horizontal = AppSpacing.m, vertical = AppSpacing.xs)
            )
        }
    }
}

@Composable
private fun DividerOr() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, AppColor.surface, Color.Transparent)
                    )
                )
        )
        Text(
            "or",
            color = AppColor.textHint,
            style = AppTypography.labelM,
            modifier = Modifier.padding(horizontal = AppSpacing.m)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, AppColor.surface, Color.Transparent)
                    )
                )
        )
    }
}
