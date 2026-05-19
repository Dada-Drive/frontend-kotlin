package tn.turbodrive.data.socket

import kotlinx.coroutines.flow.SharedFlow
import tn.turbodrive.domain.models.Role
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point for app code (ViewModels, use cases) to interact
 * with the Socket.IO channel.
 *
 * Forwards the typed event flow and the connect/disconnect/emit lifecycle
 * from [SocketService]. The indirection lets future enrichment (event
 * filtering, derived flows per VM, replay caches for crash recovery in
 * R-3.5) land here without touching call sites.
 */
@Singleton
class SocketEventManager
    @Inject
    constructor(
        private val socketService: SocketService,
    ) {
        /** Typed flow of all backend Socket.IO events + synthetic lifecycle. */
        val events: SharedFlow<SocketEvent> = socketService.events

        val isConnected: Boolean
            get() = socketService.isConnected

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
