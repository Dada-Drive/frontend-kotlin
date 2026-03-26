package com.dadadrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.PhoneScreen
import com.dadadrive.ui.auth.WelcomeScreen
import com.dadadrive.ui.onboarding.OnboardingScreen
import com.dadadrive.ui.splash.SplashScreen
import com.dadadrive.ui.theme.Black
import com.dadadrive.ui.theme.DadaDriveTheme
import com.dadadrive.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object Phone : Screen("phone")
    object Home : Screen("home")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DadaDriveTheme {
                DadaDriveNavHost()
            }
        }
    }
}

@Composable
private fun DadaDriveNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onPhoneClick = { navController.navigate(Screen.Phone.route) },
                onGoogleClick = { /* TODO: Google Auth */ }
            )
        }

        composable(Screen.Phone.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            PhoneScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomePlaceholder()
        }
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

