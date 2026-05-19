package tn.turbodrive.presentation.riderhome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * S11 — Route input bottom sheet.
 *
 * Redesign v2: origin + destination fields with dashed connecting line,
 * "Ajouter un arrêt" chip, "Pour moi / Pour quelqu'un" segmented,
 * schedule footer + map picker CTA.
 *
 * Paparazzi tests snapshot [SearchDestinationContent] independently.
 */
@Composable
fun SearchDestinationScreen(
    originText: String,
    destinationText: String,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onAddStop: () -> Unit,
    onScheduleClick: () -> Unit,
    onMapPickerClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchDestinationContent(
        originText = originText,
        destinationText = destinationText,
        onOriginChange = onOriginChange,
        onDestinationChange = onDestinationChange,
        onAddStop = onAddStop,
        onScheduleClick = onScheduleClick,
        onMapPickerClick = onMapPickerClick,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun SearchDestinationContent(
    originText: String,
    destinationText: String,
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onAddStop: () -> Unit,
    onScheduleClick: () -> Unit,
    onMapPickerClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var forOther by remember { mutableStateOf(false) }

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

            Spacer(Modifier.height(16.dp))

            // Header: back + title + close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(AppIcon.arrowLeft),
                        contentDescription = stringResource(R.string.search_destination_back),
                        tint = colors.textPrimary,
                    )
                }
                Spacer(Modifier.width(AppSpacing.m))
                Text(
                    text = stringResource(R.string.search_destination_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        painter = painterResource(AppIcon.close),
                        contentDescription = stringResource(R.string.search_destination_close),
                        tint = colors.textPrimary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Segmented "Pour moi / Pour quelqu'un"
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppRadius.m))
                        .background(colors.surfaceAlt),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                listOf(
                    false to stringResource(R.string.search_destination_for_me),
                    true to stringResource(R.string.search_destination_for_other),
                ).forEach { (isOther, label) ->
                    val selected = (forOther == isOther)
                    Surface(
                        onClick = { forOther = isOther },
                        shape = RoundedCornerShape(AppRadius.m),
                        color = if (selected) colors.surface else colors.surfaceAlt,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(4.dp),
                    ) {
                        Text(
                            text = label,
                            style = AppTypography.labelM,
                            color = if (selected) colors.textPrimary else colors.textSubtle,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        )
                    }
                }
            }

            if (forOther) {
                Spacer(Modifier.height(16.dp))
                SearchDestinationTextField(
                    value = "",
                    placeholder = stringResource(R.string.search_destination_passenger_name),
                    leadingIcon = AppIcon.user,
                    onValueChange = {},
                )
                Spacer(Modifier.height(10.dp))
                SearchDestinationTextField(
                    value = "",
                    placeholder = "+216 XX XXX XXX",
                    leadingIcon = AppIcon.phone,
                    onValueChange = {},
                )
            }

            Spacer(Modifier.height(20.dp))

            // Origin/Destination fields with dashed connector
            Box(modifier = Modifier.fillMaxWidth()) {
                // Dashed vertical connector line
                Box(
                    modifier =
                        Modifier
                            .padding(start = 5.dp, top = 32.dp, bottom = 32.dp)
                            .width(2.dp)
                            .height(72.dp)
                            .border(
                                width = 1.dp,
                                color = colors.divider,
                                shape = RoundedCornerShape(1.dp),
                            ),
                )

                Column {
                    // Origin
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                        )
                        Spacer(Modifier.width(14.dp))
                        SearchDestinationTextField(
                            value = originText,
                            placeholder = stringResource(R.string.search_destination_origin_hint),
                            onValueChange = onOriginChange,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Add stop chip
                    Row(modifier = Modifier.padding(start = 26.dp, top = 4.dp, bottom = 4.dp)) {
                        Surface(
                            onClick = onAddStop,
                            shape = RoundedCornerShape(AppRadius.full),
                            color = colors.surfaceAlt,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    painter = painterResource(AppIcon.plus),
                                    contentDescription = null,
                                    tint = colors.textSubtle,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = stringResource(R.string.search_destination_add_stop),
                                    fontSize = 12.5.sp,
                                    color = colors.textSubtle,
                                )
                            }
                        }
                    }

                    // Destination
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(colors.error),
                        )
                        Spacer(Modifier.width(14.dp))
                        SearchDestinationTextField(
                            value = destinationText,
                            placeholder = stringResource(R.string.search_destination_destination_hint),
                            onValueChange = onDestinationChange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Footer: schedule picker row
            HorizontalDivider(color = colors.divider)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(AppIcon.clock),
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.search_destination_now),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onScheduleClick) {
                    Text(
                        text = stringResource(R.string.search_destination_schedule),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                }
            }

            // Map picker CTA
            Surface(
                onClick = onMapPickerClick,
                shape = RoundedCornerShape(AppRadius.full),
                color = colors.primary,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AppSpacing.buttonHeight),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.search_destination_map_picker_cta),
                        style = AppTypography.button,
                        color = colors.surface,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchDestinationTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: Int? = null,
) {
    val colors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(AppRadius.m),
        color = colors.surfaceAlt,
        modifier =
            modifier
                .fillMaxWidth()
                .height(46.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            if (leadingIcon != null) {
                Icon(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    tint = colors.textSubtle,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = value.ifBlank { placeholder },
                style = AppTypography.bodyM,
                color = if (value.isBlank()) colors.textSubtle else colors.textPrimary,
            )
        }
    }
}
