package com.dadadrive

import android.app.Application
import android.util.Log
import com.here.sdk.core.engine.AuthenticationMode
import com.here.sdk.core.engine.SDKNativeEngine
import com.here.sdk.core.engine.SDKOptions
import com.here.sdk.core.errors.InstantiationErrorException
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DadaDriveApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialisation HERE Maps SDK
        initHereSdk()
        // Cloudinary : upload via OkHttp dans CloudinaryManager (pas de SDK cloudinary-android = moins de mémoire / APK).
    }

    private fun initHereSdk() {
        try {
            // Création du mode d'authentification avec la clé API HERE
            val accessKeyId = BuildConfig.HERE_ACCESS_KEY_ID
            val accessKeySecret = BuildConfig.HERE_ACCESS_KEY_SECRET
            val authMode = AuthenticationMode.withKeySecret(
                accessKeyId,
                accessKeySecret
            )
            val options = SDKOptions(authMode)
            SDKNativeEngine.makeSharedInstance(this, options)
        } catch (e: InstantiationErrorException) {
            Log.e("HereSDK", "Erreur d'initialisation HERE SDK: ${e.message}")
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        SDKNativeEngine.getSharedInstance()?.dispose()
        SDKNativeEngine.setSharedInstance(null)
    }
}
