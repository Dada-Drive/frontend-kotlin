package tn.dadadrive.app

import android.app.Application
import com.dadadrive.BuildConfig
import tn.dadadrive.core.diagnostics.BootDiagnostics
import tn.dadadrive.core.diagnostics.CrashReporting
import tn.dadadrive.core.logging.AppLogger
import tn.dadadrive.data.socket.SocketLifecycleController
import tn.dadadrive.data.storage.LanguagePreferenceStore
import tn.dadadrive.map.HereOfflineMapsManager
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.errors.InstantiationErrorException
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class DadaDriveApplication : Application() {

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
        val accessKeyId     = BuildConfig.HERE_ACCESS_KEY_ID
        val accessKeySecret = BuildConfig.HERE_ACCESS_KEY_SECRET

        AppLogger.d("HereSDK Key ID length: ${accessKeyId.length}")
        AppLogger.d("HereSDK Key Secret length: ${accessKeySecret.length}")

        if (accessKeyId.isBlank() || accessKeySecret.isBlank()) {
            AppLogger.e("HereSDK clés HERE vides — vérifier local.properties + build.gradle")
            return
        }

        try {
            val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options  = HereOfflineMapsManager.createSdkOptions(this, authMode)
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