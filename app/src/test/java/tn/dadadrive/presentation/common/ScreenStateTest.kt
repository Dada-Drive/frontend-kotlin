package tn.dadadrive.presentation.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError

class ScreenStateTest {
    private data class User(val id: String, val name: String)

    @Test
    fun `Idle is assignable to any typed ScreenState (variance check)`() {
        // If `out T` were missing, this assignment would fail to compile.
        val stateUser: ScreenState<User> = ScreenState.Idle
        val stateInt: ScreenState<Int> = ScreenState.Idle
        assertTrue(stateUser is ScreenState.Idle)
        assertTrue(stateInt is ScreenState.Idle)
    }

    @Test
    fun `Loading is assignable to any typed ScreenState (variance check)`() {
        val state: ScreenState<User> = ScreenState.Loading
        assertTrue(state is ScreenState.Loading)
    }

    @Test
    fun `Loaded(user) infers ScreenState of User`() {
        val user = User(id = "u1", name = "Alice")
        val state: ScreenState<User> = ScreenState.Loaded(user)
        assertTrue(state is ScreenState.Loaded)
        assertEquals(user, (state as ScreenState.Loaded).value)
    }

    @Test
    fun `Error carries the PresentableError payload`() {
        val err =
            PresentableError(
                message = "boom",
                category = ErrorCategory.Server,
                isRetryable = false,
            )
        val state: ScreenState<User> = ScreenState.Error(err)
        assertTrue(state is ScreenState.Error)
        assertEquals(err, (state as ScreenState.Error).error)
    }

    /**
     * Smoke compile-time check : exhaustive `when` without `else` clause
     * compiles, proving the sealed hierarchy is closed. If a future maintainer
     * adds a new `ScreenState` subtype without updating callers, this test
     * fails to compile -- exactly the safety the sealed interface is for.
     */
    @Test
    fun `when expression is exhaustive without else`() {
        val states: List<ScreenState<User>> =
            listOf(
                ScreenState.Idle,
                ScreenState.Loading,
                ScreenState.Loaded(User("u1", "Alice")),
                ScreenState.Error(
                    PresentableError(
                        message = "x",
                        category = ErrorCategory.Network,
                        isRetryable = true,
                    ),
                ),
            )
        val labels =
            states.map { s ->
                when (s) {
                    ScreenState.Idle -> "idle"
                    ScreenState.Loading -> "loading"
                    is ScreenState.Loaded -> "loaded:${s.value.id}"
                    is ScreenState.Error -> "error:${s.error.message}"
                }
            }
        assertEquals(listOf("idle", "loading", "loaded:u1", "error:x"), labels)
    }
}
