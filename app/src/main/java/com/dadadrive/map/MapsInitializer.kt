package com.dadadrive.map

import android.content.Context

/**
 * Le SDK HERE est déjà initialisé dans `DadaDriveApplication` via `SDKNativeEngine.makeSharedInstance`.
 * Ne pas instancier de `MapView` HERE détaché ici : sans largeur/hauteur, le SDK logue
 * `hsdk-WatermarkProcessor: Map view width or height is not set`.
 */
object MapsInitializer {

    @Suppress("UNUSED_PARAMETER")
    fun initialize(applicationContext: Context) {
        // Intentionnellement vide — voir commentaire fichier.
    }
}
