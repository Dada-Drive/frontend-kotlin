package com.dadadrive.ui.map

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils

// ═══════════════════════════════════════════════════════════════════════
// Layout descriptor — used by CenterPickupPinOverlay in MapScreen
// ═══════════════════════════════════════════════════════════════════════

data class TeardropPinLayout(
    val bitmapWidth:  Float,
    val bitmapHeight: Float,
    val tipYFromTop:  Float
)

fun teardropPickupPinLayout(): TeardropPinLayout = TeardropPinLayout(
    bitmapWidth  = 160f,
    bitmapHeight = 224f,
    tipYFromTop  = 224f
)

fun teardropPickupPinAnchorYNormalized(): Double = 1.0

/** Same teardrop outline as Swift / iOS pickup pin (160×224). */
internal fun buildTeardropPinPath(pinW: Float, pinH: Float): Path {
    val cx = pinW / 2f
    val r = pinW / 2f
    val headCY = r
    val tipY = pinH

    val d = tipY - headCY
    val sinTheta = (r / d).coerceAtMost(0.9999f)
    val theta = Math.asin(sinTheta.toDouble()).toFloat()
    val cosTheta = Math.cos(theta.toDouble()).toFloat()
    val tx = cx - r * cosTheta
    val ty = headCY + r * sinTheta

    return Path().apply {
        moveTo(tx, ty)
        val oval = RectF(cx - r, headCY - r, cx + r, headCY + r)
        arcTo(
            oval,
            (90f + Math.toDegrees(theta.toDouble())).toFloat(),
            -(360f - 2f * Math.toDegrees(theta.toDouble()).toFloat()),
            false
        )
        lineTo(cx, tipY)
        lineTo(tx, ty)
        close()
    }
}

// ═══════════════════════════════════════════════════════════════════════
// Pickup-location teardrop pin
// ═══════════════════════════════════════════════════════════════════════

fun createTeardropPickupLocationBitmap(primaryArgb: Int): Bitmap {
    val pinW = 160f
    val pinH = 224f

    val bitmap = Bitmap.createBitmap(pinW.toInt(), pinH.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val green = primaryArgb or 0xFF000000.toInt()
    val lightGreen = ColorUtils.blendARGB(green, 0xFFFFFFFF.toInt(), 0.18f)
    val darkGreen = ColorUtils.blendARGB(green, 0xFF000000.toInt(), 0.28f)

    val path = buildTeardropPinPath(pinW, pinH)
    val cx = pinW / 2f
    val r = pinW / 2f
    val headCY = r

    val aa = Paint(Paint.ANTI_ALIAS_FLAG)

    // Drop shadow
    canvas.save()
    aa.color = 0x24000000.toInt()
    aa.style = Paint.Style.FILL
    aa.maskFilter = BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL)
    canvas.translate(1.5f, 3f)
    canvas.drawPath(path, aa)
    canvas.restore()
    aa.maskFilter = null

    // Base gradient
    canvas.save()
    canvas.clipPath(path)
    aa.shader = LinearGradient(
        pinW * 0.25f, pinH * 0.05f,
        pinW * 0.75f, pinH,
        intArrayOf(lightGreen, green, darkGreen),
        floatArrayOf(0f, 0.45f, 1f),
        Shader.TileMode.CLAMP
    )
    aa.style = Paint.Style.FILL
    canvas.drawPath(path, aa)
    aa.shader = null
    canvas.restore()

    // Gloss
    canvas.save()
    canvas.clipPath(path)
    aa.shader = LinearGradient(
        pinW * 0.10f, pinH * 0.10f,
        pinW * 0.55f, pinH * 0.60f,
        intArrayOf(0x47FFFFFF.toInt(), 0x00FFFFFF.toInt()),
        null,
        Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, pinW, pinH, aa)
    aa.shader = null
    canvas.restore()

    // Specular highlight
    aa.color = 0x85FFFFFF.toInt()
    aa.maskFilter = BlurMaskFilter(1.5f, BlurMaskFilter.Blur.NORMAL)
    canvas.drawOval(RectF(cx - 11.5f, headCY - 3f, cx - 2.5f, headCY + 3f), aa)
    aa.maskFilter = null

    // White center dot
    aa.color = 0xFFFFFFFF.toInt()
    aa.style = Paint.Style.FILL
    canvas.drawCircle(cx, headCY, r * 0.28f, aa)

    // Green inner dot
    aa.color = green
    canvas.drawCircle(cx, headCY, r * 0.16f, aa)

    return bitmap
}

