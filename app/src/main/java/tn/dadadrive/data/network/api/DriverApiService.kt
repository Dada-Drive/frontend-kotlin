// Équivalent Swift : Data/Repositories/DriverRepository.swift (appels HTTP)
package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.ActiveRideDto
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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DriverApiService {

    @GET("driver/profile")
    suspend fun getProfile(): DriverProfileResponseDto

    @POST("driver/profile")
    suspend fun createProfile(@Body body: CreateDriverProfileRequestDto): DriverProfileResponseDto

    @PATCH("driver/profile")
    suspend fun updateProfile(@Body body: CreateDriverProfileRequestDto): DriverProfileResponseDto

    @GET("driver/vehicle")
    suspend fun getVehicle(): VehicleResponseDto

    @POST("driver/vehicle")
    suspend fun createVehicle(@Body body: CreateVehicleRequestDto): VehicleResponseDto

    @PATCH("driver/vehicle")
    suspend fun updateVehicle(@Body body: CreateVehicleRequestDto): VehicleResponseDto

    @PATCH("driver/status")
    suspend fun setOnlineStatus(@Body body: SetOnlineStatusRequestDto): DriverProfileResponseDto

    @PATCH("driver/location")
    suspend fun updateLocation(@Body body: UpdateDriverLocationRequestDto): DriverProfileResponseDto

    @GET("rides/available")
    suspend fun getAvailableRides(): AvailableRidesResponseDto

    @POST("rides/{id}/accept")
    suspend fun acceptRide(@Path("id") id: String): RideOfferResponseDto

    @POST("rides/{id}/refuse")
    suspend fun refuseRide(@Path("id") id: String): MessageResponseDto

    @GET("rides/my")
    suspend fun getMyRides(): RidesResponseDto

    @GET("rides/{id}")
    suspend fun getRide(@Path("id") id: String): RideResponseDto

    @PATCH("rides/{id}/start")
    suspend fun startRide(@Path("id") id: String): RideResponseDto

    @PATCH("rides/{id}/complete")
    suspend fun completeRide(@Path("id") id: String): CompleteRideResponseDto

    @PATCH("rides/{id}/cancel")
    suspend fun cancelRide(
        @Path("id") id: String,
        @Body body: CancelRideRequestDto
    ): RideResponseDto
}
