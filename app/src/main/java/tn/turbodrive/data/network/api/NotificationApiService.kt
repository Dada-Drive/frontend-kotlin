package tn.turbodrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import tn.turbodrive.data.network.envelope.ApiResponse
import tn.turbodrive.data.network.model.NotificationTokenRequest

interface NotificationApiService {
    @POST("notifications/token")
    suspend fun saveToken(
        @Body body: NotificationTokenRequest,
    ): Response<ApiResponse<Unit>>

    @DELETE("notifications/token")
    suspend fun removeToken(): Response<ApiResponse<Unit>>
}
