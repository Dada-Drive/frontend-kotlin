package tn.turbodrive.presentation.components.designsystem

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppMotion
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

private val ToggleHeight = 40.dp
private val TogglePadding = 4.dp

/**
 * Segmented pill toggle — R-4.4.
 *
 * Generic multi-option control for 2–4 choices (e.g. TND/USD, Cash/Wallet,
 * Aujourd'hui/Semaine). Ported from `turbodrive_redesign/design-system.jsx`
 * (Segmented component, L367-391).
 *
 * Active pill : ink (textPrimary) background + onInk text, 2dp shadow.
 * Inactive label : textMuted (textHint).
 * Animation : 160ms ease on pill offset.
 *
 * @param options The list of selectable values (must be non-empty)
 * @param selected The currently active value
 * @param onSelect Callback with newly selected value
 * @param optionLabel Converts a value to its display string
 * @param enabled False → greyed out (40% alpha) and non-interactive
 */
@Composable
fun <T> PriceToggle(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    require(options.isNotEmpty()) { "PriceToggle requires at least one option" }

    val c = LocalAppColors.current
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    PriceToggleLayout(
        options = options,
        selectedIndex = selectedIndex,
        modifier =
            modifier
                .height(ToggleHeight)
                .alpha(if (enabled) 1f else 0.4f)
                .clip(RoundedCornerShape(AppRadius.full))
                .background(c.surfaceMuted),
        optionContent = { index, option ->
            val isActive = index == selectedIndex
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(AppRadius.full))
                        .then(
                            if (enabled) {
                                Modifier.clickable { onSelect(option) }
                            } else {
                                Modifier
                            },
                        )
                        .padding(horizontal = AppSpacing.l),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = optionLabel(option),
                    style = AppTypography.labelM,
                    color = if (isActive) c.onPrimary else c.textHint,
                )
            }
        },
        pillColor = {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(AppRadius.full))
                        .background(c.textPrimary),
            )
        },
    )
}

@Composable
private fun <T> PriceToggleLayout(
    options: List<T>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    optionContent: @Composable (index: Int, option: T) -> Unit,
    pillColor: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val itemPlaceables =
            options.mapIndexed { index, option ->
                subcompose("item_$index") { optionContent(index, option) }
                    .map { it.measure(looseConstraints) }
            }

        val itemWidths = itemPlaceables.map { it.maxOf { p -> p.width } }
        val totalWidth = itemWidths.sum().coerceAtLeast(constraints.minWidth)
        val height =
            constraints.maxHeight.takeIf { it != Int.MAX_VALUE }
                ?: ToggleHeight.roundToPx()

        val selectedOffsetPx = itemWidths.take(selectedIndex).sum()
        val pillWidth = itemWidths.getOrElse(selectedIndex) { 0 }

        val pillPlaceable =
            subcompose("pill") {
                val animatedOffset: Dp by animateDpAsState(
                    targetValue = selectedOffsetPx.toDp(),
                    animationSpec = tween(durationMillis = AppMotion.DURATION_FAST_MS),
                    label = "pillOffset",
                )
                Box(
                    modifier =
                        Modifier
                            .width(pillWidth.toDp())
                            .offset(x = animatedOffset),
                ) { pillColor() }
            }.map { it.measure(looseConstraints) }

        layout(totalWidth, height) {
            pillPlaceable.forEach { it.placeRelative(TogglePadding.roundToPx(), TogglePadding.roundToPx()) }
            var x = 0
            itemPlaceables.forEachIndexed { _, placeables ->
                placeables.forEach { it.placeRelative(x, 0) }
                x += placeables.maxOf { p -> p.width }
            }
        }
    }
}

@Composable
internal fun PriceTogglePreviewContent() {
    val c = LocalAppColors.current
    Surface(color = c.surface) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "TND",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.wrapContentWidth(),
            )
            PriceToggle(
                options = listOf("TND", "USD"),
                selected = "USD",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.wrapContentWidth(),
            )
            PriceToggle(
                options = listOf("Cash", "Wallet", "Card"),
                selected = "Wallet",
                onSelect = {},
                optionLabel = { it },
                modifier = Modifier.wrapContentWidth(),
                enabled = false,
            )
        }
    }
}
