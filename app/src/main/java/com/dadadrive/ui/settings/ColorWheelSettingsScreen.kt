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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dadadrive.ui.theme.AppColorScheme
import com.dadadrive.ui.theme.AppTheme
import com.dadadrive.ui.theme.LocalAppColors
import com.dadadrive.ui.theme.ThemeViewModel
import com.dadadrive.ui.theme.allColorEntries

@Composable
fun ColorWheelSettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val colors      = LocalAppColors.current
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val systemDark  = isSystemInDarkTheme()
    val previewScheme = currentTheme.resolveScheme(systemDark)

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
                            contentDescription = "Retour",
                            tint = colors.onBackground
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Thème de couleurs",
                            color = colors.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personnalisez l'apparence de l'app",
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
                    text = "THÈMES DISPONIBLES",
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
                Text(
                    text = "TOUTES LES COULEURS — ${previewScheme.name.uppercase()}",
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
                    Text("DADA DRIVE", color = scheme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
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
                    Text("Continuer", color = scheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(scheme.darkInput)
                        .border(1.dp, scheme.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Annuler", color = scheme.textSecondary, fontSize = 10.sp)
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
                    contentDescription = "Sélectionné",
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
