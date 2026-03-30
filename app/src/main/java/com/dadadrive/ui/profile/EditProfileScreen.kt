package com.dadadrive.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dadadrive.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
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

    // Navigation et feedback après sauvegarde
    LaunchedEffect(saveState) {
        when (saveState) {
            is ProfileViewModel.SaveState.Success -> {
                viewModel.resetSaveState()
                onBack()
            }
            // Nom sauvegardé mais photo Cloudinary échouée : afficher l'erreur puis revenir
            is ProfileViewModel.SaveState.PartialSuccess -> {
                snackbarHostState.showSnackbar(
                    (saveState as ProfileViewModel.SaveState.PartialSuccess).warning
                )
                viewModel.resetSaveState()
                onBack()
            }
            is ProfileViewModel.SaveState.Error -> {
                snackbarHostState.showSnackbar((saveState as ProfileViewModel.SaveState.Error).message)
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    // Photo picker — galerie
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) pickedPhotoUri = uri }

    val c = LocalAppColors.current
    Box(modifier = Modifier.fillMaxSize().background(c.darkSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        color = c.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = c.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = c.darkSurface)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Avatar cliquable ──────────────────────────────────────
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
                            contentDescription = "Photo de profil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(c.surfaceMuted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = c.textPrimary,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Icône caméra
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .background(c.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Changer la photo",
                            tint = c.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = fullName.ifBlank { user?.fullName ?: "" },
                    color = c.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))
                RoleBadge(user?.role ?: "rider")
                Spacer(Modifier.height(32.dp))

                // ── Full Name — modifiable ────────────────────────────────
                ProfileFieldLabel("Full Name")
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = c.textSecondary)
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    shape = RoundedCornerShape(12.dp),
                    colors = profileFieldColors(),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // ── Email — verrouillé ────────────────────────────────────
                ProfileFieldLabel("Email")
                LockedField(value = user?.email ?: "", leadingIcon = Icons.Default.Email)

                Spacer(Modifier.height(16.dp))

                // ── Phone — verrouillé ────────────────────────────────────
                ProfileFieldLabel("Phone Number")
                LockedField(value = user?.phoneNumber ?: "", leadingIcon = Icons.Default.Phone)

                Spacer(Modifier.height(16.dp))

                // ── Role — verrouillé ─────────────────────────────────────
                ProfileFieldLabel("Role")
                LockedField(
                    value = if (user?.role == "driver") "Driver" else "Passenger",
                    leadingIcon = Icons.Default.Place
                )

                Spacer(Modifier.height(40.dp))

                // ── Save Changes ──────────────────────────────────────────
                val isLoading = saveState is ProfileViewModel.SaveState.Loading
                Button(
                    onClick = {
                        viewModel.saveProfile(
                            fullName = fullName,
                            localAvatarUri = pickedPhotoUri?.toString()
                        )
                    },
                    enabled = !isLoading && fullName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.primary,
                        disabledContainerColor = c.primary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = c.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            "Save Changes",
                            color = c.onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Snackbar erreurs
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = LocalAppColors.current.surfaceMuted,
                contentColor = LocalAppColors.current.textPrimary
            )
        }
    }
}

@Composable
private fun ProfileFieldLabel(label: String) {
    Text(
        text = label,
        color = LocalAppColors.current.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

@Composable
private fun LockedField(value: String, leadingIcon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = LocalAppColors.current.textSecondary) },
        trailingIcon = {
            Icon(Icons.Default.Lock, contentDescription = "Verrouillé", tint = LocalAppColors.current.textTertiary)
        },
        enabled = false,
        shape = RoundedCornerShape(12.dp),
        colors = profileFieldColors()
    )
}

@Composable
private fun profileFieldColors(): TextFieldColors {
    val c = LocalAppColors.current
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = c.textPrimary,
        unfocusedTextColor = c.textPrimary,
        disabledTextColor = c.textDisabled,
        focusedBorderColor = c.primary,
        unfocusedBorderColor = c.surfaceMuted,
        disabledBorderColor = c.surfaceMuted,
        focusedContainerColor = c.surfaceElevated,
        unfocusedContainerColor = c.surfaceElevated,
        disabledContainerColor = c.surfaceElevated
    )
}

@Composable
private fun RoleBadge(role: String) {
    val label = if (role == "driver") "Driver" else "Passenger"
    Box(
        modifier = Modifier
            .background(LocalAppColors.current.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(label, color = LocalAppColors.current.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun computeInitials(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "?"
    }
}
