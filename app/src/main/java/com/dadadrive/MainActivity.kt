package com.dadadrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.PhoneScreen
import com.dadadrive.ui.auth.WelcomeScreen
import com.dadadrive.ui.onboarding.OnboardingScreen
import com.dadadrive.ui.splash.SplashScreen
import com.dadadrive.ui.theme.Black
import com.dadadrive.ui.theme.DadaDriveTheme
import com.dadadrive.ui.theme.White

sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Welcome : Screen()
    object Phone : Screen()
    object Home : Screen()
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DadaDriveTheme {
                DadaDriveNavHost(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
private fun DadaDriveNavHost(authViewModel: AuthViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    when (currentScreen) {
        Screen.Splash -> SplashScreen(
            onSplashFinished = { currentScreen = Screen.Onboarding }
        )

        Screen.Onboarding -> OnboardingScreen(
            onFinished = { currentScreen = Screen.Welcome }
        )

        Screen.Welcome -> WelcomeScreen(
            onPhoneClick = { currentScreen = Screen.Phone },
            onGoogleClick = { /* TODO: Google Auth */ }
        )

        Screen.Phone -> PhoneScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = Screen.Welcome },
            onSuccess = { currentScreen = Screen.Home }
        )

        Screen.Home -> HomePlaceholder()
    }
}

@Composable
private fun HomePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to\nDADA DRIVE",
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )
    }
}
