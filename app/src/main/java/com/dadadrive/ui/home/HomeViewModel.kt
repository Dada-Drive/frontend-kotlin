package com.dadadrive.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.model.Driver
import com.dadadrive.domain.model.Location
import com.dadadrive.domain.model.Ride
import com.dadadrive.domain.usecase.ride.BookRideUseCase
import com.dadadrive.domain.usecase.ride.CancelRideUseCase
import com.dadadrive.domain.usecase.ride.GetNearbyDriversUseCase
import com.dadadrive.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val nearbyDrivers: List<Driver> = emptyList(),
    val currentLocation: Location? = null,
    val destination: Location? = null,
    val activeRide: Ride? = null,
    val isLoadingDrivers: Boolean = false,
    val isBookingRide: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNearbyDriversUseCase: GetNearbyDriversUseCase,
    private val bookRideUseCase: BookRideUseCase,
    private val cancelRideUseCase: CancelRideUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateCurrentLocation(location: Location) {
        _uiState.update { it.copy(currentLocation = location) }
        loadNearbyDrivers(location)
    }

    fun updateDestination(destination: Location) {
        _uiState.update { it.copy(destination = destination) }
    }

    fun loadNearbyDrivers(location: Location) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDrivers = true) }
            when (val result = getNearbyDriversUseCase(location)) {
                is Resource.Success -> _uiState.update { it.copy(nearbyDrivers = result.data, isLoadingDrivers = false) }
                is Resource.Error -> _uiState.update { it.copy(isLoadingDrivers = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun bookRide() {
        val state = _uiState.value
        val pickup = state.currentLocation ?: return
        val destination = state.destination ?: run {
            _uiState.update { it.copy(errorMessage = "Veuillez choisir une destination") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBookingRide = true, errorMessage = null) }
            when (val result = bookRideUseCase(pickup, destination)) {
                is Resource.Success -> _uiState.update { it.copy(isBookingRide = false, activeRide = result.data) }
                is Resource.Error -> _uiState.update { it.copy(isBookingRide = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun cancelRide() {
        val rideId = _uiState.value.activeRide?.id ?: return
        viewModelScope.launch {
            when (val result = cancelRideUseCase(rideId)) {
                is Resource.Success -> _uiState.update { it.copy(activeRide = null) }
                is Resource.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
