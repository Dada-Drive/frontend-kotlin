package tn.dadadrive.data.socket

import io.socket.client.IO
import io.socket.client.Socket
import tn.dadadrive.core.diagnostics.CrashReporting
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService @Inject constructor(
    private val crashReporting: CrashReporting,
) {

    private val lock = Any()
    private var socket: Socket? = null
    private var lastToken: String? = null
    private var lastNamespace: String? = null

    val isConnected: Boolean
        get() = synchronized(lock) { socket?.connected() == true }

    fun connect(token: String, namespace: String) {
        synchronized(lock) {
            lastToken = token
            lastNamespace = namespace
            disconnectLocked()
            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                transports = arrayOf("websocket", "polling")
                reconnection = true
                reconnectionDelay = 1_000
                reconnectionDelayMax = 30_000
            }
            val url = SocketUrl.build(namespace)
            val s = IO.socket(url, opts)
            s.on(Socket.EVENT_CONNECT) { }
            s.on(Socket.EVENT_DISCONNECT) { }
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val msg = args.firstOrNull()?.toString().orEmpty()
                crashReporting.recordSocketConnectionFailure(msg.take(200))
            }
            s.connect()
            socket = s
        }
    }

    fun disconnect() {
        synchronized(lock) {
            lastToken = null
            lastNamespace = null
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
        val ns = lastNamespace ?: return
        connect(token, ns)
    }

    private fun disconnectLocked() {
        runCatching {
            socket?.off()
            socket?.disconnect()
        }
        socket = null
    }
}
