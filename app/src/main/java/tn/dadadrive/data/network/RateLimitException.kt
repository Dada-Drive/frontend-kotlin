package tn.dadadrive.data.network

import retrofit2.HttpException

/**
 * Exception émise lorsque le backend renvoie un `429 Too Many Requests`.
 *
 * Transporte [retryAfterSeconds] (parsé depuis le header HTTP `Retry-After`) pour que
 * l'UI puisse griser le bouton concerné et afficher un compte à rebours, au lieu de
 * laisser l'utilisateur retaper tout de suite et reprendre un autre 429.
 */
class RateLimitException(
    val retryAfterSeconds: Int,
    message: String,
) : Exception(message)

/**
 * Lit le header `Retry-After` d'une réponse 429. Le header peut être un entier (secondes)
 * ou une date HTTP ; on ne gère que la forme numérique (la seule que nos backends Express
 * renvoient). Retourne [fallbackSeconds] si le header est absent/illisible.
 */
internal fun HttpException.retryAfterSeconds(fallbackSeconds: Int): Int {
    val header = response()?.headers()?.get("Retry-After") ?: return fallbackSeconds
    return header.trim().toIntOrNull()?.coerceAtLeast(1) ?: fallbackSeconds
}
