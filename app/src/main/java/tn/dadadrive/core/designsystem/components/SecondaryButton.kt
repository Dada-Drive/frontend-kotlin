package tn.dadadrive.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.dadadrive.core.designsystem.spacing.AppSpacing
import tn.dadadrive.core.theme.LocalAppColors

private val secondaryHeight = 52.dp
private val secondaryBorder = 1.5.dp

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    val c = LocalAppColors.current
    val clickable = enabled && !isLoading
    val borderColor = when {
        !clickable -> c.textDisabled
        else -> c.primary
    }
    OutlinedButton(
        onClick = onClick,
        enabled = clickable,
        modifier = modifier
            .fillMaxWidth()
            .height(secondaryHeight),
        shape = RoundedCornerShape(AppSpacing.buttonRadius),
        border = BorderStroke(secondaryBorder, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = c.primary,
            disabledContentColor = c.textDisabled,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = c.primary,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                leading?.invoke()
                if (leading != null) Spacer(Modifier.width(AppSpacing.s))
                Text(text = text, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
