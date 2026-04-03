package com.dadadrive.ui.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dadadrive.R
import com.dadadrive.ui.theme.LocalAppColors

/**
 * Barre du haut type app classique : logo [R.drawable.ic_dadadrive_logo] + « DADA DRIVE », puis recherche,
 * notifications et avatar (badge rouge « hors ligne » optionnel, ex. conducteur offline).
 */
@Composable
internal fun MapHomeTopHeader(
    avatarUrl: String?,
    profileInitials: String,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    showOfflineStatusBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val dark = isSystemInDarkTheme()
    val headerInk = c.textPrimary
    val logoDiameter = 30.dp
    val logoRing = if (dark) headerInk else Color.Black
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = c.surface,
        shadowElevation = if (dark) 0.dp else 2.dp,
        tonalElevation = if (dark) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(logoDiameter + 4.dp)
                        .border(width = 2.dp, color = logoRing, shape = CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_dadadrive_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(logoDiameter)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "DADA DRIVE",
                    color = headerInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = headerInk,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = headerInk,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = 0.dp, end = 2.dp)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(2.dp, c.primary, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onProfileClick)
                            .background(c.darkInput),
                        contentAlignment = Alignment.Center
                    ) {
                        val initialsColor =
                            if (c.darkInput.luminance() > 0.45f) c.textPrimary else Color.White
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                profileInitials,
                                color = initialsColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (showOfflineStatusBadge) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(16.dp)
                                .background(Color(0xFFE53935), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Top pill behind logo + title (Swift translucent bar). */
@Composable
internal fun mapTopOverlayPillColor(): Color {
    val dark = isSystemInDarkTheme()
    return if (dark) Color(0xE61A1A1A) else Color(0xF2FFFFFF)
}

@Composable
internal fun MapProfileAvatarCluster(
    avatarUrl: String?,
    profileInitials: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "map_avatar_pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "p1"
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "p1a"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing, delayMillis = 800),
            RepeatMode.Restart
        ),
        label = "p2"
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = LinearEasing, delayMillis = 800),
            RepeatMode.Restart
        ),
        label = "p2a"
    )

    val dotSize = 46.dp
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dotSize + 8.dp)
                .scale(pulse2)
                .background(Color.Transparent, CircleShape)
                .border(2.dp, c.primary.copy(alpha = pulse2Alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(dotSize + 8.dp)
                .scale(pulse1)
                .background(Color.Transparent, CircleShape)
                .border(2.dp, c.primary.copy(alpha = pulse1Alpha), CircleShape)
        )
        Box(modifier = Modifier.size(dotSize + 4.dp).background(c.primary, CircleShape))
        Box(modifier = Modifier.size(dotSize + 2.dp).background(Color.White, CircleShape))
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(c.darkInput)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(profileInitials, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp)
                .size(13.dp)
                .background(c.primary, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
internal fun MapBrandPill(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = mapTopOverlayPillColor(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(c.primary, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "D",
                    color = c.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "DadaDrive",
                color = c.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
internal fun MapNotificationButton(modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.size(52.dp),
        shape = CircleShape,
        color = mapTopOverlayPillColor(),
        shadowElevation = 4.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** Panneau type Swift [MapTypePickerPanel] : Default / Satellite / Hybrid. */
@Composable
internal fun MapTypePickerPanel(
    selected: AppMapDisplayMode,
    onSelect: (AppMapDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.width(178.dp),
        shape = RoundedCornerShape(12.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, c.primary.copy(alpha = 0.18f))
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppMapDisplayMode.entries.forEach { mode ->
                val picked = mode == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (picked) c.primary.copy(alpha = 0.10f) else Color.Transparent)
                        .clickable { onSelect(mode) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mode.pickerIcon(),
                        contentDescription = null,
                        tint = if (picked) c.primary else c.textHint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(mode.labelRes),
                        color = if (picked) c.textPrimary else c.textHint,
                        fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (picked) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private val AppMapDisplayMode.labelRes: Int
    get() = when (this) {
        AppMapDisplayMode.NORMAL -> R.string.map_mode_default
        AppMapDisplayMode.SATELLITE -> R.string.map_mode_satellite
        AppMapDisplayMode.HYBRID -> R.string.map_mode_hybrid
    }

private fun AppMapDisplayMode.pickerIcon(): ImageVector = when (this) {
    AppMapDisplayMode.NORMAL -> Icons.Outlined.Map
    AppMapDisplayMode.SATELLITE -> Icons.Outlined.Public
    AppMapDisplayMode.HYBRID -> Icons.Outlined.Layers
}

/**
 * Rangée Swift [MapControlsOverlay] : panneau de type (optionnel) + boutons carte / recentrage.
 */
@Composable
internal fun MapFloatingControlsRow(
    showMapTypePicker: Boolean,
    mapDisplayMode: AppMapDisplayMode,
    onMapDisplayModeChange: (AppMapDisplayMode) -> Unit,
    onToggleMapTypePicker: () -> Unit,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    anchorUnderStatusBar: Boolean = false,
    /** Conducteur : aligner en haut à droite ; passager (bas) : [Alignment.Bottom]. */
    rowVerticalAlignment: Alignment.Vertical = Alignment.Bottom
) {
    val rowMod = if (anchorUnderStatusBar) {
        modifier.statusBarsPadding().padding(top = 76.dp, end = 16.dp)
    } else {
        modifier
    }
    Row(
        modifier = rowMod.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = rowVerticalAlignment
    ) {
        AnimatedVisibility(
            visible = showMapTypePicker,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            MapTypePickerPanel(
                selected = mapDisplayMode,
                onSelect = {
                    onMapDisplayModeChange(it)
                    onToggleMapTypePicker()
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        MapSideFloatingControls(
            onLayersClick = onToggleMapTypePicker,
            onRecenterClick = onRecenterClick,
            mapTypePanelOpen = showMapTypePicker,
            anchorUnderStatusBar = false,
            compact = compact,
            modifier = Modifier
        )
    }
}

@Composable
internal fun MapSideFloatingControls(
    onLayersClick: () -> Unit,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** When true, offset below status bar + header (overlay on map). When false, place in bottom stack (e.g. above search bar). */
    anchorUnderStatusBar: Boolean = true,
    /** Smaller circles + icons (passenger map above search bar, or tighter driver HUD). */
    compact: Boolean = false,
    /** Panneau « type de carte » ouvert (bouton carte en surbrillance verte, comme Swift). */
    mapTypePanelOpen: Boolean = false
) {
    val c = LocalAppColors.current
    val fabSize = if (compact) 40.dp else 52.dp
    val mapIcon = if (compact) 18.dp else 20.dp
    val locIcon = if (compact) 20.dp else 26.dp
    val gap = if (compact) 8.dp else 12.dp
    val elevation = if (compact) 3.dp else 4.dp
    val columnMod = if (anchorUnderStatusBar) {
        modifier.statusBarsPadding().padding(top = 76.dp, end = 16.dp)
    } else {
        modifier
    }
    val typeFabColor = if (mapTypePanelOpen) c.primary else mapTopOverlayPillColor()
    val typeIconTint = if (mapTypePanelOpen) c.onPrimary else c.textPrimary
    Column(
        modifier = columnMod,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Surface(
            modifier = Modifier
                .size(fabSize)
                .clickable(onClick = onLayersClick),
            shape = CircleShape,
            color = typeFabColor,
            shadowElevation = elevation
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Map,
                    contentDescription = "Map type",
                    tint = typeIconTint,
                    modifier = Modifier.size(mapIcon)
                )
            }
        }
        Surface(
            modifier = Modifier
                .size(fabSize)
                .clickable(onClick = onRecenterClick),
            shape = CircleShape,
            color = mapTopOverlayPillColor(),
            shadowElevation = elevation
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(locIcon)
                )
            }
        }
    }
}
