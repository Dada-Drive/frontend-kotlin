package tn.turbodrive.presentation.riderhome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbodrive.R
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

/**
 * S12 (plan) / S14 (JSX) — Route preview + fare estimate sheet.
 *
 * Bottom sheet over map showing: route summary (origin/destination),
 * vehicle category chip, fare estimate card, hide-estimate toggle,
 * payment row, and "Demander une course" CTA.
 *
 * Paparazzi tests snapshot [RoutePreviewBottomSheet] independently.
 */
@Composable
fun RoutePreviewScreen(
    originText: String,
    destinationText: String,
    fareAmountTnd: String,
    vehicleCategory: String,
    onRequestRide: () -> Unit,
    onChangeCategory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    mapContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize()) {
        mapContent()

        RoutePreviewBottomSheet(
            originText = originText,
            destinationText = destinationText,
            fareAmountTnd = fareAmountTnd,
            vehicleCategory = vehicleCategory,
            onRequestRide = onRequestRide,
            onChangeCategory = onChangeCategory,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun RoutePreviewBottomSheet(
    originText: String,
    destinationText: String,
    fareAmountTnd: String,
    vehicleCategory: String,
    onRequestRide: () -> Unit,
    onChangeCategory: () -> Unit,
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
            modifier = Modifier.padding(start = AppSpacing.screenH, end = AppSpacing.screenH, top = 14.dp, bottom = 28.dp),
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

            Spacer(Modifier.height(14.dp))

            // Header: "Récapitulatif"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.route_preview_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                )
                Surface(
                    shape = RoundedCornerShape(AppRadius.full),
                    color = colors.surfaceAlt,
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
                            text = "14:30",
                            style = AppTypography.labelS,
                            color = colors.textPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Route summary: origin → destination with dashed connector
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .padding(start = 5.dp, top = 22.dp, bottom = 22.dp)
                            .width(2.dp)
                            .height(28.dp)
                            .border(1.dp, colors.divider, RoundedCornerShape(1.dp)),
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(30.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = originText,
                            style = AppTypography.bodyM,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(30.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors.error),
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = destinationText,
                            style = AppTypography.bodyM,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "14.2 km · 22 min",
                            fontSize = 12.sp,
                            color = colors.textSubtle,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Vehicle category chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(AppRadius.full),
                    color = colors.surfaceAlt,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcon.car),
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = vehicleCategory,
                            style = AppTypography.labelS,
                            color = colors.textPrimary,
                        )
                    }
                }
                Spacer(Modifier.width(AppSpacing.s))
                Text(
                    text = stringResource(R.string.route_preview_change_category),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSubtle,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Fare estimate card
            Surface(
                shape = RoundedCornerShape(AppRadius.l),
                color = colors.surfaceAlt,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.route_preview_estimated_price_label),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSubtle,
                            letterSpacing = 0.06.sp,
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(
                                text = fareAmountTnd,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary,
                            )
                            Text(
                                text = "TND",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSubtle,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(AppRadius.full),
                            color = colors.accentSoft,
                        ) {
                            Text(
                                text = "± 1 TND",
                                style = AppTypography.labelS,
                                color = colors.accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Payment row
            Surface(
                shape = RoundedCornerShape(AppRadius.m),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = colors.surfaceAlt,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(AppIcon.wallet),
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.route_preview_payment_cash),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        painter = painterResource(AppIcon.chevronRight),
                        contentDescription = null,
                        tint = colors.textSubtle,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Request ride CTA
            Surface(
                onClick = onRequestRide,
                shape = RoundedCornerShape(AppRadius.full),
                color = colors.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.buttonHeight),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.route_preview_request_cta),
                        style = AppTypography.button,
                        color = colors.surface,
                    )
                }
            }
        }
    }
}
