package com.dadadrive.ui.theme

import androidx.compose.ui.graphics.Color

val FacebookBlue = Color(0xFF1877F2)
val GoogleRed = Color(0xFFEA4335)

fun Color.lighter(by: Float): Color = lerpColor(this, Color.White, by.coerceIn(0f, 1f))
fun Color.darker(by: Float): Color = lerpColor(this, Color.Black, by.coerceIn(0f, 1f))

private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val inv = 1f - fraction
    return Color(
        red = start.red * inv + end.red * fraction,
        green = start.green * inv + end.green * fraction,
        blue = start.blue * inv + end.blue * fraction,
        alpha = start.alpha * inv + end.alpha * fraction
    )
}
