package com.dadadrive.ui.map

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils

/**
 * Bitmap du marqueur de position type « pickup » (goutte verte 3D + ombre au sol),
 * équivalent Android du `TeardropPin` Swift dans PickupPinOverlay.
 *
 * L’ancrage HERE doit être [Anchor2D](horizontal = 0.5, vertical = tipNormalizedY)
 * où [tipNormalizedY] est fourni par [teardropPickupPinAnchorYNormalized].
 */
fun createTeardropPickupLocationBitmap(primaryArgb: Int): Bitmap {
    val scale = 3f
    val pinWidth = 40f * scale
    val pinHeight = 56f * scale

    val horizontalPad = 28f * scale
    val topPad = 20f * scale
    val bottomPad = 22f * scale

    val bitmapW = (pinWidth + horizontalPad * 2).toInt().coerceAtLeast(1)
    val bitmapH = (topPad + pinHeight + bottomPad).toInt().coerceAtLeast(1)
    val pinLeft = (bitmapW - pinWidth) / 2f

    val base = primaryArgb or 0xFF000000.toInt()
    val greenLight = ColorUtils.blendARGB(base, 0xFFFFFFFF.toInt(), 0.18f)
    val greenDark = ColorUtils.blendARGB(base, 0xFF000000.toInt(), 0.28f)

    val bitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val tipX = bitmapW / 2f
    val tipY = topPad + pinHeight

    // ── Ombre au sol (RadialGradient comme Swift) ───────────────────────────
    val shadowW = 26f * scale
    val shadowH = 7f * scale
    val shadowCx = tipX
    val shadowCy = tipY + 3f * scale
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(
            shadowCx, shadowCy,
            13f * scale,
            intArrayOf(0x38000000.toInt(), 0x00000000.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawOval(
        RectF(
            shadowCx - shadowW / 2f,
            shadowCy - shadowH / 2f,
            shadowCx + shadowW / 2f,
            shadowCy + shadowH / 2f
        ),
        shadowPaint
    )

    canvas.save()
    canvas.translate(pinLeft, topPad)

    // Goutte « classique » (demi-cercle + pointe) — rendu fiable sur Canvas Android
    val path = mapPinTeardropPath(pinWidth, pinHeight)
    val rHead = pinWidth / 2f

    // ── Ombre portée du pin (flou + léger décalage) ─────────────────────────
    val dropShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x24000000.toInt()
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(2.5f * scale, BlurMaskFilter.Blur.NORMAL)
    }
    canvas.save()
    canvas.translate(1.5f * scale, 3f * scale)
    canvas.drawPath(path, dropShadow)
    canvas.restore()
    dropShadow.maskFilter = null

    // ── Corps : dégradé vertical (clair → vert → foncé) ─────────────────────
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            pinWidth * 0.25f, pinHeight * 0.05f,
            pinWidth * 0.75f, pinHeight,
            intArrayOf(greenLight, base, greenDark),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
    }
    canvas.drawPath(path, bodyPaint)

    // ── Reflet (blanc → transparent), clip sur la goutte ─────────────────────
    canvas.save()
    canvas.clipPath(path)
    val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            pinWidth * 0.1f, pinHeight * 0.1f,
            pinWidth * 0.55f, pinHeight * 0.6f,
            intArrayOf(0x47FFFFFF.toInt(), 0x00FFFFFF.toInt()),
            null,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, pinWidth, pinHeight, glossPaint)
    canvas.restore()

    // ── Petit highlight elliptique (haut-gauche) ────────────────────────────
    val specPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x85FFFFFF.toInt()
        maskFilter = BlurMaskFilter(1.5f * scale, BlurMaskFilter.Blur.NORMAL)
    }
    val specCx = pinWidth / 2f - 7f * scale
    val specCy = rHead * 0.35f
    canvas.drawOval(
        RectF(specCx - 4.5f * scale, specCy - 3f * scale, specCx + 4.5f * scale, specCy + 3f * scale),
        specPaint
    )
    specPaint.maskFilter = null

    // ── Cercle blanc intérieur (RadialGradient) ─────────────────────────────
    val innerCx = pinWidth / 2f
    val innerCy = rHead * 0.92f
    val innerR = 7f * scale
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = RadialGradient(
            innerCx, innerCy, innerR * 1.2f,
            intArrayOf(0xFFFFFFFF.toInt(), 0xE6FFFFFF.toInt()),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        setShadowLayer(2f * scale, 0f, 1f * scale, 0x26000000.toInt())
    }
    canvas.drawCircle(innerCx, innerCy, innerR, innerPaint)

    canvas.restore()

    return bitmap
}

/** Dimensions du bitmap + position Y de la pointe (pour overlay centré à l’écran). */
data class TeardropPinLayout(val bitmapWidth: Int, val bitmapHeight: Int, val tipYFromTop: Float)

fun teardropPickupPinLayout(): TeardropPinLayout {
    val scale = 3f
    val pinWidth = 40f * scale
    val pinHeight = 56f * scale
    val horizontalPad = 28f * scale
    val topPad = 20f * scale
    val bottomPad = 22f * scale
    val bitmapW = (pinWidth + horizontalPad * 2).toInt().coerceAtLeast(1)
    val bitmapH = (topPad + pinHeight + bottomPad).toInt().coerceAtLeast(1)
    val tipY = topPad + pinHeight
    return TeardropPinLayout(bitmapW, bitmapH, tipY)
}

/** Normalisé vertical (0..1) du point d’ancrage sur la pointe du pin pour [Anchor2D]. */
fun teardropPickupPinAnchorYNormalized(): Double {
    val scale = 3f
    val pinHeight = 56f * scale
    val topPad = 20f * scale
    val bottomPad = 22f * scale
    val bitmapH = (topPad + pinHeight + bottomPad).toInt().coerceAtLeast(1)
    val tipY = topPad + pinHeight
    return (tipY / bitmapH).toDouble().coerceIn(0.0, 1.0)
}

/**
 * Pin carte : demi-cercle inférieur de la tête + pointe — proche des pins classiques / Swift.
 */
private fun mapPinTeardropPath(w: Float, h: Float): Path {
    val cx = w / 2f
    val r = w / 2f
    val oval = RectF(cx - r, 0f, cx + r, 2f * r)
    val path = Path()
    path.moveTo(cx - r, r)
    path.arcTo(oval, 180f, 180f, false)
    path.lineTo(cx, h)
    path.close()
    return path
}
