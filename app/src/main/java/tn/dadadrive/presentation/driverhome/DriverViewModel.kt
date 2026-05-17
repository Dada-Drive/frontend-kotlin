// Équivalent Swift : Presentation/Driver/DriverHomeViewModel.swift
package tn.dadadrive.presentation.driverhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.here.sdk.core.GeoCoordinates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tn.dadadrive.core.constants.Constants
import tn.dadadrive.data.local.ActiveRideDraftCache
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.ActiveRide
import tn.dadadrive.domain.models.AvailableRide
import tn.dadadrive.domain.models.CompleteRideResult
import tn.dadadrive.domain.models.PresentableError
import tn.dadadrive.domain.models.RideStatus
import tn.dadadrive.domain.usecases.driver.AcceptRideUseCase
import tn.dadadrive.domain.usecases.driver.CancelRideUseCase
import tn.dadadrive.domain.usecases.driver.CompleteRideUseCase
import tn.dadadrive.domain.usecases.driver.GetAvailableRidesUseCase
import tn.dadadrive.domain.usecases.driver.GetMyRidesUseCase
import tn.dadadrive.domain.usecases.driver.RefuseRideUseCase
import tn.dadadrive.domain.usecases.driver.SetOnlineStatusUseCase
import tn.dadadrive.domain.usecases.driver.StartRideUseCase
import tn.dadadrive.presentation.common.ScreenState
import tn.dadadrive.presentation.common.dataOrNull
import javax.inject.Inject

/**
 * Migrated from legacy trio pattern (`_loading + _error + _data`) to multi-flow
 * `ScreenState<T>` per domain (R-2.2 Vague A).
 *
 * Three independent ScreenState flows :
 * - [onlineState] : boolean driver online status. Loading during toggle. Error on toggle failure.
 * - [availableRidesState] : list of nearby ride requests. Loading on user-triggered fetch.
 *   Polling updates silently (preserves last list on transient failure).
 * - [activeRideState] : currently assigned ride or `Idle` when none. Transitions on
 *   accept/start/complete/cancel.
 *
 * Transient UI toggles ([showAvailableRides], [showActiveRide], [completeResult],
 * [showCompleteResult]) remain raw StateFlows — they are pure UI state, not data lifecycles.
 *
 * **Error semantics (per-domain)** : each ScreenState carries its own [ScreenState.Error]
 * with a typed [PresentableError]. There is no shared global error flow. Consumers display
 * the merged error from whichever domain failed last (see DriverHomeScreen).
 *
 * **Note on data preservation across Error** : transitioning a ScreenState to `Error` loses
 * the previous `Loaded(value)` payload. Consumers that need to keep showing stale data
 * during an error condition should cache the last `dataOrNull()` in a `remember { mutableStateOf(...) }`.
 */
