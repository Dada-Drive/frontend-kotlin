package tn.dadadrive.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tn.dadadrive.core.designsystem.spacing.AppSpacing
import tn.dadadrive.core.theme.DadaDriveTheme
import tn.dadadrive.core.theme.LocalAppColors

enum class PrimaryButtonState {
    Default,
    Loading,
    Disabled,
    Success
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: PrimaryButtonState = PrimaryButtonState.Default,
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
    onSuccessReset: () -> Unit = {}
) {
    val c = LocalAppColors.current
    val isLoading = state == PrimaryButtonState.Loading
    val isSuccess = state == PrimaryButtonState.Success
    val isForcedDisabled = state == PrimaryButtonState.Disabled
    val clickable = enabled && !isLoading && !isSuccess && !isForcedDisabled

    LaunchedEffect(state) {
        if (state == PrimaryButtonState.Success) {
            delay(2_500)
            onSuccessReset()
        }
    }

    val bg = when {
        isSuccess -> c.successGreen
        else -> containerColor ?: c.primary
    }
    val fg = when {
        isSuccess -> c.onPrimary
        else -> contentColor ?: c.onPrimary
    }

    Button(
        onClick = onClick,
        enabled = clickable,
        modifier = modifier
            .fillMaxWidth()
            .height(AppSpacing.buttonHeight),
        shape = RoundedCornerShape(AppSpacing.buttonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = bg,
            contentColor = fg,
            disabledContainerColor = c.buttonDisabledBackground,
            disabledContentColor = c.buttonDisabledText
        )
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = fg,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            isSuccess -> {
                Icon(Icons.Filled.Check, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
            }
            else -> {
                Text(text = text, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonLoadingPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {}, state = PrimaryButtonState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonDisabledPreview() {
    DadaDriveTheme {
        PrimaryButton(text = "Continuer", onClick = {}, state = PrimaryButtonState.Disabled)
    }
}
