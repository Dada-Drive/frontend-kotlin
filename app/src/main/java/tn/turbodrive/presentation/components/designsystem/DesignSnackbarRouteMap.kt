package tn.turbodrive.presentation.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.designsystem.tokens.AppShadow
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

enum class DesignSnackbarVariant {
    Error,
    Success,
    Warning,
    Info,
    InsufficientBalance,
    TooFar,
    Network,
}

@Composable
fun DesignSnackbar(
    title: String,
    message: String,
    variant: DesignSnackbarVariant,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val triple =
        when (variant) {
            DesignSnackbarVariant.Error -> Triple(c.error, AppIcon.alertTriangle, c.errorSoft)
            DesignSnackbarVariant.Success -> Triple(c.accent, AppIcon.circleCheck, c.surfaceAlt)
            DesignSnackbarVariant.Warning -> Triple(c.warning, AppIcon.alertTriangle, c.surfaceAlt)
            DesignSnackbarVariant.Info -> Triple(c.info, AppIcon.info, c.surfaceAlt)
            DesignSnackbarVariant.InsufficientBalance -> Triple(c.error, AppIcon.wallet, c.errorSoft)
            DesignSnackbarVariant.TooFar -> Triple(c.warning, AppIcon.mapPin, c.surfaceAlt)
            DesignSnackbarVariant.Network -> Triple(c.textSecondary, AppIcon.wifiOff, c.surfaceAlt)
        }
    val accent = triple.first
    val icon = triple.second
    val bg = triple.third
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(AppShadow.toast())
                .background(bg, RoundedCornerShape(AppSpacing.inputRadius))
                .padding(AppSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTypography.labelL, color = accent)
            Text(message, style = AppTypography.bodyS, color = c.textSecondary)
        }
        IconButton(onClick = onDismiss) {
            Icon(painterResource(AppIcon.close), contentDescription = null, tint = c.textSecondary)
        }
    }
}

@Composable
fun DesignAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 40.dp,
    online: Boolean = false,
) {
    val c = LocalAppColors.current
    Box(modifier) {
        Box(
            modifier =
                Modifier
                    .size(sizeDp)
                    .background(c.surfaceAlt, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials.take(2).uppercase(),
                style = AppTypography.labelL,
                color = c.textPrimary,
            )
        }
        if (online) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .background(c.accent, CircleShape),
            )
        }
    }
}

enum class DesignMapPinVariant { User, Pickup, Destination, Stop }

@Composable
fun DesignMapPin(
    variant: DesignMapPinVariant,
    modifier: Modifier = Modifier,
    stopNumber: Int? = null,
) {
    val c = LocalAppColors.current
    val color: Color
    val size: Dp
    when (variant) {
        DesignMapPinVariant.User -> {
            color = c.accent
            size = 32.dp
        }
        DesignMapPinVariant.Pickup -> {
            color = c.primary
            size = 48.dp
        }
        DesignMapPinVariant.Destination -> {
            color = c.error
            size = 40.dp
        }
        DesignMapPinVariant.Stop -> {
            color = c.warning
            size = 24.dp
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(size)
                    .background(color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (variant == DesignMapPinVariant.Stop && stopNumber != null) {
                Text(stopNumber.toString(), style = AppTypography.labelM, color = c.onPrimary)
            } else {
                Box(Modifier.size(size / 4).background(c.onPrimary, CircleShape))
            }
        }
        if (variant == DesignMapPinVariant.Pickup || variant == DesignMapPinVariant.Destination) {
            Box(Modifier.width(4.dp).height(20.dp).background(color))
        }
    }
}

@Composable
fun RouteRow(
    pickupLabel: String,
    destinationLabel: String,
    modifier: Modifier = Modifier,
    stops: List<String> = emptyList(),
) {
    val c = LocalAppColors.current
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(pickupLabel, style = AppTypography.bodyS, color = c.textSecondary)
        stops.forEach { s ->
            Text("• $s", style = AppTypography.bodyS, color = c.warning)
        }
        Text(destinationLabel, style = AppTypography.bodyS, color = c.error)
    }
}

private fun initialsFromName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "?"
    if (parts.size == 1) return parts[0].take(2).uppercase()
    return (parts[0].first().toString() + parts[1].first().toString()).uppercase()
}

@Composable
fun OfferCard(
    driverName: String,
    rating: String,
    vehicle: String,
    fare: String,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(AppShadow.card())
                .background(c.surface, RoundedCornerShape(AppSpacing.cardRadius))
                .padding(AppSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignAvatar(initials = initialsFromName(driverName), sizeDp = 48.dp)
        Column(Modifier.weight(1f)) {
            Text(driverName, style = AppTypography.labelL, color = c.textPrimary)
            Text("$rating · $vehicle", style = AppTypography.bodyS, color = c.textSecondary)
        }
        Text(fare, style = AppTypography.headingS, color = c.textPrimary)
    }
}

@Composable
fun RideStatusPill(
    label: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val c = LocalAppColors.current
    val dot = color ?: c.primary
    Row(
        modifier =
            modifier
                .background(c.surfaceAlt, RoundedCornerShape(AppSpacing.buttonRadius))
                .padding(horizontal = AppSpacing.m, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(Modifier.size(8.dp).background(dot, CircleShape))
        Text(label, style = AppTypography.labelM, color = c.textPrimary)
    }
}

@Composable
fun MapDriverMarkerPlaceholder(modifier: Modifier = Modifier) {
    Text(
        "MapMarker: 72 rotation variants — see design-system.md §7.21 (TODO).",
        style = AppTypography.bodyS,
        color = LocalAppColors.current.textSecondary,
        modifier = modifier.padding(AppSpacing.s),
    )
}