@HiltViewModel
@Suppress("LongParameterList")
class DriverViewModel
    @Inject
    constructor(
        private val setOnlineStatusUseCase: SetOnlineStatusUseCase,
        private val getAvailableRidesUseCase: GetAvailableRidesUseCase,
        private val acceptRideUseCase: AcceptRideUseCase,
        private val refuseRideUseCase: RefuseRideUseCase,
        private val getMyRidesUseCase: GetMyRidesUseCase,
        private val startRideUseCase: StartRideUseCase,
        private val completeRideUseCase: CompleteRideUseCase,
        private val cancelRideUseCase: CancelRideUseCase,
        private val activeRideDraftCache: ActiveRideDraftCache,
        private val errorMapper: PresentableErrorMapper,
    ) : ViewModel() {
        private val maxOfferDistanceKm = 3.0

        private val _onlineState = MutableStateFlow<ScreenState<Boolean>>(ScreenState.Loaded(false))
        val onlineState: StateFlow<ScreenState<Boolean>> = _onlineState.asStateFlow()

        private val _availableRidesState =
            MutableStateFlow<ScreenState<List<AvailableRide>>>(ScreenState.Loaded(emptyList()))
        val availableRidesState: StateFlow<ScreenState<List<AvailableRide>>> =
            _availableRidesState.asStateFlow()

        private val _activeRideState = MutableStateFlow<ScreenState<ActiveRide>>(ScreenState.Idle)
        val activeRideState: StateFlow<ScreenState<ActiveRide>> = _activeRideState.asStateFlow()

        private val _showAvailableRides = MutableStateFlow(false)
        val showAvailableRides: StateFlow<Boolean> = _showAvailableRides.asStateFlow()

        private val _showActiveRide = MutableStateFlow(false)
        val showActiveRide: StateFlow<Boolean> = _showActiveRide.asStateFlow()

        private val _completeResult = MutableStateFlow<CompleteRideResult?>(null)
        val completeResult: StateFlow<CompleteRideResult?> = _completeResult.asStateFlow()

        private val _showCompleteResult = MutableStateFlow(false)
        val showCompleteResult: StateFlow<Boolean> = _showCompleteResult.asStateFlow()

        private var pollJob: Job? = null
        private var driverLocation: GeoCoordinates? = null

        private val currentIsOnline: Boolean
            get() = _onlineState.value.dataOrNull() == true

        private val currentAvailableRides: List<AvailableRide>
            get() = _availableRidesState.value.dataOrNull().orEmpty()

        private val currentActiveRide: ActiveRide?
            get() = _activeRideState.value.dataOrNull()

        fun updateDriverLocation(location: GeoCoordinates?) {
            driverLocation = location
        }

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

        private fun clearPersistedActiveRideDraft() {
            viewModelScope.launch(Dispatchers.IO) {
                activeRideDraftCache.clear()
            }
        }

        suspend fun persistActiveRideDraftForBackground() {
            activeRideDraftCache.saveActiveOrClear(currentActiveRide)
        }

        fun toggleOnlineStatus() {
            if (_onlineState.value is ScreenState.Loading) return
            viewModelScope.launch {
                val target = !currentIsOnline
                _onlineState.value = ScreenState.Loading
                setOnlineStatusUseCase(target).fold(
                    onSuccess = { profile ->
                        _onlineState.value = ScreenState.Loaded(profile.isOnline)
                        if (profile.isOnline) {
                            fetchAvailableRides()
                            fetchMyRides()
                            startPolling()
                        } else {
                            stopPolling()
                            _availableRidesState.value = ScreenState.Loaded(emptyList())
                            _activeRideState.value = ScreenState.Idle
                            clearPersistedActiveRideDraft()
                            _showActiveRide.value = false
                        }
                    },
                    onFailure = { e ->
                        _onlineState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        private fun fetchAvailableRides() {
            if (!currentIsOnline || currentActiveRide != null) {
                _availableRidesState.value = ScreenState.Loaded(emptyList())
                return
            }
            viewModelScope.launch {
                // Only show Loading when there's no list to display ; polling updates silently.
                if (currentAvailableRides.isEmpty()) {
                    _availableRidesState.value = ScreenState.Loading
                }
                getAvailableRidesUseCase().fold(
                    onSuccess = { rides ->
                        _availableRidesState.value = ScreenState.Loaded(filterNearbyRides(rides))
                    },
                    onFailure = { e ->
                        _availableRidesState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        private fun fetchMyRides() {
            if (!currentIsOnline) return
            viewModelScope.launch {
                getMyRidesUseCase().fold(
                    onSuccess = { rides ->
                        val assigned =
                            rides.firstOrNull {
                                it.status == RideStatus.Accepted || it.status == RideStatus.InProgress
                            }
                        val current = currentActiveRide
                        if (assigned != null) {
                            if (current?.id != assigned.id) {
                                _activeRideState.value = ScreenState.Loaded(assigned)
                                _availableRidesState.value = ScreenState.Loaded(emptyList())
                                _showActiveRide.value = true
                                stopPolling()
                                startPollingActiveOnly()
                            } else {
                                _activeRideState.value = ScreenState.Loaded(assigned)
                            }
                        } else if (current != null) {
                            // Passenger may have cancelled from their app: clear stale driver active ride UI.
                            _activeRideState.value = ScreenState.Idle
                            clearPersistedActiveRideDraft()
                            _showActiveRide.value = false
                            _availableRidesState.value = ScreenState.Loaded(emptyList())
                            stopPolling()
                            startPolling()
                        }
                    },
                    onFailure = { },
                )
            }
        }

        fun acceptRide(ride: AvailableRide) {
            if (!currentIsOnline || currentActiveRide != null) return
            val loc = driverLocation
            if (loc != null) {
                val distance =
                    distanceKm(
                        loc.latitude,
                        loc.longitude,
                        ride.pickupLat,
                        ride.pickupLng,
                    )
                if (distance > maxOfferDistanceKm) {
                    _availableRidesState.value =
                        ScreenState.Error(localPresentableError("This request is outside your 3 km area."))
                    _availableRidesState.value =
                        ScreenState.Loaded(currentAvailableRides.filter { it.id != ride.id })
                    return
                }
            }
            viewModelScope.launch {
                acceptRideUseCase(ride.id).fold(
                    onSuccess = {
                        _availableRidesState.value =
                            ScreenState.Loaded(currentAvailableRides.filter { it.id != ride.id })
                    },
                    onFailure = { e ->
                        _availableRidesState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        fun refuseRide(ride: AvailableRide) {
            viewModelScope.launch {
                refuseRideUseCase(ride.id).fold(
                    onSuccess = {
                        _availableRidesState.value =
                            ScreenState.Loaded(currentAvailableRides.filter { it.id != ride.id })
                    },
                    onFailure = { e ->
                        _availableRidesState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        fun startRide() {
            val ride = currentActiveRide ?: return
            viewModelScope.launch {
                startRideUseCase(ride.id).fold(
                    onSuccess = { _activeRideState.value = ScreenState.Loaded(it) },
                    onFailure = { e ->
                        _activeRideState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        fun completeRide() {
            val ride = currentActiveRide ?: return
            viewModelScope.launch {
                completeRideUseCase(ride.id).fold(
                    onSuccess = { result ->
                        _completeResult.value = result
                        _showActiveRide.value = false
                        _showCompleteResult.value = true
                        _activeRideState.value = ScreenState.Idle
                        clearPersistedActiveRideDraft()
                        stopPolling()
                        startPolling()
                    },
                    onFailure = { e ->
                        _activeRideState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        fun cancelRide(reason: String = "Driver cancelled") {
            val ride = currentActiveRide ?: return
            viewModelScope.launch {
                cancelRideUseCase(ride.id, reason).fold(
                    onSuccess = {
                        _activeRideState.value = ScreenState.Idle
                        clearPersistedActiveRideDraft()
                        _showActiveRide.value = false
                        stopPolling()
                        startPolling()
                    },
                    onFailure = { e ->
                        _activeRideState.value = ScreenState.Error(errorMapper.fromThrowable(e))
                    },
                )
            }
        }

        private fun startPolling() {
            stopPolling()
            pollJob =
                viewModelScope.launch {
                    while (isActive) {
                        if (currentIsOnline && currentActiveRide == null) {
                            fetchAvailableRides()
                            fetchMyRides()
                        }
                        delay(Constants.DRIVER_POLL_INTERVAL_MS)
                    }
                }
        }

        private fun startPollingActiveOnly() {
            stopPolling()
            pollJob =
                viewModelScope.launch {
                    while (isActive) {
                        if (currentIsOnline) fetchMyRides()
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

        private fun filterNearbyRides(rides: List<AvailableRide>): List<AvailableRide> {
            val loc = driverLocation ?: return emptyList()
            return rides.filter { ride ->
                val distance =
                    distanceKm(
                        loc.latitude,
                        loc.longitude,
                        ride.pickupLat,
                        ride.pickupLng,
                    )
                distance <= maxOfferDistanceKm
            }
        }

        private fun distanceKm(
            lat1: Double,
            lng1: Double,
            lat2: Double,
            lng2: Double,
        ): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)
            val a =
                kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                    kotlin.math.cos(Math.toRadians(lat1)) *
                    kotlin.math.cos(Math.toRadians(lat2)) *
                    kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
            val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
            return earthRadiusKm * c
        }

        private fun localPresentableError(message: String): PresentableError =
            PresentableError(
                message = message,
                category = tn.dadadrive.domain.models.ErrorCategory.BusinessRule,
                isRetryable = false,
            )
    }
