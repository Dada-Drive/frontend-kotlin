package tn.dadadrive.data.network.envelope

/**
 * Exception levée par [unwrap] lorsque le backend renvoie `success == false` ou un HTTP error.
 *
 * Porte l'[ApiError] structuré pour permettre aux call sites d'accéder au [ApiError.code] stable.
 * Coexiste avec `ApiBusinessException` (chemin OkHttp legacy `ApiClient.kt`) — pas de migration cascade en R-1.1.
 *
 * @param apiError erreur structurée (code + message + details)
 * @param httpCode code HTTP de la réponse (null si non disponible)
 */
class BackendException(
    val apiError: ApiError,
    val httpCode: Int? = null,
) : RuntimeException("[${apiError.code}] ${apiError.message}")
