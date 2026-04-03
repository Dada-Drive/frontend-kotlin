package com.dadadrive.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.data.remote.cloudinary.CloudinaryManager
import com.dadadrive.data.remote.model.UpdateProfileRequest
import com.dadadrive.domain.model.User
import com.dadadrive.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService,
    private val cloudinaryManager: CloudinaryManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        data class PartialSuccess(val warning: String) : SaveState()
        data class Error(val message: String) : SaveState()
    }

    private val _user = MutableStateFlow(userManager.getUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun refresh() {
        _user.value = userManager.getUser()
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    /**
     * Save profile:
     * 1. Upload new photo to Cloudinary if provided (overwrite strategy)
     * 2. PATCH /api/users/me with name and Cloudinary URL
     * 3. Update local user via UserManager
     *
     * Non-blocking on Cloudinary: if upload fails, name is still saved.
     */
    fun saveProfile(fullName: String, localAvatarUri: String? = null) {
        if (fullName.isBlank()) return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading

            // 1. Upload photo to Cloudinary if needed
            val userId = _user.value?.id ?: ""
            var finalAvatarUrl: String? = _user.value?.profilePictureUri
            var photoUploadError: String? = null

            if (localAvatarUri != null && userId.isNotBlank()) {
                val publicId = cloudinaryManager.publicIdForUser(userId)
                cloudinaryManager.uploadImage(Uri.parse(localAvatarUri), publicId)
                    .onSuccess { cloudUrl ->
                        finalAvatarUrl = cloudUrl
                        userManager.saveCloudinaryPublicId(publicId)
                        Log.i("ProfileVM", "Photo uploaded: $cloudUrl")
                    }
                    .onFailure { e ->
                        photoUploadError = e.message ?: "unknown error"
                        Log.e("ProfileVM", "Cloudinary upload failed: ${e.message}", e)
                    }
            }

            // 2. Call backend PATCH /api/users/me
            try {
                val response = authApiService.updateProfile(
                    UpdateProfileRequest(
                        fullName = fullName.trim(),
                        avatarUrl = finalAvatarUrl
                    )
                )
                val updated = _user.value?.copy(
                    fullName = response.user.fullName ?: fullName.trim(),
                    profilePictureUri = finalAvatarUrl ?: _user.value?.profilePictureUri
                ) ?: return@launch

                userManager.saveUser(updated)
                _user.value = userManager.getUser()

                _saveState.value = if (photoUploadError != null) {
                    SaveState.PartialSuccess("Photo not saved: $photoUploadError")
                } else {
                    SaveState.Success
                }
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    400 -> "Invalid or missing name."
                    401 -> "Session expired. Please log in again."
                    else -> "Server error (${e.code()})."
                }
                _saveState.value = SaveState.Error(msg)
            } catch (e: Exception) {
                // No network → save locally anyway
                userManager.updateFullName(fullName.trim())
                if (finalAvatarUrl != null) userManager.updateAvatarUri(finalAvatarUrl!!)
                _user.value = userManager.getUser()
                _saveState.value = SaveState.Success
            }
        }
    }

    /** Équivalent Swift : AuthRepository.logout (MenuSheet). */
    fun logout(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { authRepository.logout() }
            _user.value = null
            onFinished()
        }
    }
}