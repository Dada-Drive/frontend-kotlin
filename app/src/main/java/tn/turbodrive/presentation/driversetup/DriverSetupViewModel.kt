package tn.turbodrive.presentation.driversetup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turbodrive.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.turbodrive.data.network.error.PresentableErrorMapper
import tn.turbodrive.domain.models.ErrorCategory
import tn.turbodrive.domain.models.OcrDocType
import tn.turbodrive.domain.models.OcrFields
import tn.turbodrive.domain.usecases.driver.CreateDriverProfileUseCase
import tn.turbodrive.domain.usecases.driver.CreateVehicleUseCase
import tn.turbodrive.domain.usecases.driver.OcrProgress
import tn.turbodrive.domain.usecases.driver.UploadAndPollOcrUseCase
import tn.turbodrive.presentation.common.ScreenState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DriverSetupViewModel
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
        private val createDriverProfileUseCase: CreateDriverProfileUseCase,
        private val createVehicleUseCase: CreateVehicleUseCase,
        private val uploadAndPollOcrUseCase: UploadAndPollOcrUseCase,
        private val errorMapper: PresentableErrorMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ScreenState<Unit>>(ScreenState.Idle)
        val state: StateFlow<ScreenState<Unit>> = _state.asStateFlow()

        // R-5.2 — OCR progress per doc type (Personal CIN, License Permis).
        // Kept separate from [state] so the submission flow keeps its current
        // ScreenState semantics. Each doc type drives its own banner.

        private val _cinOcrProgress = MutableStateFlow<OcrProgress?>(null)
        val cinOcrProgress: StateFlow<OcrProgress?> = _cinOcrProgress.asStateFlow()

        private val _cinOcrFields = MutableStateFlow<OcrFields?>(null)
        val cinOcrFields: StateFlow<OcrFields?> = _cinOcrFields.asStateFlow()

        private val _permisOcrProgress = MutableStateFlow<OcrProgress?>(null)
        val permisOcrProgress: StateFlow<OcrProgress?> = _permisOcrProgress.asStateFlow()

        private val _permisOcrFields = MutableStateFlow<OcrFields?>(null)
        val permisOcrFields: StateFlow<OcrFields?> = _permisOcrFields.asStateFlow()

        /** Clears the current error — driven by the auto-dismiss snackbar timer. */
        fun dismissError() {
            if (_state.value is ScreenState.Error) {
                _state.value = ScreenState.Idle
            }
        }

        /**
         * Run OCR on a captured document image. The corresponding progress
         * [StateFlow] flips through Uploading → Processing → Ready (or a
         * failure terminal). When Ready, the parsed [OcrFields] are exposed
         * via [cinOcrFields] / [permisOcrFields] for the screen to auto-fill
         * its text inputs.
         *
         * Side-effects only — does NOT mutate the submission state.
         */
        fun uploadDocumentForOcr(
            file: File,
            docType: OcrDocType,
        ) {
            val (progress, fields) =
                when (docType) {
                    OcrDocType.Cin -> _cinOcrProgress to _cinOcrFields
                    OcrDocType.Permis -> _permisOcrProgress to _permisOcrFields
                    OcrDocType.CarteGrise, OcrDocType.Assurance -> return
                }
            viewModelScope.launch {
                uploadAndPollOcrUseCase(file, docType.wire).collect { step ->
                    progress.value = step
                    if (step is OcrProgress.Ready) {
                        fields.value = step.result.fields
                    }
                }
            }
        }

        fun consumeCinOcrFields() {
            _cinOcrFields.value = null
        }

        fun consumePermisOcrFields() {
            _permisOcrFields.value = null
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
            onComplete: () -> Unit,
        ) {
            viewModelScope.launch {
                _state.value = ScreenState.Loading
                var profileCreateFailed = false
                createDriverProfileUseCase(
                    fullLicenseNumber,
                    licenseExpiry,
                    cin,
                    cinDeliveredAt,
                    cinPhotoFront,
                    cinPhotoBack,
                    licensePhotoFront,
                    licensePhotoBack,
                ).onFailure {
                    profileCreateFailed = true
                }
                createVehicleUseCase(make, model, year, plateNumber, color, vehicleType, seats, photoFront, photoSide, photoBack).fold(
                    onSuccess = {
                        _state.value = ScreenState.Loaded(Unit)
                        onComplete()
                    },
                    onFailure = { throwable ->
                        val mapped = errorMapper.fromThrowable(throwable)
                        val finalError =
                            if (profileCreateFailed && mapped.category == ErrorCategory.Validation) {
                                mapped.copy(
                                    message = appContext.getString(R.string.driver_setup_error_profile_vehicle),
                                    messageResId = R.string.driver_setup_error_profile_vehicle,
                                )
                            } else {
                                mapped
                            }
                        _state.value = ScreenState.Error(finalError)
                    },
                )
            }
        }
    }
