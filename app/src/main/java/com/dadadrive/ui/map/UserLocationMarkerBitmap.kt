package com.dadadrive.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.max

/**
 * Marqueur de position réelle : photo de profil (ou initiales) dans un disque,
 * anneau blanc, bordure verte type header + petit point de statut en bas à droite.
 * Ancrage HERE recommandé : [Anchor2D](0.5, 0.5) (centre = coordonnées GPS).
 */
fun createUserLocationMarkerBitmap(
    avatar: Bitmap?,
    initials: String,
    primaryArgb: Int,
    placeholderBgArgb: Int
): Bitmap {
    val scale = 3f
    val totalPx = (56 * scale).toInt().coerceAtLeast(1)
    val cx = totalPx / 2f
    val cy = totalPx / 2f

    val green = primaryArgb or 0xFF000000.toInt()
    val greenOuterR = totalPx / 2f - 2f * scale
    val whiteOuterR = greenOuterR - 4f * scale
    val innerR = whiteOuterR - 4f * scale

    val bitmap = Bitmap.createBitmap(totalPx, totalPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val aa = Paint(Paint.ANTI_ALIAS_FLAG)

    // Anneau vert
    aa.color = green
    aa.style = Paint.Style.FILL
    canvas.drawCircle(cx, cy, greenOuterR, aa)

    // Anneau blanc
    aa.color = 0xFFFFFFFF.toInt()
    canvas.drawCircle(cx, cy, whiteOuterR, aa)

    // Fond intérieur (placeholder)
    aa.color = placeholderBgArgb or 0xFF000000.toInt()
    canvas.drawCircle(cx, cy, innerR, aa)

    if (avatar != null && !avatar.isRecycled) {
        val shader = centerCropShader(avatar, innerR, cx, cy)
        aa.shader = shader
        aa.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy, innerR, aa)
        aa.shader = null
    } else {
        val label = initials.trim().take(2).ifEmpty { "?" }.uppercase()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = innerR * 0.72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val fm = textPaint.fontMetrics
        val textY = cy - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label, cx, textY, textPaint)
    }

    // Point de statut (bas-droite, comme le header)
    val dotR = 5.5f * scale
    val dotCx = cx + greenOuterR * 0.58f
    val dotCy = cy + greenOuterR * 0.58f
    aa.color = green
    aa.style = Paint.Style.FILL
    canvas.drawCircle(dotCx, dotCy, dotR, aa)
    aa.color = 0xFFFFFFFF.toInt()
    aa.style = Paint.Style.STROKE
    aa.strokeWidth = 2f * scale
    canvas.drawCircle(dotCx, dotCy, dotR, aa)

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
