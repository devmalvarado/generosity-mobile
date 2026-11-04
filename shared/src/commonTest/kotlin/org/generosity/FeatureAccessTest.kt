package org.generosity

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.generosity.app.GenerosityAppState
import org.generosity.data.*
import org.generosity.domain.*
import kotlin.test.*

class FeatureAccessTest {
    private class Connection : AppConnection {
        val disk = mutableMapOf<String, String>()
        var response = ApiResponse(200, """{"features":{"contributions.create":true,"needs.progress":false}}""")
        val requests = mutableListOf<ApiRequest>()
        var handler: ((ApiRequest) -> ApiResponse)? = null
        override fun read(key: String) = disk[key].orEmpty()
        override fun write(key: String, value: String?) { if (value == null) disk.remove(key) else disk[key] = value }
        override fun newOperationId() = "test-operation"
        override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) { requests += request; completion(handler?.invoke(request) ?: response) }
    }

    @Test fun effectiveFeaturesAreAccountScopedAndUnknownOrMalformedValuesCannotEnableActions() = runTest {
        val connection = Connection()
        val store = BackendStore(connection)
        assertTrue(store.features("1").createContributions)
        assertEquals("1", connection.requests.last().accountId)
        assertEquals("/api/v1/me/features", connection.requests.last().path)
        assertFalse(store.features(null).showNeedProgress)
        assertNull(connection.requests.last().accountId)
        assertEquals("/api/v1/features", connection.requests.last().path)
        connection.response = ApiResponse(200, """{"features":{"preview.community":true,"preview.payments":false,"preview.unknown":true}}""")
        assertEquals(listOf("community"), store.features("1").previewGroups)
        connection.response = ApiResponse(200, """{"features":{"future.feature":true}}""")
        assertEquals(FeatureAccess(), store.features(null))
        connection.response = ApiResponse(200, """{"features":{"contributions.create":"true"}}""")
        assertFails { store.features("1") }
    }

    @Test fun pausedQueueKeepsItsOperationKeyAndResumesWithoutDuplicatingTheOffer() = runTest {
        val connection = Connection()
        val store = BackendStore(connection)
        store.offer("1", null, ContributionOffer(HelpMethod.GOODS, quantity = 1, quantityUnit = "box"))
        connection.response = ApiResponse(409, """{"error":{"code":"feature_disabled"}}""")
        store.flush("1") { true }
        assertEquals("feature_disabled", store.pending("1").single().errorCode)
        val resumed = BackendStore(connection)
        connection.response = ApiResponse(201, "{}")
        resumed.flush("1") { true }
        assertTrue(resumed.pending("1").isEmpty())
        assertEquals(connection.requests.first().operationId, connection.requests.last().operationId)
        assertEquals(connection.requests.first().body, connection.requests.last().body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun accountChangesAndOfflineRefreshCannotReuseTesterAccess() = runTest {
        val connection = Connection()
        var offline = false
        connection.handler = { request -> when {
            request.path.endsWith("/features") -> if (offline) ApiResponse(0, "") else ApiResponse(200,
                """{"features":{"contributions.create":${request.accountId == "1"},"needs.progress":true,"preview.community":${request.accountId == "1"}}}""")
            request.path.startsWith("/api/v1/sync/catalog") -> ApiResponse(200, """{"catalog":${BundledKindnessCatalog.JSON}}""")
            else -> ApiResponse(200, """{"user":{"id":${request.accountId}},"contributions":[]}""")
        } }
        val app = GenerosityAppState(backgroundScope, backend = BackendStore(connection))
        app.signIn("1", "María", remoteAccount = true); runCurrent()
        assertTrue(app.state.value.canRecordContributions)
        assertEquals(listOf("community"), app.state.value.features.previewGroups)
        offline = true
        app.onResume(); runCurrent()
        assertFalse(app.state.value.canRecordContributions)
        assertTrue(app.state.value.features.previewGroups.isEmpty())
        assertTrue(app.state.value.centers.isNotEmpty())
        offline = false
        app.onResume(); runCurrent()
        assertTrue(app.state.value.canRecordContributions)
        app.signIn("2", "Juan", remoteAccount = true); runCurrent()
        assertFalse(app.state.value.canRecordContributions)
        assertTrue(app.state.value.features.previewGroups.isEmpty())
        app.continueAnonymously(); runCurrent()
        assertTrue(app.state.value.features.previewGroups.isEmpty())
        assertFalse(app.state.value.features.createContributions)
        assertTrue(app.state.value.contributions.isEmpty())
    }
}
