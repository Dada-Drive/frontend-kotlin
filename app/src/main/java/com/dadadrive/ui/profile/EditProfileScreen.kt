package com.dadadrive.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dadadrive.ui.theme.AppColor
import com.dadadrive.ui.theme.AppTypography
import com.dadadrive.ui.theme.AppRadius
import com.dadadrive.ui.theme.AppSpacing

/**
 * Matches Swift EditProfileView.swift exactly:
 * - Custom nav bar (arrow.left · "Edit Profile" centred · invisible spacer)
 * - Avatar section with camera button overlay (green circle + camera icon)
 * - Name under avatar + role badge (green capsule)
 * - Editable Full Name field with person icon
 * - Locked Email, Phone, Role fields with lock icon
 * - Save button at the bottom
 * - Same colours, spacing, corner radii from shared design system
 */

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember(user?.fullName) { mutableStateOf(user?.fullName ?: "") }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Navigation and feedback after save
    LaunchedEffect(saveState) {
        when (saveState) {
            is ProfileViewModel.SaveState.Success -> {
                viewModel.resetSaveState()
                onBack()
            }
            is ProfileViewModel.SaveState.PartialSuccess -> {
                snackbarHostState.showSnackbar(
                    (saveState as ProfileViewModel.SaveState.PartialSuccess).warning
                )
                viewModel.resetSaveState()
                onBack()
            }
            is ProfileViewModel.SaveState.Error -> {
                snackbarHostState.showSnackbar(
                    (saveState as ProfileViewModel.SaveState.Error).message
                )
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pickedPhotoUri = uri }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColor.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Nav bar — matches Swift: arrow.left · "Edit Profile" · invisible spacer ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.l)
                    .padding(top = AppSpacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColor.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Edit Profile",
                    style = AppTypography.headingS,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.textPrimary
                )
                Spacer(Modifier.weight(1f))
                // Invisible spacer to balance back button
                Spacer(Modifier.size(44.dp))
            }

            // ── Scrollable content ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.xl, vertical = AppSpacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(AppSpacing.xl))

                // ── Avatar section ──────────────────────────────────────
                val displayUri: Any? = pickedPhotoUri ?: user?.profilePictureUri
                val initials = computeInitials(user?.fullName ?: "")

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clickable {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    if (displayUri != null) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // Initials circle — matches Swift initialsCircle
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppColor.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = AppColor.textPrimary,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Green border overlay — matches Swift Circle().stroke(AppColor.surface, lineWidth: 2)
                    // In Swift, the avatar has surface border; on Android we skip stroke for simplicity

                    // Camera button — matches Swift: green circle + camera icon at bottom-right
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .background(AppColor.green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person, // camera.fill equivalent
                            contentDescription = "Change photo",
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.m))

                // Name under avatar — matches Swift
                Text(
                    text = fullName.ifBlank { user?.fullName ?: "" },
                    color = AppColor.textPrimary,
                    style = AppTypography.headingS,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(AppSpacing.s))

                // Role badge — matches Swift RoleBadge: green 15% bg, green text, capsule
                RoleBadge(user?.role ?: "rider")

                Spacer(Modifier.height(AppSpacing.xxl))

                // ── Full Name — editable ────────────────────────────────
                ProfileFieldLabel("Full Name")
                EditableField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    leadingIcon = Icons.Default.Person,
                    placeholder = "Enter your name"
                )

                Spacer(Modifier.height(AppSpacing.l))

                // ── Email — locked ──────────────────────────────────────
                ProfileFieldLabel("Email")
                LockedField(
                    value = user?.email ?: "No email set",
                    leadingIcon = Icons.Default.Email,
                    isEmpty = user?.email == null
                )

                Spacer(Modifier.height(AppSpacing.l))

                // ── Phone — locked ──────────────────────────────────────
                ProfileFieldLabel("Phone Number")
                LockedField(
                    value = user?.phoneNumber ?: "No phone set",
                    leadingIcon = Icons.Default.Phone,
                    isEmpty = user?.phoneNumber == null
                )

                Spacer(Modifier.height(AppSpacing.l))

                // ── Role — locked ───────────────────────────────────────
                ProfileFieldLabel("Role")
                LockedField(
                    value = if (user?.role == "driver") "Driver" else "Passenger",
                    leadingIcon = Icons.Default.Place,
                    isEmpty = false
                )

                Spacer(Modifier.height(40.dp))

                // ── Save Changes button ─────────────────────────────────
                val isLoading = saveState is ProfileViewModel.SaveState.Loading
                Button(
                    onClick = {
                        viewModel.saveProfile(
                            fullName = fullName,
                            localAvatarUri = pickedPhotoUri?.toString()
                        )
                    },
                    enabled = !isLoading && fullName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColor.green,
                        disabledContainerColor = AppColor.green.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(AppRadius.full)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            "Save Changes",
                            color = Color.Black,
                            style = AppTypography.headingS,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.xxxl))
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(AppSpacing.l)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = AppColor.surface,
                contentColor = AppColor.textPrimary
            )
        }
    }
}

