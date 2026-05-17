package tn.turbodrive.domain.models

import androidx.annotation.StringRes

data class PresentableError(
    val message: String,
    val category: ErrorCategory,
    val isRetryable: Boolean,
    val retryAfterSeconds: Int? = null,
    /**
     * Typed backend code (R-1.2). `null` when the error did not originate from a
     * backend envelope (e.g. IOException, generic HttpException without code).
     * ViewModels can branch on this without parsing [message].
     */
    val code: BackendErrorCode? = null,
    /**
     * Pre-resolved `@StringRes` of the localized message (R-1.2). Set by
     * [tn.turbodrive.data.network.error.PresentableErrorMapper] when the error
     * code maps to a known string. UI layers can call `stringResource(messageResId)`
     * directly without re-mapping via `code.toStringRes()`.
     *
     * `null` for non-envelope errors or for [BackendErrorCode.UNKNOWN].
     * In that case, fall back to [message] which is already resolved.
     */
    @StringRes val messageResId: Int? = null,
)

sealed interface ErrorCategory {
    data object Network : ErrorCategory

    data object Validation : ErrorCategory

    data object Server : ErrorCategory

    data object Auth : ErrorCategory

    data object BusinessRule : ErrorCategory

    data object RateLimit : ErrorCategory

    data object NotFound : ErrorCategory

    data object Permission : ErrorCategory
}
