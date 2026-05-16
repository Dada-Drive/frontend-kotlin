package tn.dadadrive.presentation.navigation

import android.net.Uri
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Route interne (ex. [AppRoute.Wallet.route]) à ouvrir une fois la session [Authenticated].
 */
@Singleton
class DeepLinkQueue @Inject constructor() {

    private val pending = AtomicReference<String?>(null)

    fun enqueueFromUri(uri: Uri?) {
        if (uri == null) return
        when {
            uri.scheme == "turbodrive" && uri.host == "wallet" -> enqueue(AppRoute.Wallet.route)
            else -> { }
        }
    }

    fun enqueue(route: String) {
        pending.set(route)
    }

    fun consumePending(): String? = pending.getAndSet(null)
}
