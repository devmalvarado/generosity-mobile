package org.generosity

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.generosity.app.GenerosityAppState
import org.generosity.data.*
import org.generosity.domain.*
import org.generosity.usecase.DiscoverKindnessCentersUseCase
import org.generosity.usecase.DiscoveryQuery
import kotlin.test.*

class BackendStoreTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun pushReconcilesOwnedInboxOnceAndNeverOpensAnotherAccountsTarget() = runTest {
        val platform = Connection()
        var read = false
        platform.handler = { request -> when {
            request.path.startsWith("/api/v1/sync/catalog") -> ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON}}""")
            request.path.endsWith("/read") -> { read = true; ApiResponse(200, "{}") }
            else -> ApiResponse(200, """{"user":{"id":${request.accountId}},"contributions":[],"notices":{"items":[{"id":"notice-1","eventId":"event-1","centerId":"kc-children-001","needId":"need-school-supplies","title":{"es":"Útiles","en":"Supplies"},"createdAt":"2026-11-02T00:00:00Z","read":$read}]}}""")
        } }
        val app = GenerosityAppState(backgroundScope, backend = BackendStore(platform))
        app.signIn("1", "María", remoteAccount = true); runCurrent()
        app.receivePush("1", "notice-1", open = false); runCurrent()
        assertNull(app.state.value.pushBanner)
        app.setPreferences(NotificationPreference(urgentNeedsEnabled = true)); runCurrent()
        app.receivePush("2", "notice-1", open = true); runCurrent()
        assertNull(app.state.value.noticeDestinationId)
        app.receivePush("1", "notice-1", open = false); runCurrent()
        assertEquals("notice-1", app.state.value.pushBanner)
        assertFalse(read)
        app.dismissPushBanner()
        app.receivePush("1", "notice-1", open = false); runCurrent()
        assertNull(app.state.value.pushBanner)
        app.receivePush("1", "notice-1", open = true); runCurrent()
        assertEquals("kc-children-001", app.state.value.noticeDestinationId)
        assertTrue(read)
        assertTrue(app.state.value.notices.single().read)
        app.continueAnonymously(); runCurrent()
        assertTrue(app.state.value.notices.isEmpty())
        assertNull(app.state.value.noticeDestinationId)
        app.signIn("2", "Otra cuenta", remoteAccount = true); runCurrent()
        app.receivePush("1", "notice-1", open = true); runCurrent()
        assertNull(app.state.value.noticeDestinationId)
        assertNull(app.state.value.pushBanner)
    }

    @Test fun inboxIsPrivateReadIsDurableAndPushRegistrationIsNotReplayed() = runTest {
        val platform = Connection()
        platform.disk["account-1"] = """{"user":{"id":1},"contributions":[],"notices":{"items":[{"id":"notice-1","eventId":"event-1","centerId":"c1","needId":"n1","title":{"es":"Útiles"},"createdAt":"2026-11-02T00:00:00Z","read":false}]}}"""
        val store = BackendStore(platform)
        assertTrue(store.notices("2").isEmpty())
        assertFalse(store.notices("1").single().read)
        store.markNoticeRead("1", "notice-1")
        store.markNoticeRead("1", "notice-1")
        assertEquals(1, store.pending("1").size)
        assertTrue(BackendStore(platform).notices("1").single().read)
        assertFails { store.markNoticeRead("2", "notice-1") }
        platform.response = ApiResponse(200, "{\"deliveryConfigured\":true}")
        assertTrue(store.registerPush("1", PushDevice("token-".repeat(10), "android", true), SupportedLocale.SPANISH))
        val first = ApiJson.parse(platform.requests.last().body!!)
        val restarted = BackendStore(platform)
        assertEquals("token-".repeat(10), restarted.pushToken("android"))
        assertNull(restarted.pushToken("ios"))
        restarted.registerPush("2", PushDevice("new-token-".repeat(10), "android", false), SupportedLocale.ENGLISH)
        assertEquals(first.text("secret"), ApiJson.parse(platform.requests.last().body!!).text("secret"))
        assertEquals(platform.requests.first().path, platform.requests.last().path)
        assertTrue(restarted.pending("2").isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun coldStartCanRevokeStoredPushTokenWithoutRegisteringWithFirebaseAgain() = runTest {
        val platform = Connection()
        platform.response = ApiResponse(200, "{\"deliveryConfigured\":true}")
        BackendStore(platform).registerPush("1", PushDevice("old-token-".repeat(10), "android", true), SupportedLocale.ENGLISH)
        platform.requests.clear()
        platform.handler = { request -> when {
            request.path.startsWith("/api/v1/sync/catalog") -> ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON}}""")
            request.path == "/api/v1/sync/account" -> ApiResponse(200, """{"user":{"id":1},"contributions":[]}""")
            else -> ApiResponse(200, "{\"deliveryConfigured\":true}")
        } }
        val app = GenerosityAppState(backgroundScope, backend = BackendStore(platform))
        app.signIn("1", "María", remoteAccount = true)
        app.setPushDevice(null, "android", false)
        runCurrent()
        val revoked = ApiJson.parse(platform.requests.last { it.path.contains("push-installations") }.body!!)
        assertEquals(false, revoked["authorized"])
        assertEquals("old-token-".repeat(10), revoked["token"])
    }

    @Test fun acknowledgedReceiptProgressSurvivesRestart() = runTest {
        val platform = Connection()
        val catalog = BundledKindnessCatalog.JSON.replace("\"quantityCommitted\": 5", "\"quantityCommitted\": 5, \"quantityReceived\": 2")
        platform.response = ApiResponse(200, """{"catalog":$catalog,"updatedAt":"2026-11-02T00:00:00Z"}""")
        BackendStore(platform).refreshCatalog()
        val restored = BackendStore(platform).needById("need-school-supplies")!!
        assertEquals(2, restored.quantityReceived)
        assertEquals(5, restored.quantityCommitted)
        assertEquals(25, restored.committedPercent)
    }

    @Test fun bloodDonationRefreshPersistsAndFiltersSeparatelyFromHealthAndOrgans() = runTest {
        val document = ApiJson.parse(BundledKindnessCatalog.JSON)
        val rows = document.objects("centers")
        val blood = rows.last() + mapOf("id" to "blood-bank", "categories" to listOf("blood_donation", "health"), "needs" to emptyList<Any>())
        val platform = Connection()
        platform.response = ApiResponse(200, ApiJson.encode(mapOf("catalog" to mapOf("schemaVersion" to 1, "centers" to rows + blood))))
        BackendStore(platform).refreshCatalog()
        assertEquals("/api/v1/sync/catalog?category_version=2&progress_version=1", platform.requests.single().path)
        val discovery = DiscoverKindnessCentersUseCase(BackendStore(platform))
        assertEquals(listOf("blood-bank"), discovery(DiscoveryQuery(categories = setOf(CenterCategory.BLOOD_DONATION))).map { it.id })
        assertEquals(listOf(rows.last().text("id")), discovery(DiscoveryQuery(categories = setOf(CenterCategory.ORGAN_TISSUE_DONATION))).map { it.id })
        assertEquals(2, discovery(DiscoveryQuery(categories = setOf(CenterCategory.HEALTH))).size)
        assertEquals(2, discovery(DiscoveryQuery(categories = setOf(CenterCategory.BLOOD_DONATION, CenterCategory.CHILDREN_SUPPORT))).size)
    }

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
        platform.handler = { request -> if (request.path == "/api/v1/sync/catalog?category_version=2&progress_version=1") ApiResponse(200, """{"catalog":{"schemaVersion":1,"centers":[]},"updatedAt":"2026-11-01T00:00:00Z"}""") else ApiResponse(401, "{}") }
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
