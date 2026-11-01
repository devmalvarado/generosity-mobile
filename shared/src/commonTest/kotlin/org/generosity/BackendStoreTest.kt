package org.generosity

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.generosity.app.GenerosityAppState
import org.generosity.data.*
import org.generosity.domain.*
import kotlin.test.*

class BackendStoreTest {
    private class Connection : AppConnection {
        val disk = mutableMapOf<String, String>()
        val requests = mutableListOf<ApiRequest>()
        var response = ApiResponse(0, "")
        var handler: ((ApiRequest) -> ApiResponse)? = null
        var failWrites = false
        var counter = 0
        override fun read(key: String) = disk[key].orEmpty()
        override fun write(key: String, value: String?) {
            check(!failWrites)
            if (value == null) disk.remove(key) else disk[key] = value
        }
        override fun newOperationId() = "00000000-0000-4000-8000-${(++counter).toString().padStart(12, '0')}"
        override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) { requests += request; completion(handler?.invoke(request) ?: response) }
    }

    @Test fun lostResponseRetainsSameOperationAcrossRestartAndDoesNotLeakToOtherAccount() = runTest {
        val platform = Connection()
        val first = BackendStore(platform)
        first.offer("1", "need-a", ContributionOffer(HelpMethod.GOODS, quantity = 3, note = "Niñas \"sí\"\nñ"))
        assertFailsWith<BackendFailure> { first.flush("1") { true } }
        val key = platform.requests.single().operationId
        val restarted = BackendStore(platform)
        assertEquals(1, restarted.pending("1").size)
        assertTrue(restarted.pending("2").isEmpty())
        platform.response = ApiResponse(201, "{}")
        restarted.flush("1") { true }
        assertEquals(key, platform.requests.last().operationId)
        assertEquals("Niñas \"sí\"\nñ", ApiJson.parse(platform.requests.last().body!!).text("notes"))
        assertTrue(restarted.pending("1").isEmpty())
    }

    @Test fun accountChangeAndExpiredSessionSuspendQueue() = runTest {
        val platform = Connection()
        val store = BackendStore(platform)
        store.offer("1", null, ContributionOffer(HelpMethod.TIME, activity = "Tutoría", plannedDurationMinutes = 60))
        store.flush("1") { false }
        assertTrue(platform.requests.isEmpty())
        platform.response = ApiResponse(401, "{}")
        assertFailsWith<BackendFailure> { store.flush("1") { true } }
        assertNull(store.pending("1").single().errorCode)
    }

    @Test fun serverRejectionIsVisibleAndNeverAutomaticallyResubmitted() = runTest {
        val platform = Connection()
        val store = BackendStore(platform)
        store.offer("1", "need-a", ContributionOffer(HelpMethod.GOODS, quantity = 3))
        platform.response = ApiResponse(409, "{\"error\":{\"code\":\"capacity_exceeded\"}}")
        store.flush("1") { true }
        assertEquals("capacity_exceeded", store.pending("1").single().errorCode)
        store.flush("1") { true }
        assertEquals(1, platform.requests.size)
        store.discard("1", store.pending("1").single().id)
        assertTrue(store.pending("1").isEmpty())
    }

    @Test fun uncertainOperationCannotBeDiscardedAndSaveFailureNeverSends() = runTest {
        val platform = Connection()
        val store = BackendStore(platform)
        store.offer("1", null, ContributionOffer(HelpMethod.GOODS, quantity = 1))
        store.discard("1", store.pending("1").single().id)
        assertEquals(1, store.pending("1").size)
        platform.failWrites = true
        assertFails { store.offer("1", null, ContributionOffer(HelpMethod.GOODS, quantity = 2)) }
        assertTrue(platform.requests.isEmpty())
    }

    @Test fun invalidSnapshotNeverReplacesLastKnownCatalog() = runTest {
        val platform = Connection()
        platform.response = ApiResponse(200, "{\"catalog\":{\"schemaVersion\":1,\"centers\":[]},\"updatedAt\":\"2026-11-01T00:00:00Z\"}")
        val store = BackendStore(platform)
        store.refreshCatalog()
        val saved = platform.disk["catalog"]
        platform.response = ApiResponse(200, "{\"catalog\":{\"schemaVersion\":99,\"centers\":[]}}")
        assertFailsWith<BackendFailure> { store.refreshCatalog() }
        assertEquals(saved, platform.disk["catalog"])
    }

    @Test fun accountResponseCannotPoisonAnotherAccountsCache() = runTest {
        val platform = Connection()
        platform.response = ApiResponse(200, "{\"user\":{\"id\":2},\"contributions\":[]}")
        val store = BackendStore(platform)
        assertFails { store.refreshAccount("1") }
        assertNull(platform.disk["account-1"])
        platform.response = ApiResponse(200, """{"user":{"id":1},"contributions":[],"visits":[{"user_id":2,"center_id":"private-visit"}]}""")
        assertFails { store.refreshAccount("1") }
        assertNull(platform.disk["account-1"])
        platform.response = ApiResponse(200, "{\"user\":{\"id\":1},\"contributions\":[]}")
        store.refreshAccount("1")
        assertEquals(emptyList(), BackendStore(platform).history("1"))
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun appKeepsSavedHistoryOfflineAndRefreshesPublicDataAfterSessionExpiry() = runTest {
        val platform = Connection()
        val raw = """{"user":{"id":1},"contributions":[{"id":"offer-a","user_id":1,"title":{"es":"Alimentos"},"center_id":"center-a","need_id":"need-a","method":"goods","status":"fulfilled","quantity":2,"quantity_unit":"packs","created_at":"2026-11-01T00:00:00Z","version":3}]}"""
        platform.disk["account-1"] = raw
        platform.handler = { request -> if (request.path.endsWith("/catalog")) ApiResponse(200, """{"catalog":{"schemaVersion":1,"centers":[]},"updatedAt":"2026-11-01T00:00:00Z"}""") else ApiResponse(401, "{}") }
        val app = GenerosityAppState(backgroundScope, backend = BackendStore(platform))
        app.signIn("1", "María", remoteAccount = true)
        runCurrent()
        assertEquals("offer-a", app.state.value.contributions.single().id)
        assertEquals(1, app.state.value.impact.centersSupported)
        assertEquals("unauthenticated", app.state.value.syncError)
        assertEquals("2026-11-01T00:00:00Z", app.state.value.catalogUpdatedAt)
        app.continueAnonymously(); runCurrent()
        assertTrue(app.state.value.contributions.isEmpty())
        assertEquals(raw, platform.disk["account-1"])
    }

    @Test fun invalidOfferNeverReplacesOrEnqueuesData() = runTest {
        val platform = Connection(); val store = BackendStore(platform)
        assertFails { store.offer("1", null, ContributionOffer(HelpMethod.GOODS, quantity = 0)) }
        assertFails { store.offer("1", null, ContributionOffer(HelpMethod.TIME, plannedDurationMinutes = 60, activity = "")) }
        assertFails { store.offer("1", null, ContributionOffer(HelpMethod.GOODS, quantity = 1, note = "x".repeat(2001))) }
        assertTrue(platform.disk.isEmpty()); assertTrue(platform.requests.isEmpty())
    }

    @Test fun guestVisitsAndPreferencesStaySeparateAfterSignInAndRestart() = runTest {
        val platform = Connection()
        platform.response = ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON}}""")
        val store = BackendStore(platform); store.refreshCatalog()
        store.recordVisit(null, "kc-children-001", "2026-11-01T00:00:00Z")
        store.savePreferences(null, NotificationPreference(urgentNeedsEnabled = true))
        val restarted = BackendStore(platform)
        assertTrue("kc-children-001" in restarted.visitedCenters(null))
        assertTrue(restarted.preferences(null).urgentNeedsEnabled)
        assertFalse(restarted.preferences("1").urgentNeedsEnabled)
        assertTrue(restarted.visitedCenters("1").isEmpty())
        restarted.flush("1") { true }
        assertEquals(1, platform.requests.size)
        restarted.recordVisit("1", "kc-children-001", "2026-11-01T00:00:00Z")
        assertEquals(1, restarted.pending("1").size)
        assertTrue(restarted.pending("2").isEmpty())
    }

}
