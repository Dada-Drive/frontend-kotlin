package tn.turbodrive.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Regroupe les actions « app en arrière-plan » (§9) : enregistrement par écran, exécution sur [ON_STOP].
 */
@Singleton
class AppProcessLifecycleBridge
    @Inject
    constructor() : DefaultLifecycleObserver {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val actions = Collections.synchronizedList(mutableListOf<suspend () -> Unit>())

        private val registered = java.util.concurrent.atomic.AtomicBoolean(false)

        fun ensureRegistered() {
            if (registered.getAndSet(true)) return
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }

        fun register(action: suspend () -> Unit): () -> Unit {
            synchronized(actions) { actions.add(action) }
            return {
                synchronized(actions) { actions.remove(action) }
            }
        }

        override fun onStop(owner: LifecycleOwner) {
            val snapshot: List<suspend () -> Unit>
            synchronized(actions) { snapshot = actions.toList() }
            for (block in snapshot) {
                scope.launch {
                    runCatching { block() }
                }
            }
        }
    }
