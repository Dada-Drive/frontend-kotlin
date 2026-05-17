package tn.turbodrive.data.socket

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketLifecycleController
    @Inject
    constructor(
        private val socketService: SocketService,
    ) : DefaultLifecycleObserver {
        private val registered = AtomicBoolean(false)

        fun register() {
            if (registered.getAndSet(true)) return
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        override fun onStop(owner: LifecycleOwner) {
            socketService.onAppBackgrounded()
        }

        override fun onStart(owner: LifecycleOwner) {
            socketService.onAppForegrounded()
        }
    }
