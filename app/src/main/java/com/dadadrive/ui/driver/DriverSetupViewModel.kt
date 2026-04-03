// Équivalent Swift : Presentation/Driver/DriverSetupViewModel.swift (logique POST profil + véhicule)
package com.dadadrive.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.domain.model.VehicleType
import com.dadadrive.domain.usecase.driver.CreateDriverProfileUseCase
import com.dadadrive.domain.usecase.driver.CreateVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverSetupViewModel @Inject constructor(
    private val createDriverProfileUseCase: CreateDriverProfileUseCase,
    private val createVehicleUseCase: CreateVehicleUseCase
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun submitDriverSetup(
        fullLicenseNumber: String,
        licenseExpiry: String,
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: VehicleType,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            createDriverProfileUseCase(fullLicenseNumber, licenseExpiry).onFailure { e ->
                _error.value = e.message
                _loading.value = false
                return@launch
            }
            createVehicleUseCase(make, model, year, plateNumber, color, vehicleType).fold(
                onSuccess = {
                    _loading.value = false
                    onComplete()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _loading.value = false
                }
            )
        }
    }
}
