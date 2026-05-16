package tn.dadadrive.presentation.navigation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import tn.dadadrive.core.constants.Constants
import tn.dadadrive.core.diagnostics.BootDiagnostics
import tn.dadadrive.data.storage.AppPreferences
import tn.dadadrive.presentation.auth.AuthState
import tn.dadadrive.presentation.auth.AuthViewModel
import tn.dadadrive.presentation.auth.NameEntryScreen
import tn.dadadrive.presentation.auth.PhoneScreen
import tn.dadadrive.presentation.auth.WelcomeScreen
import tn.dadadrive.presentation.driverhome.DriverHomeScreen
import tn.dadadrive.presentation.driversetup.DriverAccessSetupScreen
import tn.dadadrive.presentation.driversetup.DriverPhoneSettingsScreen
import tn.dadadrive.presentation.driversetup.DriverSetupScreen
import tn.dadadrive.presentation.driversetup.DriverTaxiLicenseUploadScreen
import tn.dadadrive.presentation.driversetup.DriverVerificationPendingScreen
import tn.dadadrive.presentation.map.MapScreen
import tn.dadadrive.presentation.onboarding.OnboardingScreen
import tn.dadadrive.presentation.profile.EditProfileScreen
import tn.dadadrive.presentation.profile.ProfileViewModel
import tn.dadadrive.presentation.role.RoleSelectionScreen
import tn.dadadrive.presentation.session.SessionState
import tn.dadadrive.presentation.settings.ColorWheelSettingsScreen
import tn.dadadrive.presentation.splash.SessionViewModel
import tn.dadadrive.presentation.splash.SplashScreen
import tn.dadadrive.presentation.wallet.WalletScreen
import tn.dadadrive.core.theme.ThemeViewModel
import com.dadadrive.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes

