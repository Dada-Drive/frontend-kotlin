package tn.turbodrive.data.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import tn.turbodrive.di.ApplicationScope
import tn.turbodrive.domain.models.Role
import tn.turbodrive.domain.usecases.ResyncOnReconnectUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for app code (ViewModels, use cases) to interact
 * with the Socket.IO channel.
 *
 * Forwards the typed event flow and the connect/disconnect/emit lifecycle
 * from [SocketService]. On every [SocketEvent.Connected] (reconnect), fires
 * [ResyncOnReconnectUseCase] in [appScope] and then emits
 * [SocketEvent.ResyncCompleted] so consumers can refresh stale state.
 */
@Singleton
class SocketEventManager
    @Inject
    constructor(
        private val socketService: SocketService,
        private val resyncUseCase: ResyncOnReconnectUseCase,
        @ApplicationScope private val appScope: CoroutineScope,
    ) {
        /** Typed flow of all backend Socket.IO events + synthetic lifecycle. */
        val events: SharedFlow<SocketEvent> = socketService.events

        val isConnected: Boolean
            get() = socketService.isConnected

        init {
            appScope.launch {
                socketService.events.collect { event ->
                    if (event is SocketEvent.Connected) {
                        runCatching { resyncUseCase() }
                            .onSuccess { socketService.emitInternalSync(SocketEvent.ResyncCompleted) }
                    }
                }
            }
        }

        /** Connect using [role] (namespace `/riders` or `/drivers`). Idempotent. */
        fun connect(
            token: String,
            role: Role,
        ) {
            socketService.connect(token, role)
        }

        fun disconnect() {
            socketService.disconnect()
        }

        /** Emit an outbound event (e.g. for negotiation in R-3.4, Session B). */
        fun emit(
            eventName: String,
            payload: String,
        ) {
            socketService.emit(eventName, payload)
        }
    }
