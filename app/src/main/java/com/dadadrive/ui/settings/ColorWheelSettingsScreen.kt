package com.dadadrive.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.ui.theme.AppColorScheme
import com.dadadrive.ui.theme.AppTheme
import com.dadadrive.ui.theme.LocalAppColors
import com.dadadrive.ui.theme.ThemeViewModel
import com.dadadrive.ui.theme.allColorEntries
import com.dadadrive.R

@Composable
fun ColorWheelSettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val colors      = LocalAppColors.current
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val customSecondaryArgb by themeViewModel.customSecondaryArgb.collectAsState()
    val systemDark  = isSystemInDarkTheme()
    val previewScheme = currentTheme.resolveScheme(
        systemDark,
        customSecondaryArgb?.let { Color(it) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = colors.onBackground
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.settings_color_title),
                            color = colors.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_color_subtitle),
                            color = colors.textHint,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                ThemePreviewCard(
                    scheme = previewScheme,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.settings_themes_available),
                    color = colors.textHint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 10.dp)
                )
            }

            items(AppTheme.entries) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = theme == currentTheme,
                    onSelect = { themeViewModel.setTheme(theme) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                SecondaryColorSection(
                    themeViewModel = themeViewModel,
                    currentTheme = currentTheme,
                    systemDark = systemDark,
                    customSecondaryArgb = customSecondaryArgb,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)
                )
            }

            item {
                Text(
                    text = stringResource(R.string.settings_all_colors_title, previewScheme.name.uppercase()),
                    color = colors.textHint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(
                items = previewScheme.allColorEntries(),
                key = { it.first }
            ) { (tokenName, color) ->
                ColorTokenRow(
                    label = tokenName,
                    color = color,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ── Aperçu du thème actif ────────────────────────────────────────────────────

@Composable
private fun ThemePreviewCard(
    scheme: AppColorScheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surface)
            .border(1.dp, scheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(scheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("D", color = scheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.brand_dada_drive), color = scheme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                    Text(scheme.name, color = scheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.settings_preview_continue), color = scheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.settings_preview_secondary), color = scheme.onSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Carte d'un thème ─────────────────────────────────────────────────────────

@Composable
private fun ThemeCard(
    theme: AppTheme,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val scheme = theme.resolveScheme(isSystemInDarkTheme())

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary.copy(alpha = 0.08f) else colors.surface,
        animationSpec = tween(durationMillis = 250),
        label = "bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) borderColor else colors.dividerGrey,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Swatch principal
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(scheme.primary, CircleShape)
                .border(3.dp, scheme.primary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Nom + swatches
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.displayName,
                color = if (isSelected) scheme.primary else colors.onBackground,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            ColorSwatchRow(scheme = scheme)
        }

        Spacer(Modifier.width(8.dp))

        // Indicateur sélection
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(scheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.5.dp, colors.textHint, CircleShape)
            )
        }
    }
}

// ── Rangée de pastilles de couleurs ─────────────────────────────────────────

@Composable
private fun ColorSwatchRow(scheme: AppColorScheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        ColorSwatch(color = scheme.primary)
        ColorSwatch(color = scheme.primaryDisabled)
        ColorSwatch(color = scheme.surface)
        ColorSwatch(color = scheme.darkInput)
        ColorSwatch(color = scheme.divider)
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.12f), CircleShape)
    )
}

@Composable
private fun ColorTokenRow(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val fg = LocalAppColors.current.textSecondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LocalAppColors.current.surfaceElevated.copy(alpha = 0.5f))
            .border(1.dp, LocalAppColors.current.dividerGrey.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
                .border(1.dp, Color.Black.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class SecondaryPreset(val argb: Int?, val labelRes: Int)

private fun argbRgb(r: Int, g: Int, b: Int): Int =
    (0xFF shl 24) or (r shl 16) or (g shl 8) or b

@Composable
private fun SecondaryColorSection(
    themeViewModel: ThemeViewModel,
    currentTheme: AppTheme,
    systemDark: Boolean,
    customSecondaryArgb: Int?,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val defaultSecondary = currentTheme.resolveScheme(systemDark, null).secondary
    val presets = remember {
        listOf(
            SecondaryPreset(null, R.string.secondary_preset_auto),
            SecondaryPreset(argbRgb(96, 125, 139), R.string.secondary_preset_slate),
            SecondaryPreset(argbRgb(84, 110, 122), R.string.secondary_preset_blue_grey),
            SecondaryPreset(argbRgb(0, 137, 123), R.string.secondary_preset_teal),
            SecondaryPreset(argbRgb(92, 107, 192), R.string.secondary_preset_indigo),
            SecondaryPreset(argbRgb(255, 112, 67), R.string.secondary_preset_coral),
            SecondaryPreset(argbRgb(142, 36, 170), R.string.secondary_preset_plum),
            SecondaryPreset(argbRgb(255, 193, 7), R.string.secondary_preset_amber),
            SecondaryPreset(argbRgb(0, 191, 165), R.string.secondary_preset_mint)
        )
    }
    Column(modifier) {
        Text(
            text = stringResource(R.string.settings_secondary_title).uppercase(),
            color = c.textHint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = stringResource(R.string.settings_secondary_subtitle),
            color = c.textSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        presets.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { preset ->
                    SecondaryPresetChip(
                        label = stringResource(preset.labelRes),
                        swatchColor = if (preset.argb == null) defaultSecondary else Color(preset.argb),
                        selected = if (preset.argb == null) {
                            customSecondaryArgb == null
                        } else {
                            preset.argb == customSecondaryArgb
                        },
                        onClick = {
                            if (preset.argb == null) themeViewModel.clearCustomSecondary()
                            else themeViewModel.setCustomSecondaryArgb(preset.argb)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        TextButton(
            onClick = { themeViewModel.clearCustomSecondary() },
            enabled = customSecondaryArgb != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.settings_secondary_reset), color = c.primary)
        }
    }
}

@Composable
private fun SecondaryPresetChip(
    label: String,
    swatchColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatchColor)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) c.primary else Color.Black.copy(alpha = 0.18f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (swatchColor.luminance() > 0.55f) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = c.textSecondary,
            fontSize = 10.sp,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}
