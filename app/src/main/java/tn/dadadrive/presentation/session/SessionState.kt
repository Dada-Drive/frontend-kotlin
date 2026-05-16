// Phase 3 roadmap — session routing (équiv. Swift Domain/Models/SessionState)
package tn.dadadrive.presentation.session

import tn.dadadrive.domain.models.User

sealed class SessionState {
    data object Loading : SessionState()
    /** Carte / exploration sans compte (pas d’appels API authentifiés). */
    data object BrowsingGuest : SessionState()
    data object Unauthenticated : SessionState()
    data object NeedsPhone : SessionState()
    data object NeedsName : SessionState()
    data object NeedsRole : SessionState()
    data object NeedsDriverSetup : SessionState()
    data object NeedsDriverApproval : SessionState()
    data class Authenticated(val user: User) : SessionState()
}
