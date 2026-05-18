package tn.turbodrive.presentation.components.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.spacing.AppIconSize
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

/**
 * Scrollable list of driver ride-offer cards — R-4.4.
 *
 * Ported from `turbodrive_redesign/screens-rider.jsx` (S16Offers + OfferCard,
 * L705-790). The "Stacked" name is kept for R-4.4 ACTION_PLAN traceability,
 * but the UX is a vertical LazyColumn (not a swipe-stack) — see planning note
 * in docs/COMPONENTS.md for rationale.
 *
 * Each [OfferCard] shows :
 *   - Pickup → Dropoff route summary
 *   - Distance (km) + ETA (min)
 *   - Fare (TND), large and bold
 *   - [LinearProgressTimer] draining the 30s validity window
 *   - Reject (outline) + Accept · {fare} TND (solid) buttons
 *
 * @param offers List of incoming ride offers (empty = [emptyContent])
 * @param onAccept Called with the accepted offer
 * @param onReject Called with the rejected offer
 * @param emptyContent Composable shown when [offers] is empty (default message)
 */
@Composable
fun StackedOffersList(
    offers: List<RideOffer>,
    onAccept: (RideOffer) -> Unit,
    onReject: (RideOffer) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = { DefaultOffersEmptyState() },
) {
    if (offers.isEmpty()) {
        emptyContent()
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding =
            androidx.compose.foundation.layout.PaddingValues(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.m,
            ),
    ) {
        items(offers, key = { it.id }) { offer ->
            OfferCard(
                offer = offer,
                onAccept = { onAccept(offer) },
                onReject = { onReject(offer) },
            )
        }
    }
}

@Composable
private fun OfferCard(
    offer: RideOffer,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val c = LocalAppColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.m),
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
    ) {
        Column(
            modifier = Modifier.padding(vertical = AppSpacing.m, horizontal = AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            ) {
                Icon(
                    painter = painterResource(AppIcon.mapPin),
                    contentDescription = null,
                    tint = c.successGreen,
                    modifier = Modifier.size(AppIconSize.m),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.pickupAddress,
                        style = AppTypography.labelM,
                        color = c.textPrimary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                    ) {
                        Icon(
                            painter = painterResource(AppIcon.arrowDown),
                            contentDescription = null,
                            tint = c.textHint,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = offer.dropoffAddress,
                            style = AppTypography.bodyS,
                            color = c.textSecondary,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    text = "%.2f TND".format(offer.fare),
                    style = AppTypography.headingS.copy(fontWeight = FontWeight.Bold),
                    color = c.primary,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(AppIcon.car),
                    contentDescription = null,
                    tint = c.textHint,
                    modifier = Modifier.size(AppIconSize.s),
                )
                Text(
                    text = "%.1f km".format(offer.distanceKm),
                    style = AppTypography.bodyS,
                    color = c.textHint,
                )
                Spacer(Modifier.width(AppSpacing.s))
                Icon(
                    painter = painterResource(AppIcon.clock),
                    contentDescription = null,
                    tint = c.textHint,
                    modifier = Modifier.size(AppIconSize.s),
                )
                Text(
                    text = "${offer.estimatedMinutes} min",
                    style = AppTypography.bodyS,
                    color = c.textHint,
                )
            }

            LinearProgressTimer(
                durationMs = offer.validityRemainingMs,
                modifier = Modifier.fillMaxWidth(),
                progressOverride = (offer.validityRemainingMs / 30_000f).coerceIn(0f, 1f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(AppRadius.full),
                    border = BorderStroke(1.dp, c.border),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = c.textPrimary,
                        ),
                ) {
                    Text("Refuser", style = AppTypography.labelM)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(2f).height(40.dp),
                    shape = RoundedCornerShape(AppRadius.full),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = c.primary,
                            contentColor = c.onPrimary,
                        ),
                ) {
                    Text(
                        "Accepter · ${"%.0f".format(offer.fare)} TND",
                        style = AppTypography.labelM,
                    )
                }
            }
        }
    }
}

@Composable
internal fun DefaultOffersEmptyState() {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(AppSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.m, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(AppIcon.car),
            contentDescription = null,
            tint = c.textHint,
            modifier = Modifier.size(AppIconSize.xl),
        )
        Text(
            text = "Aucune offre disponible",
            style = AppTypography.bodyM,
            color = c.textHint,
        )
    }
}
