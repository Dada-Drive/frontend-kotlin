package tn.turbodrive.presentation.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors

private val StepperButtonWidth = 78.dp
private val StepperButtonHeight = 44.dp
private val PriceTextSize = 28.sp
private val SuffixTextSize = 14.sp

/**
 * Fare negotiation numeric stepper — R-4.4.
 *
 * Horizontal control with pill-shaped decrement/increment buttons framing a
 * large price display. Ported from `turbodrive_redesign/screens-driver.jsx`
 * (PriceStepper, L350-414).
 *
 * Differentiated from the generic [DesignStepper] (Int + IntRange) by :
 *   - Double precision (fractional fares)
 *   - Configurable `step`, `formatter`, and `suffix` (default "TND")
 *   - Optional [boundaryHint] tooltip text shown above the component when
 *     the user is at a boundary (handled by caller — display as a simple
 *     caption here so snapshots can reflect it)
 *   - Pill buttons label  "−1 TND" / "+1 TND" rather than plain "−"/"+"
 *
 * @param value Current fare value
 * @param onValueChange Called with the new value after a step; clamped to [min, max]
 * @param min Minimum allowed value (decrement button disabled when value ≤ min)
 * @param max Maximum allowed value (increment button disabled when value ≥ max)
 * @param step Increment/decrement amount per tap (default 1.0)
 * @param formatter Converts [value] to the displayed string (default no decimals)
 * @param suffix Currency label appended after the price (default "TND")
 * @param boundaryHint Optional hint shown above when value is at a boundary
 * @param enabled False → all controls greyed out and non-interactive
 */
@Composable
fun PriceStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    min: Double,
    max: Double,
    step: Double = 1.0,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    formatter: (Double) -> String = { "%.0f".format(it) },
    suffix: String = "TND",
    boundaryHint: String? = null,
) {
    val c = LocalAppColors.current
    val atMin = value <= min
    val atMax = value >= max

    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
    ) {
        if (boundaryHint != null) {
            Box(
                modifier =
                    Modifier
                        .background(c.textPrimary, RoundedCornerShape(AppRadius.s))
                        .padding(horizontal = AppSpacing.m, vertical = AppSpacing.xxs),
            ) {
                Text(
                    text = boundaryHint,
                    style = AppTypography.labelS,
                    color = c.onPrimary,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.l),
        ) {
            StepperButton(
                label = "−1 $suffix",
                enabled = enabled && !atMin,
                atBoundary = atMin,
                onClick = { onValueChange((value - step).coerceAtLeast(min)) },
            )

            Text(
                text =
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = PriceTextSize, fontWeight = FontWeight.Bold)) {
                            append(formatter(value))
                        }
                        append(" ")
                        withStyle(SpanStyle(fontSize = SuffixTextSize, fontWeight = FontWeight.SemiBold)) {
                            append(suffix)
                        }
                    },
                color = c.textPrimary,
                modifier = Modifier.widthIn(min = 80.dp),
            )

            StepperButton(
                label = "+1 $suffix",
                enabled = enabled && !atMax,
                atBoundary = atMax,
                onClick = { onValueChange((value + step).coerceAtMost(max)) },
            )
        }
    }
}

@Composable
private fun StepperButton(
    label: String,
    enabled: Boolean,
    atBoundary: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalAppColors.current
    val containerColor = if (atBoundary) c.surfaceMuted else c.surface
    val contentColor = if (atBoundary) c.textDisabled else c.textPrimary
    val borderColor = if (atBoundary) c.border else c.textPrimary

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(AppRadius.full),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = c.surfaceMuted,
                disabledContentColor = c.textDisabled,
            ),
        modifier =
            Modifier
                .width(StepperButtonWidth)
                .height(StepperButtonHeight)
                .border(1.dp, borderColor, RoundedCornerShape(AppRadius.full)),
    ) {
        Text(label, style = AppTypography.labelM)
    }
}

@Composable
internal fun PriceStepperPreviewContent() {
    val c = LocalAppColors.current
    Surface(color = c.surface) {
        Column(
            modifier = Modifier.padding(AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PriceStepper(value = 15.0, onValueChange = {}, min = 5.0, max = 50.0)
            PriceStepper(value = 5.0, onValueChange = {}, min = 5.0, max = 50.0, boundaryHint = "Limite −1 TND")
            PriceStepper(value = 50.0, onValueChange = {}, min = 5.0, max = 50.0, boundaryHint = "Limite +1 TND")
            PriceStepper(value = 15.0, onValueChange = {}, min = 5.0, max = 50.0, enabled = false)
        }
    }
}
