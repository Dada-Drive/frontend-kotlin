package com.dadadrive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dadadrive.core.constants.Constants
import com.dadadrive.ui.auth.AuthState
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.OtpScreen
import com.dadadrive.ui.auth.PhoneScreen
import com.dadadrive.ui.auth.WelcomeScreen
import com.dadadrive.ui.onboarding.OnboardingScreen
import com.dadadrive.ui.pending.PendingScreen
import com.dadadrive.ui.splash.SplashScreen
import com.dadadrive.ui.theme.Black
import com.dadadrive.ui.theme.DadaDriveTheme
import com.dadadrive.ui.theme.White
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome")
    object Phone : Screen("phone")
    object Otp : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/${java.net.URLEncoder.encode(phone, "UTF-8")}"
    }
    object Pending : Screen("pending")
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
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    authViewModel.resetState()
                    navController.navigate(Screen.Pending.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            }

            WelcomeScreen(
                authState = authState,
                onPhoneClick = { navController.navigate(Screen.Phone.route) },
                onGoogleClick = {
                    scope.launch {
                        try {
                            Log.d("GoogleAuth", "=== DÉBUT GOOGLE SIGN IN ===")

                            val credentialManager = CredentialManager.create(context)

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(Constants.GOOGLE_WEB_CLIENT_ID)
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            // ✅ FIX — utiliser createFrom() au lieu du cast direct
                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                Log.d("GoogleAuth", "Token obtenu avec succès !")
                                authViewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                            } else {
                                Log.e("GoogleAuth", "Type de credential inconnu: ${credential.type}")
                            }

                        } catch (e: GetCredentialCancellationException) {
                            Log.w("GoogleAuth", "Annulé par l'utilisateur")
                        } catch (e: NoCredentialException) {
                            Log.e("GoogleAuth", "Aucun compte Google: ${e.message}")
                        } catch (e: Exception) {
                            Log.e("GoogleAuth", "ERREUR: ${e::class.simpleName} — ${e.message}")
                            Log.e("GoogleAuth", "Stack: ${e.stackTraceToString()}")
                        }
                    }
                }
            )
        }

        composable(Screen.Phone.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            PhoneScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { fullPhone ->
                    navController.navigate(Screen.Otp.createRoute(fullPhone))
                }
            )
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStackEntry ->
            val phone = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("phone") ?: "",
                "UTF-8"
            )
            val authViewModel: AuthViewModel = hiltViewModel()
            OtpScreen(
                phone = phone,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.Pending.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Pending.route) {
            PendingScreen()
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