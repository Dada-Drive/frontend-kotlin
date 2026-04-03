// Équivalent Swift : Data/Repositories/DriverRepository.swift (appels HTTP)
package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.ActiveRideDto
import com.dadadrive.data.remote.model.AvailableRidesResponseDto
import com.dadadrive.data.remote.model.CancelRideRequestDto
import com.dadadrive.data.remote.model.CompleteRideResponseDto
import com.dadadrive.data.remote.model.CreateDriverProfileRequestDto
import com.dadadrive.data.remote.model.CreateVehicleRequestDto
import com.dadadrive.data.remote.model.DriverProfileResponseDto
import com.dadadrive.data.remote.model.MessageResponseDto
import com.dadadrive.data.remote.model.RideOfferResponseDto
import com.dadadrive.data.remote.model.RideResponseDto
import com.dadadrive.data.remote.model.RidesResponseDto
import com.dadadrive.data.remote.model.SetOnlineStatusRequestDto
import com.dadadrive.data.remote.model.VehicleResponseDto
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
