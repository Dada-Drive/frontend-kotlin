package tn.turbodrive.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.components.AnimatedDadaCoinIcon
import tn.turbodrive.presentation.components.DadaCoinMetal

private const val ASSET_LOGO_MARK = "logo.png"

@Composable
internal fun TurboDriveLogoMark(
    modifier: Modifier = Modifier,
    tint: Color = Color.Black,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    AsyncImage(
        model =
            ImageRequest.Builder(context)
                .data("file:///android_asset/$ASSET_LOGO_MARK")
                .crossfade(false)
                .build(),
        contentDescription = stringResource(R.string.cd_turbodrive_logo),
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
    )
}

/**
 * Barre du haut type app classique : logo [R.drawable.ic_turbodrive_logo] + « DADA DRIVE », puis recherche,
 * notifications et avatar (badge rouge « hors ligne » optionnel, ex. conducteur offline).
 */
@Composable
internal fun MapHomeTopHeader(
    avatarUrl: String?,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    walletAmountText: String = "0",
    isWalletLoading: Boolean = false,
    showOfflineStatusBadge: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Box(
        modifier =
            modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 68.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = mapTopOverlayPillColor(),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        TurboDriveLogoMark(modifier = Modifier.size(26.dp))
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        color = c.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(62.dp)
                        .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(62.dp)
                            .border(2.5.dp, Color.Black, CircleShape)
                            .clip(CircleShape)
                            .background(c.darkInput),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.cd_profile),
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 3.dp, y = 3.dp)
                            .size(24.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.18f),
                                spotColor = Color.Black.copy(alpha = 0.18f),
                            )
                            .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                        repeat(3) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(width = 11.dp, height = 2.dp)
                                        .background(Color.Black, RoundedCornerShape(1.dp)),
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier =
                        Modifier
                            .size(62.dp)
                            .clickable(onClick = onWalletClick),
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier.size(58.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedDadaCoinIcon(
                                size = 58.dp,
                                metal = DadaCoinMetal.Silver,
                            )
                        }
                        if (isWalletLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(11.dp),
                                strokeWidth = 1.5.dp,
                                color = c.textPrimary,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "map_avatar_pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "p1",
    )
    val pulse1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "p1a",
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec =
            infiniteRepeatable(
                tween(2200, easing = LinearEasing, delayMillis = 800),
                RepeatMode.Restart,
            ),
        label = "p2",
    )
    val pulse2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                tween(2200, easing = LinearEasing, delayMillis = 800),
                RepeatMode.Restart,
            ),
        label = "p2a",
    )

    val dotSize = 46.dp
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(dotSize + 8.dp)
                    .scale(pulse2)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, Color.Black.copy(alpha = pulse2Alpha), CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .size(dotSize + 8.dp)
                    .scale(pulse1)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, Color.Black.copy(alpha = pulse1Alpha), CircleShape),
        )
        Box(modifier = Modifier.size(dotSize + 4.dp).background(Color.Black, CircleShape))
        Box(modifier = Modifier.size(dotSize + 2.dp).background(Color.White, CircleShape))
        Box(
            modifier =
                Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(c.darkInput)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 22.dp)
                    .size(13.dp)
                    .background(Color.Black, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
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
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                TurboDriveLogoMark(modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.app_name),
                color = c.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
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
        shadowElevation = 4.dp,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(AppIcon.bell),
                contentDescription = stringResource(R.string.cd_notifications),
                tint = c.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Panneau type Swift [MapTypePickerPanel] : Default / Satellite / Hybrid. */
@Composable
internal fun MapTypePickerPanel(
    selected: AppMapDisplayMode,
    onSelect: (AppMapDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Surface(
        modifier = modifier.width(178.dp),
        shape = RoundedCornerShape(12.dp),
        color = c.surfaceElevated,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, c.primary.copy(alpha = 0.18f)),
    ) {
        Column(
            Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppMapDisplayMode.entries.forEach { mode ->
                val picked = mode == selected
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (picked) c.primary.copy(alpha = 0.10f) else Color.Transparent)
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        // Justified: factory returns ImageVector — Layers has no AppIcon equivalent
                        imageVector = mode.pickerIcon(),
                        contentDescription = null,
                        tint = if (picked) c.primary else c.textSubtle,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(mode.labelRes),
                        color = if (picked) c.textPrimary else c.textSubtle,
                        fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    if (picked) {
                        Icon(
                            painter = painterResource(AppIcon.check),
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

private val AppMapDisplayMode.labelRes: Int
    get() =
        when (this) {
            AppMapDisplayMode.NORMAL -> R.string.map_mode_default
            AppMapDisplayMode.SATELLITE -> R.string.map_mode_satellite
            AppMapDisplayMode.HYBRID -> R.string.map_mode_hybrid
        }

private fun AppMapDisplayMode.pickerIcon(): ImageVector =
    when (this) {
        AppMapDisplayMode.NORMAL -> Icons.Outlined.Map // Justified: factory must return ImageVector to satisfy HYBRID (Layers)
        AppMapDisplayMode.SATELLITE -> Icons.Outlined.Public // Justified: factory must return ImageVector to satisfy HYBRID (Layers)
        AppMapDisplayMode.HYBRID -> Icons.Outlined.Layers // Justified: map layers control, no AppIcon equivalent
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
    recenterIconTint: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    anchorUnderStatusBar: Boolean = false,
    /** Conducteur : aligner en haut à droite ; passager (bas) : [Alignment.Bottom]. */
    rowVerticalAlignment: Alignment.Vertical = Alignment.Bottom,
) {
    val rowMod =
        if (anchorUnderStatusBar) {
            modifier.statusBarsPadding().padding(top = 76.dp, end = 16.dp)
        } else {
            modifier
        }
    Row(
        modifier = rowMod.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = rowVerticalAlignment,
    ) {
        AnimatedVisibility(
            visible = showMapTypePicker,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MapTypePickerPanel(
                selected = mapDisplayMode,
                onSelect = {
                    onMapDisplayModeChange(it)
                    onToggleMapTypePicker()
                },
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        MapSideFloatingControls(
            onLayersClick = onToggleMapTypePicker,
            onRecenterClick = onRecenterClick,
            recenterIconTint = recenterIconTint,
            mapTypePanelOpen = showMapTypePicker,
            anchorUnderStatusBar = false,
            compact = compact,
            modifier = Modifier,
        )
    }
}

@Composable
internal fun MapSideFloatingControls(
    onLayersClick: () -> Unit,
    onRecenterClick: () -> Unit,
    recenterIconTint: Color,
    modifier: Modifier = Modifier,
    /** When true, offset below status bar + header (overlay on map). When false, place in bottom stack (e.g. above search bar). */
    anchorUnderStatusBar: Boolean = true,
    /** Smaller circles + icons (passenger map above search bar, or tighter driver HUD). */
    compact: Boolean = false,
    /** Panneau « type de carte » ouvert (bouton carte en surbrillance verte, comme Swift). */
    mapTypePanelOpen: Boolean = false,
) {
    val c = LocalAppColors.current
    val fabSize = if (compact) 40.dp else 52.dp
    val mapIcon = if (compact) 18.dp else 20.dp
    val locIcon = if (compact) 20.dp else 26.dp
    val gap = if (compact) 8.dp else 12.dp
    val elevation = if (compact) 3.dp else 4.dp
    val columnMod =
        if (anchorUnderStatusBar) {
            modifier.statusBarsPadding().padding(top = 76.dp, end = 16.dp)
        } else {
            modifier
        }
    val typeFabColor = if (mapTypePanelOpen) c.primary else mapTopOverlayPillColor()
    val typeIconTint = if (mapTypePanelOpen) c.onPrimary else c.textPrimary
    Column(
        modifier = columnMod,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        Surface(
            modifier =
                Modifier
                    .size(fabSize)
                    .clickable(onClick = onLayersClick),
            shape = CircleShape,
            color = typeFabColor,
            shadowElevation = elevation,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(AppIcon.map),
                    contentDescription = stringResource(R.string.cd_map_type),
                    tint = typeIconTint,
                    modifier = Modifier.size(mapIcon),
                )
            }
        }
        Surface(
            modifier =
                Modifier
                    .size(fabSize)
                    .clickable(onClick = onRecenterClick),
            shape = CircleShape,
            color = mapTopOverlayPillColor(),
            shadowElevation = elevation,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(AppIcon.mapPin),
                    contentDescription = null,
                    tint = recenterIconTint,
                    modifier = Modifier.size(locIcon),
                )
            }
        }
    }
}
