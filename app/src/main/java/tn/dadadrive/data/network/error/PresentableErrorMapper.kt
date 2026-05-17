package tn.dadadrive.data.network.error

import android.content.Context
import androidx.annotation.StringRes
import com.dadadrive.R
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import tn.dadadrive.data.network.ApiBusinessException
import tn.dadadrive.data.network.ApiEnvelopeException
import tn.dadadrive.data.network.envelope.BackendException
import tn.dadadrive.domain.models.BackendErrorCode
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentableErrorMapper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun fromThrowable(throwable: Throwable): PresentableError =
            when (throwable) {
                is HttpException -> fromHttpException(throwable)
                is BackendException ->
                    fromBackend(
                        httpCode = throwable.httpCode ?: 0,
                        code = throwable.apiError.code,
                        retryAfterHeader = null,
                    ) ?: fromHttp(throwable.httpCode ?: 0, throwable.apiError.code, null)
                is ApiBusinessException ->
                    fromBackend(throwable.httpCode, throwable.errorCode, null)
                        ?: fromHttp(throwable.httpCode, null, null)
                is ApiEnvelopeException -> fromHttp(throwable.httpCode, null, throwable.retryAfterHeader)
                is SocketTimeoutException ->
                    PresentableError(
                        message = context.getString(R.string.error_network),
                        category = ErrorCategory.Network,
                        isRetryable = true,
                    )
                is IOException ->
                    PresentableError(
                        message = context.getString(R.string.error_network),
                        category = ErrorCategory.Network,
                        isRetryable = true,
                    )
                else ->
                    PresentableError(
                        message = context.getString(R.string.error_generic),
                        category = ErrorCategory.Server,
                        isRetryable = false,
                    )
            }

        fun fromHttpException(e: HttpException): PresentableError {
            val retryAfter = e.response()?.headers()?.get("Retry-After")
            val backendCode = parseErrorCodeFromBody(e)
            return fromBackend(e.code(), backendCode, retryAfter)
                ?: fromHttp(e.code(), backendCode, retryAfter)
        }

        private fun parseErrorCodeFromBody(e: HttpException): String? {
            val body =
                try {
                    e.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                } ?: return null
            return try {
                val obj = JsonParser.parseString(body).asJsonObject
                obj.getAsJsonObject("error")?.get("code")?.asString
            } catch (_: Exception) {
                null
            }
        }

        fun fromHttp(
            httpCode: Int,
            backendErrorCode: String?,
            retryAfterHeader: String?,
        ): PresentableError {
            val retryAfter = retryAfterHeader?.toIntOrNull()
            return fromBackend(httpCode, backendErrorCode, retryAfterHeader)
                ?: when (httpCode) {
                    400 -> msg(R.string.error_validation, ErrorCategory.Validation, false)
                    401 -> msg(R.string.error_unauthorized, ErrorCategory.Auth, false)
                    403 -> msg(R.string.error_forbidden, ErrorCategory.Permission, false)
                    404 -> msg(R.string.error_not_found, ErrorCategory.NotFound, false)
                    409 -> msg(R.string.error_conflict, ErrorCategory.BusinessRule, false)
                    422 -> msg(R.string.error_validation, ErrorCategory.Validation, false)
                    429 ->
                        PresentableError(
                            context.getString(R.string.error_rate_limited),
                            ErrorCategory.RateLimit,
                            true,
                            retryAfterSeconds = retryAfter,
                        )
                    500, 502, 503 -> msg(R.string.error_server_error, ErrorCategory.Server, true)
                    else ->
                        PresentableError(
                            context.getString(R.string.error_generic),
                            ErrorCategory.Server,
                            isRetryable = httpCode in 500..599,
                        )
                }
        }

        private fun fromBackend(
            httpCode: Int,
            code: String?,
            retryAfterHeader: String?,
        ): PresentableError? {
            if (code.isNullOrBlank()) return null
            // TOKEN_EXPIRED / TOKEN_INVALID intentionally skipped --
            // delegated to TokenAuthenticator (refresh-and-retry flow).
            if (code == "TOKEN_EXPIRED" || code == "TOKEN_INVALID") return null
            val enumCode = BackendErrorCode.fromString(code)
            val resId = stringResForBackendCode(enumCode) ?: return null
            val retryAfter = retryAfterHeader?.toIntOrNull()
            return PresentableError(
                message = context.getString(resId),
                category = categoryFor(httpCode),
                isRetryable = httpCode == 429 || httpCode in 500..599,
                retryAfterSeconds = if (httpCode == 429) retryAfter else null,
                code = enumCode,
            )
        }

        /**
         * Exhaustive enum mapping. Adding a new value to [BackendErrorCode] forces
         * an update here (compile-time `when` exhaustiveness).
         *
         * [BackendErrorCode.UNKNOWN] returns null so [fromBackend] falls back to
         * the generic HTTP-based message in [fromHttp].
         *
         * Suppress(CyclomaticComplexMethod) : intentional 1:1 lookup table -- splitting
         * by category would fragment a single source of truth and break the compile-time
         * exhaustiveness guarantee that justifies the enum approach.
         */
        @StringRes
        @Suppress("CyclomaticComplexMethod")
        private fun stringResForBackendCode(code: BackendErrorCode): Int? =
            when (code) {
                BackendErrorCode.UNAUTHORIZED -> R.string.error_unauthorized
                BackendErrorCode.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
                BackendErrorCode.ACCOUNT_SUSPENDED -> R.string.error_account_suspended
                BackendErrorCode.ACCOUNT_NOT_FOUND -> R.string.error_account_not_found
                BackendErrorCode.FORBIDDEN -> R.string.error_forbidden
                BackendErrorCode.OTP_EXPIRED -> R.string.error_otp_expired
                BackendErrorCode.OTP_MAX_ATTEMPTS -> R.string.error_otp_max_attempts
                BackendErrorCode.OTP_INVALID -> R.string.error_otp_invalid
                BackendErrorCode.OTP_RATE_LIMITED -> R.string.error_otp_rate_limited
                BackendErrorCode.RIDE_NOT_FOUND -> R.string.error_ride_not_found
                BackendErrorCode.RIDE_INVALID_STATUS -> R.string.error_ride_invalid_status
                BackendErrorCode.RIDE_ALREADY_ACCEPTED -> R.string.error_ride_already_accepted
                BackendErrorCode.RIDE_EXPIRED -> R.string.error_ride_expired
                BackendErrorCode.RIDE_OUTSIDE_BOUNDS -> R.string.error_ride_outside_bounds
                BackendErrorCode.OFFER_NOT_FOUND -> R.string.error_offer_not_found
                BackendErrorCode.RIDE_STOP_NOT_FOUND -> R.string.error_ride_stop_not_found
                BackendErrorCode.INSUFFICIENT_BALANCE -> R.string.error_insufficient_balance
                BackendErrorCode.WALLET_SUSPENDED -> R.string.error_wallet_suspended
                BackendErrorCode.DUPLICATE_TRANSACTION -> R.string.error_duplicate_transaction
                BackendErrorCode.INVALID_AMOUNT -> R.string.error_invalid_amount
                BackendErrorCode.DRIVER_NOT_FOUND -> R.string.error_driver_not_found
                BackendErrorCode.DRIVER_NOT_APPROVED -> R.string.error_driver_not_approved
                BackendErrorCode.DRIVER_OFFLINE -> R.string.error_driver_offline
                BackendErrorCode.VEHICLE_NOT_FOUND -> R.string.error_vehicle_not_found
                BackendErrorCode.RATING_NOT_FOUND -> R.string.error_rating_not_found
                BackendErrorCode.RATING_ALREADY_EXISTS -> R.string.error_rating_already_exists
                BackendErrorCode.UPLOAD_INVALID_TYPE -> R.string.error_upload_invalid_type
                BackendErrorCode.UPLOAD_TOO_LARGE -> R.string.error_upload_too_large
                BackendErrorCode.UPLOAD_RATE_LIMITED -> R.string.error_upload_rate_limited
                BackendErrorCode.NOTIFICATION_NOT_FOUND -> R.string.error_notification_not_found
                BackendErrorCode.VALIDATION_ERROR -> R.string.error_validation
                BackendErrorCode.NOT_FOUND -> R.string.error_not_found
                BackendErrorCode.RATE_LIMITED -> R.string.error_rate_limited
                BackendErrorCode.INTERNAL_ERROR -> R.string.error_server_error
                BackendErrorCode.UNKNOWN -> null
            }

        private fun categoryFor(httpCode: Int): ErrorCategory =
            when (httpCode) {
                400, 422 -> ErrorCategory.Validation
                401 -> ErrorCategory.Auth
                403 -> ErrorCategory.Permission
                404 -> ErrorCategory.NotFound
                409 -> ErrorCategory.BusinessRule
                429 -> ErrorCategory.RateLimit
                in 500..599 -> ErrorCategory.Server
                else -> ErrorCategory.Server
            }

        private fun msg(
            @StringRes res: Int,
            category: ErrorCategory,
            isRetryable: Boolean,
        ): PresentableError =
            PresentableError(
                message = context.getString(res),
                category = category,
                isRetryable = isRetryable,
            )
    }
