package tn.dadadrive.presentation.common

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tn.dadadrive.data.network.error.PresentableErrorMapper
import tn.dadadrive.domain.models.ErrorCategory
import tn.dadadrive.domain.models.PresentableError

class ScreenStateExtensionsTest {
    private data class User(val id: String)

    private lateinit var mapper: PresentableErrorMapper
    private val mappedError =
        PresentableError(
            message = "mapped",
            category = ErrorCategory.Server,
            isRetryable = true,
        )

    @Before
    fun setUp() {
        mapper = mockk()
        every { mapper.fromThrowable(any()) } returns mappedError
    }

    @Test
    fun `dataOrNull returns value on Loaded and null on other states`() {
        val user = User("u1")
        val loaded: ScreenState<User> = ScreenState.Loaded(user)
        assertEquals(user, loaded.dataOrNull())

        assertNull((ScreenState.Idle as ScreenState<User>).dataOrNull())
        assertNull((ScreenState.Loading as ScreenState<User>).dataOrNull())
        assertNull((ScreenState.Error(mappedError) as ScreenState<User>).dataOrNull())
    }

    @Test
    fun `errorOrNull returns PresentableError on Error and null on other states`() {
        val errorState: ScreenState<User> = ScreenState.Error(mappedError)
        assertSame(mappedError, errorState.errorOrNull())

        assertNull(ScreenState.Idle.errorOrNull())
        assertNull(ScreenState.Loading.errorOrNull())
        assertNull(ScreenState.Loaded(User("u1")).errorOrNull())
    }

    @Test
    fun `boolean accessors reflect the exact state branch`() {
        val idle: ScreenState<User> = ScreenState.Idle
        assertTrue(idle.isIdle)
        assertFalse(idle.isLoading)
        assertFalse(idle.isLoaded)
        assertFalse(idle.isError)

        val loading: ScreenState<User> = ScreenState.Loading
        assertTrue(loading.isLoading)
        assertFalse(loading.isIdle)

        val loaded: ScreenState<User> = ScreenState.Loaded(User("u1"))
        assertTrue(loaded.isLoaded)
        assertFalse(loaded.isLoading)

        val errored: ScreenState<User> = ScreenState.Error(mappedError)
        assertTrue(errored.isError)
        assertFalse(errored.isLoaded)
    }

    @Test
    fun `toScreenState success returns Loaded with the value`() {
        val user = User("u1")
        val state = Result.success(user).toScreenState(mapper)
        assertTrue(state is ScreenState.Loaded)
        assertEquals(user, (state as ScreenState.Loaded).value)
    }

    @Test
    fun `toScreenState failure returns Error with mapped PresentableError`() {
        val state: ScreenState<User> =
            Result.failure<User>(RuntimeException("boom")).toScreenState(mapper)
        assertTrue(state is ScreenState.Error)
        assertSame(mappedError, (state as ScreenState.Error).error)
    }

    @Test
    fun `asScreenStateFlow emits Loading then terminal states for each upstream emission`() =
        runBlocking {
            val user = User("u1")
            val ex = RuntimeException("boom")
            val emissions =
                flowOf<Result<User>>(Result.success(user), Result.failure(ex))
                    .asScreenStateFlow(mapper)
                    .toList()

            assertEquals(3, emissions.size)
            assertTrue("First emission must be Loading", emissions[0] is ScreenState.Loading)
            assertTrue(emissions[1] is ScreenState.Loaded)
            assertEquals(user, (emissions[1] as ScreenState.Loaded).value)
            assertTrue(emissions[2] is ScreenState.Error)
            assertSame(mappedError, (emissions[2] as ScreenState.Error).error)
        }
}
