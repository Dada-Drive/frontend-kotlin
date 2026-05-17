package tn.turbodrive.presentation.auth

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.turbodrive.R
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

private val WelcomeCream: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

private val HeroBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textPrimary

private val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.successGreen

@Composable
fun WelcomeScreen(
    onPhoneClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    authState: AuthState = AuthState.Idle,
    googleCooldownSeconds: Int = 0,
) {
    val c = LocalAppColors.current
    val view = LocalView.current
    val isLoading = authState is AuthState.Loading
    val rawError = (authState as? AuthState.Error)?.message
    val error =
        when {
            googleCooldownSeconds > 0 ->
                stringResource(R.string.welcome_rate_limit_seconds, googleCooldownSeconds)
            else -> rawError
        }

    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    Column(Modifier.fillMaxSize().background(WelcomeCream)) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(0.48f)
                    .background(HeroBlack),
        ) {
            WelcomeHeroDotsAndCar()
        }
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(0.52f)
                    .offset(y = (-20).dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = WelcomeCream,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(top = 28.dp, bottom = 16.dp)
                        .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.welcome_hero_title),
                    color = Color.Black,
                    style = AppTypography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.welcome_hero_subtitle),
                    color = c.textSecondary,
                    style = AppTypography.bodyM,
                    textAlign = TextAlign.Center,
                )
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(c.errorContainer)
                                .border(1.dp, c.errorContainer, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("!", color = c.errorRed, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = error,
                            color = c.errorRed,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    WelcomePillButton(
                        background = Color.Black,
                        contentColor = Color.White,
                        border = null,
                        onClick = onPhoneClick,
                        enabled = !isLoading,
                    ) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.welcome_continue_phone),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                    val isCoolingDown = googleCooldownSeconds > 0
                    WelcomePillButton(
                        background = Color.White,
                        contentColor = Color.Black,
                        border = Color.Black,
                        onClick = onGoogleClick,
                        enabled = !isLoading && !isCoolingDown,
                    ) {
                        Text("G", color = c.googleRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            when {
                                isLoading -> stringResource(R.string.welcome_signing_in)
                                isCoolingDown ->
                                    stringResource(R.string.welcome_google_cooldown, googleCooldownSeconds)
                                else -> stringResource(R.string.welcome_continue_google)
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                    WelcomePillButton(
                        background = Color.White,
                        contentColor = Color.Black,
                        border = Color.Black,
                        onClick = onFacebookClick,
                        enabled = !isLoading,
                    ) {
                        Text("f", color = c.facebookBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.welcome_continue_facebook),
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text =
                        buildAnnotatedString {
                            append(stringResource(R.string.welcome_legal_part1))
                            withStyle(
                                SpanStyle(
                                    color = c.textSecondary,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            ) {
                                append(stringResource(R.string.welcome_terms_link))
                            }
                            append(stringResource(R.string.welcome_legal_connector))
                            withStyle(
                                SpanStyle(
                                    color = c.textSecondary,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            ) {
                                append(stringResource(R.string.welcome_privacy_link))
                            }
                            append(stringResource(R.string.welcome_legal_suffix))
                        },
                    style = AppTypography.labelS.copy(color = c.textTertiary, lineHeight = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun WelcomePillButton(
    background: Color,
    contentColor: Color,
    border: Color?,
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(999.dp))
                .then(
                    if (border != null) {
                        Modifier.border(1.dp, border, RoundedCornerShape(999.dp))
                    } else {
                        Modifier
                    },
                )
                .background(
                    if (enabled) background else background.copy(alpha = 0.55f),
                    RoundedCornerShape(999.dp),
                )
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content,
    )
}

@Composable
private fun WelcomeHeroDotsAndCar() {
    val accentGreen = AccentGreen
    val tanBorder = LocalAppColors.current.border
    Canvas(Modifier.fillMaxSize()) {
        val step = 14.dp.toPx()
        var x = 0f
        while (x < size.width) {
            var y = 0f
            while (y < size.height) {
                drawCircle(Color.White.copy(alpha = 0.06f), 1.2f, Offset(x, y))
                y += step
            }
            x += step
        }
        val cx = size.width * 0.5f
        val cy = size.height * 0.52f
        val carW = size.width * 0.42f
        val carH = size.height * 0.14f
        val left = cx - carW / 2f
        val top = cy - carH / 2f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(carW, carH),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        )
        val wheelR = 11.dp.toPx()
        drawCircle(accentGreen, wheelR, center = Offset(left + carW * 0.28f, top + carH))
        drawCircle(accentGreen, wheelR, center = Offset(left + carW * 0.72f, top + carH))
        drawRoundRect(
            color = tanBorder,
            topLeft = Offset(left + carW * 0.78f, top + carH * 0.22f),
            size = Size(carW * 0.12f, carH * 0.28f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        val lineY = cy
        for (i in 0 until 4) {
            val lx = left - 24f - i * 18f
            drawLine(
                Color.White.copy(alpha = 0.12f),
                Offset(lx, lineY - 4 + i * 2.5f),
                Offset(lx + 22f, lineY - 4 + i * 2.5f),
                strokeWidth = 2f,
            )
        }
        for (i in 0 until 4) {
            val lx = left + carW + 8f + i * 18f
            drawLine(
                Color.White.copy(alpha = 0.12f),
                Offset(lx, lineY - 4 + i * 2.5f),
                Offset(lx + 22f, lineY - 4 + i * 2.5f),
                strokeWidth = 2f,
            )
        }
    }
}
