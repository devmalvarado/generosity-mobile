package org.generosity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.generosity.app.GenerosityAppState
import org.generosity.domain.ContributionOffer
import org.generosity.domain.HelpMethod
import org.generosity.domain.RejectionReason

@OptIn(ExperimentalCoroutinesApi::class)
class GenerosityAppStateTest {
    @Test
    fun registeredUserCanOfferGoodsAndReturnToAnonymousBrowsing() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.start()
        app.signIn("user-1", "Ana")
        runCurrent()
        val center = app.state.value.centers.first { center ->
            center.needs.any { it.quantityRemaining != null }
        }
        val need = center.needs.first { it.quantityRemaining != null }
        app.selectCenter(center.id)

        repeat(2) {
            app.offerHelp(need.id, ContributionOffer(HelpMethod.GOODS, quantity = 1))
            runCurrent()
        }

        val state = app.state.value
        assertEquals(listOf("contribution-1", "contribution-2"), state.contributions.map { it.id })
        assertEquals(need.quantityRemaining!! - 2, state.currentNeeds.first { it.id == need.id }.quantityRemaining)
        assertTrue(state.impact.isEmpty, "Unconfirmed offers must not count as impact")

        app.continueAnonymously()
        runCurrent()
        assertTrue(app.state.value.contributions.isEmpty())
        assertTrue(app.state.value.impact.isEmpty)
    }

    @Test
    fun remoteAccountsCannotCreateDemoContributions() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.start()
        app.signIn("42", "María Peña", remoteAccount = true)
        runCurrent()
        val need = app.state.value.centers.flatMap { it.needs }.first { it.quantityRemaining != null }
        app.offerHelp(need.id, ContributionOffer(HelpMethod.GOODS, quantity = 1))
        app.recordPersonalHelp(ContributionOffer(HelpMethod.GOODS, note = "example"))
        runCurrent()
        assertTrue(app.state.value.remoteAccount)
        assertEquals(false, app.state.value.canRecordContributions)
        assertTrue(app.state.value.contributions.isEmpty())
        app.continueAnonymously()
        runCurrent()
        assertEquals(false, app.state.value.profile.isRegistered)
        assertEquals(false, app.state.value.remoteAccount)
    }

    @Test
    fun anUnknownNeedCannotBeRecordedAsPersonalHelp() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.signIn("user-1", "Ana")
        runCurrent()

        app.offerHelp("missing-need", ContributionOffer(HelpMethod.GOODS, quantity = 1))
        runCurrent()

        assertEquals(RejectionReason.NEED_NOT_FOUND, app.state.value.lastRejection)
        assertTrue(app.state.value.contributions.isEmpty())
    }

    @Test
    fun anonymousOfferDoesNotChangeTheNeedOrHistory() = runTest {
        val app = GenerosityAppState(backgroundScope)
        app.start()
        runCurrent()
        val center = app.state.value.centers.first { it.needs.any { need -> need.quantityRemaining != null } }
        val need = center.needs.first { it.quantityRemaining != null }
        app.selectCenter(center.id)

        app.offerHelp(need.id, ContributionOffer(HelpMethod.GOODS, quantity = 1))
        runCurrent()

        assertEquals(RejectionReason.REGISTRATION_REQUIRED, app.state.value.lastRejection)
        assertEquals(need.quantityRemaining, app.state.value.currentNeeds.first { it.id == need.id }.quantityRemaining)
        assertTrue(app.state.value.contributions.isEmpty())
    }
}
