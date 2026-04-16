package com.dadadrive.data.remote.api

import com.dadadrive.data.remote.model.CancelRideBodyDto
import com.dadadrive.data.remote.model.FareApiResponse
import com.dadadrive.data.remote.model.DriverRatingsResponseDto
import com.dadadrive.data.remote.model.RideOffersResponseDto
import com.dadadrive.data.remote.model.RideRatingResponseDto
import com.dadadrive.data.remote.model.RequestRideRequestDto
import com.dadadrive.data.remote.model.RideResponseDto
import com.dadadrive.data.remote.model.RidesResponseDto
import com.dadadrive.data.remote.model.SubmitRideRatingRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface RidesApiService {

    @GET("rides/fare")
    suspend fun getFare(
        @Query("distance_km") distanceKm: Double,
        @Query("estimated_minutes") estimatedMinutes: Int
    ): FareApiResponse

    @POST("rides")
    suspend fun requestRide(
        @Body body: RequestRideRequestDto
    ): RideResponseDto

    @GET("rides/{id}/offers")
    suspend fun getRideOffers(@Path("id") rideId: String): RideOffersResponseDto

    @GET("rides/scheduled")
    suspend fun getScheduledRides(): RidesResponseDto

    @PATCH("rides/{id}/offers/{offerId}/pick")
    suspend fun pickRideOffer(
        @Path("id") rideId: String,
        @Path("offerId") offerId: String
    ): RideResponseDto

    @PATCH("rides/{id}/cancel")
    suspend fun cancelRideRequest(
        @Path("id") rideId: String,
        @Body body: CancelRideBodyDto
    ): RideResponseDto

    @POST("ratings/rides/{rideId}")
    suspend fun submitRideRating(
        @Path("rideId") rideId: String,
        @Body body: SubmitRideRatingRequestDto
    ): RideRatingResponseDto

    @GET("ratings/rides/{rideId}")
    suspend fun getRideRating(
        @Path("rideId") rideId: String
    ): RideRatingResponseDto

    @GET("ratings/drivers/{driverId}")
    suspend fun getDriverRatings(
        @Path("driverId") driverId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): DriverRatingsResponseDto
}
