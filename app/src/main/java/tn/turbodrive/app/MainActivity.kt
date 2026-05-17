// Équivalent Swift : Presentation/AppCoordinatorView.swift + AuthCoordinatorView (NavHost)
package tn.turbodrive.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import tn.turbodrive.core.diagnostics.BootDiagnostics
import tn.turbodrive.core.theme.FontScaleViewModel
import tn.turbodrive.core.theme.ThemeViewModel
import tn.turbodrive.core.theme.TurboDriveTheme
import tn.turbodrive.data.storage.AppPreferences
import tn.turbodrive.map.MapsInitializer
import tn.turbodrive.presentation.navigation.AppNavHost
import tn.turbodrive.presentation.navigation.DeepLinkQueue
import tn.turbodrive.presentation.notifications.DriverNotificationBridge
import tn.turbodrive.presentation.splash.SessionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var deepLinkQueue: DeepLinkQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        BootDiagnostics.step("MainActivity", "onCreate start")
        super.onCreate(savedInstanceState)
        DriverNotificationBridge.publishFromIntent(intent)
        deepLinkQueue.enqueueFromUri(intent?.data)
        BootDiagnostics.step("MainActivity", "after super.onCreate, appPreferences injected = ${::appPreferences.isInitialized}")

        try {
            MapsInitializer.initialize(applicationContext)
            Log.d(TAG_HERE_MAPS, "HERE Maps SDK pre-initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG_HERE_MAPS, "HERE Maps SDK pre-initialization failed: ${e.message}", e)
        }

        enableEdgeToEdge()
        BootDiagnostics.step("MainActivity", "setContent { … } start")
        setContent {
            val configuration = LocalConfiguration.current
            key(configuration.locales.toLanguageTags()) {
                val sessionViewModel: SessionViewModel = hiltViewModel()
                BootDiagnostics.step("MainActivity", "SessionViewModel obtained")
                val themeViewModel: ThemeViewModel = hiltViewModel()
                BootDiagnostics.step("MainActivity", "ThemeViewModel obtained")
                val fontScaleViewModel: FontScaleViewModel = hiltViewModel()
                val currentTheme by themeViewModel.currentTheme.collectAsState()
                val customSecondaryArgb by themeViewModel.customSecondaryArgb.collectAsState()
                val fontScale by fontScaleViewModel.scaleFactor.collectAsState()
                val darkTheme = isSystemInDarkTheme()
                val appColors =
                    remember(currentTheme, darkTheme, customSecondaryArgb) {
                        val secondaryOverride = customSecondaryArgb?.let { argb -> Color(argb) }
                        currentTheme.resolveScheme(darkTheme, secondaryOverride)
                    }

                TurboDriveTheme(
                    darkTheme = darkTheme,
                    appColors = appColors,
                    fontScale = fontScale,
                ) {
                    AppNavHost(
                        appPreferences = appPreferences,
                        sessionViewModel = sessionViewModel,
                        themeViewModel = themeViewModel,
                        deepLinkQueue = deepLinkQueue,
                    )
                }
            }
        }
        BootDiagnostics.step("MainActivity", "onCreate end (setContent schedulé)")
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        DriverNotificationBridge.publishFromIntent(intent)
        deepLinkQueue.enqueueFromUri(intent.data)
    }

    private companion object {
        private const val TAG_HERE_MAPS = "HereMaps"
    }
}
