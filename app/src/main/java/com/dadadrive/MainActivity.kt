package com.dadadrive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import com.dadadrive.ui.map.MapScreen
import com.dadadrive.ui.onboarding.OnboardingScreen
import com.dadadrive.ui.profile.EditProfileScreen
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.role.RoleSelectionScreen
import com.dadadrive.ui.settings.ColorWheelSettingsScreen
import com.dadadrive.ui.splash.SessionViewModel
import com.dadadrive.ui.splash.SplashScreen
import com.dadadrive.ui.theme.DadaDriveTheme
import com.dadadrive.ui.theme.ThemeViewModel
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
    object RoleSelection : Screen("role_selection")
    object Map : Screen("map")
    object EditProfile : Screen("edit_profile")
    object Home : Screen("home")
    object ColorSettings : Screen("settings/colors")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            val darkTheme = isSystemInDarkTheme()
            val appColors = remember(currentTheme, darkTheme) {
                currentTheme.resolveScheme(darkTheme)
            }

            DadaDriveTheme(darkTheme = darkTheme, appColors = appColors) {
                DadaDriveNavHost(themeViewModel = themeViewModel)
            }
        }
    }
}

@Composable
private fun DadaDriveNavHost(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    // Instance unique de ProfileViewModel partagée entre MapScreen et EditProfileScreen.
    // Créée ici (hors NavHost) → scoped à l'Activity → même StateFlow pour les deux écrans.
    val sharedProfileViewModel: ProfileViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val sessionState by sessionViewModel.state.collectAsState()
            var splashDone by remember { mutableStateOf(false) }

            // Navigate quand les DEUX conditions sont remplies :
            // 1. Animation splash terminée  2. Vérification session terminée
            LaunchedEffect(splashDone, sessionState) {
                if (splashDone && sessionState != SessionViewModel.SessionState.Checking) {
                    val destination = when (sessionState) {
                        SessionViewModel.SessionState.Valid -> Screen.Map.route
                        else -> Screen.Onboarding.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            SplashScreen(onSplashFinished = { splashDone = true })
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
                when (authState) {
                    is AuthState.Success -> {
                        authViewModel.resetState()
                        navController.navigate(Screen.RoleSelection.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                    is AuthState.NeedsPhone -> {
                        // Google user sans numéro : vérification téléphone d'abord
                        authViewModel.resetState()
                        navController.navigate(Screen.Phone.route)
                    }
                    else -> {}
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
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onSuccess = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToColorSettings = {
                    navController.navigate(Screen.ColorSettings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = sharedProfileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            MapScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToColorSettings = {
                    navController.navigate(Screen.ColorSettings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ColorSettings.route) {
            ColorWheelSettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }
    }
}