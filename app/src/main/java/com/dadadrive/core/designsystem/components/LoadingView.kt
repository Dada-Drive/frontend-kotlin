package com.dadadrive.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.core.designsystem.spacing.AppSpacing
import com.dadadrive.ui.theme.DadaDriveTheme

// ─────────────────────────────────────────────────────────
// LOADING VIEW
// ─────────────────────────────────────────────────────────

/**
 * A centered full-screen loading indicator with an optional [message].
 *
 * @param modifier           Optional [Modifier].
 * @param message            Optional text displayed below the spinner.
 * @param indicatorSize      Diameter of the [CircularProgressIndicator].
 * @param indicatorColor     Color of the spinner arc.
 * @param backgroundColor    Background color of the container.
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = null,
    indicatorSize: Dp = 48.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.background
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = indicatorColor,
                modifier = Modifier.size(indicatorSize),
                strokeWidth = 3.dp
            )
            if (message != null) {
                Spacer(Modifier.height(AppSpacing.lg))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * A compact inline loading indicator for use inside buttons, lists, or cards.
 *
 * @param modifier       Optional [Modifier].
 * @param size           Diameter of the indicator.
 * @param color          Color of the spinner arc.
 * @param strokeWidth    Width of the spinner stroke.
 */
@Composable
fun InlineLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp
) {
    CircularProgressIndicator(
        color = color,
        modifier = modifier.size(size),
        strokeWidth = strokeWidth
    )
}

// ─────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LoadingViewPreview() {
    DadaDriveTheme {
        LoadingView(message = "Loading…")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun LoadingViewNoMessagePreview() {
    DadaDriveTheme {
        LoadingView()
    }
}
