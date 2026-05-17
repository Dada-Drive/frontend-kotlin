package tn.turbodrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tn.turbodrive.data.network.envelope.ApiResponse
import tn.turbodrive.data.network.model.DriverRegistrationRequestDto
import tn.turbodrive.data.network.model.DriverRegistrationResponseDto

/**
 * Placeholder API contract for upcoming backend endpoints.
 * Wire the real URL path once backend confirms it.
 */
interface DriverRegistrationApiService {
    @POST("driver/registration")
    suspend fun submitRegistration(
        @Body body: DriverRegistrationRequestDto,
    ): Response<ApiResponse<DriverRegistrationResponseDto>>
}
