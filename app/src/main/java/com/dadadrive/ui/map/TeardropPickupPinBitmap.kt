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