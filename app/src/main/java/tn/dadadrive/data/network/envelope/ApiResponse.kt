package tn.dadadrive.data.network.envelope

/**
 * Wrapper homogène pour toutes les réponses backend Retrofit.
 *
 * Format JSON attendu :
 * ```
 * { "success": true,  "data": {...}, "error": null }
 * { "success": false, "data": null,  "error": { "code": "...", "message": "..." } }
 * ```
 *
 * Décolle le format de transport (success/error) du payload métier ([T]) — voir phase R-1.1 du plan.
 *
 * @param success indicateur de succès business (différent du HTTP status code)
 * @param data payload typé, présent uniquement si `success == true`
 * @param error présent uniquement si `success == false`
 */
data class ApiResponse<T>(
    val success: Boolean = true,
    val data: T? = null,
    val error: ApiError? = null,
)
