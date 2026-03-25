package com.dadadrive.ui.auth.register

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.ui.auth.AuthState
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.login.AuthInputField

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val PHONE_REGEX = Regex("^[+]?[0-9]{8,15}$")

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    // Errors (null = no error)
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> profilePictureUri = uri }

    val backgroundColor = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground

    // Local validation — returns true if all fields are valid
    fun validate(): Boolean {
        var valid = true

        fullNameError = if (fullName.isBlank()) {
            valid = false; "Full name is required"
        } else null

        emailError = when {
            email.isBlank() -> { valid = false; "Email address is required" }
            !EMAIL_REGEX.matches(email.trim()) -> { valid = false; "Invalid email format (e.g. name@example.com)" }
            else -> null
        }

        phoneError = when {
            phoneNumber.isBlank() -> { valid = false; "Phone number is required" }
            !PHONE_REGEX.matches(phoneNumber.trim()) -> { valid = false; "Invalid phone number (8–15 digits)" }
            else -> null
        }

        passwordError = when {
            password.isBlank() -> { valid = false; "Password is required" }
            password.length < 6 -> { valid = false; "Minimum 6 characters required" }
            else -> null
        }

        confirmPasswordError = when {
            confirmPassword.isBlank() -> { valid = false; "Please confirm your password" }
            confirmPassword != password -> { valid = false; "Passwords do not match" }
            else -> null
        }

        return valid
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                onSignupSuccess()
                authViewModel.resetState()
            }
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back button — stays left
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title — centered, single line
            Text(
                text = "Create new account",
                color = onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Profile picture — centered
            ProfilePicturePicker(
                uri = profilePictureUri,
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- FIELDS ---

            AuthInputField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (fullNameError != null) fullNameError = null
                },
                label = "Full Name",
                placeholder = "Enter your name",
                errorMessage = fullNameError
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                value = email,
                onValueChange = {
                    email = it
                    if (emailError != null) emailError = null
                },
                label = "Email Address",
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    if (phoneError != null) phoneError = null
                },
                label = "Phone Number",
                placeholder = "+1 234 567 8900",
                keyboardType = KeyboardType.Phone,
                errorMessage = phoneError
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                value = password,
                onValueChange = {
                    password = it
                    if (passwordError != null) passwordError = null
                    // Re-check confirm if already filled
                    if (confirmPasswordError != null && confirmPassword == it) {
                        confirmPasswordError = null
                    }
                },
                label = "Create Password",
                placeholder = "Enter your password",
                isPassword = true,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthInputField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (confirmPasswordError != null) confirmPasswordError = null
                },
                label = "Confirm Password",
                placeholder = "Repeat your password",
                isPassword = true,
                errorMessage = confirmPasswordError
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    if (validate()) {
                        authViewModel.signup(
                            fullName = fullName,
                            email = email,
                            password = password,
                            phoneNumber = phoneNumber,
                            profilePictureUri = profilePictureUri?.toString()
                        )
                    }
                },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ProfilePicturePicker(
    uri: Uri?,
    onPickImage: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onBackground

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Profile Picture",
            color = labelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(surfaceColor)
                .border(1.5.dp, borderColor, CircleShape)
                .clickable { onPickImage() },
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Choose photo",
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
