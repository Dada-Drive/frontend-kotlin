package tn.turbodrive.data.repositories

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tn.turbodrive.data.socket.SocketEventManager

class NegotiationRepositoryImplTest {
    private val socketEventManager = mockk<SocketEventManager>(relaxed = true)
    private val repository = NegotiationRepositoryImpl(socketEventManager)

    private val capturedName = slot<String>()
    private val capturedPayload = slot<String>()

    @Before
    fun setUp() {
        every { socketEventManager.emit(capture(capturedName), capture(capturedPayload)) } returns Unit
    }

    @Test
    fun `propose emits correct event name with rideId and fare — null message omitted`() =
        runTest {
            repository.propose("r1", 12.5)

            assertEquals("negotiate:propose", capturedName.captured)
            val json = Json.parseToJsonElement(capturedPayload.captured).jsonObject
            assertEquals("r1", json["rideId"]?.jsonPrimitive?.content)
            assertEquals(12.5, json["proposedFare"]?.jsonPrimitive?.content?.toDouble())
            assertNull("message must be absent when null", json["message"])
        }

    @Test
    fun `propose includes message field in payload when provided`() =
        runTest {
            repository.propose("r1", 12.5, "too expensive")

            val json = Json.parseToJsonElement(capturedPayload.captured).jsonObject
            assertEquals("too expensive", json["message"]?.jsonPrimitive?.content)
        }

    @Test
    fun `accept emits correct event name with only rideId`() =
        runTest {
            repository.accept("r2")

            assertEquals("negotiate:accept", capturedName.captured)
            val json = Json.parseToJsonElement(capturedPayload.captured).jsonObject
            assertEquals("r2", json["rideId"]?.jsonPrimitive?.content)
            assertEquals(1, json.size)
        }

    @Test
    fun `counter emits correct event name with rideId and counterFare`() =
        runTest {
            repository.counter("r3", 9.0)

            assertEquals("negotiate:counter", capturedName.captured)
            val json = Json.parseToJsonElement(capturedPayload.captured).jsonObject
            assertEquals("r3", json["rideId"]?.jsonPrimitive?.content)
            assertEquals(9.0, json["counterFare"]?.jsonPrimitive?.content?.toDouble())
            assertNull("message must be absent when null", json["message"])
        }

    @Test
    fun `reject emits correct event name and omits null reason`() =
        runTest {
            val result = repository.reject("r4")

            assertTrue(result.isSuccess)
            assertEquals("negotiate:reject", capturedName.captured)
            val json = Json.parseToJsonElement(capturedPayload.captured).jsonObject
            assertEquals("r4", json["rideId"]?.jsonPrimitive?.content)
            assertNull("reason must be absent when null", json["reason"])
        }

    @Test
    fun `emit exception propagates as Result failure`() =
        runTest {
            every { socketEventManager.emit(any(), any()) } throws RuntimeException("socket closed")

            val result = repository.propose("r1", 12.5)

            assertFalse(result.isSuccess)
            assertTrue(result.isFailure)
        }
}
