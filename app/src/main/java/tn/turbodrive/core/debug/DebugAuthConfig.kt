package tn.turbodrive.core.debug

import com.turbodrive.BuildConfig
import tn.turbodrive.domain.models.User

/**
 * Données de test pour le flux « Ignorer » sur l’écran d’accueil.
 * Désactivez [INJECT_STATIC_USER_ON_WELCOME_SKIP] ou compilez en release pour ne plus l’utiliser.
 */
object DebugAuthConfig {
    const val INJECT_STATIC_USER_ON_WELCOME_SKIP: Boolean = true

    val staticSkipUser =
        User(
            id = "debug-static-rider",
            fullName = "Utilisateur test",
            email = "test.local@turbodrive.app",
            phoneNumber = "+21612345678",
            role = "rider",
            profilePictureUri = null,
        )

    fun shouldInjectStaticUserOnWelcomeSkip(): Boolean = BuildConfig.DEBUG && INJECT_STATIC_USER_ON_WELCOME_SKIP
}
