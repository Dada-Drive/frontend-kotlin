package tn.dadadrive.domain.models

/**
 * Type-safe wrapper for backend error codes (R-1.2).
 *
 * Pure domain layer — no Android dependency. The @StringRes mapping lives in
 * [tn.dadadrive.data.network.error.PresentableErrorMapper.stringResForBackendCode].
 *
 * Adding a new code here forces the `when` in the mapper to handle it
 * (exhaustiveness check at compile time).
 *
 * `TOKEN_EXPIRED` / `TOKEN_INVALID` are intentionally NOT in this enum —
 * they are handled upstream by `TokenAuthenticator` which triggers the
 * refresh-and-retry flow without reaching the user-facing error layer.
 */
enum class BackendErrorCode {
    // Auth
    UNAUTHORIZED,
    INVALID_CREDENTIALS,
    ACCOUNT_SUSPENDED,
    ACCOUNT_NOT_FOUND,
    FORBIDDEN,

    // OTP
    OTP_EXPIRED,
    OTP_MAX_ATTEMPTS,
    OTP_INVALID,
    OTP_RATE_LIMITED,

    // Rides
    RIDE_NOT_FOUND,
    RIDE_INVALID_STATUS,
    RIDE_ALREADY_ACCEPTED,
    RIDE_EXPIRED,
    RIDE_OUTSIDE_BOUNDS,
    OFFER_NOT_FOUND,
    RIDE_STOP_NOT_FOUND,

    // Wallet
    INSUFFICIENT_BALANCE,
    WALLET_SUSPENDED,
    DUPLICATE_TRANSACTION,
    INVALID_AMOUNT,

    // Driver / Vehicle
    DRIVER_NOT_FOUND,
    DRIVER_NOT_APPROVED,
    DRIVER_OFFLINE,
    VEHICLE_NOT_FOUND,

    // Rating
    RATING_NOT_FOUND,
    RATING_ALREADY_EXISTS,

    // Upload
    UPLOAD_INVALID_TYPE,
    UPLOAD_TOO_LARGE,
    UPLOAD_RATE_LIMITED,

    // Notification
    NOTIFICATION_NOT_FOUND,

    // Generic
    VALIDATION_ERROR,
    NOT_FOUND,
    RATE_LIMITED,
    INTERNAL_ERROR,

    // Fallback for codes the backend invents after this build was shipped.
    UNKNOWN,
    ;

    companion object {
        fun fromString(code: String?): BackendErrorCode {
            if (code.isNullOrBlank()) return UNKNOWN
            val normalized = code.trim().uppercase()
            return entries.firstOrNull { it.name == normalized } ?: UNKNOWN
        }
    }
}
