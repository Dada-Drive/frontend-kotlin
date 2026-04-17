package com.dadadrive.ui.map

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import com.dadadrive.R
import com.dadadrive.core.designsystem.spacing.AppRadius
import com.dadadrive.core.designsystem.spacing.AppSpacing
import com.dadadrive.ui.theme.AppColor
import com.dadadrive.ui.theme.AppTypography
import com.dadadrive.ui.theme.LocalAppColors

private val BallRadius = 20.dp
private val NeedleHeight = 20.dp
private val BubbleGap = 14.dp
private val BubbleCorner = 10.dp

@Composable
fun PickupPinOverlay(
    address: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    isDestination: Boolean = false,
    onCancel: (() -> Unit)? = null,
    isOutOfBounds: Boolean = false,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val pinColor = when {
        isOutOfBounds -> AppColor.error
        isDestination -> AppColor.destination
        else -> AppColor.green
    }

    val isEmpty = address.isBlank()
    val context = LocalContext.current
    val vibrator = remember(context) {
        ContextCompat.getSystemService(context, Vibrator::class.java)
    }

    fun vibrateHeavyClick() {
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    fun vibrateLand() {
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }

    var prevLoading by remember { mutableStateOf(isLoading) }
    LaunchedEffect(isLoading) {
        if (prevLoading && !isLoading) {
            vibrateLand()
        }
        prevLoading = isLoading
    }

    val pinTotalHeight = BallRadius * 2 + NeedleHeight

    val density = LocalDensity.current
    val jumpUpPx = remember(density) { with(density) { (-20).dp.toPx() } }
    val jumpOffset = remember { Animatable(0f) }
    var previousAddressForJump by remember { mutableStateOf<String?>(null) }

    suspend fun performPinJump() {
        jumpOffset.snapTo(jumpUpPx)
        jumpOffset.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    LaunchedEffect(Unit) {
        performPinJump()
    }

    LaunchedEffect(address) {
        if (address.isEmpty()) {
            previousAddressForJump = address
            return@LaunchedEffect
        }
        val prev = previousAddressForJump
        if (prev != null && prev != address) {
            performPinJump()
        }
        previousAddressForJump = address
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        var bubbleHeightPx by remember { mutableIntStateOf(0) }
        val bubbleHeight = with(density) { bubbleHeightPx.toDp() }.coerceAtLeast(56.dp)

        val centerY = maxHeight / 2
        val columnTopOffset = centerY - bubbleHeight - BubbleGap - pinTotalHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = columnTopOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.onGloballyPositioned { bubbleHeightPx = it.size.height },
            ) {
                PickupAddressBubble(
                    address = address,
                    isLoading = isLoading,
                    isDestination = isDestination,
                    isOutOfBounds = isOutOfBounds,
                    pinColor = pinColor,
                    onConfirm = {
                        if (!isEmpty && !isLoading) onConfirm()
                    },
                )
            }

            Spacer(modifier = Modifier.height(BubbleGap))

            BoltPin(
                modifier = Modifier.offset { IntOffset(0, jumpOffset.value.roundToInt()) },
                radius = BallRadius,
                pointerHeight = NeedleHeight,
                color = pinColor,
                isDragging = isDragging,
                isLoading = isLoading,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(bottom = AppSpacing.l),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            PickupConfirmButton(
                pinColor = pinColor,
                isEmpty = isEmpty,
                isLoading = isLoading,
                isOutOfBounds = isOutOfBounds,
                isDestination = isDestination,
                onClick = {
                    vibrateHeavyClick()
                    onConfirm()
                },
            )

            if (onCancel != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(AppRadius.full))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCancel,
                        ),
                    shape = RoundedCornerShape(AppRadius.full),
                    color = colors.surface,
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            style = AppTypography.labelL.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickupAddressBubble(
    address: String,
    isLoading: Boolean,
    isDestination: Boolean,
    isOutOfBounds: Boolean,
    pinColor: Color,
    onConfirm: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isEmpty = address.isBlank()
    val enabled = !isEmpty && !isLoading

    val labelText = when {
        isOutOfBounds -> stringResource(R.string.map_outside_tunisia_title)
        isDestination -> stringResource(R.string.map_confirm_destination)
        else -> stringResource(R.string.map_pickup_location)
    }
    val bodyText = when {
        isOutOfBounds -> stringResource(R.string.map_outside_tunisia_hint)
        isLoading -> stringResource(R.string.map_getting_address)
        isEmpty -> stringResource(R.string.map_move_to_set)
        else -> address
    }

    val bubbleShape = RoundedCornerShape(BubbleCorner)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, bubbleShape, clip = false)
            .clip(bubbleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onConfirm,
            ),
        shape = bubbleShape,
        color = colors.surface,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = labelText,
                        style = AppTypography.labelS.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = pinColor,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = pinColor,
                            )
                        }
                        Text(
                            text = bodyText,
                            style = AppTypography.bodyM.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary,
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                    val w = 16.dp.toPx()
                    val h = 8.dp.toPx()
                    val path = Path().apply {
                        moveTo(size.width / 2f - w / 2f, 0f)
                        lineTo(size.width / 2f + w / 2f, 0f)
                        lineTo(size.width / 2f, h)
                        close()
                    }
                    drawPath(path, colors.surface)
                }
            }
        }
    }
}

