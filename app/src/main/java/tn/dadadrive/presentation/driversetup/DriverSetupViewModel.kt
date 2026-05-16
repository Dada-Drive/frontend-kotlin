package tn.dadadrive.presentation.driversetup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.R
import tn.dadadrive.domain.usecases.driver.CreateDriverProfileUseCase
import tn.dadadrive.domain.usecases.driver.CreateVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverSetupViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val createDriverProfileUseCase: CreateDriverProfileUseCase,
    private val createVehicleUseCase: CreateVehicleUseCase
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Clears the current error — driven by the auto-dismiss snackbar timer. */
    fun dismissError() {
        _error.value = null
    }

    fun submitDriverSetup(
        fullLicenseNumber: String,
        licenseExpiry: String,
        cin: String,
        cinDeliveredAt: String,
        cinPhotoFront: String,
        cinPhotoBack: String,
        licensePhotoFront: String,
        licensePhotoBack: String,
        make: String,
        model: String,
        year: Int,
        plateNumber: String,
        color: String,
        vehicleType: String,
        seats: Int,
        photoFront: String,
        photoSide: String,
        photoBack: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            var profileCreateFailed = false
            createDriverProfileUseCase(
                fullLicenseNumber, licenseExpiry,
                cin, cinDeliveredAt,
                cinPhotoFront, cinPhotoBack,
                licensePhotoFront, licensePhotoBack
            ).onFailure { e ->
                // If profile already exists from a previous partial attempt, keep going with vehicle creation.
                profileCreateFailed = true
            }
            createVehicleUseCase(make, model, year, plateNumber, color, vehicleType, seats, photoFront, photoSide, photoBack).fold(
                onSuccess = {
                    _loading.value = false
                    _error.value = null
                    onComplete()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _loading.value = false
                    if (profileCreateFailed && (_error.value.isNullOrBlank() || _error.value == "HTTP 400")) {
                        _error.value = appContext.getString(R.string.driver_setup_error_profile_vehicle)
                    }
                }
            )
        }
    }
}