/**
 * Swift-equivalent pushpin bitmap (ball + needle), with tip at image bottom.
 * Use with Anchor2D(0.5, 1.0) so HERE places the needle tip on coordinates.
 *
 * [PUSH_PIN_MAP_SCALE] augmente la taille à l’écran (bitmap plus grand = pin plus lisible sur la carte).
 */
private const val PUSH_PIN_MAP_SCALE = 2.1f

fun createPushPinBitmap(colorArgb: Int): Bitmap {
    val ballRadius = 14f * PUSH_PIN_MAP_SCALE
    val needleHeight = 22f * PUSH_PIN_MAP_SCALE
    val needleHalfW = 2f * PUSH_PIN_MAP_SCALE
    val needleJoinInset = 2f * PUSH_PIN_MAP_SCALE
    val tipWidth = 1f * PUSH_PIN_MAP_SCALE
    val width = ballRadius * 2f
    val height = (ballRadius * 2f) + needleHeight

    val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val aa = Paint(Paint.ANTI_ALIAS_FLAG)

    val centerX = width / 2f
    val centerY = ballRadius
    val ballDiameter = ballRadius * 2f

    // Needle under the ball (metallic gradient).
    val needleTop = (ballDiameter - needleJoinInset).coerceAtLeast(0f)
    val needleRect = RectF(centerX - needleHalfW, needleTop, centerX + needleHalfW, needleTop + needleHeight)
    val needlePath = Path().apply {
        moveTo(needleRect.left, needleRect.top)
        lineTo(needleRect.right, needleRect.top)
        lineTo(centerX + tipWidth / 2f, needleRect.bottom)
        lineTo(centerX - tipWidth / 2f, needleRect.bottom)
        close()
    }
    aa.shader = LinearGradient(
        needleRect.left,
        needleRect.centerY(),
        needleRect.right,
        needleRect.centerY(),
        intArrayOf(0xFF737373.toInt(), 0xFFC7C7C7.toInt(), 0xFF616161.toInt()),
        floatArrayOf(0f, 0.5f, 1f),
        Shader.TileMode.CLAMP
    )
    aa.style = Paint.Style.FILL
    canvas.drawPath(needlePath, aa)
    aa.shader = null

    // Ball base.
    aa.color = colorArgb or 0xFF000000.toInt()
    canvas.drawCircle(centerX, centerY, ballRadius, aa)

    // Ball gloss (radial).
    aa.shader = android.graphics.RadialGradient(
        centerX - ballRadius * 0.30f,
        centerY - ballRadius * 0.40f,
        ballRadius * 0.85f,
        intArrayOf(0xB8FFFFFF.toInt(), 0x2EFFFFFF, 0x00FFFFFF),
        floatArrayOf(0f, 0.55f, 1f),
        Shader.TileMode.CLAMP
    )
    canvas.drawCircle(centerX, centerY, ballRadius, aa)
    aa.shader = null

    // Specular spot.
    aa.color = 0x8CFFFFFF.toInt()
    aa.maskFilter = BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL)
    val spotR = ballRadius * 0.15f
    canvas.drawCircle(centerX - ballRadius * 0.28f, centerY + ballRadius * 0.20f, spotR, aa)
    aa.maskFilter = null

    return bitmap
}