package com.dadadrive.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.ui.theme.AppColor
import com.dadadrive.ui.theme.AppTypography
import com.dadadrive.ui.theme.AppRadius
import com.dadadrive.ui.theme.AppSpacing
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Groups,
        title = "Your app for fair deals",
        subtitle = "Choose rides that are right for you"
    ),
    OnboardingPage(
        icon = Icons.Default.Map,
        title = "Rides across Tunisia",
        subtitle = "Available in Tunis, Sfax, Sousse and more"
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val current = pagerState.currentPage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.l, vertical = AppSpacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            if (current > 0) {
                                scope.launch { pagerState.animateScrollToPage(current - 1) }
                            } else {
                                onFinished()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColor.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.weight(1f))
                LogoView()
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(44.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                OnboardingPageView(page = pages[index])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.xl),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { idx ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = AppSpacing.s / 2)
                            .size(8.dp)
                            .background(
                                if (idx == current) AppColor.textPrimary
                                else AppColor.textPrimary.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
            ) {
                Button(
                    onClick = onFinished,
                    shape = RoundedCornerShape(AppRadius.full),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColor.green,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "Continue",
                        style = AppTypography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        append("Joining our app means you agree with our ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Terms of Use") }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
                    },
                    style = AppTypography.labelSmall,
                    color = AppColor.textHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AppSpacing.l)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 200.dp)
                    .rotate(-10f)
                    .background(AppColor.green, RoundedCornerShape(24.dp))
                    .align(Alignment.Center)
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(8f)
                    .background(AppColor.green, RoundedCornerShape(24.dp))
                    .align(Alignment.Center)
            )
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(Modifier.height(AppSpacing.xl))

        Text(
            text = page.title,
            style = AppTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AppColor.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.m))

        Text(
            text = page.subtitle,
            style = AppTypography.bodyMedium,
            color = AppColor.textHint,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LogoView() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(AppColor.green, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("D", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Text(
            text = "DadaDrive",
            style = AppTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColor.textPrimary
        )
    }
}
