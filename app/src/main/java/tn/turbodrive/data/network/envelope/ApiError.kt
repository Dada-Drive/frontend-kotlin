package tn.turbodrive.data.network.envelope

/**
 * Erreur business renvoyée par le backend dans le wrapper [ApiResponse].
 *
 * Le mapping `code` → message localisé est repoussé à la phase R-1.2 (codes erreur localisés).
 * En R-1.1, [message] est propagé tel quel.
 *
 * @param code code stable côté backend (ex. "INVALID_OTP", "RATE_LIMITED", "USER_NOT_FOUND")
 * @param message message lisible (souvent FR, brut, non localisé)
 * @param details détails optionnels (params invalides, retry-after, etc.) — typé `Any?` pour Gson
 */
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, Any?>? = null,
)
