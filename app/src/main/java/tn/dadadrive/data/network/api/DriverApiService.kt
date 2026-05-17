// Équivalent Swift : Data/Repositories/DriverRepository.swift (appels HTTP)
package tn.dadadrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import tn.dadadrive.data.network.envelope.ApiResponse
import tn.dadadrive.data.network.model.AvailableRidesResponseDto
import tn.dadadrive.data.network.model.CancelRideRequestDto
import tn.dadadrive.data.network.model.CompleteRideResponseDto
import tn.dadadrive.data.network.model.CreateDriverProfileRequestDto
import tn.dadadrive.data.network.model.CreateVehicleRequestDto
import tn.dadadrive.data.network.model.DriverProfileResponseDto
import tn.dadadrive.data.network.model.MessageResponseDto
import tn.dadadrive.data.network.model.RideOfferResponseDto
import tn.dadadrive.data.network.model.RideResponseDto
import tn.dadadrive.data.network.model.RidesResponseDto
import tn.dadadrive.data.network.model.SetOnlineStatusRequestDto
import tn.dadadrive.data.network.model.UpdateDriverLocationRequestDto
import tn.dadadrive.data.network.model.VehicleResponseDto

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
