package com.dadadrive.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.graphics.ColorUtils
import kotlin.math.max

/**
 * User location HERE marker: teardrop (Swift `createUserLocationMarkerImage`),
 * avatar or initials in head circle — tout le « ping » en tons verts (primaire).
 * Anchor: [com.here.sdk.core.Anchor2D](0.5, 1.0) so the tip sits on the GPS point.
 */
fun createUserLocationMarkerBitmap(
    avatar: Bitmap?,
    initials: String,
    primaryArgb: Int
): Bitmap {
    val pinW = 104f
    val pinH = pinW * 224f / 160f
    val sc = pinW / 160f

    val green = primaryArgb or 0xFF000000.toInt()
    val lightGreen = ColorUtils.blendARGB(green, 0xFFFFFFFF.toInt(), 0.18f)
    val darkGreen = ColorUtils.blendARGB(green, 0xFF000000.toInt(), 0.28f)
    /** Fond du disque central (initiales) : vert plus soutenu, cohérent avec la goutte. */
    val innerDiscGreen = ColorUtils.blendARGB(green, 0xFF000000.toInt(), 0.14f)

    val path = buildTeardropPinPath(pinW, pinH)

    val bitmap = Bitmap.createBitmap(pinW.toInt(), pinH.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val aa = Paint(Paint.ANTI_ALIAS_FLAG)

    val cx = pinW / 2f
    val r = pinW / 2f
    val headCY = r

    // Drop shadow
    canvas.save()
    aa.color = 0x24000000.toInt()
    aa.style = Paint.Style.FILL
    aa.maskFilter = BlurMaskFilter((2.5f * sc).coerceAtLeast(1.2f), BlurMaskFilter.Blur.NORMAL)
    canvas.translate(1.5f * sc, 3f * sc)
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
    aa.maskFilter = BlurMaskFilter((1.5f * sc).coerceAtLeast(1f), BlurMaskFilter.Blur.NORMAL)
    canvas.drawOval(
        RectF(cx - 11.5f * sc, headCY - 3f * sc, cx - 2.5f * sc, headCY + 3f * sc),
        aa
    )
    aa.maskFilter = null

    // Avatar: inset from head edge, green ring (primary), image/initials inside
    val avatarOuterR = r - 5f * sc
    val ringW = (2f * sc).coerceAtLeast(1.5f)
    val avatarInnerR = avatarOuterR - ringW

    val headClip = Path().apply { addCircle(cx, headCY, avatarOuterR, Path.Direction.CW) }
    canvas.save()
    canvas.clipPath(headClip)

    aa.shader = null
    aa.style = Paint.Style.FILL
    aa.color = innerDiscGreen
    canvas.drawCircle(cx, headCY, avatarInnerR, aa)

    if (avatar != null && !avatar.isRecycled) {
        val shader = centerCropShader(avatar, avatarInnerR, cx, headCY)
        aa.shader = shader
        aa.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, headCY, avatarInnerR, aa)
        aa.shader = null
    } else {
        val label = initials.trim().take(2).ifEmpty { "?" }.uppercase()
        val onGreen =
            if (ColorUtils.calculateLuminance(innerDiscGreen) > 0.55) 0xFF121212.toInt()
            else 0xFFFFFFFF.toInt()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onGreen
            textSize = avatarInnerR * 0.72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val fm = textPaint.fontMetrics
        val textY = headCY - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, cx, textY, textPaint)
    }

    aa.style = Paint.Style.STROKE
    aa.strokeWidth = ringW
    aa.color = green
    val ringCenterR = (avatarInnerR + avatarOuterR) / 2f
    canvas.drawCircle(cx, headCY, ringCenterR, aa)
    aa.style = Paint.Style.FILL
    canvas.restore()

    return bitmap
}

private fun centerCropShader(source: Bitmap, innerR: Float, cx: Float, cy: Float): BitmapShader {
    val diameter = innerR * 2f
    val sc = max(diameter / source.width, diameter / source.height)
    val matrix = Matrix()
    matrix.postScale(sc, sc)
    matrix.postTranslate(cx - source.width * sc / 2f, cy - source.height * sc / 2f)
    val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    shader.setLocalMatrix(matrix)
    return shader
}