@Composable
fun AppNavHost(
    appPreferences: AppPreferences,
    sessionViewModel: SessionViewModel,
    themeViewModel: ThemeViewModel,
    deepLinkQueue: DeepLinkQueue,
) {
    val navController = rememberNavController()
    var splashDone by remember { mutableStateOf(false) }
    var introComplete by remember { mutableStateOf<Boolean?>(null) }
    val sessionState by sessionViewModel.sessionState.collectAsState()
    val preferPassengerWhileDriverPending by sessionViewModel.preferPassengerMapWhileDriverPending.collectAsState()
    val sharedProfileViewModel: ProfileViewModel = hiltViewModel()

    DisposableEffect(sessionViewModel) {
        val owner = ProcessLifecycleOwner.get()
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                sessionViewModel.onAppForegrounded()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        BootDiagnostics.step("AppNavHost", "NavHost + ProfileViewModel (first composition)")
        introComplete = appPreferences.readIntroOnboardingComplete()
    }

    LaunchedEffect(splashDone, sessionState, introComplete, preferPassengerWhileDriverPending) {
        if (!splashDone) return@LaunchedEffect
        if (sessionState is SessionState.Loading) return@LaunchedEffect
        val intro = introComplete ?: return@LaunchedEffect
        val dest = SessionNavigationMapper.destination(
            state = sessionState,
            introOnboardingComplete = intro,
            preferPassengerMapWhileDriverPending = preferPassengerWhileDriverPending,
        ) ?: return@LaunchedEffect
        val current = navController.currentDestination?.route
        if (routesMatch(current, dest)) return@LaunchedEffect
        navController.navigate(dest) {
            popUpTo(AppRoute.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    LaunchedEffect(sessionState) {
        if (sessionState !is SessionState.Authenticated) return@LaunchedEffect
        val pending = deepLinkQueue.consumePending() ?: return@LaunchedEffect
        navController.navigate(pending) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
    ) {
        composable(AppRoute.Splash.route) {
            SplashScreen(onSplashFinished = { splashDone = true })
        }

        composable(AppRoute.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.authState.collectAsState()
            val googleCooldownSeconds by authViewModel.googleCooldown.collectAsState()
            val context = LocalContext.current

            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                if (result.resultCode != Activity.RESULT_OK || result.data == null) {
                    return@rememberLauncherForActivityResult
                }
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    if (idToken != null) {
                        authViewModel.loginWithGoogle(idToken)
                    } else {
                        authViewModel.reportGoogleCredentialError(
                            context.getString(R.string.welcome_google_error_no_id_token),
                        )
                    }
                } catch (e: ApiException) {
                    if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                        return@rememberLauncherForActivityResult
                    }
                    Log.e("GoogleAuth", "GoogleSignIn ApiException code=${e.statusCode}", e)
                    authViewModel.reportGoogleCredentialError(
                        context.getString(R.string.welcome_google_error_unknown),
                    )
                }
            }

            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Success -> {
                        sessionViewModel.refreshSession()
                        authViewModel.resetState()
                    }
                    is AuthState.NeedsPhone -> {
                        navController.navigate(AppRoute.Phone.createRoute(true))
                        authViewModel.resetState()
                    }
                    else -> {}
                }
            }

            WelcomeScreen(
                authState = authState,
                googleCooldownSeconds = googleCooldownSeconds,
                onSkipClick = { sessionViewModel.continueWithoutAccount() },
                onPhoneClick = { navController.navigate(AppRoute.Phone.createRoute(false)) },
                onFacebookClick = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.welcome_facebook_soon),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                onGoogleClick = {
                    if (googleCooldownSeconds > 0) return@WelcomeScreen
                    val webClientId = Constants.GOOGLE_WEB_CLIENT_ID
                    if (webClientId.isBlank()) {
                        authViewModel.reportGoogleCredentialError(
                            context.getString(R.string.welcome_google_error_missing_web_client_id),
                        )
                        return@WelcomeScreen
                    }
                    val activity = context as? ComponentActivity
                    if (activity == null) {
                        Log.e("GoogleAuth", "Context is not a ComponentActivity")
                        authViewModel.reportGoogleCredentialError(
                            context.getString(R.string.welcome_google_error_internal),
                        )
                        return@WelcomeScreen
                    }
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build()
                    val client = GoogleSignIn.getClient(activity, gso)
                    googleSignInLauncher.launch(client.signInIntent)
                },
            )
        }

        composable(AppRoute.Onboarding.route) {
            val scope = rememberCoroutineScope()
            OnboardingScreen(
                onCompleteIntro = {
                    scope.launch {
                        appPreferences.setIntroOnboardingComplete(true)
                        introComplete = true
                        navController.navigate(AppRoute.Welcome.route) {
                            popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = AppRoute.Phone.route,
            arguments = listOf(
                navArgument("fromSession") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { entry ->
            val fromSession = entry.arguments?.getBoolean("fromSession") ?: false
            val authViewModel: AuthViewModel = hiltViewModel()
            PhoneScreen(
                authViewModel = authViewModel,
                onBack = {
                    if (fromSession) sessionViewModel.forceLogout()
                    else navController.popBackStack()
                },
                onOtpVerified = { sessionViewModel.refreshSession() },
            )
        }

        composable(AppRoute.NameEntry.route) {
            NameEntryScreen(
                onContinue = { sessionViewModel.refreshSession() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.RoleSelection.route) {
            RoleSelectionScreen(
                onSuccess = { sessionViewModel.refreshSession() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.DriverSetup.route) {
            DriverSetupScreen(
                onComplete = { navController.navigate(AppRoute.DriverAccessSetup.route) },
                onBack = { sessionViewModel.forceLogout() },
            )
        }

        composable(AppRoute.DriverAccessSetup.route) {
            DriverAccessSetupScreen(
                onClose = { sessionViewModel.forceLogout() },
                onGoToSettingsFlow = { navController.navigate(AppRoute.DriverPhoneSettings.route) },
                onOpenTaxiLicenseUpload = { navController.navigate(AppRoute.DriverTaxiLicenseUpload.route) },
            )
        }

        composable(AppRoute.DriverTaxiLicenseUpload.route) {
            DriverTaxiLicenseUploadScreen(
                onClose = { sessionViewModel.forceLogout() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.DriverPhoneSettings.route) {
            DriverPhoneSettingsScreen(
                onClose = { sessionViewModel.forceLogout() },
                onDone = { navController.navigate(AppRoute.DriverVerificationPending.route) },
            )
        }

        composable(AppRoute.DriverVerificationPending.route) {
            DriverVerificationPendingScreen(
                onClose = {
                    sessionViewModel.preferPassengerBrowseWhileDriverPending()
                },
                onContinueAsPassenger = {
                    sessionViewModel.preferPassengerBrowseWhileDriverPending()
                },
                onBackToHome = {
                    sessionViewModel.preferPassengerBrowseWhileDriverPending()
                },
            )
        }

        composable(AppRoute.Map.route) {
            MapScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = { navController.navigate(AppRoute.EditProfile.route) },
                onNavigateToColorSettings = { navController.navigate(AppRoute.ColorSettings.route) },
                onNavigateToWallet = { navController.navigate(AppRoute.Wallet.route) },
                onNavigateToSignup = {
                    navController.navigate(AppRoute.Welcome.route)
                },
                onLogout = {
                    sessionViewModel.forceLogout()
                },
            )
        }

        composable(AppRoute.DriverHome.route) {
            DriverHomeScreen(
                profileViewModel = sharedProfileViewModel,
                onNavigateToEditProfile = { navController.navigate(AppRoute.EditProfile.route) },
                onNavigateToColorSettings = { navController.navigate(AppRoute.ColorSettings.route) },
                onNavigateToWallet = { navController.navigate(AppRoute.Wallet.route) },
                onLogout = { sessionViewModel.forceLogout() },
            )
        }

        composable(AppRoute.EditProfile.route) {
            EditProfileScreen(
                viewModel = sharedProfileViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(AppRoute.ColorSettings.route) {
            ColorWheelSettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel,
            )
        }

        composable(AppRoute.Wallet.route) {
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
