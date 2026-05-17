package tn.dadadrive.presentation.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.PresentableError

/**
 * Returns the loaded value if [this] is [ScreenState.Loaded], or `null` otherwise.
 * Convenient for UI fast-paths that only care about the data.
 */
fun <T> ScreenState<T>.dataOrNull(): T? = (this as? ScreenState.Loaded<T>)?.value

/**
 * Returns the [PresentableError] if [this] is [ScreenState.Error], or `null` otherwise.
 */
fun ScreenState<*>.errorOrNull(): PresentableError? = (this as? ScreenState.Error)?.error

/** Boolean convenience accessors so callers don't need to inspect the sealed type. */
val ScreenState<*>.isIdle: Boolean get() = this is ScreenState.Idle

val ScreenState<*>.isLoading: Boolean get() = this is ScreenState.Loading

val ScreenState<*>.isLoaded: Boolean get() = this is ScreenState.Loaded<*>

val ScreenState<*>.isError: Boolean get() = this is ScreenState.Error

/**
 * Converts a [Result] to a terminal [ScreenState]. Does NOT emit [ScreenState.Loading] —
 * the caller (typically a ViewModel) is responsible for setting `Loading` before
 * invoking the suspending call that produces this [Result].
 *
 * The provided [mapper] translates the [Throwable] from `Result.failure` into a
 * fully-localized [PresentableError] (with `messageResId` resolved when possible).
 */
fun <T> Result<T>.toScreenState(mapper: PresentableErrorMapper): ScreenState<T> =
    fold(
        onSuccess = { ScreenState.Loaded(it) },
        onFailure = { ScreenState.Error(mapper.fromThrowable(it)) },
    )

/**
 * Transforms a continuous `Flow<Result<T>>` into `Flow<ScreenState<T>>` that emits
 * [ScreenState.Loading] first, then a terminal state ([Loaded] or [Error]) for each
 * upstream emission.
 *
 * Designed for Socket-driven or polled streams (R-3.x) where the UI needs the
 * Loading affordance between updates.
 *
 * For one-shot calls, prefer the synchronous [toScreenState] inside a coroutine.
 */
fun <T> Flow<Result<T>>.asScreenStateFlow(mapper: PresentableErrorMapper): Flow<ScreenState<T>> =
    flow {
        emit(ScreenState.Loading)
        collect { result -> emit(result.toScreenState(mapper)) }
    }
