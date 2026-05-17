package tn.dadadrive.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import tn.dadadrive.data.network.envelope.ApiResponse
import tn.dadadrive.data.network.model.AddRideStopsRequestDto
import tn.dadadrive.data.network.model.CancelRideBodyDto
import tn.dadadrive.data.network.model.DriverRatingsResponseDto
import tn.dadadrive.data.network.model.FareApiResponse
import tn.dadadrive.data.network.model.NearbyDriversResponseDto
import tn.dadadrive.data.network.model.RequestRideRequestDto
import tn.dadadrive.data.network.model.RideOffersResponseDto
import tn.dadadrive.data.network.model.RideRatingResponseDto
import tn.dadadrive.data.network.model.RideResponseDto
import tn.dadadrive.data.network.model.RideStopsResponseDto
import tn.dadadrive.data.network.model.RidesResponseDto
import tn.dadadrive.data.network.model.SubmitRideRatingRequestDto

interface RidesApiService {
    @GET("rides/fare")
    suspend fun getFare(
        @Query("distance_km") distanceKm: Double,
        @Query("estimated_minutes") estimatedMinutes: Int,
    ): Response<ApiResponse<FareApiResponse>>

    @POST("rides")
    suspend fun requestRide(
        @Body body: RequestRideRequestDto,
    ): Response<ApiResponse<RideResponseDto>>

    @GET("rides/{id}/offers")
    suspend fun getRideOffers(
        @Path("id") rideId: String,
    ): Response<ApiResponse<RideOffersResponseDto>>

    @GET("rides/my")
    suspend fun getMyRides(): Response<ApiResponse<RidesResponseDto>>

    @POST("rides/{id}/stops")
    suspend fun addRideStops(
        @Path("id") rideId: String,
        @Body body: AddRideStopsRequestDto,
    ): Response<ApiResponse<RideStopsResponseDto>>

    @GET("rides/{id}/stops")
    suspend fun getRideStops(
        @Path("id") rideId: String,
    ): Response<ApiResponse<RideStopsResponseDto>>

    @GET("rides/scheduled")
    suspend fun getScheduledRides(): Response<ApiResponse<RidesResponseDto>>

    @PATCH("rides/{id}/offers/{offerId}/pick")
    suspend fun pickRideOffer(
        @Path("id") rideId: String,
        @Path("offerId") offerId: String,
    ): Response<ApiResponse<RideResponseDto>>

    @PATCH("rides/{id}/cancel")
    suspend fun cancelRideRequest(
        @Path("id") rideId: String,
        @Body body: CancelRideBodyDto,
    ): Response<ApiResponse<RideResponseDto>>

    @POST("ratings/rides/{rideId}")
    suspend fun submitRideRating(
        @Path("rideId") rideId: String,
        @Body body: SubmitRideRatingRequestDto,
    ): Response<ApiResponse<RideRatingResponseDto>>

    @GET("ratings/rides/{rideId}")
    suspend fun getRideRating(
        @Path("rideId") rideId: String,
    ): Response<ApiResponse<RideRatingResponseDto>>

    @GET("ratings/drivers/{driverId}")
    suspend fun getDriverRatings(
        @Path("driverId") driverId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Response<ApiResponse<DriverRatingsResponseDto>>

    @GET("driver/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Double = 5.0,
    ): Response<ApiResponse<NearbyDriversResponseDto>>
}
