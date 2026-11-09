package org.generosity

import kotlin.test.*
import org.generosity.app.GenerosityUiState
import org.generosity.data.BundledKindnessCatalog
import org.generosity.domain.*
import org.generosity.preview.PreviewSession

class ConnectedDesignTest {
    @Test fun needsFeedUsesOnlyVisibleCentersAndCurrentAuthoritativeNeeds() {
        val center = BundledKindnessCatalog.centers().first { it.needs.isNotEmpty() }
        val need = center.needs.first()
        val current = need.copy(id = "current", status = NeedStatus.ACTIVE, expiresOnIsoDate = "2026-11-05")
        val expired = current.copy(id = "expired", expiresOnIsoDate = "2026-11-03")
        val covered = current.copy(id = "covered", status = NeedStatus.COVERED)
        val state = GenerosityUiState(centers = listOf(center.copy(needs = listOf(expired, current, covered))), referenceIsoDate = "2026-11-04")
        assertEquals(setOf("current", "covered"), state.discoveredNeeds.map { it.id }.toSet())
        assertTrue(state.copy(centers = emptyList(), selectedCenterDetails = center).discoveredNeeds.isEmpty())
    }

    @Test fun productionNavigationNeverOpensFixturesEvenWhenEveryGroupIsEnabled() {
        val session = PreviewSession(PreviewSession.groupNames, "es", connectedOnly = true)
        assertEquals(PreviewSession.routes.filter { it.id in PreviewSession.connectedRoutes }, session.availableRoutes())
        for (id in listOf("saved", "reviews", "events", "achievements", "donate", "phone", "editor", "certificate")) {
            assertFalse(session.canOpen(id), id)
            session.navigate(id)
            assertEquals("map", session.route)
        }
        assertTrue(session.canOpen("personal"))
        session.navigate("history")
        session.updateAccess(listOf("discovery"))
        assertEquals("map", session.route)
        assertFalse(session.canOpen("history"))
        session.updateAccess(emptyList())
        assertTrue(session.availableRoutes().isEmpty())
    }

    @Test fun connectedNavigationKeepsFlagsAndFollowsPersistedLanguage() {
        val session = PreviewSession(listOf("discovery"), "en")
        session.updateLanguage("es-MX")
        assertEquals("Mapa", session.title())
        assertTrue(session.canOpen("needs"))
        assertFalse(session.canOpen("offer"))
        assertFalse(session.canOpen("personal"))
        assertFalse("donate" in PreviewSession.connectedRoutes)
        session.updateAccess(emptyList())
        assertTrue(session.availableRoutes().isEmpty())
    }
}
