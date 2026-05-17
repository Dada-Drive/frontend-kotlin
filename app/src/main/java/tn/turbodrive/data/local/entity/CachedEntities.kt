package tn.turbodrive.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user_profile")
data class CachedUserProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String?,
    val phone: String?,
    val email: String?,
    val avatarUrl: String?,
    val role: String?,
    val cachedAt: Long,
)

@Entity(tableName = "cached_wallet_balance")
data class CachedWalletBalanceEntity(
    @PrimaryKey val id: String = "singleton",
    val balance: Double,
    val currency: String,
    val status: String?,
    val cachedAt: Long,
)

@Entity(tableName = "cached_active_ride")
data class CachedActiveRideEntity(
    @PrimaryKey val id: String = "singleton",
    val rideStateJson: String,
    val cachedAt: Long,
)

@Entity(tableName = "cached_saved_place")
data class CachedSavedPlaceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val type: String,
    val cachedAt: Long,
)
