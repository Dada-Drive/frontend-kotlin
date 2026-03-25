package com.dadadrive.ui.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadadrive.R
import com.dadadrive.ui.auth.AuthState
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.theme.FacebookBlue
import com.dadadrive.ui.theme.GoogleRed

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val bg = MaterialTheme.colorScheme.background
    val fg = MaterialTheme.colorScheme.onBackground

    fun validate(): Boolean {
        var ok = true
        emailError = when {
            email.isBlank() -> { ok = false; "Email address is required" }
            !EMAIL_REGEX.matches(email.trim()) -> { ok = false; "Invalid email format (e.g. name@example.com)" }
            else -> null
        }
        passwordError = when {
            password.isBlank() -> { ok = false; "Password is required" }
            password.length < 6 -> { ok = false; "Minimum 6 characters required" }
            else -> null
        }
        return ok
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> { onLoginSuccess(); authViewModel.resetState() }
            is AuthState.Error -> { snackbarHostState.showSnackbar(state.message); authViewModel.resetState() }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(bg)) {

        // ── Scrollable content (leaves space for the fixed bottom) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 168.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_dadadrive_logo),
                contentDescription = "DadaDrive Logo",
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(20.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "DADA DRIVE",
                color = fg,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )
            Spacer(Modifier.height(52.dp))

            AuthInputField(
                value = email,
                onValueChange = { email = it; if (emailError != null) emailError = null },
                label = "Email Address",
                placeholder = "name@example.com",
                keyboardType = KeyboardType.Email,
                errorMessage = emailError
            )
            Spacer(Modifier.height(28.dp))
            AuthInputField(
                value = password,
                onValueChange = { password = it; if (passwordError != null) passwordError = null },
                label = "Password",
                placeholder = "Enter your password",
                isPassword = true,
                errorMessage = passwordError
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Forgot Password?",
                color = fg,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.End).clickable { }
            )
            Spacer(Modifier.height(36.dp))

            SocialDivider()
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                SocialButton(label = "f", backgroundColor = FacebookBlue, onClick = {})
                Spacer(Modifier.width(20.dp))
                SocialButton(label = "G", backgroundColor = GoogleRed, onClick = {})
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Fixed bottom: SIGN IN + Sign Up link ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(bg.copy(alpha = 0f), bg, bg),
                        startY = 0f,
                        endY = 100f
                    )
                )
                .padding(horizontal = 28.dp)
                .padding(bottom = 36.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { if (validate()) authViewModel.login(email, password) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text(text = "SIGN IN  →", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Text(text = "Don't have an account? ", color = fg.copy(alpha = 0.6f), fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }
        }

        // ── Loading overlay ──
        if (authState is AuthState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(bg.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ─────────────────────────────────────────────────────────
// AUTH INPUT FIELD (shared with SignupScreen)
// ─────────────────────────────────────────────────────────

@Composable
fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null
) {
    val fg = MaterialTheme.colorScheme.onBackground
    val hasError = errorMessage != null
    val underlineColor = if (hasError) MaterialTheme.colorScheme.error else fg.copy(alpha = 0.25f)
    val primary = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = if (hasError) MaterialTheme.colorScheme.error else fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = fg, fontSize = 16.sp),
            cursorBrush = SolidColor(primary),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = underlineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = if (hasError) 1.5.dp.toPx() else 1.dp.toPx()
                    )
                }
                .padding(bottom = 10.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, color = fg.copy(alpha = 0.4f), fontSize = 16.sp)
                inner()
            }
        )
        if (hasError) {
            Spacer(Modifier.height(4.dp))
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────
// PRIVATE HELPERS
// ─────────────────────────────────────────────────────────

@Composable
private fun SocialDivider() {
    val fg = MaterialTheme.colorScheme.onBackground
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = fg.copy(alpha = 0.15f))
        Text("  Sign in with social  ", color = fg.copy(alpha = 0.5f), fontSize = 13.sp, textAlign = TextAlign.Center)
        HorizontalDivider(modifier = Modifier.weight(1f), color = fg.copy(alpha = 0.15f))
    }
}

@Composable
private fun SocialButton(label: String, backgroundColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(50.dp).background(backgroundColor, CircleShape).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
