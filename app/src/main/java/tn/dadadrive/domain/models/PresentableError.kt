package tn.dadadrive.domain.models

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
