package tn.dadadrive.presentation.navigation

import tn.dadadrive.presentation.session.SessionState

object SessionNavigationMapper {

    fun destination(
        state: SessionState,
        introOnboardingComplete: Boolean,
        preferPassengerMapWhileDriverPending: Boolean = false,
    ): String? =
        when (state) {
            SessionState.Loading -> null
            SessionState.BrowsingGuest -> AppRoute.Map.route
            SessionState.Unauthenticated -> {
                if (introOnboardingComplete) AppRoute.Welcome.route else AppRoute.Onboarding.route
            }
            SessionState.NeedsPhone -> AppRoute.Phone.createRoute(true)
            SessionState.NeedsName -> AppRoute.NameEntry.route
            SessionState.NeedsRole -> AppRoute.RoleSelection.route
            SessionState.NeedsDriverSetup -> AppRoute.DriverSetup.route
            SessionState.NeedsDriverApproval ->
                if (preferPassengerMapWhileDriverPending) AppRoute.Map.route
                else AppRoute.DriverVerificationPending.route
            is SessionState.Authenticated ->
                if (state.user.role == "driver") AppRoute.DriverHome.route else AppRoute.Map.route
        }
}
