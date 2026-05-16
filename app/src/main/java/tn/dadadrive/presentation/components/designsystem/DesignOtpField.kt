package tn.dadadrive.presentation.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tn.dadadrive.core.designsystem.spacing.AppSpacing
import tn.dadadrive.core.theme.AppTypography
import tn.dadadrive.core.theme.LocalAppColors

@Composable
fun DesignOtpField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onFilled: (String) -> Unit = {}
) {
    val c = LocalAppColors.current
    val digits = code.filter { it.isDigit() }.take(6)

    LaunchedEffect(digits) {
        if (digits.length == 6) onFilled(digits)
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val ch = digits.getOrNull(index)?.toString().orEmpty()
                val focused = index == digits.length
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .border(
                            2.dp,
                            if (focused) c.primary else c.border,
                            RoundedCornerShape(AppSpacing.inputRadius)
                        )
                        .background(c.surface, RoundedCornerShape(AppSpacing.inputRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ch, style = AppTypography.monoM, color = c.textPrimary)
                }
            }
        }
        BasicTextField(
            value = digits,
            onValueChange = { raw ->
                val next = raw.filter { it.isDigit() }.take(6)
                if (next != digits) onCodeChange(next)
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(56.dp)
                .alpha(0.001f),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = AppTypography.monoM.copy(color = c.textPrimary, textAlign = TextAlign.Center)
        )
    }
}
