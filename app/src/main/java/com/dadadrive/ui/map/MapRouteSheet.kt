package com.dadadrive.ui.map

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import java.util.Calendar

private const val MINI_MAPS_ASSET_PATH = "mini-maps.png"
internal sealed class ActiveRouteField {
    data object Origin : ActiveRouteField()
    data class Intermediate(val index: Int) : ActiveRouteField()
    data object FinalDestination : ActiveRouteField()
}
private const val SCHEDULE_PICKUP_MIN_LEAD_MS = 30L * 60L * 1000L

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

private val PICKUP_CALLOUT_VERTICAL_OFFSET = (-68).dp

@Composable
private fun rememberRouteSheetMiniMapBitmap(): ImageBitmap? {
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
internal fun PickupPinOverlay(
    address: String?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    confirmButtonText: String,
    isDestination: Boolean = false,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val pinColor = if (isDestination) c.errorRed else c.primary
    val addressText = address.orEmpty()
    val canConfirm = isLoading || addressText.isNotBlank()

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = PICKUP_CALLOUT_VERTICAL_OFFSET)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    modifier = Modifier.clickable(enabled = canConfirm, onClick = onConfirm),
                    shape = RoundedCornerShape(14.dp),
                    color = c.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.8.dp, color = c.textPrimary)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = stringResource(if (isDestination) R.string.map_confirm_destination else R.string.map_pickup_location),
                                color = pinColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp
                            )
                            Text(
                                text = when {
                                    isLoading -> stringResource(R.string.map_getting_address)
                                    addressText.isBlank() -> stringResource(R.string.map_move_to_set_pickup)
                                    else -> addressText
                                },
                                color = c.textPrimary,
                                fontSize = if (isDestination) 14.sp else 13.sp,
                                fontWeight = if (isDestination && !isLoading && addressText.isNotBlank()) FontWeight.SemiBold else FontWeight.Medium,
                                lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                            tint = if (canConfirm) c.textHint else c.textHint.copy(alpha = 0.30f), modifier = Modifier.size(18.dp))
                    }
                }
                Canvas(modifier = Modifier.size(width = 12.dp, height = 7.dp).offset(y = 1.dp)) {
                    val triangle = Path().apply {
                        moveTo(size.width / 2f, size.height); lineTo(0f, 0f); lineTo(size.width, 0f); close()
                    }
                    drawPath(path = triangle, color = c.surface)
                }
            }
        }

        CenterPushPinOverlay(color = pinColor)

        Canvas(modifier = Modifier.align(Alignment.Center).offset(y = 6.dp).size(width = 20.dp, height = 5.dp)) {
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Transparent)),
                radius = size.minDimension
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp).padding(bottom = 40.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(enabled = canConfirm, onClick = onConfirm),
                shape = RoundedCornerShape(999.dp),
                color = if (canConfirm) pinColor else pinColor.copy(alpha = 0.45f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.height(48.dp).fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(32.dp))
                    Text(
                        text = confirmButtonText,
                        color = if (isDestination) Color.Black else c.onPrimary,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                    )
                    if (isDestination) {
                        Surface(modifier = Modifier.size(34.dp), shape = CircleShape, color = Color.White, shadowElevation = 2.dp) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.NearMe, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Spacer(Modifier.width(34.dp))
                    }
                }
            }
            if (onCancel != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onCancel),
                    shape = RoundedCornerShape(999.dp), color = c.surface,
                    shadowElevation = 2.dp, border = BorderStroke(1.dp, c.textPrimary)
                ) {
                    Box(modifier = Modifier.height(48.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.common_cancel), color = c.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
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

@Composable
private fun CenterPushPinOverlay(color: Color) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(width = 28.dp, height = 50.dp)) {
            val w = size.width; val h = size.height
            val ballRadius = 14f / 28f * w
            val centerX = w / 2f; val centerY = ballRadius
            val needleTop = (ballRadius * 2f - (2f / 50f * h)).coerceAtLeast(0f)
            val needleBottom = h
            val halfNeedleTopWidth = (4f / 28f * w) / 2f
            val halfNeedleBottomWidth = (1f / 28f * w) / 2f
            val needlePath = Path().apply {
                moveTo(centerX - halfNeedleTopWidth, needleTop)
                lineTo(centerX + halfNeedleTopWidth, needleTop)
                lineTo(centerX + halfNeedleBottomWidth, needleBottom)
                lineTo(centerX - halfNeedleBottomWidth, needleBottom)
                close()
            }
            drawPath(path = needlePath, brush = Brush.horizontalGradient(listOf(Color(0xFF737373), Color(0xFFC7C7C7), Color(0xFF616161))))
            drawCircle(color = color, radius = ballRadius, center = Offset(centerX, centerY))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.72f), Color.White.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(centerX - ballRadius * 0.30f, centerY - ballRadius * 0.40f),
                    radius = ballRadius * 0.85f
                ),
                radius = ballRadius, center = Offset(centerX, centerY)
            )
            drawCircle(color = Color.White.copy(alpha = 0.55f), radius = ballRadius * 0.15f, center = Offset(centerX - ballRadius * 0.28f, centerY + ballRadius * 0.20f))
        }
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
// Animated swap button between the two fields
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun SwapFieldsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .size(30.dp)
            .shadow(elevation = 3.dp, shape = CircleShape, clip = false)
            .background(c.surface, CircleShape)
            .border(1.dp, c.dividerGrey.copy(alpha = 0.5f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                scope.launch {
                    // Bounce scale + rotate 180° on tap
                    launch {
                        scaleAnim.animateTo(0.82f, tween(80, easing = FastOutSlowInEasing))
                        scaleAnim.animateTo(1.08f, tween(100, easing = FastOutSlowInEasing))
                        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    }
                    rotationAnim.animateTo(
                        rotationAnim.value + 180f,
                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                    )
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.SwapVert,
            contentDescription = "Swap",
            tint = c.textSecondary,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    rotationZ = rotationAnim.value
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
        )
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

// ─────────────────────────────────────────────────────────────────────────────
// Bottom sheet wrapper — auto-expands to full screen when typing
//
// Usage in MapScreen:
//   if (showRouteSheet) {
//       RouteItineraryBottomSheet(onDismiss = { showRouteSheet = false }) { sheetState ->
//           RouteItinerarySheetContent(..., sheetState = sheetState)
//       }
//   }
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteItineraryBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable (sheetState: SheetState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { true }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = LocalAppColors.current.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        content(sheetState)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fluid animated text field row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RouteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isActive: Boolean,
    isOrigin: Boolean,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource,
    fieldColors: androidx.compose.material3.TextFieldColors,
    imeAction: ImeAction = ImeAction.Next,
    onMapPickerClick: () -> Unit,
    cachedMiniMap: ImageBitmap?,
    onFocused: () -> Unit = {},
    showRemove: Boolean = false,
    onRemoveClick: (() -> Unit)? = null,
    accentColorOverride: Color? = null,
    intermediateStopNumber: Int? = null,
    showMapPicker: Boolean = true,
    staticBorderColor: Color? = null,
    staticBackgroundColor: Color? = null,
    staticTextColor: Color? = null,
    disableShadow: Boolean = false
) {
    val c = LocalAppColors.current
    val accentColor = accentColorOverride ?: if (isOrigin) c.primary else c.errorRed
    val borderColor by animateColorAsState(
        targetValue = if (isActive) accentColor.copy(alpha = 0.45f) else c.dividerGrey.copy(alpha = 0.55f),
        animationSpec = tween(220), label = "border_color"
    )
    val shadowElevation by animateDpAsState(
        targetValue = if (isActive) 4.dp else 1.dp,
        animationSpec = tween(220), label = "shadow_elev"
    )
    val searchIconColor by animateColorAsState(
        targetValue = if (isActive) accentColor else c.textHint.copy(alpha = 0.7f),
        animationSpec = tween(180), label = "search_icon_color"
    )

    val resolvedBorderColor = staticBorderColor ?: borderColor
    val resolvedBackgroundColor = staticBackgroundColor ?: c.surface
    val resolvedShadow = if (disableShadow) 0.dp else shadowElevation

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(resolvedShadow, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(resolvedBackgroundColor)
            .border(1.dp, resolvedBorderColor, RoundedCornerShape(12.dp))
            .padding(start = 10.dp, end = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (intermediateStopNumber != null) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color(0xFF8E8E93), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = intermediateStopNumber.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = searchIconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onFocusChanged { if (it.isFocused) onFocused() },
            readOnly = readOnly,
            singleLine = true,
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            colors = fieldColors,
            interactionSource = interactionSource
        )
        if (showMapPicker) {
            IconButton(onClick = onMapPickerClick, modifier = Modifier.size(40.dp)) {
                if (cachedMiniMap != null) {
                    Image(
                        bitmap = cachedMiniMap,
                        contentDescription = stringResource(R.string.map_pick_on_map),
                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(5.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_map_picker_route),
                        contentDescription = stringResource(R.string.map_pick_on_map),
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        if (showRemove && onRemoveClick != null) {
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove stop",
                    tint = c.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main route sheet content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RouteItinerarySheetContent(
    originValue: String,
    onOriginChange: (String) -> Unit,
    intermediateStops: List<String>,
    finalDestination: String,
    onIntermediateStopChange: (index: Int, value: String) -> Unit,
    onAddIntermediateStop: () -> Unit,
    onRemoveIntermediateStop: (index: Int) -> Unit,
    onFinalDestinationChange: (String) -> Unit,
    pickupResults: List<AddressSearchHit>,
    pickupSearchLoading: Boolean,
    destinationResults: List<AddressSearchHit>,
    destinationSearchLoading: Boolean,
    onPickupHit: (AddressSearchHit) -> Unit,
    onIntermediateStopHit: (index: Int, hit: AddressSearchHit) -> Unit,
    onFinalDestinationHit: (AddressSearchHit) -> Unit,
    onClose: () -> Unit,
    onOpenPickupMapPicker: () -> Unit,
    onOpenDestinationMapPicker: () -> Unit,
    pickupNow: Boolean,
    onPickupNowToggle: () -> Unit,
    forMe: Boolean,
    onForMeToggle: () -> Unit,
    scheduledAtEpochMs: Long?,
    onScheduledAtChosen: (Long) -> Unit,
    passengerName: String,
    onPassengerNameChange: (String) -> Unit,
    passengerPhone: String,
    onPassengerPhoneChange: (String) -> Unit,
    sheetState: SheetState? = null
) {
    val c = LocalAppColors.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val maxIntermediateStops = 4

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        cursorColor = c.primary,
        focusedPlaceholderColor = c.textHint,
        unfocusedPlaceholderColor = c.textHint
    )
    val intermediateFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        cursorColor = c.primary,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    )
    val originFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        focusedTextColor = c.textSecondary,
        unfocusedTextColor = c.textSecondary,
        cursorColor = c.primary,
        focusedPlaceholderColor = c.textHint,
        unfocusedPlaceholderColor = c.textHint
    )

    val originInteraction = remember { MutableInteractionSource() }
    val intermediateInteractions = remember(intermediateStops.size) {
        List(intermediateStops.size) { MutableInteractionSource() }
    }
    val finalDestinationInteraction = remember { MutableInteractionSource() }
    val originFocused by originInteraction.collectIsFocusedAsState()
    val finalDestinationFocused by finalDestinationInteraction.collectIsFocusedAsState()
    val finalDestinationFocusRequester = remember { FocusRequester() }
    val intermediateFocusRequesters = remember { mutableStateListOf<FocusRequester>() }
    LaunchedEffect(intermediateStops.size) {
        while (intermediateFocusRequesters.size < intermediateStops.size) {
            intermediateFocusRequesters.add(FocusRequester())
        }
        while (intermediateFocusRequesters.size > intermediateStops.size) {
            intermediateFocusRequesters.removeLast()
        }
    }
    val intermediateFocusedStates = intermediateInteractions.map { it.collectIsFocusedAsState().value }
    var pendingNewIntermediateFocus by remember { mutableStateOf(false) }
    var activeField by remember { mutableStateOf<ActiveRouteField>(ActiveRouteField.Origin) }

    if (originFocused) {
        activeField = ActiveRouteField.Origin
    }
    intermediateFocusedStates.indexOfFirst { it }.takeIf { it >= 0 }?.let { idx ->
        activeField = ActiveRouteField.Intermediate(idx)
    }
    if (finalDestinationFocused) {
        activeField = ActiveRouteField.FinalDestination
    }

    val miniMapIcon = rememberRouteSheetMiniMapBitmap()

    // Auto-expand sheet when keyboard opens
    val anyFieldFocused = originFocused || finalDestinationFocused || intermediateFocusedStates.any { it }
    LaunchedEffect(anyFieldFocused) {
        if (anyFieldFocused && sheetState != null) {
            scope.launch { sheetState.expand() }
        }
    }
    LaunchedEffect(intermediateStops.size, pendingNewIntermediateFocus) {
        if (pendingNewIntermediateFocus && intermediateStops.isNotEmpty()) {
            intermediateFocusRequesters.lastOrNull()?.requestFocus()
            pendingNewIntermediateFocus = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.map_enter_route),
                color = c.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Surface(shape = CircleShape, color = c.surfaceMuted, modifier = Modifier.size(36.dp)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = c.textHint, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Route input card ───────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = c.surfaceMuted.copy(alpha = 0.45f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // ── LEFT: Connector icon ───────────────────────────────────
                RouteConnectorIcon(
                    intermediateCount = intermediateStops.size,
                    activeField = activeField
                )

                // ── CENTER: Fields + swap button ───────────────────────────
                Column(modifier = Modifier.weight(1f)) {

                    // Origin field
                    RouteTextField(
                        value = stringResource(R.string.map_route_origin_placeholder),
                        onValueChange = {},
                        placeholder = stringResource(R.string.map_route_origin_placeholder),
                        isActive = activeField is ActiveRouteField.Origin,
                        isOrigin = true,
                        readOnly = true,
                        interactionSource = originInteraction,
                        fieldColors = originFieldColors,
                        imeAction = ImeAction.Next,
                        onMapPickerClick = onOpenPickupMapPicker,
                        cachedMiniMap = miniMapIcon,
                        onFocused = {
                            activeField = ActiveRouteField.Origin
                        },
                        modifier = Modifier.clickable {
                            activeField = ActiveRouteField.Origin
                            focusManager.clearFocus()
                        }
                    )

                    // ── SWAP BUTTON between the two fields ─────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(c.dividerGrey.copy(alpha = 0.30f))
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Thin separator line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Color.Transparent)
                        )
                        // Swap button floating on the right side of the separator
                        SwapFieldsButton(
                            onClick = {
                                val tempOrigin = originValue
                                onOriginChange(finalDestination)
                                onFinalDestinationChange(tempOrigin)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(y = 0.dp)
                                .padding(vertical = 4.dp)
                        )
                    }

                    intermediateStops.forEachIndexed { index, stopValue ->
                        Spacer(Modifier.height(8.dp))
                        RouteTextField(
                            value = stopValue,
                            onValueChange = { onIntermediateStopChange(index, it) },
                            placeholder = stringResource(R.string.map_destination_placeholder),
                            isActive = activeField == ActiveRouteField.Intermediate(index),
                            isOrigin = false,
                            focusRequester = intermediateFocusRequesters.getOrNull(index),
                            interactionSource = intermediateInteractions[index],
                            fieldColors = intermediateFieldColors,
                            imeAction = ImeAction.Search,
                            onMapPickerClick = {},
                            cachedMiniMap = miniMapIcon,
                            onFocused = {
                                activeField = ActiveRouteField.Intermediate(index)
                            },
                            showRemove = true,
                            onRemoveClick = { onRemoveIntermediateStop(index) },
                            intermediateStopNumber = index + 1,
                            showMapPicker = false,
                            staticBorderColor = Color(0xFF3A3A3C),
                            staticBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            staticTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disableShadow = true,
                            modifier = Modifier.clickable {
                                activeField = ActiveRouteField.Intermediate(index)
                                intermediateFocusRequesters.getOrNull(index)?.requestFocus()
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    RouteTextField(
                        value = finalDestination,
                        onValueChange = onFinalDestinationChange,
                        placeholder = stringResource(R.string.map_destination_placeholder),
                        isActive = activeField is ActiveRouteField.FinalDestination,
                        isOrigin = false,
                        focusRequester = finalDestinationFocusRequester,
                        interactionSource = finalDestinationInteraction,
                        fieldColors = fieldColors,
                        imeAction = ImeAction.Search,
                        onMapPickerClick = onOpenDestinationMapPicker,
                        cachedMiniMap = miniMapIcon,
                        onFocused = {
                            activeField = ActiveRouteField.FinalDestination
                        },
                        modifier = Modifier.clickable {
                            activeField = ActiveRouteField.FinalDestination
                            finalDestinationFocusRequester.requestFocus()
                        }
                    )

                    if (finalDestination.isNotBlank() && intermediateStops.size < maxIntermediateStops) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    pendingNewIntermediateFocus = true
                                    onAddIntermediateStop()
                                },
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = c.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Ajouter un arrêt",
                                color = c.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Suggestion list ────────────────────────────────────────────────
        val activeDestinationText = when (val field = activeField) {
            is ActiveRouteField.Intermediate -> intermediateStops.getOrElse(field.index) { "" }
            is ActiveRouteField.FinalDestination -> finalDestination
            is ActiveRouteField.Origin -> ""
        }
        RouteSheetSuggestionBlock(
            results = destinationResults,
            loading = destinationSearchLoading,
            queryLengthOk = activeDestinationText.trim().length >= 3,
            onHit = { hit ->
                when (val field = activeField) {
                    is ActiveRouteField.Intermediate -> onIntermediateStopHit(field.index, hit)
                    is ActiveRouteField.FinalDestination -> onFinalDestinationHit(hit)
                    is ActiveRouteField.Origin -> Unit
                }
                focusManager.clearFocus()
            }
        )

        Spacer(Modifier.weight(1f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Suggestion block
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RouteSearchOptionChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.dividerGrey.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RouteSheetSuggestionBlock(
    results: List<AddressSearchHit>,
    loading: Boolean,
    queryLengthOk: Boolean,
    onHit: (AddressSearchHit) -> Unit
) {
    val c = LocalAppColors.current
    when {
        loading -> {
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = c.primary, strokeWidth = 2.dp)
            }
        }
        results.isNotEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surface)
                    .border(1.dp, c.dividerGrey.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(vertical = 4.dp)
            ) {
                results.forEachIndexed { index, hit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHit(hit) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Location pin icon for suggestions
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(c.surfaceMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = c.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = hit.label,
                            color = c.textPrimary, fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < results.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            color = c.dividerGrey.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
        queryLengthOk -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.surfaceMuted.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.map_no_address_results),
                    color = c.textSecondary, fontSize = 13.sp
                )
            }
        }
    }
}