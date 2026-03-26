package com.dadadrive.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.dadadrive.core.designsystem.spacing.AppSpacing
import com.dadadrive.ui.theme.DadaDriveTheme

// ─────────────────────────────────────────────────────────
// APP TEXT FIELD
// ─────────────────────────────────────────────────────────

/**
 * A theme-aware text field following the DadaDrive design system.
 *
 * @param value                Current text value.
 * @param onValueChange        Called when the user modifies the text.
 * @param modifier             Optional [Modifier].
 * @param label                Optional label displayed above the field.
 * @param placeholder          Optional hint shown when the field is empty.
 * @param leadingIcon          Optional composable rendered at the start of the field.
 * @param trailingIcon         Optional composable rendered at the end of the field.
 * @param errorMessage         When non-null, the field is in an error state and this message is shown below.
 * @param singleLine           Whether the field is constrained to a single line.
 * @param enabled              Whether the field accepts user interaction.
 * @param keyboardOptions      Software keyboard options (type, IME action, etc.).
 * @param keyboardActions      Callbacks for IME actions.
 * @param visualTransformation Transformation applied to the displayed text (e.g. password masking).
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val isError = errorMessage != null

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(AppSpacing.inputRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(AppSpacing.xxs))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = AppSpacing.xs)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    DadaDriveTheme {
        var text by remember { mutableStateOf("") }
        AppTextField(
            value = text,
            onValueChange = { text = it },
            label = "Email",
            placeholder = "example@email.com",
            modifier = Modifier.padding(AppSpacing.lg)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTextFieldErrorPreview() {
    DadaDriveTheme {
        AppTextField(
            value = "invalid-email",
            onValueChange = {},
            label = "Email",
            errorMessage = "Please enter a valid email address.",
            modifier = Modifier.padding(AppSpacing.lg)
        )
    }
}
