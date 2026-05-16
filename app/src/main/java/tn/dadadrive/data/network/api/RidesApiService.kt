package tn.dadadrive.data.network.api

import tn.dadadrive.data.network.model.AddRideStopsRequestDto
import tn.dadadrive.data.network.model.CancelRideBodyDto
import tn.dadadrive.data.network.model.NearbyDriversResponseDto
import tn.dadadrive.data.network.model.FareApiResponse
import tn.dadadrive.data.network.model.DriverRatingsResponseDto
import tn.dadadrive.data.network.model.RideOffersResponseDto
import tn.dadadrive.data.network.model.RideRatingResponseDto
import tn.dadadrive.data.network.model.RideStopsResponseDto
import tn.dadadrive.data.network.model.RequestRideRequestDto
import tn.dadadrive.data.network.model.RideResponseDto
import tn.dadadrive.data.network.model.RidesResponseDto
import tn.dadadrive.data.network.model.SubmitRideRatingRequestDto
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

    @GET("rides/my")
    suspend fun getMyRides(): RidesResponseDto

    @POST("rides/{id}/stops")
    suspend fun addRideStops(
        @Path("id") rideId: String,
        @Body body: AddRideStopsRequestDto
    ): RideStopsResponseDto

    @GET("rides/{id}/stops")
    suspend fun getRideStops(@Path("id") rideId: String): RideStopsResponseDto

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

    @GET("driver/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius_km") radiusKm: Double = 5.0
    ): NearbyDriversResponseDto
}
