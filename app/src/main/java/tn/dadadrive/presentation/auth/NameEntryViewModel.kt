// Équivalent Swift : Presentation/Auth/NameEntry/NameEntryViewModel.swift
package tn.dadadrive.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError
import tn.dadadrive.domain.protocols.UserRepository
import tn.dadadrive.presentation.common.ScreenState
import javax.inject.Inject

@HiltViewModel
class NameEntryViewModel
    @Inject
    constructor(
        private val userRepository: UserRepository,
        private val errorMapper: PresentableErrorMapper,
    ) : ViewModel() {
        private val _state = MutableStateFlow<ScreenState<Unit>>(ScreenState.Idle)
        val state: StateFlow<ScreenState<Unit>> = _state.asStateFlow()

        fun submitFullName(
            fullName: String,
            email: String? = null,
            onSuccess: () -> Unit,
        ) {
            val trimmed = fullName.trim()
            if (trimmed.isBlank()) {
                _state.value = ScreenState.Error(blankNameError())
                return
            }
            val emailTrimmed = email?.trim().orEmpty()
            viewModelScope.launch {
                _state.value = ScreenState.Loading
                _state.value =
                    userRepository.updateProfile(
                        fullName = trimmed,
                        email = emailTrimmed.ifBlank { null },
                    ).fold(
                        onSuccess = {
                            onSuccess()
                            ScreenState.Loaded(Unit)
                        },
                        onFailure = { e ->
                            ScreenState.Error(errorMapper.fromThrowable(e))
                        },
                    )
            }
        }

        /** Resets [state] to [ScreenState.Idle] from an Error so the snackbar can be dismissed. */
        fun dismissError() {
            if (_state.value is ScreenState.Error) {
                _state.value = ScreenState.Idle
            }
        }

        private fun blankNameError(): PresentableError =
            PresentableError(
                message = "Please enter your name",
                category = ErrorCategory.Validation,
                isRetryable = false,
            )
    }
