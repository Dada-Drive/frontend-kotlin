package tn.dadadrive.presentation.navigation

/**
 * Destinations du graphe principal (Phase 3 — routes typées).
 * Les chemins restent alignés sur l’historique `MainActivity` / NavController.
 */
sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Onboarding : AppRoute("onboarding")
    data object Welcome : AppRoute("welcome")
    data object NameEntry : AppRoute("name_entry")
    data object RoleSelection : AppRoute("role_selection")
    data object Map : AppRoute("map")
    data object DriverHome : AppRoute("driver_home")
    data object DriverSetup : AppRoute("driver_setup")
    data object DriverAccessSetup : AppRoute("driver_access_setup")
    data object DriverPhoneSettings : AppRoute("driver_phone_settings")
    data object DriverTaxiLicenseUpload : AppRoute("driver_taxi_license_upload")
    data object DriverVerificationPending : AppRoute("driver_verification_pending")
    data object EditProfile : AppRoute("edit_profile")
    data object ColorSettings : AppRoute("settings/colors")
    data object Wallet : AppRoute("wallet")
    data object Phone : AppRoute("phone?fromSession={fromSession}") {
        fun createRoute(fromSession: Boolean): String = "phone?fromSession=$fromSession"
    }
}
