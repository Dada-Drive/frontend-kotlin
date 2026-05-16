package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.DriverRegistrationRequestDto
import tn.dadadrive.data.network.model.DriverRegistrationResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Placeholder API contract for upcoming backend endpoints.
 * Wire the real URL path once backend confirms it.
 */
interface DriverRegistrationApiService {
    @POST("driver/registration")
    suspend fun submitRegistration(@Body body: DriverRegistrationRequestDto): DriverRegistrationResponseDto
}
