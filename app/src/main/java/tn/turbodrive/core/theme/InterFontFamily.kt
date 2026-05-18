package tn.turbodrive.core.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.turbodrive.R

/**
 * Inter font family (R-4.2).
 *
 * Single variable font (`inter_variable.ttf`) shipped in `res/font/`. The 4 UI
 * weights (400/500/600/700) are resolved at runtime via the `wght` variation
 * axis. This is preferred over 4 static TTFs because :
 * - APK size : 856 KB vs ~1.2 MB total for 4 statics
 * - Single source of truth for letter-shape consistency across weights
 * - Compose 1.5+ resolves [FontWeight] -> [FontVariation.weight] natively
 *
 * Replaces `FontFamily.Default` in [AppTypography] for all text styles
 * (mono* styles keep `FontFamily.Monospace` for OTP / plate number display).
 */
@OptIn(ExperimentalTextApi::class)
val InterFontFamily =
    FontFamily(
        Font(
            resId = R.font.inter_variable,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(WEIGHT_REGULAR)),
        ),
        Font(
            resId = R.font.inter_variable,
            weight = FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(WEIGHT_MEDIUM)),
        ),
        Font(
            resId = R.font.inter_variable,
            weight = FontWeight.SemiBold,
            variationSettings = FontVariation.Settings(FontVariation.weight(WEIGHT_SEMIBOLD)),
        ),
        Font(
            resId = R.font.inter_variable,
            weight = FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(WEIGHT_BOLD)),
        ),
    )

private const val WEIGHT_REGULAR = 400
private const val WEIGHT_MEDIUM = 500
private const val WEIGHT_SEMIBOLD = 600
private const val WEIGHT_BOLD = 700
