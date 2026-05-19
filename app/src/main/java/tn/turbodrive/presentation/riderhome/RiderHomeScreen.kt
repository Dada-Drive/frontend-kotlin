package tn.turbodrive.presentation.riderhome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

/**
 * S10 — Rider home idle screen.
 *
 * Composes map content (provided via slot — HereMapViewComposable is managed by the
 * caller to avoid duplicating the heavy VM wiring) with the idle-state overlays:
 * - header (MapHomeTopHeader from map/)
 * - side controls (MapSideFloatingControls from map/)
 * - bottom idle sheet (search prompt + service categories + shortcuts)
 *
 * Paparazzi tests snapshot [RiderHomeBottomSheet] independently.
 */
@Composable
fun RiderHomeScreen(
    firstName: String,
    onSearchClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onShortcutClick: (label: String) -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()

        RiderHomeBottomSheet(
            firstName = firstName,
            onSearchClick = onSearchClick,
            onScheduleClick = onScheduleClick,
            onShortcutClick = onShortcutClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun RiderHomeBottomSheet(
    firstName: String,
    onSearchClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onShortcutClick: (label: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        shape = RoundedCornerShape(topStart = AppRadius.xl, topEnd = AppRadius.xl),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(horizontal = AppSpacing.screenH, vertical = AppSpacing.l)
                    .navigationBarsPadding(),
        ) {
            // Sheet handle
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(AppRadius.full))
                        .background(colors.divider),
            )

            Spacer(Modifier.height(AppSpacing.l))

            // Greeting
            val greeting =
                if (firstName.isNotBlank()) {
                    stringResource(R.string.rider_home_greeting_named, firstName)
                } else {
                    stringResource(R.string.rider_home_greeting_generic)
                }
            Text(
                text = greeting,
                style = AppTypography.headingL,
                color = colors.textPrimary,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = stringResource(R.string.rider_home_subtitle),
                style = AppTypography.bodyM,
                color = colors.textSubtle,
            )

            Spacer(Modifier.height(AppSpacing.l))

            // Search pill
            Surface(
                onClick = onSearchClick,
                shape = RoundedCornerShape(AppRadius.full),
                color = colors.surfaceAlt,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.l),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(AppIcon.search),
                        contentDescription = null,
                        tint = colors.textSubtle,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(AppSpacing.m))
                    Text(
                        text = stringResource(R.string.rider_home_search_hint),
                        style = AppTypography.bodyM,
                        color = colors.textSubtle,
                        modifier = Modifier.weight(1f),
                    )
                    // "Plus tard" schedule badge
                    Surface(
                        onClick = onScheduleClick,
                        shape = RoundedCornerShape(AppRadius.full),
                        color = colors.divider,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(AppIcon.clock),
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = stringResource(R.string.rider_home_schedule_badge),
                                style = AppTypography.labelS,
                                color = colors.textPrimary,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.l))

            // Quick shortcuts — Maison, Travail, Favoris
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            ) {
                listOf(
                    Triple(AppIcon.home, R.string.rider_home_shortcut_home, "8 min"),
                    Triple(AppIcon.briefcase, R.string.rider_home_shortcut_work, "12 min"),
                    Triple(AppIcon.star, R.string.rider_home_shortcut_favorites, null as String?),
                ).forEach { (iconRes, labelRes, eta) ->
                    val label = stringResource(labelRes)
                    Surface(
                        onClick = { onShortcutClick(label) },
                        shape = RoundedCornerShape(AppRadius.m),
                        color = colors.surfaceAlt,
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Column {
                                Text(
                                    text = label,
                                    style = AppTypography.labelM,
                                    color = colors.textPrimary,
                                )
                                if (eta != null) {
                                    Text(
                                        text = eta,
                                        fontSize = 11.sp,
                                        color = colors.textSubtle,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.s))
        }
    }
}
