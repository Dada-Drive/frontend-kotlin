package tn.turbodrive.presentation.map

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.turbodrive.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.turbodrive.core.designsystem.spacing.AppRadius
import tn.turbodrive.core.designsystem.spacing.AppSpacing
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.AppTypography
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.core.theme.MapColorTokens
import kotlin.math.max
import kotlin.math.roundToInt

private val BallRadius = 20.dp
private val NeedleHeight = 20.dp
private val BubbleGap = 14.dp
private val BubbleCorner = 10.dp

/** Son [R.raw.jump] quand la nouvelle adresse est résolue (curseur posé). */
private fun playMapPickLocationSettledSound(context: Context) {
    runCatching {
        val app = context.applicationContext
        val mp = MediaPlayer.create(app, R.raw.jump) ?: return@runCatching
        mp.setOnCompletionListener { runCatching { it.release() } }
        mp.setOnErrorListener { _, _, _ ->
            runCatching { mp.release() }
            true
        }
        mp.start()
    }
}

@Composable
fun PickupPinOverlay(
    address: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    isDestination: Boolean = false,
    isIntermediateStop: Boolean = false,
    onCancel: (() -> Unit)? = null,
    isOutOfBounds: Boolean = false,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val pinColor =
        when {
            isOutOfBounds -> colors.error
            isIntermediateStop -> Color(STOP_PIN_YELLOW_ARGB)
            else -> Color.Black
        }

    val isEmpty = address.isBlank()
    val isMoving = isDragging
    val context = LocalContext.current

    fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = ContextCompat.getSystemService(context, VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.getSystemService(context, Vibrator::class.java)
        }
    }

    fun vibrateHeavyClick() {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    /**
     * Vibration marquée lorsque le géocodage a fini : la « pose » du curseur sur la carte est fixée.
     */
    fun vibrateMapLocationSettled() {
        val vibrator = getVibrator(context) ?: return
        vibrator ?: return
        if (!vibrator.hasVibrator()) return
        val pattern = longArrayOf(0L, 200L, 100L, 280L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    var prevLoading by remember { mutableStateOf(isLoading) }
    LaunchedEffect(isLoading) {
        if (prevLoading && !isLoading) {
            vibrateMapLocationSettled()
            playMapPickLocationSettledSound(context)
        }
        prevLoading = isLoading
    }

    var prevMoving by remember { mutableStateOf(isMoving) }
    LaunchedEffect(isMoving) {
        if (prevMoving && !isMoving) {
            vibrateHeavyClick()
        }
        prevMoving = isMoving
    }

    val pinTotalHeight = BallRadius * 2 + NeedleHeight

    val density = LocalDensity.current
    val jumpUpPx = remember(density) { with(density) { (-32).dp.toPx() } }
    val jumpOffset = remember { Animatable(0f) }

    suspend fun performPinJump() {
        jumpOffset.snapTo(jumpUpPx)
        jumpOffset.animateTo(
            targetValue = 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow,
                ),
        )
    }

    LaunchedEffect(Unit) {
        performPinJump()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val centerY = maxHeight / 2
        val pinCenterY = centerY - (pinTotalHeight / 2)
        val bubbleCenterYOffset = (pinCenterY - centerY) - BallRadius - NeedleHeight - BubbleGap

        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = bubbleCenterYOffset),
            contentAlignment = Alignment.Center,
        ) {
            PickupAddressBubble(
                address = address,
                isLoading = isLoading,
                isMoving = isMoving,
                isDestination = isDestination,
                isIntermediateStop = isIntermediateStop,
                isOutOfBounds = isOutOfBounds,
                pinColor = pinColor,
                onConfirm = {
                    if (!isEmpty && !isLoading && !isMoving) {
                        vibrateHeavyClick()
                        onConfirm()
                    }
                },
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .offset(y = pinCenterY - centerY),
            contentAlignment = Alignment.Center,
        ) {
            GroundShadow(isMoving = isMoving)
            val pinShadowRadius by animateDpAsState(
                targetValue = if (isMoving) 14.dp else 7.dp,
                animationSpec = spring(dampingRatio = 0.50f, stiffness = Spring.StiffnessMediumLow),
                label = "pinShadowRadius",
            )
            val pinShadowYOffset by animateDpAsState(
                targetValue = if (isMoving) 10.dp else 4.dp,
                animationSpec = spring(dampingRatio = 0.50f, stiffness = Spring.StiffnessMediumLow),
                label = "pinShadowYOffset",
            )
            val pinShadowAlpha by animateFloatAsState(
                targetValue = if (isMoving) 0.20f else 0.38f,
                animationSpec = spring(dampingRatio = 0.50f, stiffness = Spring.StiffnessMediumLow),
                label = "pinShadowAlpha",
            )
            BoltPin(
                modifier =
                    Modifier
                        .offset { IntOffset(0, jumpOffset.value.roundToInt()) }
                        .offset(y = if (isMoving) (-20).dp else 0.dp)
                        .shadow(
                            elevation = pinShadowRadius,
                            shape = RoundedCornerShape(999.dp),
                            ambientColor = pinColor.copy(alpha = pinShadowAlpha),
                            spotColor = pinColor.copy(alpha = pinShadowAlpha),
                        )
                        .offset(y = pinShadowYOffset - 4.dp),
                radius = BallRadius,
                pointerHeight = NeedleHeight,
                color = pinColor,
                isDragging = isMoving,
                isLoading = isLoading,
            )
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            PickupConfirmButton(
                pinColor = pinColor,
                isEmpty = isEmpty,
                isLoading = isLoading,
                isMoving = isMoving,
                isOutOfBounds = isOutOfBounds,
                isDestination = isDestination,
                isIntermediateStop = isIntermediateStop,
                onClick = {
                    vibrateHeavyClick()
                    onConfirm()
                },
            )

            if (onCancel != null) {
                Surface(
                    modifier =
                        Modifier
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
                            style =
                                AppTypography.labelL.copy(
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
    isMoving: Boolean,
    isDestination: Boolean,
    isIntermediateStop: Boolean,
    isOutOfBounds: Boolean,
    pinColor: Color,
    onConfirm: () -> Unit,
) {
    val colors = LocalAppColors.current
    val isEmpty = address.isBlank()
    val enabled = !isEmpty && !isLoading && !isMoving
    val bubbleBg = if (isLoading) Color.Black else colors.surface
    val labelTextColor = if (isLoading) Color.White else pinColor
    val bodyTextColor =
        when {
            isOutOfBounds -> colors.error
            isLoading -> Color.White
            else -> colors.textPrimary
        }
    val arrowTint = if (isLoading) Color.White else colors.textSecondary

    val labelText =
        when {
            isOutOfBounds -> stringResource(R.string.map_outside_tunisia_title)
            isIntermediateStop -> stringResource(R.string.map_stop_location)
            isDestination -> stringResource(R.string.map_confirm_destination)
            else -> stringResource(R.string.map_pickup_location)
        }
    val bodyText =
        when {
            isOutOfBounds -> stringResource(R.string.map_outside_tunisia_hint)
            isLoading -> stringResource(R.string.map_getting_address)
            isEmpty -> stringResource(R.string.map_move_to_set)
            else -> address
        }

    val bubbleShape = RoundedCornerShape(BubbleCorner)
    Surface(
        modifier =
            Modifier
                .wrapContentSize()
                .shadow(8.dp, bubbleShape, clip = false)
                .clip(bubbleShape)
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onConfirm,
                ),
        shape = bubbleShape,
        color = bubbleBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.m, vertical = AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.6.dp,
                    color = colors.textPrimary,
                )
            }
            Column {
                Text(
                    text = labelText,
                    style =
                        AppTypography.labelS.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = labelTextColor,
                        ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bodyText,
                    style =
                        AppTypography.bodyM.copy(
                            fontWeight = FontWeight.Medium,
                            color = bodyTextColor,
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 250.dp),
                )
            }
            Icon(
                painter = painterResource(AppIcon.chevronRight),
                contentDescription = null,
                tint = if (isEmpty && !isLoading) colors.textSecondary.copy(alpha = 0.3f) else arrowTint,
                modifier = Modifier.size(16.dp),
            )
        }
    }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.size(width = 12.dp, height = 6.dp)) {
            val path =
                Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
            drawPath(path, bubbleBg)
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
        animationSpec =
            spring(
                dampingRatio = 0.42f,
                stiffness = Spring.StiffnessLow,
            ),
        label = "innerDot",
    )

    val stemWidth = maxOf(4.dp, diameter * 0.072f)
    val pulseTargetScale =
        remember(innerSize, diameter) {
            if (innerSize.value <= 0f) 1f else diameter.value / innerSize.value
        }

    Box(
        modifier = modifier.wrapContentSize(align = Alignment.TopCenter),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(diameter)
                        .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.fillMaxSize().background(color, CircleShape))
                Box(
                    modifier =
                        Modifier
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
                modifier =
                    Modifier
                        .width(stemWidth)
                        .height(pointerHeight)
                        .offset(y = (-1).dp)
                        .background(color),
            )
        }
    }
}

