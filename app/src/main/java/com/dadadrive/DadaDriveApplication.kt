package com.dadadrive

import android.app.Application
import android.util.Log
import com.dadadrive.data.local.LanguagePreferenceStore
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DadaDriveApplication : Application() {

    @Inject lateinit var languagePreferenceStore: LanguagePreferenceStore

    override fun onCreate() {
        super.onCreate()
        // Équiv. Swift : LanguageManager + .environment(\\.locale, …) au cold start
        languagePreferenceStore.syncApplicationLocalesWithStoredOrDeviceDefaults()
        initHereSdk()
    }

    private fun initHereSdk() {
        val accessKeyId     = BuildConfig.HERE_ACCESS_KEY_ID
        val accessKeySecret = BuildConfig.HERE_ACCESS_KEY_SECRET

        // ✅ Vérification que les clés sont bien lues
        Log.d("HereSDK", "Key ID length     : ${accessKeyId.length}")
        Log.d("HereSDK", "Key Secret length : ${accessKeySecret.length}")

        if (accessKeyId.isBlank() || accessKeySecret.isBlank()) {
            Log.e("HereSDK", "❌ Clés HERE vides ! Vérifie local.properties + build.gradle")
            return
        }

        try {
            val authMode = AuthenticationMode.withKeySecret(accessKeyId, accessKeySecret)
            val options  = SDKOptions(authMode)
            SDKNativeEngine.makeSharedInstance(this, options)
            Log.i("HereSDK", "✅ HERE SDK initialisé avec succès")
        } catch (e: InstantiationErrorException) {
            Log.e("HereSDK", "❌ Erreur d'initialisation HERE SDK: ${e.message}")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        SDKNativeEngine.getSharedInstance()?.dispose()
        SDKNativeEngine.setSharedInstance(null)
    }
}