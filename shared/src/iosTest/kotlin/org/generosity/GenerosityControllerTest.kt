package org.generosity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.generosity.app.GenerosityAppState
import org.generosity.app.GenerosityAppStateIos
import org.generosity.data.SampleImportedCatalog
import org.generosity.domain.CenterCategory
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.NeedPriority
import org.generosity.domain.NeedStatus
import org.generosity.domain.SupportedLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GenerosityControllerTest {
    @Test
    fun iosDiscoveryAndNeedsMatchTheStateConsumedByAndroid() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val original = SampleImportedCatalog.centers()
        val goods = original.first().needs.single()
        val catalog = listOf(original.first().copy(needs = listOf(
            goods.copy(id = "normal", priority = NeedPriority.NORMAL, urgent = false),
            goods.copy(id = "covered", status = NeedStatus.COVERED),
            goods.copy(id = "cancelled", status = NeedStatus.CANCELLED),
            goods.copy(id = "expired", expiresOnIsoDate = "2026-05-12"),
            goods.copy(id = "high", priority = NeedPriority.HIGH),
            goods
        ))) + original.drop(1)
        val android = GenerosityAppState(backgroundScope, catalog = catalog)
        val ios = GenerosityAppStateIos(GenerosityAppState(backgroundScope, catalog = catalog))
        try {
            android.start()
            ios.start {}
            runCurrent()
            assertEquals(android.state.value, ios.currentState)
            android.toggleCategory(CenterCategory.CHILDREN_SUPPORT)
            ios.toggleCategory("children_support")
            android.setSearchText("  BRIGHT  ")
            ios.setSearchText("  BRIGHT  ")
            runCurrent()
            assertEquals(listOf("kc-children-001"), ios.currentState.centers.map { it.id })
            assertEquals(android.state.value, ios.currentState)
            android.selectCenter("kc-children-001")
            ios.selectCenter("kc-children-001")
            assertEquals(listOf(goods.id, "high", "normal"), ios.currentState.currentNeeds.map { it.id })
            assertEquals(listOf(true, false, false), ios.currentState.currentNeeds.map { it.isUrgent })
            assertEquals(android.state.value, ios.currentState)
            android.setLocale(SupportedLocale.SPANISH)
            ios.setLocale("es")
            runCurrent()
            assertTrue(ios.currentState.centers.isEmpty())
            assertNull(ios.currentState.selectedCenter)
            assertEquals(android.state.value, ios.currentState)
            android.setSearchText(" hogar ")
            ios.setSearchText(" hogar ")
            runCurrent()
            assertEquals("kc-children-001", ios.currentState.selectedCenter?.id)
            assertEquals(android.state.value, ios.currentState)
            android.toggleCategory(CenterCategory.OLDER_ADULTS)
            ios.toggleCategory("older_adults")
            android.setSearchText("")
            ios.setSearchText("")
            runCurrent()
            assertEquals(listOf("kc-seniors-001", "kc-children-001"), ios.currentState.centers.map { it.id })
            assertEquals(android.state.value, ios.currentState)
        } finally {
            ios.stop()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun repeatedObservationAndNavigationDoNotDuplicateAnOffer() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val android = GenerosityAppState(backgroundScope)
        val ios = GenerosityAppStateIos(GenerosityAppState(backgroundScope))
        try {
            android.start()
            ios.start {}
            android.signIn("test-user", "Test user")
            ios.signIn("test-user", "Test user")
            runCurrent()
            repeat(5) {
                ios.stop()
                ios.start {}
                android.selectCenter("kc-children-001")
                ios.selectCenter("kc-children-001")
                runCurrent()
                android.selectCenter(null)
                ios.selectCenter(null)
            }
            android.selectCenter("kc-children-001")
            ios.selectCenter("kc-children-001")
            android.offerHelp("need-school-supplies", ContributionOffer(HelpMethod.GOODS, quantity = 5))
            ios.offerGoods("need-school-supplies", 5)
            runCurrent()
            assertEquals(1, ios.currentState.contributions.size)
            assertEquals(10, ios.currentState.currentNeeds.single().quantityRemaining)
            assertTrue(ios.currentState.impact.isEmpty)
            assertEquals(android.state.value, ios.currentState)
        } finally {
            ios.stop()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restartingObservationReplacesPreviousSubscriptionAndStopDetaches() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val controller = GenerosityAppStateIos(GenerosityAppState(backgroundScope))
        try {
            var firstCalls = 0
            var secondCalls = 0
            controller.start { firstCalls++ }
            runCurrent()
            val originalCalls = firstCalls
            controller.start { secondCalls++ }
            runCurrent()
            controller.setLocale("es")
            runCurrent()
            assertEquals(originalCalls, firstCalls)
            assertTrue(secondCalls > 0)
            assertEquals("es", controller.currentState.locale.code)
            controller.stop()
            val callsAtStop = secondCalls
            controller.setLocale("en")
            runCurrent()
            assertEquals(callsAtStop, secondCalls)
        } finally {
            controller.stop()
            Dispatchers.resetMain()
        }
    }
}
