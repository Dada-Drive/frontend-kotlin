package com.dadadrive.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import kotlin.math.roundToInt
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.PI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.LocalActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.dadadrive.R
import com.here.sdk.core.Anchor2D
import com.here.sdk.core.Color as HereColor
import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.GeoPolyline
import com.here.sdk.core.Point2D
import com.here.sdk.mapview.LineCap
import com.here.sdk.mapview.MapCamera
import com.here.sdk.mapview.MapCameraListener
import com.here.sdk.mapview.MapFeatureModes
import com.here.sdk.mapview.MapFeatures
import com.here.sdk.mapview.MapImage
import com.here.sdk.mapview.MapImageFactory
import com.here.sdk.mapview.MapMarker as HereMapMarker
import com.here.sdk.mapview.MapMeasureDependentRenderSize
import com.here.sdk.mapview.MapPolyline as HereMapPolyline
import com.here.sdk.mapview.MapMeasure
import com.here.sdk.mapview.MapScheme
import com.here.sdk.mapview.MapView as HereMapView
import com.here.sdk.mapview.RenderSize
import com.dadadrive.core.language.AppLanguage
import com.dadadrive.core.pricing.RiderFareEstimate
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.PassengerRideOffer
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.ui.language.LanguageViewModel
import com.dadadrive.ui.language.localizedDisplayName
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.theme.LocalAppColors
import com.dadadrive.ui.wallet.WalletViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileBottomSheet(
    sheetState: SheetState,
    user: com.dadadrive.domain.model.User?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onColorSettings: () -> Unit = {},
    onLogout: () -> Unit
) {
    val c = LocalAppColors.current
    val activity = LocalActivity.current
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val selectedLang by languageViewModel.selected.collectAsState()
    var languagePickerExpanded by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = c.surfaceElevated,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(c.dragHandle, CircleShape)
            )
        }
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Close, null, tint = c.textHint)
            }
        }
        Column(
            modifier            = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sheetAvatarUrl = user?.profilePictureUri
            val sheetInitials = run {
                val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
                when {
                    parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                    parts.size == 1 -> parts[0].take(2).uppercase()
                    else -> "?"
                }
            }
            Box(
                modifier         = Modifier.size(72.dp).clip(CircleShape).background(c.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                if (!sheetAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        sheetAvatarUrl, stringResource(R.string.cd_photo_profile),
                        Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(sheetInitials, color = c.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(user?.fullName ?: "", color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(user?.phoneNumber ?: user?.email ?: "", color = c.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            val primary   = c.primary
            val roleLabel = stringResource(
                if (user?.role == "driver") R.string.role_label_driver else R.string.role_label_passenger
            )
            Box(
                modifier = Modifier
                    .background(primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(roleLabel, color = primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(8.dp))
            ProfileMenuItem(Icons.Default.Edit, stringResource(R.string.menu_edit_profile), onClick = onEditProfile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { languagePickerExpanded = !languagePickerExpanded }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Language, stringResource(R.string.menu_language), tint = c.textPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.menu_language), color = c.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    "${selectedLang.flag} ${selectedLang.localizedDisplayName()}",
                    color = c.textHint,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (languagePickerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = c.textHint,
                    modifier = Modifier.size(18.dp)
                )
            }
            AnimatedVisibility(
                visible = languagePickerExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    AppLanguage.menuChoices.forEachIndexed { index, lang ->
                        val isLast = index == AppLanguage.menuChoices.lastIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    languageViewModel.selectLanguage(lang)
                                    languagePickerExpanded = false
                                    activity?.recreate()
                                }
                                .background(
                                    if (selectedLang == lang) c.primary.copy(alpha = 0.08f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.flag, fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(lang.localizedDisplayName(), color = c.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            if (selectedLang == lang) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (!isLast) HorizontalDivider(color = c.dividerGrey, modifier = Modifier.padding(start = 40.dp))
                    }
                    HorizontalDivider(
                        color = c.dividerGrey,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    )
                    TextButton(
                        onClick = {
                            languageViewModel.followDeviceLanguage()
                            languagePickerExpanded = false
                            activity?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.menu_language_follow_device),
                            color = c.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.menu_appearance_colors), onClick = onColorSettings)
            ProfileMenuItem(Icons.Default.Search,  stringResource(R.string.menu_help_support),     onClick = {})
            ProfileMenuItem(Icons.Default.Info,      stringResource(R.string.menu_terms_of_service), onClick = {})
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(4.dp))
            ProfileMenuItem(Icons.Default.ExitToApp, stringResource(R.string.menu_log_out), tint = c.errorRed, onClick = onLogout)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_version_label), color = c.textTertiary, fontSize = 11.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val resolved = tint ?: LocalAppColors.current.textPrimary
    val chevron  = LocalAppColors.current.textTertiary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = resolved, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = resolved, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = chevron, modifier = Modifier.size(18.dp))
    }
}

