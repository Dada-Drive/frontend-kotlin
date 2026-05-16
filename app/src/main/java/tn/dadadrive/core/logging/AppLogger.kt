package tn.dadadrive.core.logging

import timber.log.Timber

object AppLogger {

    fun v(message: String, throwable: Throwable? = null) {
        log { if (throwable != null) Timber.v(throwable, message) else Timber.v(message) }
    }

    fun d(message: String, throwable: Throwable? = null) {
        log { if (throwable != null) Timber.d(throwable, message) else Timber.d(message) }
    }

    fun i(message: String, throwable: Throwable? = null) {
        log { if (throwable != null) Timber.i(throwable, message) else Timber.i(message) }
    }

    fun w(message: String, throwable: Throwable? = null) {
        log { if (throwable != null) Timber.w(throwable, message) else Timber.w(message) }
    }

    fun e(message: String, throwable: Throwable? = null) {
        log { if (throwable != null) Timber.e(throwable, message) else Timber.e(message) }
    }

    private inline fun log(block: () -> Unit) {
        block()
    }
}
