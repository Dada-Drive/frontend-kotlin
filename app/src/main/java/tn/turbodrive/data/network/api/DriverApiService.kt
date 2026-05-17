// Équivalent Swift : Data/Repositories/DriverRepository.swift (appels HTTP)
package tn.turbodrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import tn.turbodrive.data.network.annotation.Idempotent
import tn.turbodrive.data.network.envelope.ApiResponse
import tn.turbodrive.data.network.model.AvailableRidesResponseDto
import tn.turbodrive.data.network.model.CancelRideRequestDto
import tn.turbodrive.data.network.model.CompleteRideResponseDto
import tn.turbodrive.data.network.model.CreateDriverProfileRequestDto
import tn.turbodrive.data.network.model.CreateVehicleRequestDto
import tn.turbodrive.data.network.model.DriverProfileResponseDto
import tn.turbodrive.data.network.model.MessageResponseDto
import tn.turbodrive.data.network.model.RideOfferResponseDto
import tn.turbodrive.data.network.model.RideResponseDto
import tn.turbodrive.data.network.model.RidesResponseDto
import tn.turbodrive.data.network.model.SetOnlineStatusRequestDto
import tn.turbodrive.data.network.model.UpdateDriverLocationRequestDto
import tn.turbodrive.data.network.model.VehicleResponseDto

interface DriverApiService {
    @GET("driver/profile")
    suspend fun getProfile(): Response<ApiResponse<DriverProfileResponseDto>>

    @POST("driver/profile")
    suspend fun createProfile(
        @Body body: CreateDriverProfileRequestDto,
    ): Response<ApiResponse<DriverProfileResponseDto>>

    @PATCH("driver/profile")
    suspend fun updateProfile(
        @Body body: CreateDriverProfileRequestDto,
    ): Response<ApiResponse<DriverProfileResponseDto>>

    @GET("driver/vehicle")
    suspend fun getVehicle(): Response<ApiResponse<VehicleResponseDto>>

    @POST("driver/vehicle")
    suspend fun createVehicle(
        @Body body: CreateVehicleRequestDto,
    ): Response<ApiResponse<VehicleResponseDto>>

    @PATCH("driver/vehicle")
    suspend fun updateVehicle(
        @Body body: CreateVehicleRequestDto,
    ): Response<ApiResponse<VehicleResponseDto>>

    @PATCH("driver/status")
    suspend fun setOnlineStatus(
        @Body body: SetOnlineStatusRequestDto,
    ): Response<ApiResponse<DriverProfileResponseDto>>

    @PATCH("driver/location")
    suspend fun updateLocation(
        @Body body: UpdateDriverLocationRequestDto,
    ): Response<ApiResponse<DriverProfileResponseDto>>

    @GET("rides/available")
    suspend fun getAvailableRides(): Response<ApiResponse<AvailableRidesResponseDto>>

    @Idempotent
    @POST("rides/{id}/accept")
    suspend fun acceptRide(
        @Path("id") id: String,
    ): Response<ApiResponse<RideOfferResponseDto>>

    @POST("rides/{id}/refuse")
    suspend fun refuseRide(
        @Path("id") id: String,
    ): Response<ApiResponse<MessageResponseDto>>

    @GET("rides/my")
    suspend fun getMyRides(): Response<ApiResponse<RidesResponseDto>>

    @GET("rides/{id}")
    suspend fun getRide(
        @Path("id") id: String,
    ): Response<ApiResponse<RideResponseDto>>

    @PATCH("rides/{id}/start")
    suspend fun startRide(
        @Path("id") id: String,
    ): Response<ApiResponse<RideResponseDto>>

    @PATCH("rides/{id}/complete")
    suspend fun completeRide(
        @Path("id") id: String,
    ): Response<ApiResponse<CompleteRideResponseDto>>

    @PATCH("rides/{id}/cancel")
    suspend fun cancelRide(
        @Path("id") id: String,
        @Body body: CancelRideRequestDto,
    ): Response<ApiResponse<RideResponseDto>>
}
