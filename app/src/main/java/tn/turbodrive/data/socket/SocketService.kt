package tn.turbodrive.data.socket

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tn.turbodrive.core.diagnostics.CrashReporting
import tn.turbodrive.domain.models.Role
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService
    @Inject
    constructor(
        private val crashReporting: CrashReporting,
        private val decoder: SocketEventDecoder,
    ) {
        private val lock = Any()
        private var socket: Socket? = null
        private var lastToken: String? = null
        private var lastRole: Role? = null

        private val _events =
            MutableSharedFlow<SocketEvent>(
                replay = 0,
                extraBufferCapacity = EVENT_BUFFER_CAPACITY,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

        /**
         * Typed flow of all Socket.IO events emitted by the backend, plus
         * synthetic [SocketEvent.Connected] / [SocketEvent.Disconnected].
         *
         * - `replay = 0` — late subscribers don't receive past events.
         * - `extraBufferCapacity = 64` — slow collectors get an absorbing buffer.
         * - `DROP_OLDEST` — under sustained pressure, recent events win.
         */
        val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

        val isConnected: Boolean
            get() = synchronized(lock) { socket?.connected() == true }

        /**
         * Connect to the backend Socket.IO server.
         *
         * The namespace is derived from [role]:
         * - [Role.RIDER] → `/riders`
         * - [Role.DRIVER] → `/drivers`
         *
         * Re-connecting tears down any prior socket first (idempotent).
         */
        fun connect(
            token: String,
            role: Role,
        ) {
            synchronized(lock) {
                lastToken = token
                lastRole = role
                disconnectLocked()
                val opts =
                    IO.Options().apply {
                        auth = mapOf("token" to token)
                        transports = arrayOf("websocket", "polling")
                        reconnection = true
                        reconnectionDelay = RECONNECTION_DELAY_MS
                        reconnectionDelayMax = RECONNECTION_DELAY_MAX_MS
                    }
                val url = SocketUrl.build(role.namespace())
                val s = IO.socket(url, opts)
                wireLifecycleHandlers(s)
                wireBusinessHandlers(s)
                s.connect()
                socket = s
            }
        }

        fun disconnect() {
            synchronized(lock) {
                lastToken = null
                lastRole = null
                disconnectLocked()
            }
        }

        fun onAppBackgrounded() {
            synchronized(lock) {
                disconnectLocked()
            }
        }

        fun onAppForegrounded() {
            val token = lastToken ?: return
            val role = lastRole ?: return
            connect(token, role)
        }

        /**
         * Emit a raw event to the backend (e.g. for negotiation use cases
         * landing in Session B R-3.4). [payload] is a JSON string.
         */
        fun emit(
            eventName: String,
            payload: String,
        ) {
            synchronized(lock) {
                socket?.emit(eventName, payload)
            }
        }

        private fun wireLifecycleHandlers(s: Socket) {
            s.on(Socket.EVENT_CONNECT) {
                _events.tryEmit(SocketEvent.Connected)
            }
            s.on(Socket.EVENT_DISCONNECT) {
                _events.tryEmit(SocketEvent.Disconnected)
            }
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val msg = args.firstOrNull()?.toString().orEmpty()
                crashReporting.recordSocketConnectionFailure(msg.take(CONNECT_ERROR_MSG_LIMIT))
            }
        }

        private fun wireBusinessHandlers(s: Socket) {
            BUSINESS_EVENT_NAMES.forEach { name ->
                s.on(name) { args ->
                    val raw = args.firstOrNull()?.toString().orEmpty()
                    val event = decoder.decode(name, raw)
                    if (event != null) {
                        _events.tryEmit(event)
                    }
                }
            }
        }

        private fun disconnectLocked() {
            runCatching {
                socket?.off()
                socket?.disconnect()
            }
            socket = null
        }

        private fun Role.namespace(): String =
            when (this) {
                Role.RIDER -> "/riders"
                Role.DRIVER -> "/drivers"
            }

        private companion object {
            const val EVENT_BUFFER_CAPACITY = 64
            const val RECONNECTION_DELAY_MS = 1_000L
            const val RECONNECTION_DELAY_MAX_MS = 30_000L
            const val CONNECT_ERROR_MSG_LIMIT = 200

            val BUSINESS_EVENT_NAMES =
                listOf(
                    "ride:new_request",
                    "ride:new_offer",
                    "ride:accepted",
                    "ride:offer_rejected",
                    "ride:driver_arrived",
                    "ride:status_changed",
                    "ride:completed",
                    "ride:cancelled",
                    "ride:driver_location",
                    "negotiate:propose",
                    "negotiate:accept",
                    "negotiate:counter",
                    "negotiate:reject",
                    "wallet:topup_confirmed",
                    "wallet:transaction_new",
                    "notification:new",
                    "shared:passenger_joined",
                    "shared:passenger_left",
                    "shared:passenger_picked_up",
                    "shared:passenger_dropped_off",
                    "shared:ride_completed",
                )
        }
    }
