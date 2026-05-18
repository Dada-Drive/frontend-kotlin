package tn.turbodrive.presentation.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.turbodrive.R
import kotlinx.coroutines.launch
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.branding.TurboBrandMarkSmall

private const val PAGE_COUNT = 3

private val ScreenBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.background

private val CardIllustrationBg: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.border

private val AccentGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.accent

private val TextGray: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.textSecondary

private val DotInactive: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current.borderStrong

@Composable
fun OnboardingScreen(onCompleteIntro: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val locPermission = Manifest.permission.ACCESS_FINE_LOCATION
    var hasLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, locPermission) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val locLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            hasLocation = granted
            onCompleteIntro()
        }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .statusBarsPadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TurboBrandMarkSmall()
            TextButton(onClick = onCompleteIntro) {
                Text(
                    stringResource(R.string.ob_passer),
                    color = TextGray,
                    style = AppTypography.labelL,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.Top,
        ) { page ->
            key(page) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                ) {
                    Spacer(Modifier.height(8.dp))
                    when (page) {
                        0 -> OnboardingIllustrationSpeed()
                        1 -> OnboardingIllustrationTrust()
                        else -> OnboardingIllustrationEconomy()
                    }
                    Spacer(Modifier.height(20.dp))
                    TurboPagerDots(count = PAGE_COUNT, current = pagerState.currentPage)
                    Spacer(Modifier.height(20.dp))
                    when (page) {
                        0 -> {
                            OnboardingTextBlock(
                                title = stringResource(R.string.ob_speed_title),
                                body = stringResource(R.string.ob_speed_body),
                            )
                        }
                        1 -> {
                            OnboardingTextBlock(
                                title = stringResource(R.string.ob_trust_slide_title),
                                body = stringResource(R.string.ob_trust_slide_body),
                            )
                        }
                        else -> {
                            OnboardingTextBlock(
                                title = stringResource(R.string.ob_save_title),
                                body = stringResource(R.string.ob_save_body),
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            val isLast = pagerState.currentPage == PAGE_COUNT - 1
            Button(
                onClick = {
                    if (isLast) {
                        if (hasLocation) {
                            onCompleteIntro()
                        } else {
                            locLauncher.launch(locPermission)
                        }
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape = RoundedCornerShape(999.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                    ),
            ) {
                Text(
                    text =
                        if (isLast) {
                            stringResource(R.string.ob_commencer)
                        } else {
                            stringResource(R.string.ob_continuer)
                        },
                    style = AppTypography.headingS,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.ob_legal_tiny),
                color = TextGray.copy(alpha = 0.85f),
                style = AppTypography.labelS,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun TurboPagerDots(
    count: Int,
    current: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == current
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .then(
                            if (active) {
                                Modifier
                                    .width(28.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.Black)
                            } else {
                                Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DotInactive)
                            },
                        ),
            )
        }
    }
}

@Composable
private fun OnboardingTextBlock(
    title: String,
    body: String,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            color = Color.Black,
            style = AppTypography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 26.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            color = TextGray,
            style = AppTypography.bodyM,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun IllustrationCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardIllustrationBg),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun OnboardingIllustrationSpeed() {
    val accentGreen = AccentGreen
    val hintColor = LocalAppColors.current.textSubtle
    IllustrationCard {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val y = size.height * 0.52f
                for (i in 0 until 3) {
                    val x0 = size.width * (0.12f + i * 0.06f)
                    drawLine(
                        color = hintColor,
                        start = Offset(x0, y - 6 + i * 4f),
                        end = Offset(x0 + size.width * 0.12f, y - 6 + i * 4f),
                        strokeWidth = 3f,
                    )
                }
                val carW = size.width * 0.38f
                val carH = size.height * 0.22f
                val left = size.width * 0.42f
                val top = size.height * 0.36f
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(left, top),
                    size = Size(carW, carH),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                )
                val wheelR = 12.dp.toPx()
                drawCircle(accentGreen, wheelR, center = Offset(left + carW * 0.28f, top + carH))
                drawCircle(accentGreen, wheelR, center = Offset(left + carW * 0.72f, top + carH))
            }
            Icon(
                painter = painterResource(AppIcon.zap),
                contentDescription = null,
                tint = accentGreen,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 36.dp, top = 40.dp)
                        .size(28.dp),
            )
        }
    }
}

@Composable
private fun OnboardingIllustrationTrust() {
    val accentGreen = AccentGreen
    IllustrationCard {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(160.dp)) {
                val w = size.width
                val h = size.height
                val path =
                    Path().apply {
                        moveTo(w * 0.5f, h * 0.08f)
                        lineTo(w * 0.88f, h * 0.25f)
                        lineTo(w * 0.82f, h * 0.82f)
                        lineTo(w * 0.18f, h * 0.82f)
                        lineTo(w * 0.12f, h * 0.25f)
                        close()
                    }
                drawPath(path, Color.Black)
                val checkPath =
                    Path().apply {
                        moveTo(w * 0.32f, h * 0.48f)
                        lineTo(w * 0.44f, h * 0.62f)
                        lineTo(w * 0.72f, h * 0.34f)
                    }
                drawPath(
                    checkPath,
                    color = accentGreen,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                )
                val r = 4.dp.toPx()
                listOf(
                    Offset(w * 0.12f, h * 0.22f),
                    Offset(w * 0.88f, h * 0.2f),
                    Offset(w * 0.1f, h * 0.78f),
                    Offset(w * 0.9f, h * 0.75f),
                ).forEach { drawCircle(accentGreen, r, it) }
            }
        }
    }
}

@Composable
private fun OnboardingIllustrationEconomy() {
    IllustrationCard {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(width = 168.dp, height = 104.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .background(LocalAppColors.current.textPrimary),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AccentGreen),
                )
            }
            Text(
                "TND",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 40.dp, top = 48.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(AccentGreen)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 44.dp, top = 44.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}
