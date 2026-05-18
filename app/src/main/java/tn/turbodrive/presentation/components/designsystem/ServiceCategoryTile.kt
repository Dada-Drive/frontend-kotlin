package tn.turbodrive.presentation.components.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.spacing.AppIconSize
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

private val TileWidth = 104.dp
private val TileHeight = 138.dp
private val TileRadius = AppRadius.m
private val SelectedBorderWidth = 1.5.dp
private val DefaultBorderWidth = 1.dp

/**
 * Service category selection tile — R-4.4.
 *
 * Used to pick a service type (taxi, delivery, premium) in the new booking flow.
 * Visual spec from `turbodrive_redesign/screens-categories.jsx` : 104×138dp,
 * 12dp radius, accent border 1.5dp when selected, shadow sm on default.
 *
 * Brand color for selected state : [LocalAppColors.current.primary] (accent green
 * in the redesign = successGreen; mapped to `primary` which is the brand accent).
 *
 * @param icon Painter for the service icon (use `painterResource(AppIcon.*)`)
 * @param label Service name shown below the icon
 * @param isSelected Whether this tile is the active selection
 * @param onClick Invoked when the user taps the tile
 * @param enabled False → greyed out (40% alpha) and non-interactive
 */
@Composable
fun ServiceCategoryTile(
    icon: Painter,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = LocalAppColors.current
    val border =
        when {
            !enabled -> BorderStroke(DefaultBorderWidth, c.border)
            isSelected -> BorderStroke(SelectedBorderWidth, c.primary)
            else -> BorderStroke(DefaultBorderWidth, c.border)
        }
    val containerColor = if (isSelected && enabled) c.primary.copy(alpha = 0.08f) else c.surface
    val contentColor = if (isSelected && enabled) c.primary else c.textPrimary

    Surface(
        modifier =
            modifier
                .width(TileWidth)
                .height(TileHeight)
                .alpha(if (enabled) 1f else 0.4f)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(TileRadius),
        color = containerColor,
        border = border,
        tonalElevation = if (isSelected) 0.dp else 2.dp,
        shadowElevation = if (isSelected) 0.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s, Alignment.CenterVertically),
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(AppIconSize.l),
            )
            Text(
                text = label,
                style = AppTypography.labelM,
                color = contentColor,
            )
        }
    }
}

@Composable
internal fun ServiceCategoryTilePreviewContent() {
    val c = LocalAppColors.current
    Surface(color = c.surface) {
        Column(
            modifier = Modifier.padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
            )
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = true,
                onClick = {},
            )
            ServiceCategoryTile(
                icon = painterResource(AppIcon.car),
                label = "Taxi",
                isSelected = false,
                onClick = {},
                enabled = false,
            )
        }
    }
}
