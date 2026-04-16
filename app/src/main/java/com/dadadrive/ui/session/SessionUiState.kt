// Équivalent Swift : Domain/Models/SessionState.swift + branchement AppCoordinatorView
package com.dadadrive.ui.session

import com.dadadrive.domain.model.User

sealed class SessionUiState {
    data object Loading : SessionUiState()
    /** Carte / exploration sans compte (pas d’appels API authentifiés). */
    data object BrowsingGuest : SessionUiState()
    data object Unauthenticated : SessionUiState()
    data object NeedsPhone : SessionUiState()
    data object NeedsName : SessionUiState()
    data object NeedsRole : SessionUiState()
    data object NeedsDriverSetup : SessionUiState()
    data class Authenticated(val user: User) : SessionUiState()
}
