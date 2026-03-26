package com.dadadrive.core.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────
// MODIFIER EXTENSIONS
// ─────────────────────────────────────────────────────────

/**
 * Clickable modifier that suppresses the ripple effect.
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}

/**
 * Conditionally applies [modifier] when [condition] is true.
 */
fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier =
    if (condition) this.then(modifier(Modifier)) else this

// ─────────────────────────────────────────────────────────
// COLOR EXTENSIONS
// ─────────────────────────────────────────────────────────

/**
 * Returns a copy of this [Color] with the given alpha component,
 * expressed as a float in [0f, 1f].
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

// ─────────────────────────────────────────────────────────
// STRING EXTENSIONS
// ─────────────────────────────────────────────────────────

/** Returns true if the string contains only digit characters. */
fun String.isDigitsOnly(): Boolean = isNotEmpty() && all { it.isDigit() }

/** Returns null if the string is blank, otherwise returns the string itself. */
fun String.nullIfBlank(): String? = if (isBlank()) null else this
