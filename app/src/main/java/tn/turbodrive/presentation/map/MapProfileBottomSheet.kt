package tn.turbodrive.presentation.map

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.turbodrive.R
import tn.turbodrive.core.designsystem.tokens.AppIcon
import tn.turbodrive.core.language.AppLanguage
import tn.turbodrive.core.theme.LocalAppColors
import tn.turbodrive.presentation.components.BlackCloseIconButton
import tn.turbodrive.presentation.language.LanguageViewModel
import tn.turbodrive.presentation.language.localizedDisplayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileBottomSheet(
    sheetState: SheetState,
    user: tn.turbodrive.domain.models.User?,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onColorSettings: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val c = LocalAppColors.current
    val activity = LocalActivity.current
    val languageViewModel: LanguageViewModel = hiltViewModel()
    val selectedLang by languageViewModel.selected.collectAsState()
    var languagePickerExpanded by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceElevated,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(c.dragHandle, CircleShape),
            )
        },
    ) {
        Box(Modifier.fillMaxWidth()) {
            BlackCloseIconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp),
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val sheetAvatarUrl = user?.profilePictureUri
            val sheetInitials =
                run {
                    val parts = (user?.fullName ?: "").trim().split(" ").filter { it.isNotBlank() }
                    when {
                        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                        parts.size == 1 -> parts[0].take(2).uppercase()
                        else -> "?"
                    }
                }
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(c.surfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                if (!sheetAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        sheetAvatarUrl,
                        stringResource(R.string.cd_photo_profile),
                        Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
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
            val primary = c.primary
            val roleLabel =
                stringResource(
                    if (user?.role == "driver") R.string.role_label_driver else R.string.role_label_passenger,
                )
            Box(
                modifier =
                    Modifier
                        .background(primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(roleLabel, color = primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(8.dp))
            ProfileMenuItem(AppIcon.edit, stringResource(R.string.menu_edit_profile), onClick = onEditProfile)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { languagePickerExpanded = !languagePickerExpanded }
                        .padding(vertical = 14.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(AppIcon.globe),
                    contentDescription = stringResource(R.string.menu_language),
                    tint = c.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.menu_language), color = c.textPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(
                    "${selectedLang.flag} ${selectedLang.localizedDisplayName()}",
                    color = c.textSubtle,
                    fontSize = 13.sp,
                    maxLines = 1,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(if (languagePickerExpanded) AppIcon.chevronUp else AppIcon.chevronDown),
                    contentDescription = null,
                    tint = c.textSubtle,
                    modifier = Modifier.size(18.dp),
                )
            }
            AnimatedVisibility(
                visible = languagePickerExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 8.dp)) {
                    AppLanguage.menuChoices.forEachIndexed { index, lang ->
                        val isLast = index == AppLanguage.menuChoices.lastIndex
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        languageViewModel.selectLanguage(lang)
                                        languagePickerExpanded = false
                                        activity?.recreate()
                                    }
                                    .background(
                                        if (selectedLang == lang) c.primary.copy(alpha = 0.08f) else Color.Transparent,
                                        RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(lang.flag, fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(lang.localizedDisplayName(), color = c.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            if (selectedLang == lang) {
                                Icon(
                                    painter = painterResource(AppIcon.check),
                                    contentDescription = null,
                                    tint = c.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        if (!isLast) HorizontalDivider(color = c.dividerGrey, modifier = Modifier.padding(start = 40.dp))
                    }
                    HorizontalDivider(
                        color = c.dividerGrey,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                    )
                    TextButton(
                        onClick = {
                            languageViewModel.followDeviceLanguage()
                            languagePickerExpanded = false
                            activity?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.menu_language_follow_device),
                            color = c.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            // Justified: color picker glyph, no AppIcon equivalent
            ProfileMenuItem(Icons.Default.Palette, stringResource(R.string.menu_appearance_colors), onClick = onColorSettings)
            ProfileMenuItem(AppIcon.search, stringResource(R.string.menu_help_support), onClick = {})
            ProfileMenuItem(AppIcon.info, stringResource(R.string.menu_terms_of_service), onClick = {})
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = c.dividerGrey)
            Spacer(Modifier.height(4.dp))
            ProfileMenuItem(AppIcon.logOut, stringResource(R.string.menu_log_out), tint = c.error, onClick = onLogout)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_version_label), color = c.textTertiary, fontSize = 11.sp)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun ProfileMenuItem(
    @DrawableRes icon: Int,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    ProfileMenuItemBase(
        iconContent = {
            val resolved = tint ?: LocalAppColors.current.textPrimary
            Icon(painterResource(icon), label, tint = resolved, modifier = Modifier.size(20.dp))
        },
        label = label,
        tint = tint,
        onClick = onClick,
    )
}

@Composable
internal fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    ProfileMenuItemBase(
        iconContent = {
            val resolved = tint ?: LocalAppColors.current.textPrimary
            Icon(icon, label, tint = resolved, modifier = Modifier.size(20.dp))
        },
        label = label,
        tint = tint,
        onClick = onClick,
    )
}

@Composable
private fun ProfileMenuItemBase(
    iconContent: @Composable () -> Unit,
    label: String,
    tint: Color?,
    onClick: () -> Unit,
) {
    val resolved = tint ?: LocalAppColors.current.textPrimary
    val chevron = LocalAppColors.current.textTertiary
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        iconContent()
        Spacer(Modifier.width(16.dp))
        Text(label, color = resolved, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(painterResource(AppIcon.chevronRight), null, tint = chevron, modifier = Modifier.size(18.dp))
    }
}
