// Équivalent Swift : Presentation/Driver/DriverHomeViewModel.swift
package com.dadadrive.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.core.constants.Constants
import com.dadadrive.domain.model.ActiveRide
import com.dadadrive.domain.model.AvailableRide
import com.dadadrive.domain.model.CompleteRideResult
import com.dadadrive.domain.model.RideStatus
import com.dadadrive.domain.usecase.driver.AcceptRideUseCase
import com.dadadrive.domain.usecase.driver.CancelRideUseCase
import com.dadadrive.domain.usecase.driver.CompleteRideUseCase
import com.dadadrive.domain.usecase.driver.GetAvailableRidesUseCase
import com.dadadrive.domain.usecase.driver.GetMyRidesUseCase
import com.dadadrive.domain.usecase.driver.RefuseRideUseCase
import com.dadadrive.domain.usecase.driver.SetOnlineStatusUseCase
import com.dadadrive.domain.usecase.driver.StartRideUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class DriverViewModel @Inject constructor(
    private val setOnlineStatusUseCase: SetOnlineStatusUseCase,
    private val getAvailableRidesUseCase: GetAvailableRidesUseCase,
    private val acceptRideUseCase: AcceptRideUseCase,
    private val refuseRideUseCase: RefuseRideUseCase,
    private val getMyRidesUseCase: GetMyRidesUseCase,
    private val startRideUseCase: StartRideUseCase,
    private val completeRideUseCase: CompleteRideUseCase,
    private val cancelRideUseCase: CancelRideUseCase
) : ViewModel() {

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isTogglingOnline = MutableStateFlow(false)
    val isTogglingOnline: StateFlow<Boolean> = _isTogglingOnline.asStateFlow()

    private val _availableRides = MutableStateFlow<List<AvailableRide>>(emptyList())
    val availableRides: StateFlow<List<AvailableRide>> = _availableRides.asStateFlow()

    private val _isLoadingRides = MutableStateFlow(false)
    val isLoadingRides: StateFlow<Boolean> = _isLoadingRides.asStateFlow()

    private val _showAvailableRides = MutableStateFlow(false)
    val showAvailableRides: StateFlow<Boolean> = _showAvailableRides.asStateFlow()

    private val _activeRide = MutableStateFlow<ActiveRide?>(null)
    val activeRide: StateFlow<ActiveRide?> = _activeRide.asStateFlow()

    private val _showActiveRide = MutableStateFlow(false)
    val showActiveRide: StateFlow<Boolean> = _showActiveRide.asStateFlow()

    private val _completeResult = MutableStateFlow<CompleteRideResult?>(null)
    val completeResult: StateFlow<CompleteRideResult?> = _completeResult.asStateFlow()

    private val _showCompleteResult = MutableStateFlow(false)
    val showCompleteResult: StateFlow<Boolean> = _showCompleteResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var pollJob: Job? = null

    fun setShowAvailableRides(show: Boolean) {
        _showAvailableRides.value = show
    }

    fun setShowActiveRide(show: Boolean) {
        _showActiveRide.value = show
    }

    fun dismissCompleteToast() {
        _showCompleteResult.value = false
        _completeResult.value = null
    }

    fun toggleOnlineStatus() {
        if (_isTogglingOnline.value) return
        viewModelScope.launch {
            _isTogglingOnline.value = true
            _errorMessage.value = null
            val targetOnline = !_isOnline.value
            setOnlineStatusUseCase(targetOnline).fold(
                onSuccess = { profile ->
                    _isOnline.value = profile.isOnline
                    if (_isOnline.value) {
                        fetchAvailableRides()
                        fetchMyRides()
                        startPolling()
                    } else {
                        stopPolling()
                        _availableRides.value = emptyList()
                        _activeRide.value = null
                        _showActiveRide.value = false
                    }
                },
                onFailure = { e ->
                    _errorMessage.value = when (e) {
                        is HttpException -> when (e.code()) {
                            403 -> "Your account is pending approval"
                            else -> e.message ?: "Error ${e.code()}"
                        }
                        else -> e.message
                    }
                }
            )
            _isTogglingOnline.value = false
        }
    }

    private fun fetchAvailableRides() {
        if (!_isOnline.value) return
        viewModelScope.launch {
            _isLoadingRides.value = true
            getAvailableRidesUseCase().fold(
                onSuccess = { _availableRides.value = it },
                onFailure = { _errorMessage.value = it.message }
            )
            _isLoadingRides.value = false
        }
    }

    private fun fetchMyRides() {
        if (!_isOnline.value) return
        viewModelScope.launch {
            getMyRidesUseCase().fold(
                onSuccess = { rides ->
                    val assigned = rides.firstOrNull {
                        it.status == RideStatus.Accepted || it.status == RideStatus.InProgress
                    }
                    if (assigned != null) {
                        if (_activeRide.value?.id != assigned.id) {
                            _activeRide.value = assigned
                            _showActiveRide.value = true
                            stopPolling()
                            startPollingActiveOnly()
                        } else {
                            _activeRide.value = assigned
                        }
                    }
                },
                onFailure = { }
            )
        }
    }

    fun acceptRide(ride: AvailableRide) {
        viewModelScope.launch {
            _errorMessage.value = null
            acceptRideUseCase(ride.id).fold(
                onSuccess = {
                    _availableRides.value = _availableRides.value.filter { it.id != ride.id }
                },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    fun refuseRide(ride: AvailableRide) {
        viewModelScope.launch {
            refuseRideUseCase(ride.id).fold(
                onSuccess = {
                    _availableRides.value = _availableRides.value.filter { it.id != ride.id }
                },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    fun startRide() {
        val ride = _activeRide.value ?: return
        viewModelScope.launch {
            startRideUseCase(ride.id).fold(
                onSuccess = { _activeRide.value = it },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    fun completeRide() {
        val ride = _activeRide.value ?: return
        viewModelScope.launch {
            completeRideUseCase(ride.id).fold(
                onSuccess = { result ->
                    _completeResult.value = result
                    _showActiveRide.value = false
                    _showCompleteResult.value = true
                    _activeRide.value = null
                    stopPolling()
                    startPolling()
                },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    fun cancelRide(reason: String = "Driver cancelled") {
        val ride = _activeRide.value ?: return
        viewModelScope.launch {
            cancelRideUseCase(ride.id, reason).fold(
                onSuccess = {
                    _activeRide.value = null
                    _showActiveRide.value = false
                    stopPolling()
                    startPolling()
                },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    private fun startPolling() {
        stopPolling()
        pollJob = viewModelScope.launch {
            while (isActive) {
                if (_isOnline.value && _activeRide.value == null) {
                    fetchAvailableRides()
                    fetchMyRides()
                }
                delay(Constants.DRIVER_POLL_INTERVAL_MS)
            }
        }
    }

    private fun startPollingActiveOnly() {
        stopPolling()
        pollJob = viewModelScope.launch {
            while (isActive) {
                if (_isOnline.value) fetchMyRides()
                delay(Constants.DRIVER_POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
