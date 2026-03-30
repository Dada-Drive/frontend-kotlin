package com.dadadrive.ui.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dadadrive.data.local.TokenManager
import com.dadadrive.data.local.UserManager
import com.dadadrive.data.remote.api.AuthApiService
import com.dadadrive.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApiService: AuthApiService,
    private val userManager: UserManager
) : ViewModel() {

    sealed class SessionState {
        object Checking : SessionState()
        object Valid    : SessionState()   // Session OK → MapScreen
        object Invalid  : SessionState()   // Pas de token ou token révoqué → WelcomeScreen
    }

    private val _state = MutableStateFlow<SessionState>(SessionState.Checking)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        checkSession()
    }

    /**
     * Vérifie si la session est toujours valide :
     *
     * 1. Pas de token enregistré                 → Invalid (afficher WelcomeScreen)
     * 2. Token présent + API /me répond 200       → Valid   (données user rafraîchies)
     * 3. Token présent + API répond 401 / 403     → Invalid + tokens effacés (token révoqué/expiré)
     * 4. Token présent + erreur réseau ou serveur → Valid   avec données en cache
     *    (l'utilisateur reste connecté en mode offline)
     */
    private fun checkSession() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken()

            // ── Étape 1 : pas de token → déconnecté ──────────────────────────────
            if (token.isNullOrBlank()) {
                _state.value = SessionState.Invalid
                return@launch
            }

            // ── Étape 2 : lire le cache local (utilisé comme fallback) ────────────
            val cachedUser = userManager.getUser()

            // ── Étape 3 : valider le token auprès du serveur ──────────────────────
            try {
                val userDto = authApiService.getMe()

                // Succès : on met à jour les données locales avec les infos fraîches
                userManager.saveUser(
                    User(
                        id               = userDto.id,
                        fullName         = userDto.fullName,
                        email            = userDto.email        ?: "",
                        phoneNumber      = userDto.phoneNumber  ?: "",
                        role             = userDto.role.ifBlank { "rider" },
                        profilePictureUri = userDto.avatarUrl
                    )
                )
                _state.value = SessionState.Valid

            } catch (e: HttpException) {
                when (e.code()) {
                    // 401 / 403 → token explicitement invalide ou révoqué par le serveur
                    401, 403 -> {
                        Log.w("Session", "Token invalide (${e.code()}) → déconnexion forcée")
                        tokenManager.clearTokens()
                        userManager.clearUser()
                        _state.value = SessionState.Invalid
                    }
                    // Autre erreur HTTP (500, 503…) → serveur en erreur, pas l'utilisateur
                    else -> {
                        Log.w("Session", "Erreur serveur (${e.code()}) → session cache conservée")
                        _state.value = if (cachedUser != null) SessionState.Valid
                                       else SessionState.Invalid
                    }
                }

            } catch (e: Exception) {
                // Pas de réseau, timeout, hôte injoignable, rebuild sans serveur…
                // → on conserve la session grâce aux données en cache
                Log.w("Session", "Erreur réseau (${e.message}) → fallback cache")
                _state.value = if (cachedUser != null) SessionState.Valid
                               else SessionState.Invalid
            }
        }
    }
}
