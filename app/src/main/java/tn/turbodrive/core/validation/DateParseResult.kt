package tn.turbodrive.core.validation

sealed interface DateParseResult {
    data class Valid(val iso: String) : DateParseResult

    data object Invalid : DateParseResult
}
