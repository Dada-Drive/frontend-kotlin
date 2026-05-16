package tn.dadadrive.presentation.components.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import tn.dadadrive.core.theme.LocalAppColors

enum class DesignIconButtonVariant { Standard, Call }

@Composable
fun DesignIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DesignIconButtonVariant = DesignIconButtonVariant.Standard
) {
    val c = LocalAppColors.current
    val size = if (variant == DesignIconButtonVariant.Call) 44.dp else 40.dp
    val bg: Color = if (variant == DesignIconButtonVariant.Call) c.primary else c.surface
    val fg: Color = if (variant == DesignIconButtonVariant.Call) c.onPrimary else c.textPrimary
    Box(
        modifier = modifier
            .size(size)
            .shadow(3.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.04f))
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = fg, modifier = Modifier.size(20.dp))
    }
}