// ── Field label — matches Swift ProfileFieldLabel ───────────────────────────
// Small hint text above each field

@Composable
private fun ProfileFieldLabel(label: String) {
    Text(
        text = label,
        color = AppColor.textHint,
        style = AppTypography.labelS,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppSpacing.s, bottom = 6.dp)
    )
}

// ── Editable field — matches Swift ProfileField ─────────────────────────────
// Leading icon + TextField, surface background, cornerRadius m (12)

@Composable
private fun EditableField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = AppColor.textHint)
        },
        placeholder = {
            Text(placeholder, color = AppColor.textHint, style = AppTypography.bodyL)
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        shape = RoundedCornerShape(AppRadius.m),
        colors = profileFieldColors(),
        singleLine = true,
        textStyle = AppTypography.bodyL.copy(color = AppColor.textPrimary)
    )
}

// ── Locked field — matches Swift LockedInfoRow ──────────────────────────────
// Leading icon + read-only text + trailing lock icon
// Background: surface at 50% opacity, same corner radius

@Composable
private fun LockedField(
    value: String,
    leadingIcon: ImageVector,
    isEmpty: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = AppColor.textHint)
        },
        trailingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = AppColor.textHint,
                modifier = Modifier.size(12.dp)
            )
        },
        enabled = false,
        shape = RoundedCornerShape(AppRadius.m),
        colors = lockedFieldColors(),
        textStyle = AppTypography.bodyL.copy(
            color = if (isEmpty) AppColor.textHint else AppColor.textPrimary
        )
    )
}

// ── Field colours — matches Swift profileFieldColors ────────────────────────

@Composable
private fun profileFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColor.textPrimary,
        unfocusedTextColor = AppColor.textPrimary,
        disabledTextColor = AppColor.textHint,
        focusedBorderColor = AppColor.green,
        unfocusedBorderColor = AppColor.surface,
        disabledBorderColor = AppColor.surface,
        focusedContainerColor = AppColor.surface,
        unfocusedContainerColor = AppColor.surface,
        disabledContainerColor = AppColor.surface,
        cursorColor = AppColor.green
    )
}

@Composable
private fun lockedFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        disabledTextColor = AppColor.textPrimary,
        disabledBorderColor = AppColor.surface,
        disabledContainerColor = AppColor.surface.copy(alpha = 0.5f),
        disabledLeadingIconColor = AppColor.textHint,
        disabledTrailingIconColor = AppColor.textHint
    )
}

// ── Role badge — matches Swift RoleBadge ────────────────────────────────────
// Green text on green 15% background, capsule shape

@Composable
private fun RoleBadge(role: String) {
    val label = if (role == "driver") "Driver" else "Passenger"
    Box(
        modifier = Modifier
            .background(
                AppColor.green.copy(alpha = 0.15f),
                RoundedCornerShape(AppRadius.l)
            )
            .padding(horizontal = AppSpacing.l, vertical = AppSpacing.xs)
    ) {
        Text(
            label,
            color = AppColor.green,
            style = AppTypography.labelM,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Initials helper ─────────────────────────────────────────────────────────

private fun computeInitials(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}