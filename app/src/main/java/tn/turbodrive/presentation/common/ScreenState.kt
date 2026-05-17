package tn.turbodrive.presentation.common

import tn.turbodrive.domain.models.PresentableError

/**
 * Typed exhaustive UI state for Compose screens / ViewModels (R-2.1).
 *
 * Replaces the legacy `_loading: Boolean + _error: String?` pattern : forces
 * the consuming code (Compose `when`) to handle every state explicitly and
 * makes impossible states unrepresentable (e.g. `loading=true && data!=null`).
 *
 * Covariant `out T` so [Idle], [Loading] and [Error] (which carry no `T`) are
 * assignable to any `ScreenState<X>` — required for `MutableStateFlow<ScreenState<X>>`
 * to start at `ScreenState.Idle`.
 *
 * Usage (ViewModel) :
 * ```
 * private val _state = MutableStateFlow<ScreenState<UserProfile>>(ScreenState.Idle)
 * val state: StateFlow<ScreenState<UserProfile>> = _state.asStateFlow()
 *
 * fun load(id: String) = viewModelScope.launch {
 *     _state.value = ScreenState.Loading
 *     _state.value = repo.getProfile(id).toScreenState(errorMapper)
 * }
 * ```
 *
 * Usage (Compose) :
 * ```
 * when (val s = state) {
 *     ScreenState.Idle    -> Unit
 *     ScreenState.Loading -> CenteredProgress()
 *     is ScreenState.Loaded -> UserCard(s.value)
 *     is ScreenState.Error  -> ErrorBanner(s.error)
 * }
 * ```
 */
sealed interface ScreenState<out T> {
    /** Initial state, before any action has been triggered. */
    data object Idle : ScreenState<Nothing>

    /** A request is in-flight ; UI should display a progress indicator. */
    data object Loading : ScreenState<Nothing>

    /** Successfully loaded data ; UI renders [value]. */
    data class Loaded<out T>(val value: T) : ScreenState<T>

    /**
     * Failure surface carrying a fully-localized [PresentableError] (R-1.2).
     * UI should call `stringResource(error.messageResId)` when non-null,
     * falling back to [error.message] otherwise.
     */
    data class Error(val error: PresentableError) : ScreenState<Nothing>
}