@Composable
fun BoltPin(
    radius: Dp,
    pointerHeight: Dp,
    color: Color,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val diameter = radius * 2
    val innerTarget = if (isDragging) 4.dp else diameter * 0.286f
    val innerSize by animateDpAsState(
        targetValue = innerTarget,
        animationSpec = spring(
            dampingRatio = 0.42f,
            stiffness = Spring.StiffnessLow,
        ),
        label = "innerDot",
    )

    val stemWidth = maxOf(4.dp, diameter * 0.072f)
    val pulseTargetScale = remember(innerSize, diameter) {
        if (innerSize.value <= 0f) 1f else diameter.value / innerSize.value
    }

    val shadowW by animateDpAsState(
        targetValue = if (isLoading) 10.dp else 24.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "shadowW",
    )
    val shadowH by animateDpAsState(
        targetValue = if (isLoading) 3.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "shadowH",
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.35f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "shadowAlpha",
    )

    val blurRadius by animateDpAsState(
        targetValue = if (isLoading) 14.dp else 7.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pinShadowBlur",
    )
    val shadowSpreadAlpha by animateFloatAsState(
        targetValue = if (isLoading) 0.2f else 0.38f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pinShadowSpread",
    )

    Box(
        modifier = modifier.wrapContentSize(align = Alignment.TopCenter),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(diameter)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxSize().background(color, CircleShape))
                Box(
                    modifier = Modifier
                        .size(innerSize)
                        .align(Alignment.Center)
                        .background(Color.White, CircleShape),
                )
                if (!isDragging) {
                    PinPulseRing(
                        startDiameter = innerSize,
                        targetScale = pulseTargetScale,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(stemWidth)
                    .height(pointerHeight)
                    .background(Color(0xFF9E9E9E)),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = shadowH / 2)
                .size(width = shadowW, height = shadowH)
                .alpha(shadowAlpha * shadowSpreadAlpha),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = maxOf(size.width, size.height) / 2f + blurRadius.toPx(),
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                )
            }
        }
    }
}

@Composable
private fun PinPulseRing(
    startDiameter: Dp,
    targetScale: Float,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }
    val opacity = remember { Animatable(0.65f) }

    LaunchedEffect(startDiameter, targetScale) {
        while (true) {
            scale.snapTo(1f)
            opacity.snapTo(0.65f)
            coroutineScope {
                launch {
                    scale.animateTo(
                        targetValue = targetScale,
                        animationSpec = tween(1400, easing = FastOutLinearInEasing),
                    )
                }
                launch {
                    opacity.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(1400, easing = FastOutLinearInEasing),
                    )
                }
            }
            delay(200)
        }
    }

    Box(
        modifier = modifier
            .size(startDiameter)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                alpha = opacity.value
            }
            .border(2.dp, Color.White, CircleShape),
    )
}

@Composable
private fun PickupConfirmButton(
    pinColor: Color,
    isEmpty: Boolean,
    isLoading: Boolean,
    isOutOfBounds: Boolean,
    isDestination: Boolean,
    onClick: () -> Unit,
) {
    val disabled = isEmpty || isLoading || isOutOfBounds
    val activeGradient = !disabled && !isOutOfBounds && !isEmpty

    val label = when {
        isOutOfBounds -> stringResource(R.string.map_outside_tunisia)
        isDestination -> stringResource(R.string.map_confirm_destination)
        else -> stringResource(R.string.map_confirm_pickup)
    }

    val bgColor = when {
        isOutOfBounds -> AppColor.error.copy(alpha = 0.12f)
        isEmpty -> pinColor.copy(alpha = 0.4f)
        else -> pinColor
    }

    val fgColor = when {
        isOutOfBounds -> AppColor.error
        disabled -> AppColor.textOnGreen.copy(alpha = 0.6f)
        else -> AppColor.textOnGreen
    }

    val shape = RoundedCornerShape(AppSpacing.buttonRadius)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(6.dp, shape, spotColor = pinColor.copy(alpha = 0.25f))
            .clip(shape)
            .clickable(
                enabled = !disabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .background(bgColor),
    ) {
        if (activeGradient) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                style = AppTypography.headingS.copy(fontSize = 16.sp),
                color = fgColor,
            )
        }
    }
}
