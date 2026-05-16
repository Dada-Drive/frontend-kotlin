// Équivalent Swift : Presentation/Auth/NameEntry/NameEntryViewModel.swift
package tn.dadadrive.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.dadadrive.domain.protocols.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NameEntryViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun submitFullName(fullName: String, email: String? = null, onSuccess: () -> Unit) {
        val trimmed = fullName.trim()
        if (trimmed.isBlank()) {
            _error.value = "Please enter your name"
            return
        }
        val emailTrimmed = email?.trim().orEmpty()
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            userRepository.updateProfile(
                fullName = trimmed,
                email = emailTrimmed.ifBlank { null },
            )
                .fold(
                    onSuccess = {
                        onSuccess()
                    },
                    onFailure = { e ->
                        _error.value = e.message ?: "Network error"
                    },
                )
            _loading.value = false
        }
    }
}
