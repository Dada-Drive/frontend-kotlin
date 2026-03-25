package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.dto.BookRideRequestDto
import com.dadadrive.data.remote.dto.DriverDto
import com.dadadrive.data.remote.dto.RideDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RideApi {
    @GET("drivers/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusKm: Double
    ): Response<List<DriverDto>>

    @GET("rides/estimate")
    suspend fun estimateRidePrice(
        @Query("pickup_lat") pickupLat: Double,
        @Query("pickup_lng") pickupLng: Double,
        @Query("dest_lat") destLat: Double,
        @Query("dest_lng") destLng: Double
    ): Response<Map<String, Double>>

    @POST("rides")
    suspend fun bookRide(@Body request: BookRideRequestDto): Response<RideDto>

    @DELETE("rides/{rideId}")
    suspend fun cancelRide(@Path("rideId") rideId: String): Response<Unit>

    @GET("rides/{rideId}")
    suspend fun getRideById(@Path("rideId") rideId: String): Response<RideDto>

    @GET("rides/history")
    suspend fun getRideHistory(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<List<RideDto>>
}
