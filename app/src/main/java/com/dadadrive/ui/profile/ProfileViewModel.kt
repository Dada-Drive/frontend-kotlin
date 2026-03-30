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
    private val cloudinaryManager: CloudinaryManager
) : ViewModel() {

    sealed class SaveState {
        object Idle : SaveState()
        object Loading : SaveState()
        object Success : SaveState()
        /** Nom sauvegardé mais photo échouée — affiche un warning et revient quand même. */
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
     * Sauvegarde le profil :
     * 1. Si une nouvelle photo est fournie → upload Cloudinary (stratégie overwrite)
     * 2. Appel PATCH /api/users/me avec le nom et l'URL Cloudinary
     * 3. Mise à jour locale via UserManager
     *
     * Non-bloquant sur Cloudinary : si l'upload échoue, le nom est quand même sauvegardé.
     */
    fun saveProfile(fullName: String, localAvatarUri: String? = null) {
        if (fullName.isBlank()) return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading

            // ── 1. Upload photo vers Cloudinary si nécessaire ────────────────
            val userId = _user.value?.id ?: ""
            var finalAvatarUrl: String? = _user.value?.profilePictureUri

            var photoUploadError: String? = null

            if (localAvatarUri != null && userId.isNotBlank()) {
                val publicId = cloudinaryManager.publicIdForUser(userId)
                cloudinaryManager.uploadImage(Uri.parse(localAvatarUri), publicId)
                    .onSuccess { cloudUrl ->
                        finalAvatarUrl = cloudUrl
                        userManager.saveCloudinaryPublicId(publicId)
                        Log.i("ProfileVM", "Photo uploadée : $cloudUrl")
                    }
                    .onFailure { e ->
                        photoUploadError = e.message ?: "erreur inconnue"
                        Log.e("ProfileVM", "Upload Cloudinary échoué : ${e.message}", e)
                    }
            }

            // ── 2. Appel backend PATCH /api/users/me ──────────────────────────
            try {
                val response = authApiService.updateProfile(
                    UpdateProfileRequest(
                        fullName = fullName.trim(),
                        avatarUrl = finalAvatarUrl
                    )
                )
                val updated = _user.value?.copy(
                    fullName = response.user.fullName,
                    profilePictureUri = finalAvatarUrl ?: _user.value?.profilePictureUri
                ) ?: return@launch

                userManager.saveUser(updated)
                _user.value = userManager.getUser()

                // Si la photo a échoué, revenir quand même mais afficher un avertissement
                _saveState.value = if (photoUploadError != null) {
                    SaveState.PartialSuccess("Photo non sauvegardée : $photoUploadError")
                } else {
                    SaveState.Success
                }
            } catch (e: HttpException) {
                val msg = when (e.code()) {
                    400 -> "Nom invalide ou manquant."
                    401 -> "Session expirée. Reconnectez-vous."
                    else -> "Erreur serveur (${e.code()})."
                }
                _saveState.value = SaveState.Error(msg)
            } catch (e: Exception) {
                // Pas de réseau → sauvegarder localement quand même
                userManager.updateFullName(fullName.trim())
                if (finalAvatarUrl != null) userManager.updateAvatarUri(finalAvatarUrl!!)
                _user.value = userManager.getUser()
                _saveState.value = SaveState.Success
            }
        }
    }

    fun logout() {
        tokenManager.clearTokens()
        userManager.clearUser()
    }
}
