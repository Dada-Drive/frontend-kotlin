// Équivalent Swift : effet de session expirée après échec refresh (APIClient.swift)
package com.dadadrive.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthNavigationEvents @Inject constructor() {
    private val _forceLogout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogout: SharedFlow<Unit> = _forceLogout.asSharedFlow()

    fun emitForceLogout() {
        _forceLogout.tryEmit(Unit)
    }
}
