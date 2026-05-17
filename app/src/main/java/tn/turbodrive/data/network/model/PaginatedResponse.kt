package tn.turbodrive.data.network.model

data class PaginationMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val pages: Int,
)

data class PaginatedResponse<T>(
    val items: List<T>,
    val meta: PaginationMeta,
)

data class HealthResponseDto(
    val status: String? = null,
    val version: String? = null,
)
