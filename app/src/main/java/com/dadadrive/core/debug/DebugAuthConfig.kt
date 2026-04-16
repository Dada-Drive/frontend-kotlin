package com.dadadrive.core.debug

import com.dadadrive.BuildConfig
import com.dadadrive.domain.model.User

/**
 * Données de test pour le flux « Ignorer » sur l’écran d’accueil.
 * Désactivez [injectStaticUserOnWelcomeSkip] ou compilez en release pour ne plus l’utiliser.
 */
object DebugAuthConfig {
    const val injectStaticUserOnWelcomeSkip: Boolean = true

    val staticSkipUser = User(
        id = "debug-static-rider",
        fullName = "Utilisateur test",
        email = "test.local@dadadrive.app",
        phoneNumber = "+21612345678",
        role = "rider",
        profilePictureUri = null
    )

    fun shouldInjectStaticUserOnWelcomeSkip(): Boolean =
        BuildConfig.DEBUG && injectStaticUserOnWelcomeSkip
}
