package tn.turbodrive.presentation.components.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppShadow
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

@Composable
fun DesignCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val c = LocalAppColors.current
    Card(
        modifier = modifier.then(AppShadow.card()),
        shape = RoundedCornerShape(AppSpacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(AppSpacing.l)) { content() }
    }
}

@Composable
fun DesignChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val c = LocalAppColors.current
    val bg =
        when {
            accent && selected -> c.accent.copy(alpha = 0.2f)
            selected -> c.surfaceAlt
            else -> c.surface
        }
    val fg = if (accent && selected) c.accent else c.textPrimary
    Surface(
        modifier =
            modifier
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppSpacing.buttonRadius),
        color = bg,
        border = BorderStroke(1.dp, if (selected) c.primary else c.border),
    ) {
        Text(
            label,
            style = AppTypography.labelM,
            color = fg,
            modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
        )
    }
}

@Composable
fun DesignToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onlineStyle: Boolean = false,
) {
    val c = LocalAppColors.current
    val colors =
        if (onlineStyle) {
            SwitchDefaults.colors(
                checkedThumbColor = c.onPrimary,
                checkedTrackColor = c.accent,
                uncheckedThumbColor = c.textSecondary,
                uncheckedTrackColor = c.surfaceAlt,
            )
        } else {
            SwitchDefaults.colors(
                checkedThumbColor = c.onPrimary,
                checkedTrackColor = c.primary,
                uncheckedThumbColor = c.textSecondary,
                uncheckedTrackColor = c.surfaceAlt,
            )
        }
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = colors,
    )
}

@Composable
fun DesignStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.m)) {
        val atMin = value <= range.first
        val atMax = value >= range.last
        TextButton(
            onClick = { if (!atMin) onValueChange(value - 1) },
            enabled = !atMin,
            colors = ButtonDefaults.textButtonColors(contentColor = if (atMin) c.textDisabled else c.primary),
        ) {
            Text("−", fontWeight = FontWeight.Bold)
        }
        Text(value.toString(), style = AppTypography.headingM, color = c.textPrimary)
        TextButton(
            onClick = { if (!atMax) onValueChange(value + 1) },
            enabled = !atMax,
            colors = ButtonDefaults.textButtonColors(contentColor = if (atMax) c.textDisabled else c.primary),
        ) {
            Text("+", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = AppSpacing.sheetRadius, topEnd = AppSpacing.sheetRadius),
        dragHandle = {
            val c = LocalAppColors.current
            Spacer(Modifier.height(8.dp))
            Spacer(
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(c.dragHandle, RoundedCornerShape(2.dp)),
            )
            Spacer(Modifier.height(8.dp))
        },
        modifier = modifier,
    ) {
        Column(Modifier.padding(AppSpacing.l)) { content() }
    }
}
