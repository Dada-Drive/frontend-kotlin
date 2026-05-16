// Équivalent Swift : Presentation/AppCoordinatorView.swift + AuthCoordinatorView (NavHost)
package tn.dadadrive.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import tn.dadadrive.core.diagnostics.BootDiagnostics
import tn.dadadrive.presentation.navigation.AppNavHost
import tn.dadadrive.presentation.navigation.DeepLinkQueue
import tn.dadadrive.presentation.notifications.DriverNotificationBridge
import tn.dadadrive.data.storage.AppPreferences
import tn.dadadrive.map.MapsInitializer
import tn.dadadrive.presentation.splash.SessionViewModel
import tn.dadadrive.core.theme.DadaDriveTheme
import tn.dadadrive.core.theme.FontScaleViewModel
import tn.dadadrive.core.theme.ThemeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
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
                val appColors = remember(currentTheme, darkTheme, customSecondaryArgb) {
                    val secondaryOverride = customSecondaryArgb?.let { argb -> Color(argb) }
                    currentTheme.resolveScheme(darkTheme, secondaryOverride)
                }

                DadaDriveTheme(
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
