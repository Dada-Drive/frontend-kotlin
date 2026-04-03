// Équivalent Swift : Presentation/Auth/NameEntry/NameEntryViewModel.swift
package com.dadadrive.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.data.remote.model.UpdateProfileRequest
import com.dadadrive.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class NameEntryViewModel @Inject constructor(
    private val authApiService: AuthApiService,
    private val userManager: UserManager
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun submitFullName(fullName: String, onSuccess: () -> Unit) {
        val trimmed = fullName.trim()
        if (trimmed.isBlank()) {
            _error.value = "Please enter your name"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = authApiService.updateProfile(UpdateProfileRequest(fullName = trimmed))
                val prev: User = userManager.getUser() ?: User(
                    id = response.user.id,
                    fullName = "",
                    email = "",
                    phoneNumber = "",
                    role = response.user.role.ifBlank { "rider" }
                )
                val updated = prev.copy(
                    fullName = response.user.fullName ?: trimmed,
                    email = response.user.email ?: prev.email,
                    phoneNumber = response.user.phone ?: prev.phoneNumber,
                    role = response.user.role.ifBlank { prev.role },
                    profilePictureUri = response.user.avatarUrl ?: prev.profilePictureUri
                )
                userManager.saveUser(updated)
                onSuccess()
            } catch (e: HttpException) {
                _error.value = e.message() ?: "Server error (${e.code()})"
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _loading.value = false
            }
        }
    }
}
