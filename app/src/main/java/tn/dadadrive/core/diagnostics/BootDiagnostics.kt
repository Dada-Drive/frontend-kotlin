package tn.dadadrive.core.diagnostics

import android.app.Application
import android.util.Log
/**
 * Traces d'amorçage : filtrer Logcat par **DADADRIVE_BOOT** ou **DADADRIVE_CRASH**.
 * Schéma du cold start (ordre cible) :
 * 1. Application.attachBaseContext (non loggé ici)
 * 2. Hilt → Application.onCreate
 * 3. DadaDriveApplication: locales, HERE SDK
 * 4. MainActivity.onCreate → setContent
 * 5. Composables: hiltViewModel (Session, Theme) puis NavHost
 * 6. Première frame + Splash
 *
 * Dernière étape affichée avant un crash = zone à inspecter.
 */
object BootDiagnostics {

    const val TAG_BOOT = "DADADRIVE_BOOT"
    const val TAG_CRASH = "DADADRIVE_CRASH"

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    @Volatile
    var lastBootStep: String = "not_started"
        private set

    /**
     * À appeler en début d’[Application.onCreate] (juste après [super]).
     */
    fun installGlobalCrashLogger(application: Application) {
        if (defaultHandler != null) return
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e(
                TAG_CRASH,
                "FATAL on thread ${thread.name} — $lastBootStep",
                exception
            )
            Log.e(
                TAG_CRASH,
                "Message: ${exception.message} | ${exception::class.java.name}"
            )
            val stack = exception.stackTraceToString()
            val max = 8000
            if (stack.length > max) {
                Log.e(TAG_CRASH, stack.take(max) + "…(truncated)")
            } else {
                Log.e(TAG_CRASH, stack)
            }
            defaultHandler?.uncaughtException(thread, exception)
        }
    }

    @JvmOverloads
    fun step(phase: String, detail: String = "") {
        lastBootStep = if (detail.isNotEmpty()) "$phase: $detail" else phase
        Log.i(TAG_BOOT, lastBootStep)
    }
}
