package tn.turbodrive.presentation.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tn.turbodrive.data.network.api.NotificationApiService
import tn.turbodrive.data.network.envelope.unwrap
import tn.turbodrive.data.network.model.NotificationTokenRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRegistrar
    @Inject
    constructor(
        private val notificationApiService: NotificationApiService,
    ) {
        fun registerCurrentToken(scope: CoroutineScope) {
            runCatching {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener { token ->
                        if (token.isBlank()) return@addOnSuccessListener
                        registerProvidedToken(scope, token)
                    }
                    .addOnFailureListener { err ->
                        Log.w(TAG, "Unable to fetch FCM token: ${err.message}")
                    }
            }.onFailure { err ->
                Log.w(TAG, "Firebase Messaging unavailable: ${err.message}")
            }
        }

        fun registerProvidedToken(
            scope: CoroutineScope,
            token: String,
        ) {
            if (token.isBlank()) return
            scope.launch {
                runCatching {
                    notificationApiService
                        .saveToken(NotificationTokenRequest(token = token))
                        .unwrap()
                        .getOrThrow()
                    Log.i(TAG, "FCM token saved on backend")
                }.onFailure { err ->
                    Log.w(TAG, "Unable to save FCM token: ${err.message}")
                }
            }
        }

        suspend fun unregisterToken() {
            runCatching { notificationApiService.removeToken().unwrap().getOrThrow() }
                .onFailure { err -> Log.w(TAG, "Unable to remove FCM token: ${err.message}") }
        }

        private companion object {
            private const val TAG = "PushTokenRegistrar"
        }
    }
