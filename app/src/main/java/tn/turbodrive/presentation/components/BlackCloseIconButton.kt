package tn.turbodrive.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun BlackCloseIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: androidx.compose.ui.unit.Dp = 36.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    contentDescription: String? = null,
) {
    IconButton(onClick = onClick, modifier = modifier.size(buttonSize)) {
        Surface(shape = CircleShape, color = Color.Black) {
            Box(Modifier.size(buttonSize), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}
