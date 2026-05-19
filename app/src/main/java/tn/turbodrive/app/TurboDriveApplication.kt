package tn.turbodrive.app

import android.app.Application
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.errors.InstantiationErrorException
import com.turbodrive.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import tn.turbodrive.core.diagnostics.BootDiagnostics
import tn.turbodrive.core.diagnostics.CrashReporting
import tn.turbodrive.core.logging.AppLogger
import tn.turbodrive.data.socket.SocketLifecycleController
import tn.turbodrive.data.storage.LanguagePreferenceStore
import tn.turbodrive.map.HereOfflineMapsManager
import javax.inject.Inject

@HiltAndroidApp
class TurboDriveApplication : Application() {
    @Inject lateinit var languagePreferenceStore: LanguagePreferenceStore

    @Inject lateinit var crashReporting: CrashReporting

    @Inject lateinit var socketLifecycleController: SocketLifecycleController

    @Inject lateinit var appProcessLifecycleBridge: AppProcessLifecycleBridge

    override fun onCreate() {
        super.onCreate()
        crashReporting.initialize()
        socketLifecycleController.register()
        appProcessLifecycleBridge.ensureRegistered()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        BootDiagnostics.installGlobalCrashLogger(this)
        BootDiagnostics.step("Application", "onCreate after super")
        try {
            languagePreferenceStore.syncApplicationLocalesWithStoredOrDeviceDefaults()
            BootDiagnostics.step("Application", "locales OK")
        } catch (e: Exception) {
            AppLogger.e("syncApplicationLocales failed: ${e.message}", e)
            BootDiagnostics.step("Application", "locales FAILED: ${e.message}")
        }
        initHereSdk()
        BootDiagnostics.step("Application", "onCreate end")
    }

    private fun initHereSdk() {
        val accessKeyId = BuildConfig.HERE_ACCESS_KEY_ID
        val accessKeySecret = BuildConfig.HERE_ACCESS_KEY_SECRET

        if (accessKeyId.isBlank() || accessKeySecret.isBlank()) {
            AppLogger.e("HereSDK clés HERE vides — vérifier local.properties + build.gradle")
            return
        }

        try {
            val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options = HereOfflineMapsManager.createSdkOptions(this, authMode)
            SDKNativeEngine.makeSharedInstance(this, options)
            AppLogger.i("HereSDK initialisé")
        } catch (e: InstantiationErrorException) {
            AppLogger.e("HereSDK erreur d'initialisation: ${e.message}", e)
        } catch (e: Throwable) {
            AppLogger.e("HereSDK ${e.javaClass.simpleName}: ${e.message}", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        SDKNativeEngine.getSharedInstance()?.dispose()
        SDKNativeEngine.setSharedInstance(null)
    }
}
