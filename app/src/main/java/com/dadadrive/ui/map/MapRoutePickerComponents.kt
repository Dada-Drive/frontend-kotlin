package com.dadadrive.ui.map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors
import java.util.Calendar

private const val MINI_MAPS_ASSET_PATH = "mini-maps.png"

// ─────────────────────────────────────────────────────────────────────────────
// Date/Time picker
// ─────────────────────────────────────────────────────────────────────────────

internal fun showScheduleDateTimePicker(
    context: Context,
    initialMs: Long,
    minMs: Long,
    onChosen: (Long) -> Unit
) {
    val init = Calendar.getInstance().apply { timeInMillis = initialMs.coerceAtLeast(minMs) }
    DatePickerDialog(
        context,
        { _, y, m, d ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y); set(Calendar.MONTH, m); set(Calendar.DAY_OF_MONTH, d)
            }
            TimePickerDialog(
                context,
                { _, h, min ->
                    cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, min)
                    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                    onChosen(cal.timeInMillis.coerceAtLeast(minMs))
                },
                init.get(Calendar.HOUR_OF_DAY), init.get(Calendar.MINUTE), true
            ).show()
        },
        init.get(Calendar.YEAR), init.get(Calendar.MONTH), init.get(Calendar.DAY_OF_MONTH)
    ).apply { datePicker.minDate = minMs }.show()
}

// ─────────────────────────────────────────────────────────────────────────────
// Pin overlay helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun rememberRouteSheetMiniMapBitmap(): ImageBitmap? {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.assets.open(MINI_MAPS_ASSET_PATH).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

@Composable
internal fun PickTargetAddressBubble(address: String?, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val label = address?.takeIf { it.isNotBlank() } ?: stringResource(R.string.map_move_to_set_pickup)
    val dark = isSystemInDarkTheme()
    val bg = if (dark) Color(0xE6000000) else Color(0xE6F2F2F2)
    val fg = if (dark) Color.White else c.textPrimary
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = bg, shadowElevation = 8.dp) {
        Text(
            text = label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 2, textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Google-Maps-style vertical connector icon
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun RouteConnectorIcon(
    intermediateCount: Int,
    activeField: ActiveRouteField,
    modifier: Modifier = Modifier
) {
    val totalSegments = 1 + intermediateCount
    val connectorColor = Color(0xFF8E8E93)
    val finalDestColor = LocalAppColors.current.errorRed
    val iconHeight by animateDpAsState(
        targetValue = (56 * (1 + intermediateCount) + 8 * totalSegments).dp,
        animationSpec = tween(220),
        label = "connector_height"
    )

    Canvas(modifier = modifier.width(24.dp).height(iconHeight)) {
        val cx = size.width / 2f
        val top = 10f
        val bottom = size.height - 12f
        val totalNodes = intermediateCount + 2 // origin + intermediates + final
        val step = if (totalNodes > 1) (bottom - top) / (totalNodes - 1) else 0f
        val nodeY = List(totalNodes) { idx -> top + (step * idx) }
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

        for (seg in 0 until nodeY.lastIndex) {
            val linePath = Path().apply {
                moveTo(cx, nodeY[seg] + 6f)
                lineTo(cx, nodeY[seg + 1] - 6f)
            }
            drawPath(
                path = linePath,
                color = connectorColor,
                style = Stroke(width = 2f, pathEffect = dashEffect)
            )
        }

        // Origin: grey dot
        drawCircle(color = connectorColor, radius = 4.5f, center = Offset(cx, nodeY.first()))

        // Intermediate stops: grey dots
        if (intermediateCount > 0) {
            for (index in 0 until intermediateCount) {
                drawCircle(color = connectorColor, radius = 4.5f, center = Offset(cx, nodeY[index + 1]))
            }
        }

        // Final destination: red pin
        val finalY = nodeY.last()
        val pinR = 9.2f
        val pinCY = finalY - 3f
        val tailPath = Path().apply {
            moveTo(cx - 3f, pinCY + pinR * 0.58f)
            lineTo(cx + 3f, pinCY + pinR * 0.58f)
            lineTo(cx, finalY + 8f)
            close()
        }
        drawPath(tailPath, finalDestColor)
        drawCircle(color = finalDestColor, radius = pinR, center = Offset(cx, pinCY))
        drawCircle(color = Color.White, radius = pinR * 0.40f, center = Offset(cx, pinCY))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Legacy single-field icons (kept for other screens)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun FromOriginRingIcon(active: Boolean, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val ringColor by animateColorAsState(
        targetValue = if (active) c.primary else c.textHint.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = 550f), label = "origin_ring_color"
    )
    val centerDotColor by animateColorAsState(
        targetValue = if (active) c.primary else c.textHint.copy(alpha = 0.7f),
        animationSpec = spring(stiffness = 550f), label = "origin_dot_color"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.05f else 1f, animationSpec = spring(stiffness = 600f), label = "origin_icon_scale"
    )
    Box(modifier.size(28.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size((20 * iconScale).dp).border(3.dp, ringColor, CircleShape))
        Box(modifier = Modifier.size((7 * iconScale).dp).background(centerDotColor, CircleShape))
    }
}

@Composable
internal fun DestinationDotIcon(active: Boolean, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val dotColor by animateColorAsState(
        targetValue = if (active) c.errorRed else c.textHint.copy(alpha = 0.7f),
        animationSpec = spring(stiffness = 550f), label = "destination_dot_color"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.1f else 1f, animationSpec = spring(stiffness = 600f), label = "destination_icon_scale"
    )
    Box(modifier = modifier.size(28.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size((10 * iconScale).dp).background(dotColor, CircleShape))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Map picker button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun MapPickerRouteIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cachedMiniMap: ImageBitmap? = null
) {
    val context = LocalContext.current
    val miniMapBitmap = cachedMiniMap ?: remember(context) {
        runCatching {
            context.assets.open(MINI_MAPS_ASSET_PATH).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }
    IconButton(onClick = onClick, modifier = modifier.size(40.dp)) {
        if (miniMapBitmap != null) {
            Image(
                bitmap = miniMapBitmap,
                contentDescription = stringResource(R.string.map_pick_on_map),
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_map_picker_route),
                contentDescription = stringResource(R.string.map_pick_on_map),
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