/** Même animation de « ping » que sur l’épingle Bolt (pickup / destination en mode carte). */
@Composable
internal fun PinPulseRing(
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
        modifier =
            modifier
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
    isMoving: Boolean,
    isOutOfBounds: Boolean,
    isDestination: Boolean,
    isIntermediateStop: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val disabled = isEmpty || isLoading || isMoving || isOutOfBounds
    val activeGradient = !disabled && !isOutOfBounds && !isEmpty

    val label =
        when {
            isOutOfBounds -> stringResource(R.string.map_outside_tunisia)
            isIntermediateStop -> stringResource(R.string.map_confirm_stop_point)
            isDestination -> stringResource(R.string.map_confirm_destination)
            else -> stringResource(R.string.map_confirm_pickup)
        }

    val bgColor =
        when {
            isOutOfBounds -> colors.error.copy(alpha = 0.12f)
            isEmpty -> pinColor.copy(alpha = 0.4f)
            else -> pinColor
        }

    val fgColor =
        when {
            isOutOfBounds -> colors.error
            disabled ->
                when {
                    isIntermediateStop -> MapColorTokens.pinIntermediate.copy(alpha = 0.55f)
                    else -> Color.White.copy(alpha = 0.55f)
                }
            isIntermediateStop -> MapColorTokens.pinIntermediate
            else -> Color.White
        }

    val shape = RoundedCornerShape(AppSpacing.buttonRadius)
    Box(
        modifier =
            Modifier
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(alpha = 0.16f),
                                            Color.Transparent,
                                        ),
                                ),
                        ),
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            ) {
                if (isOutOfBounds) {
                    Icon(
                        painter = painterResource(AppIcon.alertTriangle),
                        contentDescription = null,
                        tint = fgColor,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = label,
                    style = AppTypography.headingS.copy(fontSize = 16.sp),
                    color = fgColor,
                )
            }
        }
    }
}

@Composable
private fun GroundShadow(isMoving: Boolean) {
    val shadowW by animateDpAsState(
        targetValue = if (isMoving) 10.dp else 24.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "groundShadowW",
    )
    val shadowH by animateDpAsState(
        targetValue = if (isMoving) 3.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "groundShadowH",
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (isMoving) 0.35f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "groundShadowAlpha",
    )
    Box(
        modifier =
            Modifier
                .offset(y = 2.dp)
                .size(width = shadowW, height = shadowH)
                .alpha(shadowAlpha),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                brush =
                    Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = max(size.width, size.height) / 2f,
                    ),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
            )
        }
    }
}
