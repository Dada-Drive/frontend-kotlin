// Équivalent Swift : Presentation/AppCoordinatorView.swift + AuthCoordinatorView (NavHost)
package com.dadadrive

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
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
import com.dadadrive.map.MapsInitializer
import com.dadadrive.ui.auth.AuthState
import com.dadadrive.ui.auth.AuthViewModel
import com.dadadrive.ui.auth.NameEntryScreen
import com.dadadrive.ui.auth.OtpScreen
import com.dadadrive.ui.auth.PhoneScreen
import com.dadadrive.ui.auth.WelcomeScreen
import com.dadadrive.ui.driver.DriverHomeScreen
import com.dadadrive.ui.driver.DriverSetupScreen
import com.dadadrive.ui.map.MapScreen
import com.dadadrive.ui.onboarding.OnboardingScreen
import com.dadadrive.ui.profile.EditProfileScreen
import com.dadadrive.ui.profile.ProfileViewModel
import com.dadadrive.ui.role.RoleSelectionScreen
import com.dadadrive.ui.session.SessionUiState
import com.dadadrive.ui.settings.ColorWheelSettingsScreen
import com.dadadrive.ui.splash.SessionViewModel
import com.dadadrive.ui.splash.SplashScreen
import com.dadadrive.ui.theme.DadaDriveTheme
import com.dadadrive.ui.theme.ThemeViewModel
import com.dadadrive.ui.wallet.WalletScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color

private sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Welcome : Screen("welcome")
    data object NameEntry : Screen("name_entry")
    data object RoleSelection : Screen("role_selection")
    data object Map : Screen("map")
    data object DriverHome : Screen("driver_home")
    data object DriverSetup : Screen("driver_setup")
    data object EditProfile : Screen("edit_profile")
    data object ColorSettings : Screen("settings/colors")
    data object Wallet : Screen("wallet")
    data object Otp : Screen("otp/{phone}") {
        fun createRoute(phone: String) = "otp/${java.net.URLEncoder.encode(phone, "UTF-8")}"
    }
    data object Phone : Screen("phone?fromSession={fromSession}") {
        fun createRoute(fromSession: Boolean) = "phone?fromSession=$fromSession"
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            MapsInitializer.initialize(applicationContext)
            Log.d(TAG_HERE_MAPS, "HERE Maps SDK pre-initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG_HERE_MAPS, "HERE Maps SDK pre-initialization failed: ${e.message}", e)
        }

        enableEdgeToEdge()
        setContent {
            val configuration = LocalConfiguration.current
            key(configuration.locales.toLanguageTags()) {
                val sessionViewModel: SessionViewModel = hiltViewModel()
                val themeViewModel: ThemeViewModel = hiltViewModel()
                val currentTheme by themeViewModel.currentTheme.collectAsState()
                val customSecondaryArgb by themeViewModel.customSecondaryArgb.collectAsState()
                val darkTheme = isSystemInDarkTheme()
                val appColors = remember(currentTheme, darkTheme, customSecondaryArgb) {
                    val secondaryOverride = customSecondaryArgb?.let { argb -> Color(argb) }
                    currentTheme.resolveScheme(darkTheme, secondaryOverride)
                }

                DadaDriveTheme(darkTheme = darkTheme, appColors = appColors) {
                    DadaDriveNavHost(
                        sessionViewModel = sessionViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }

    private companion object {
        private const val TAG_HERE_MAPS = "HereMaps"
    }
}

@Composable
private fun DadaDriveNavHost(
    sessionViewModel: SessionViewModel,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    var splashDone by remember { mutableStateOf(false) }
    val sessionUiState by sessionViewModel.sessionState.collectAsState()
    val sharedProfileViewModel: ProfileViewModel = hiltViewModel()

    LaunchedEffect(splashDone, sessionUiState) {
        if (!splashDone) return@LaunchedEffect
        if (sessionUiState is SessionUiState.Loading) return@LaunchedEffect
        val dest = sessionToRoute(sessionUiState) ?: return@LaunchedEffect
        val current = navController.currentDestination?.route
        if (routesMatch(current, dest)) return@LaunchedEffect
        navController.navigate(dest) {
            popUpTo(Screen.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashFinished = { splashDone = true })
        }

        composable(Screen.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Success -> {
                        sessionViewModel.refreshSession()
                        authViewModel.resetState()
                    }
                    is AuthState.NeedsPhone -> {
                        navController.navigate(Screen.Phone.createRoute(true))
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }

            WelcomeScreen(
                authState = authState,
                onSkipClick = { sessionViewModel.continueWithoutAccount() },
                onPhoneClick = { navController.navigate(Screen.Onboarding.route) },
                onGoogleClick = {
                    scope.launch {
                        try {
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
                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                authViewModel.loginWithGoogle(idToken)
                            }
                        } catch (_: GetCredentialCancellationException) {
                        } catch (_: NoCredentialException) {
                        } catch (e: Exception) {
                            Log.e("GoogleAuth", "Error: ${e.message}", e)
                        }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Phone.createRoute(false)) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Phone.route,
            arguments = listOf(
                navArgument("fromSession") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            val fromSession = entry.arguments?.getBoolean("fromSession") ?: false
            val authViewModel: AuthViewModel = hiltViewModel()
            PhoneScreen(
                authViewModel = authViewModel,
                onBack = {
                    if (fromSession) sessionViewModel.forceLogout()
                    else navController.popBackStack()
                },
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
                onSuccess = { sessionViewModel.refreshSession() }
            )
        }

        composable(Screen.NameEntry.route) {
            NameEntryScreen(onContinue = { sessionViewModel.refreshSession() })
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(onSuccess = { sessionViewModel.refreshSession() })
        }

        composable(Screen.DriverSetup.route) {
            DriverSetupScreen(onComplete = { sessionViewModel.refreshSession() })
        }

        composable(Screen.Map.route) {
            MapScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToColorSettings = { navController.navigate(Screen.ColorSettings.route) },
                onNavigateToWallet = { navController.navigate(Screen.Wallet.route) },
                onLogout = {
                    sessionViewModel.forceLogout()
                }
            )
        }

        composable(Screen.DriverHome.route) {
            DriverHomeScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToColorSettings = { navController.navigate(Screen.ColorSettings.route) },
                onNavigateToWallet = { navController.navigate(Screen.Wallet.route) },
                onLogout = { sessionViewModel.forceLogout() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = sharedProfileViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ColorSettings.route) {
            ColorWheelSettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }

        composable(Screen.Wallet.route) {
            WalletScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun routesMatch(current: String?, dest: String): Boolean {
    if (current == null) return false
    val cBase = current.substringBefore('?')
    val dBase = dest.substringBefore('?')
    if (cBase != dBase) return false
    if (!dest.contains('?')) return true
    return current == dest
}

private fun sessionToRoute(state: SessionUiState): String? = when (state) {
    SessionUiState.Loading -> null
    SessionUiState.BrowsingGuest -> Screen.Map.route
    SessionUiState.Unauthenticated -> Screen.Welcome.route
    SessionUiState.NeedsPhone -> Screen.Phone.createRoute(true)
    SessionUiState.NeedsName -> Screen.NameEntry.route
    SessionUiState.NeedsRole -> Screen.RoleSelection.route
    SessionUiState.NeedsDriverSetup -> Screen.DriverSetup.route
    is SessionUiState.Authenticated ->
        if (state.user.role == "driver") Screen.DriverHome.route else Screen.Map.route
}
