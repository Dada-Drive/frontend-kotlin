package com.dadadrive.data.repository

import com.dadadrive.data.local.dao.RideDao
import com.dadadrive.data.local.entity.toEntity
import com.dadadrive.data.remote.api.RideApi
import com.dadadrive.data.remote.dto.BookRideRequestDto
import com.dadadrive.data.remote.dto.toDto
import com.dadadrive.domain.model.Driver
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.repository.RideRepository
import com.dadadrive.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RideRepositoryImpl @Inject constructor(
    private val rideApi: RideApi,
    private val rideDao: RideDao
) : RideRepository {

    override suspend fun getNearbyDrivers(location: Location, radiusKm: Double): Resource<List<Driver>> {
        return try {
            val response = rideApi.getNearbyDrivers(location.latitude, location.longitude, radiusKm)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.map { it.toDomain() })
            } else {
                Resource.Error("Impossible de trouver des chauffeurs (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun estimateRidePrice(pickup: Location, destination: Location): Resource<Double> {
        return try {
            val response = rideApi.estimateRidePrice(
                pickup.latitude, pickup.longitude,
                destination.latitude, destination.longitude
            )
            if (response.isSuccessful) {
                Resource.Success(response.body()!!["price"] ?: 0.0)
            } else {
                Resource.Error("Impossible d'estimer le prix (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun bookRide(pickup: Location, destination: Location): Resource<Ride> {
        return try {
            val response = rideApi.bookRide(
                BookRideRequestDto(pickup.toDto(), destination.toDto())
            )
            if (response.isSuccessful) {
                val ride = response.body()!!.toDomain()
                rideDao.insertRide(ride.toEntity())
                Resource.Success(ride)
            } else {
                Resource.Error("Impossible de réserver la course (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun cancelRide(rideId: String): Resource<Unit> {
        return try {
            val response = rideApi.cancelRide(rideId)
            if (response.isSuccessful) {
                rideDao.deleteRide(rideId)
                Resource.Success(Unit)
            } else {
                Resource.Error("Impossible d'annuler la course (${response.code()})")
            }
        } catch (e: Exception) {
            Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun getRideById(rideId: String): Resource<Ride> {
        return try {
            val response = rideApi.getRideById(rideId)
            if (response.isSuccessful) {
                Resource.Success(response.body()!!.toDomain())
            } else {
                val cached = rideDao.getRideById(rideId)
                if (cached != null) Resource.Success(cached.toDomain())
                else Resource.Error("Course introuvable (${response.code()})")
            }
        } catch (e: Exception) {
            val cached = rideDao.getRideById(rideId)
            if (cached != null) Resource.Success(cached.toDomain())
            else Resource.Error("Erreur réseau : ${e.localizedMessage}", e)
        }
    }

    override suspend fun getRideHistory(page: Int, limit: Int): Resource<List<Ride>> {
        return try {
            val response = rideApi.getRideHistory(page, limit)
            if (response.isSuccessful) {
                val rides = response.body()!!.map { it.toDomain() }
                rideDao.insertRides(rides.map { it.toEntity() })
                Resource.Success(rides)
            } else {
                val cached = rideDao.getRidesPaged(limit, page * limit)
                Resource.Success(cached.map { it.toDomain() })
            }
        } catch (e: Exception) {
            val cached = rideDao.getRidesPaged(limit, page * limit)
            Resource.Success(cached.map { it.toDomain() })
        }
    }

    override fun observeRideStatus(rideId: String): Flow<Ride> {
        return rideDao.observeAllRides().map { rides ->
            rides.first { it.id == rideId }.toDomain()
        }
    }
}
