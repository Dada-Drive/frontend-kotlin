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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
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

/**
 * Matches Swift OnboardingView.swift exactly:
 * - Same page content (icon, title, subtitle)
 * - Same top bar layout (back arrow · centred logo · invisible spacer)
 * - Same bottom section (Continue button + legal text)
 * - Same dot indicator
 * - Same colours, spacing, radii from the shared design system
 */

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Person,
        title = "Your app for fair deals",
        subtitle = "Choose rides that are right for you"
    ),
    OnboardingPage(
        icon = Icons.Default.LocationOn,
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

            // ── Top bar: back · logo · invisible spacer ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.l, vertical = AppSpacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
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

                // Centred logo — matches Swift LogoView
                LogoView()

                Spacer(Modifier.weight(1f))

                // Invisible spacer to keep logo centred
                Spacer(Modifier.size(44.dp))
            }

            // ── Page carousel ───────────────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { index ->
                OnboardingPageView(page = pages[index])
            }

            // ── Dot indicators ──────────────────────────────────────────
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
                                color = if (idx == current) AppColor.textPrimary
                                else AppColor.textPrimary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // ── Bottom section: Continue button + legal ─────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.m)
            ) {
                // Continue button — green, full-width, pill shape, black text, bold
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
                        style = AppTypography.headingS,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Legal text — matches Swift: "Joining our app means you agree with our
                // **Terms of Use** and **Privacy Policy**"
                Text(
                    text = buildAnnotatedString {
                        append("Joining our app means you agree with our ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Terms of Use") }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
                    },
                    style = AppTypography.labelS,
                    color = AppColor.textHint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AppSpacing.l)
                )
            }
        }
    }
}

// ── Single onboarding page ──────────────────────────────────────────────────
// Matches Swift OnboardingPageView exactly:
// - Two overlapping rotated green rounded rectangles
// - Icon on top (black tint)
// - Title (displayMedium, bold, textPrimary, center)
// - Subtitle (bodyM, textHint, center)

@Composable
private fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration area — two overlapping rotated rectangles + icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background rect 1 — 220×200, rotated -10°, offset(-20, 10) in Swift
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 200.dp)
                    .rotate(-10f)
                    .background(AppColor.green, RoundedCornerShape(24.dp))
                    .align(Alignment.Center)
            )
            // Background rect 2 — 180×180, rotated +8°, offset(20, -10) in Swift
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .rotate(8f)
                    .background(AppColor.green, RoundedCornerShape(24.dp))
                    .align(Alignment.Center)
            )
            // Icon — 120×120, black, on top
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(Modifier.height(AppSpacing.xl))

        // Title — displayMedium (26sp bold), textPrimary, center
        Text(
            text = page.title,
            style = AppTypography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = AppColor.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(AppSpacing.m))

        // Subtitle — bodyM (14sp regular), textHint, center
        Text(
            text = page.subtitle,
            style = AppTypography.bodyM,
            color = AppColor.textHint,
            textAlign = TextAlign.Center
        )
    }
}

// ── Logo ────────────────────────────────────────────────────────────────────
// Matches Swift LogoView exactly:
// - Green rounded rect (32×32, cornerRadius 8) with "D" (black, 18sp, black weight)
// - "DadaDrive" text (headingM = 18sp semibold, bold, textPrimary)

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
            Text(
                "D",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
        Text(
            text = "DadaDrive",
            style = AppTypography.headingM,
            fontWeight = FontWeight.Bold,
            color = AppColor.textPrimary
        )
    }
}