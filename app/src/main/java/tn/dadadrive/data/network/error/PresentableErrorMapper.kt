package tn.dadadrive.data.network.error

import android.content.Context
import androidx.annotation.StringRes
import com.dadadrive.R
import com.google.gson.JsonParser
import tn.dadadrive.data.network.ApiBusinessException
import tn.dadadrive.data.network.ApiEnvelopeException
import tn.dadadrive.domain.model.ErrorCategory
import tn.dadadrive.domain.model.PresentableError
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentableErrorMapper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun fromThrowable(throwable: Throwable): PresentableError = when (throwable) {
        is HttpException -> fromHttpException(throwable)
        is ApiBusinessException -> fromBackend(throwable.httpCode, throwable.errorCode, null)
            ?: fromHttp(throwable.httpCode, null, null)
        is ApiEnvelopeException -> fromHttp(throwable.httpCode, null, throwable.retryAfterHeader)
        is SocketTimeoutException -> PresentableError(
            message = context.getString(R.string.error_network),
            category = ErrorCategory.Network,
            isRetryable = true,
        )
        is IOException -> PresentableError(
            message = context.getString(R.string.error_network),
            category = ErrorCategory.Network,
            isRetryable = true,
        )
        else -> PresentableError(
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
        val body = try {
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

    fun fromHttp(httpCode: Int, backendErrorCode: String?, retryAfterHeader: String?): PresentableError {
        val retryAfter = retryAfterHeader?.toIntOrNull()
        return fromBackend(httpCode, backendErrorCode, retryAfterHeader)
            ?: when (httpCode) {
                400 -> msg(R.string.error_validation, ErrorCategory.Validation, false)
                401 -> msg(R.string.error_unauthorized, ErrorCategory.Auth, false)
                403 -> msg(R.string.error_forbidden, ErrorCategory.Permission, false)
                404 -> msg(R.string.error_not_found, ErrorCategory.NotFound, false)
                409 -> msg(R.string.error_conflict, ErrorCategory.BusinessRule, false)
                422 -> msg(R.string.error_validation, ErrorCategory.Validation, false)
                429 -> PresentableError(
                    context.getString(R.string.error_rate_limited),
                    ErrorCategory.RateLimit,
                    true,
                    retryAfterSeconds = retryAfter,
                )
                500, 502, 503 -> msg(R.string.error_server_error, ErrorCategory.Server, true)
                else -> PresentableError(
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
        if (code == "TOKEN_EXPIRED" || code == "TOKEN_INVALID") return null
        val retryAfter = retryAfterHeader?.toIntOrNull()
        val category = categoryFor(httpCode)
        val isRetryable = httpCode == 429 || httpCode in 500..599
        val resId = stringResForBackendCode(code) ?: return null
        return PresentableError(
            message = context.getString(resId),
            category = category,
            isRetryable = isRetryable,
            retryAfterSeconds = if (httpCode == 429) retryAfter else null,
        )
    }

    @StringRes
    private fun stringResForBackendCode(code: String): Int? = when (code) {
        "UNAUTHORIZED" -> R.string.error_unauthorized
        "INVALID_CREDENTIALS" -> R.string.error_invalid_credentials
        "ACCOUNT_SUSPENDED" -> R.string.error_account_suspended
        "ACCOUNT_NOT_FOUND" -> R.string.error_account_not_found
        "FORBIDDEN" -> R.string.error_forbidden
        "OTP_EXPIRED" -> R.string.error_otp_expired
        "OTP_MAX_ATTEMPTS" -> R.string.error_otp_max_attempts
        "OTP_INVALID" -> R.string.error_otp_invalid
        "OTP_RATE_LIMITED" -> R.string.error_otp_rate_limited
        "RIDE_NOT_FOUND" -> R.string.error_ride_not_found
        "RIDE_INVALID_STATUS" -> R.string.error_ride_invalid_status
        "RIDE_ALREADY_ACCEPTED" -> R.string.error_ride_already_accepted
        "RIDE_EXPIRED" -> R.string.error_ride_expired
        "RIDE_OUTSIDE_BOUNDS" -> R.string.error_ride_outside_bounds
        "OFFER_NOT_FOUND" -> R.string.error_offer_not_found
        "RIDE_STOP_NOT_FOUND" -> R.string.error_ride_stop_not_found
        "INSUFFICIENT_BALANCE" -> R.string.error_insufficient_balance
        "WALLET_SUSPENDED" -> R.string.error_wallet_suspended
        "DUPLICATE_TRANSACTION" -> R.string.error_duplicate_transaction
        "INVALID_AMOUNT" -> R.string.error_invalid_amount
        "DRIVER_NOT_FOUND" -> R.string.error_driver_not_found
        "DRIVER_NOT_APPROVED" -> R.string.error_driver_not_approved
        "DRIVER_OFFLINE" -> R.string.error_driver_offline
        "VEHICLE_NOT_FOUND" -> R.string.error_vehicle_not_found
        "RATING_NOT_FOUND" -> R.string.error_rating_not_found
        "RATING_ALREADY_EXISTS" -> R.string.error_rating_already_exists
        "UPLOAD_INVALID_TYPE" -> R.string.error_upload_invalid_type
        "UPLOAD_TOO_LARGE" -> R.string.error_upload_too_large
        "UPLOAD_RATE_LIMITED" -> R.string.error_upload_rate_limited
        "NOTIFICATION_NOT_FOUND" -> R.string.error_notification_not_found
        "VALIDATION_ERROR" -> R.string.error_validation
        "NOT_FOUND" -> R.string.error_not_found
        "RATE_LIMITED" -> R.string.error_rate_limited
        "INTERNAL_ERROR" -> R.string.error_server_error
        else -> null
    }

    private fun categoryFor(httpCode: Int): ErrorCategory = when (httpCode) {
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
    ): PresentableError = PresentableError(
        message = context.getString(res),
        category = category,
        isRetryable = isRetryable,
    )
}
