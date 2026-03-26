package com.dadadrive.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.core.designsystem.spacing.AppSpacing
import com.dadadrive.ui.theme.DadaDriveTheme

// ─────────────────────────────────────────────────────────
// PRIMARY BUTTON
// ─────────────────────────────────────────────────────────

/**
 * A full-width primary action button following the DadaDrive design system.
 *
 * @param text            Label displayed inside the button.
 * @param onClick         Called when the button is tapped.
 * @param modifier        Optional [Modifier].
 * @param enabled         Whether the button accepts user interaction.
 * @param isLoading       When true, replaces the label with a [CircularProgressIndicator].
 * @param containerColor  Background color of the button. Defaults to [MaterialTheme.colorScheme.primary].
 * @param contentColor    Foreground color of the button. Defaults to [MaterialTheme.colorScheme.onPrimary].
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(AppSpacing.buttonHeight),
        shape = RoundedCornerShape(AppSpacing.buttonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.35f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PrimaryButtonPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PrimaryButtonLoadingPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {}, isLoading = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PrimaryButtonDisabledPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {}, enabled = false)
    }
}
