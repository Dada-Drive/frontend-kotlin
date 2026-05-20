package tn.turbodrive.presentation.riderhome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turbodrive.R
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

/**
 * S14 — Waiting for drivers screen.
 *
 * Renders [mapContent] (HereMap + user pulsing pin) behind a bottom sheet
 * showing: animated search indicator, route summary, "Voir les chauffeurs"
 * CTA (→ S16 negotiation, wired in R-5.5), and cancel action.
 *
 * Paparazzi tests snapshot [WaitingForDriverContent] independently.
 */
@Composable
fun WaitingDriverScreen(
    onViewDrivers: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    originText: String = "",
    destinationText: String = "",
    mapContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()
        WaitingForDriverContent(
            originText = originText,
            destinationText = destinationText,
            onViewDrivers = onViewDrivers,
            onCancel = onCancel,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun WaitingForDriverContent(
    originText: String,
    destinationText: String,
    onViewDrivers: () -> Unit,
    onCancel: () -> Unit,
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
                Modifier.padding(
                    start = AppSpacing.screenH,
                    end = AppSpacing.screenH,
                    top = 20.dp,
                    bottom = 32.dp,
                ),
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

            Spacer(Modifier.height(20.dp))

            // Search indicator row: pulse dot + title + subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Static pulse representation (animated in production via InfiniteTransition)
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .background(colors.accent.copy(alpha = 0.2f), CircleShape),
                    )
                    Box(
                        modifier =
                            Modifier
                                .size(18.dp)
                                .background(colors.accent, CircleShape),
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.waiting_driver_title),
                        style = AppTypography.headingS,
                        color = colors.textPrimary,
                    )
                    Text(
                        text = stringResource(R.string.waiting_driver_subtitle),
                        style = AppTypography.bodyS,
                        color = colors.textSubtle,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Route summary card
            Surface(
                shape = RoundedCornerShape(AppRadius.l),
                color = colors.surfaceAlt,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier.padding(
                            start = 14.dp,
                            end = 14.dp,
                            top = 12.dp,
                            bottom = 12.dp,
                        ),
                ) {
                    // Dashed vertical connector
                    Box(
                        modifier =
                            Modifier
                                .padding(start = 5.dp, top = 20.dp, bottom = 20.dp)
                                .width(2.dp)
                                .height(24.dp)
                                .border(1.dp, colors.divider, RoundedCornerShape(1.dp)),
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(28.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colors.accent),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = originText.ifBlank { stringResource(R.string.search_destination_origin_hint) },
                                style = AppTypography.bodyM,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(28.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colors.error),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = destinationText.ifBlank { stringResource(R.string.search_destination_destination_hint) },
                                style = AppTypography.bodyM,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // View drivers CTA
            Surface(
                onClick = onViewDrivers,
                shape = RoundedCornerShape(AppRadius.full),
                color = colors.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.buttonHeight),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.waiting_driver_view_cta),
                        style = AppTypography.button,
                        color = colors.surface,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cancel (ghost, error color)
            Surface(
                onClick = onCancel,
                shape = RoundedCornerShape(AppRadius.full),
                color = colors.surface,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.waiting_driver_cancel),
                        style = AppTypography.bodyM,
                        color = colors.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
