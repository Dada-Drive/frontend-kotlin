package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.NotificationTokenRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface NotificationApiService {
    @POST("notifications/token")
    suspend fun saveToken(@Body body: NotificationTokenRequest)

    @DELETE("notifications/token")
    suspend fun removeToken()
}
