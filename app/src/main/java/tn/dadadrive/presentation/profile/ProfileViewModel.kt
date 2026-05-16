package tn.dadadrive.presentation.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import tn.dadadrive.data.storage.UserManager
import tn.dadadrive.data.network.cloudinary.CloudinaryManager
import tn.dadadrive.domain.models.User
import tn.dadadrive.domain.protocols.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val cloudinaryManager: CloudinaryManager,
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

            // 2. PATCH /api/users/me (UserRepository met à jour UserManager + cache)
            val result = userRepository.updateProfile(
                fullName = fullName.trim(),
                avatarUrl = finalAvatarUrl
            )
            result.fold(
                onSuccess = {
                    _user.value = userManager.getUser()
                    _saveState.value = if (photoUploadError != null) {
                        SaveState.PartialSuccess("Photo not saved: $photoUploadError")
                    } else {
                        SaveState.Success
                    }
                },
                onFailure = { e ->
                    val offline = e is IOException ||
                        e.cause is IOException ||
                        e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                        e.message?.contains("timeout", ignoreCase = true) == true
                    if (offline) {
                        userManager.updateFullName(fullName.trim())
                        finalAvatarUrl?.let { userManager.updateAvatarUri(it) }
                        _user.value = userManager.getUser()
                        _saveState.value = SaveState.Success
                    } else {
                        _saveState.value = SaveState.Error(
                            e.message ?: "Could not save profile."
                        )
                    }
                },
            )
        }
    }

    /**
     * Réinitialise l’UI profil puis délègue à [onFinished] (p.ex. [SessionViewModel.forceLogout]).
     * L’appel API réseau est géré côté session — ne pas l’attendre ici (sinon déconnexion bloquée si timeout).
     */
    fun logout(onFinished: () -> Unit = {}) {
        _user.value = null
        onFinished()
    }
}