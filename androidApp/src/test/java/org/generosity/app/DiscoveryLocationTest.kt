package org.generosity.app

import kotlinx.coroutines.*
import org.generosity.domain.Coordinates
import org.junit.Assert.*
import org.junit.Test

class DiscoveryLocationTest {
    private val point = Coordinates(19.35, -99.16)
    private val now = 1_000_000L
    private fun fix(age: Long = 0, accuracy: Float = 20f) = DeviceLocationFix(point, accuracy, now - age)

    private class Source : DeviceLocationSource {
        override var access = LocationAccess.PRECISE
        override var enabled = true
        var requests = 0
        var get: suspend () -> DeviceLocationFix? = { null }
        override suspend fun currentFix(): DeviceLocationFix? { requests++; return get() }
    }

    @Test fun validatesCoordinatesAccuracyAndMonotonicAge() {
        assertTrue(fix().isUsable(now))
        assertTrue(fix(120_000, 5_000f).isUsable(now))
        assertTrue(fix(-5_000).isUsable(now))
        for (bad in listOf(fix(120_001), fix(-5_001), fix(accuracy = -1f), fix(accuracy = 5_001f),
            fix(accuracy = Float.NaN), fix().copy(coordinates = Coordinates(Double.NaN, 0.0)),
            fix().copy(coordinates = Coordinates(0.0, 181.0)), fix().copy(elapsedMillis = 0))) {
            assertFalse(bad.isUsable(now))
        }
    }

    @Test fun automaticEntryAcceptsApproximateAndDoesNotRepeatFreshFix() = runBlocking {
        val source = Source().apply { access = LocationAccess.APPROXIMATE; get = { fix(accuracy = 1_500f) } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add)
        location.resume(); coroutineContext.job.children.toList().joinAll()
        assertEquals(listOf(point), updates)
        assertTrue(location.state.value.approximate)
        location.suspend(); location.resume()
        assertEquals(1, source.requests)
        location.suspend()
    }

    @Test fun denialDoesNotPromptOrReplaceManualAreaAndSettingsReturnRetries() = runBlocking {
        val source = Source().apply { access = LocationAccess.NONE; get = { fix() } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add)
        location.resume()
        assertEquals(0, source.requests)
        assertNull(location.state.value.problem)
        location.requestLocation()
        assertEquals(LocationProblem.DENIED, location.state.value.problem)
        assertTrue(updates.isEmpty())
        location.suspend(); source.access = LocationAccess.PRECISE; location.resume()
        coroutineContext.job.children.toList().joinAll()
        assertEquals(listOf(point), updates)
        source.access = LocationAccess.NONE; location.resume()
        assertEquals(listOf(point, null), updates)
        assertEquals(LocationProblem.DENIED, location.state.value.problem)
        location.suspend()
    }

    @Test fun disabledDeviceAndTimeoutLeaveManualDiscoveryUsable() = runBlocking {
        val source = Source().apply { enabled = false }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add, timeoutMillis = 10)
        location.resume()
        assertEquals(LocationProblem.DISABLED, location.state.value.problem)
        assertEquals(0, source.requests)
        source.enabled = true; source.get = { awaitCancellation() }
        location.resume(); coroutineContext.job.children.toList().joinAll()
        assertEquals(LocationProblem.UNAVAILABLE, location.state.value.problem)
        assertFalse(location.state.value.locating)
        assertTrue(updates.isEmpty())
        location.suspend()
    }

    @Test fun manualChoiceAndClearIgnoreEvenNonCooperativeLateFix() = runBlocking {
        val answer = CompletableDeferred<DeviceLocationFix>()
        val source = Source().apply { get = { withContext(NonCancellable) { answer.await() } } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add)
        location.resume(); yield()
        location.useManualOrigin()
        answer.complete(fix()); coroutineContext.job.children.toList().joinAll()
        location.resume()
        assertTrue(updates.isEmpty())
        assertEquals(1, source.requests)
        assertNull(location.state.value.fix)
        location.suspend()
    }

    @Test fun backgroundCancelsAcquisitionAndResumeRefreshesExpiredFix() = runBlocking {
        var clock = now
        var cancelled = false
        val source = Source().apply { get = { try { awaitCancellation() } finally { cancelled = true } } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { clock }, updates::add)
        location.resume(); yield(); location.suspend()
        coroutineContext.job.children.toList().joinAll()
        assertTrue(cancelled)
        assertTrue(updates.isEmpty())
        source.get = { fix() }; location.resume(); coroutineContext.job.children.toList().joinAll()
        clock += 120_001; source.get = { fix().copy(elapsedMillis = clock, coordinates = Coordinates(20.0, -100.0)) }
        location.suspend(); location.resume(); coroutineContext.job.children.toList().joinAll()
        assertEquals(listOf(point, null, Coordinates(20.0, -100.0)), updates)
        location.suspend()
    }

    @Test fun accessDowngradeReplacesPreviousPreciseFix() = runBlocking {
        val source = Source().apply { get = { fix() } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add)
        location.resume(); coroutineContext.job.children.toList().joinAll()
        source.access = LocationAccess.APPROXIMATE
        location.resume(); coroutineContext.job.children.toList().joinAll()
        assertEquals(listOf(point, null, point), updates)
        assertTrue(location.state.value.approximate)
        assertEquals(2, source.requests)
        location.suspend()
    }

    @Test fun staleFixAndSecurityFailureCannotBecomeDeviceOrigin() = runBlocking {
        val source = Source().apply { get = { fix(120_001) } }
        val updates = mutableListOf<Coordinates?>()
        val location = DiscoveryLocation(source, this, { now }, updates::add)
        location.resume(); coroutineContext.job.children.toList().joinAll()
        assertEquals(LocationProblem.UNAVAILABLE, location.state.value.problem)
        source.get = { throw SecurityException() }; location.requestLocation()
        coroutineContext.job.children.toList().joinAll()
        assertEquals(LocationProblem.DENIED, location.state.value.problem)
        assertTrue(updates.isEmpty())
        location.suspend()
    }
}
