package org.generosity

import org.generosity.preview.PreviewSession
import kotlin.test.*

class PreviewSessionTest {
    private fun session() = PreviewSession(PreviewSession.groupNames, "es")

    @Test fun nativeTabsFollowRoutesAndOnlyExposeAllowedDestinations() {
        val session = session()
        assertEquals(listOf("map", "needs", "help", "impact", "profile"), session.navigationTabs())
        for ((route, tab) in listOf("needs" to "needs", "offer" to "help", "achievements" to "impact", "settings" to "profile", "center" to "map")) {
            session.navigate(route)
            assertEquals(tab, session.navigationTab())
        }
        session.updateAccess(listOf("account"))
        assertEquals(listOf("profile"), session.navigationTabs())
        assertEquals("profile", session.navigationTab())
        session.updateAccess(listOf("community"))
        assertTrue(session.navigationTabs().isEmpty())
        assertEquals("", session.navigationTab())
        assertEquals("events", session.route)
    }

    @Test fun all34RoutesRenderInBothLanguagesWithUniqueControlsAndGuardedLinks() {
        assertEquals(34, PreviewSession.routes.size)
        assertEquals(10, PreviewSession.groupNames.size)
        for (language in listOf("es", "en")) {
            val session = PreviewSession(PreviewSession.groupNames, language)
            for (route in PreviewSession.routes) {
                session.navigate(route.id)
                assertEquals(route.title(language), session.title())
                val items = session.items()
                assertTrue(items.isNotEmpty(), route.id)
                assertEquals(items.size, items.map { it.id }.toSet().size, route.id)
                assertTrue(items.all { it.title.isNotBlank() }, route.id)
                items.filter { it.target.isNotEmpty() && it.kind == "link" }.forEach { assertTrue(session.canOpen(it.target)) }
            }
        }
    }

    @Test fun offMissingUnknownAndRevokedGroupsCannotOpenViewsOrKeepSessionData() {
        val off = PreviewSession(listOf("unknown"), "es")
        assertTrue(off.availableRoutes().isEmpty()); off.navigate("map"); assertTrue(off.items().isEmpty())
        val session = session()
        session.navigate("map"); session.setField("query", "Sangre")
        session.navigate("center"); session.act("save")
        session.updateAccess(listOf("impact"))
        assertEquals("impact", session.route)
        assertEquals("", session.value("query"))
        session.navigate("center"); assertEquals("impact", session.route)
        assertTrue(session.items().none { it.target == "campaign" })
        session.updateAccess(emptyList()); session.act("save")
        assertTrue(session.items().isEmpty())
        session.updateAccess(PreviewSession.groupNames); session.navigate("saved")
        assertTrue(session.items().none { it.kind == "center" })
    }

    @Test fun mapSearchFiltersPinsAndFavoriteListShareSelectionAndBackKeepsQuery() {
        val session = session()
        session.setField("category", "blood")
        assertEquals(listOf("c4"), session.visibleCenters().map { it.id })
        assertEquals("c4", session.selected)
        session.setField("funding", "private"); assertTrue(session.visibleCenters().isEmpty())
        session.setField("funding", "public"); session.setField("query", "Encuentro")
        session.navigate("center"); session.act("save"); session.navigate("saved")
        assertTrue(session.items().any { it.id == "c4" })
        session.back(); session.back()
        assertEquals("map", session.route); assertEquals("Encuentro", session.value("query"))
        session.setField("category", "bad"); assertEquals("blood", session.value("category"))
    }

    @Test fun offersValidateQuantitiesKeepInputAndTransitionWithoutAwardingPointsOrTouchingAnotherSession() {
        val session = session()
        session.navigate("offer"); session.setField("quantity", "0"); session.setField("date", "2026-11-09")
        session.act("submit"); assertEquals("offer", session.route); assertTrue(session.message.startsWith("Completa"))
        session.setField("quantity", "NaN"); session.act("submit"); assertEquals("offer", session.route)
        session.setField("quantity", "1"); session.setField("date", "2026-02-30"); session.act("submit"); assertEquals("offer", session.route)
        session.setField("date", "2026-11-09"); session.act("submit"); assertEquals("history", session.route)
        assertTrue(session.items().any { it.detail.contains("Pendiente") && it.detail.contains("1") })
        session.navigate("staff"); session.act("confirm-offer"); session.act("receive-offer")
        session.navigate("history"); assertTrue(session.items().any { it.detail.contains("Completado") })
        assertFalse(session.items().any { it.id == "cancel-offer" })
        session.navigate("impact"); assertEquals("1,250",session.items().first().title)
        val other = session(); other.navigate("history"); assertTrue(other.items().none { it.detail.contains("2026-11-09") })
    }

    @Test fun consentIsRequiredAndPaymentsStayBlockedEvenWhenTheirPreviewIsEnabled() {
        val session = session()
        session.navigate("referral"); session.act("submit"); assertEquals("referral",session.route)
        session.setField("consent","true");session.act("submit");assertEquals("case",session.route)
        session.navigate("donate"); assertFalse(session.items().single { it.id=="checkout" }.enabled)
        session.act("checkout");assertEquals("",session.message)
        session.navigate("tax");assertFalse(session.items().single { it.id=="issue" }.enabled)
        session.navigate("assessment");session.setField("assessment-consent","true");session.setField("followup","yes")
        session.act("submit");assertTrue(session.message.startsWith("Completa"))
    }

    @Test fun participationAndRankingCanBeReversedAndOnboardingRejectsInvalidCoordinates() {
        val session=session()
        session.navigate("event");session.act("join");assertTrue(session.items().any { it.title=="Cancelar participación" })
        session.act("join");assertTrue(session.items().any { it.title=="Quiero participar" })
        session.navigate("campaign");session.act("rank");assertTrue(session.items().any { it.title.contains("Posición 5") })
        session.act("rank");assertTrue(session.items().none { it.title.contains("Posición 5") })
        session.navigate("inbox"); session.act("read"); session.act("read")
        assertTrue(session.items().any { it.title == "Todas leídas" })
        session.navigate("onboarding")
        listOf("center-name","public-contact","address").forEach {session.setField(it,"Fictional")}
        session.setField("maps","https://maps.google.com/example");session.setField("lat","91");session.setField("lon","-99")
        session.act("submit");assertTrue(session.message.startsWith("Revisa"))
        session.setField("lat","19");session.act("submit");assertTrue(session.message.startsWith("Simulación"))
    }
}
