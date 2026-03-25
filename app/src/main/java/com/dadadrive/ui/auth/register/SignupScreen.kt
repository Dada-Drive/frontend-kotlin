package com.dadadrive.ui.auth.register

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import com.dadadrive.ui.auth.AuthState
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.login.AuthInputField
import com.dadadrive.ui.theme.Black
import com.dadadrive.ui.theme.DividerGrey
import com.dadadrive.ui.theme.GreyHint
import com.dadadrive.ui.theme.White

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> profilePictureUri = uri }

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
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create new\naccount",
                color = White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 42.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile picture picker
            ProfilePicturePicker(
                uri = profilePictureUri,
                onPickImage = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthInputField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                placeholder = "Enter your name"
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthInputField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                placeholder = "+1 234 567 8900",
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthInputField(
                value = password,
                onValueChange = { password = it },
                label = "Create Password",
                placeholder = "Enter your password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(44.dp))

            Button(
                onClick = {
                    authViewModel.signup(
                        fullName = fullName,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        profilePictureUri = profilePictureUri?.toString()
                    )
                },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Black,
                    disabledContainerColor = Color(0xFFCCCCCC),
                    disabledContentColor = Color(0xFF888888)
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
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White)
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Profile Picture",
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A))
                .border(1.5.dp, DividerGrey, CircleShape)
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
                        tint = GreyHint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Choose photo",
                        tint = GreyHint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
