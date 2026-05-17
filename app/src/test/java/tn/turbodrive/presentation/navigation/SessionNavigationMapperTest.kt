package tn.turbodrive.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tn.turbodrive.domain.models.User
import tn.turbodrive.presentation.session.SessionState

class SessionNavigationMapperTest {
    @Test
    fun loadingMapsToNull() {
        assertNull(SessionNavigationMapper.destination(SessionState.Loading, introOnboardingComplete = true))
    }

    @Test
    fun unauthenticatedRespectsIntroFlag() {
        assertEquals(
            AppRoute.Onboarding.route,
            SessionNavigationMapper.destination(SessionState.Unauthenticated, introOnboardingComplete = false),
        )
        assertEquals(
            AppRoute.Welcome.route,
            SessionNavigationMapper.destination(SessionState.Unauthenticated, introOnboardingComplete = true),
        )
    }

    @Test
    fun authenticatedDriverVsRider() {
        val rider = User("1", "N", "", "+1", "rider", null)
        val driver = User("1", "N", "", "+1", "driver", null)
        assertEquals(AppRoute.Map.route, SessionNavigationMapper.destination(SessionState.Authenticated(rider), true))
        assertEquals(AppRoute.DriverHome.route, SessionNavigationMapper.destination(SessionState.Authenticated(driver), true))
    }

    @Test
    fun needsDriverApprovalRespectsPassengerBypass() {
        assertEquals(
            AppRoute.DriverVerificationPending.route,
            SessionNavigationMapper.destination(
                SessionState.NeedsDriverApproval,
                introOnboardingComplete = true,
                preferPassengerMapWhileDriverPending = false,
            ),
        )
        assertEquals(
            AppRoute.Map.route,
            SessionNavigationMapper.destination(
                SessionState.NeedsDriverApproval,
                introOnboardingComplete = true,
                preferPassengerMapWhileDriverPending = true,
            ),
        )
    }
}
