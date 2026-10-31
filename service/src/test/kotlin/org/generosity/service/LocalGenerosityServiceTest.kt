package org.generosity.service

import org.generosity.data.CatalogImport
import java.nio.file.Files
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LocalGenerosityServiceTest {
    @Test
    fun cleanStartIsReadyWithoutProductionCredentials() {
        val service = service()
        service.start()

        assertEquals("ready", service.health()["status"])
        assertEquals("unconfigured", service.health()["identity"])
        assertEquals("unavailable", service.health()["payments"])
        assertTrue(service.publicCenters().isEmpty())
    }

    @Test
    fun rejectedCatalogLeavesThePreviousDocument() {
        val service = service()
        service.start()
        assertIs<CatalogImport.Accepted>(service.importCatalog(LocalGenerosityService.labeledDemonstrationCatalog()))
        val before = service.publicCenters().size

        val rejected = service.importCatalog("{")

        assertIs<CatalogImport.Rejected>(rejected)
        assertEquals(before, service.publicCenters().size)
    }

    @Test
    fun restartKeepsAnAcknowledgedOfferAndDoesNotRepeatIt() {
        val root = Files.createTempDirectory("generosity-service")
        val clock = mutableListOf("2026-10-31T12:00:00Z")
        val identity = AcceptingTestIdentity("account-1")
        val first = LocalGenerosityService(
            root,
            identity = identity,
            allowance = TestOfferAllowance(8),
            clock = { clock.last() }
        )
        first.start()
        first.importCatalog(LocalGenerosityService.labeledDemonstrationCatalog())

        val outcome = first.offerGoods("credential", "op-1", "need-school-supplies", 5, "2026-10-31T11:00:00Z")
        assertIs<OfferOutcome.Acknowledged>(outcome)

        val restarted = LocalGenerosityService(
            root,
            identity = identity,
            allowance = TestOfferAllowance(8),
            clock = { clock.last() }
        )
        restarted.start()
        val again = restarted.offerGoods("credential", "op-1", "need-school-supplies", 5, "2026-10-31T11:00:00Z")

        assertIs<OfferOutcome.Acknowledged>(again)
        val remaining = restarted.publicCenters()
            .flatMap { it.needs }
            .first { it.id == "need-school-supplies" }
            .quantityRemaining
        assertEquals(10, remaining)
    }

    @Test
    fun unconfiguredIdentityDoesNotWriteAnOffer() {
        val service = service()
        service.start()
        service.importCatalog(LocalGenerosityService.labeledDemonstrationCatalog())

        val outcome = service.offerGoods("anyone", "op-2", "need-school-supplies", 1, "2026-10-31T11:00:00Z")

        assertIs<OfferOutcome.Unavailable>(outcome)
        assertEquals(5, service.publicCenters().flatMap { it.needs }.first { it.id == "need-school-supplies" }.quantityCommitted)
        assertFalse(service.diagnostics().any { it.contains("anyone") })
    }

    @Test
    fun rejectedImportDoesNotAdvanceRevisionOrDuplicateCenters() {
        val root = Files.createTempDirectory("generosity-service")
        val service = LocalGenerosityService(root)
        service.start()
        assertIs<CatalogImport.Accepted>(service.importCatalog(oneCenter("center-a", "need-a", 4)))
        assertIs<CatalogImport.Accepted>(service.importCatalog(oneCenter("center-a", "need-a", 4)))

        assertEquals(1, service.publicCenters().size)
        assertEquals(2, service.publicCatalog().revision)
        val before = service.publicCatalog().revision

        assertIs<CatalogImport.Rejected>(service.importCatalog("{"))
        assertIs<CatalogImport.Rejected>(service.importCatalog(duplicateCenterDocument()))

        assertEquals(before, service.publicCatalog().revision)
        assertEquals(listOf("center-a"), service.publicCenters().map { it.id })
        assertTrue(Files.list(root).use { paths ->
            paths.noneMatch { it.fileName.toString().endsWith(".tmp") }
        })
    }

    @Test
    fun removedPublicRecordKeepsAcceptedCommitmentInTheLog() {
        val service = accepting()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 4))
        assertIs<OfferOutcome.Acknowledged>(
            service.offerGoods("owner", "op-keep", "need-a", 1, "2026-10-31T11:00:00Z")
        )

        service.importCatalog(oneCenter("center-b", "need-b", 4))

        assertEquals(listOf("center-b"), service.publicCenters().map { it.id })
        assertEquals("account-1", service.record("op-keep")?.accountId)
        service.importCatalog(oneCenter("center-a", "need-a", 4))
        assertEquals(3, service.publicCenters().flatMap { it.needs }.first { it.id == "need-a" }.quantityRemaining)
    }

    @Test
    fun changedOperationPayloadIsRejectedAndLastUnitIsExclusive() {
        val service = accepting()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-last", 1))
        assertIs<OfferOutcome.Acknowledged>(
            service.offerGoods("owner", "op-last", "need-last", 1, "2026-10-31T11:00:00Z")
        )

        val changed = service.offerGoods("owner", "op-last", "need-last", 1, "2026-10-31T12:00:00Z")
        val expired = accepting(clock = { "2027-01-01T00:00:00Z" })
        expired.start()
        expired.importCatalog(oneCenter("center-a", "need-open", 2, expires = "2026-12-31"))

        assertIs<OfferOutcome.Rejected>(changed)
        assertEquals("operation_conflict", (changed as OfferOutcome.Rejected).code)
        assertEquals(0, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
        assertIs<OfferOutcome.Rejected>(
            expired.offerGoods("owner", "op-expired", "need-open", 1, "2026-10-31T11:00:00Z")
        )
        assertEquals(null, expired.record("op-expired"))
    }

    @Test
    fun concurrentOffersCannotExceedTheLastUnit() {
        val service = accepting(allowance = TestOfferAllowance(4))
        service.start()
        service.importCatalog(oneCenter("center-a", "need-last", 1))
        val barrier = CyclicBarrier(2)
        val pool = Executors.newFixedThreadPool(2)
        val results = listOf("op-a", "op-b").map { operationId ->
            pool.submit<OfferOutcome> {
                barrier.await()
                service.offerGoods("owner", operationId, "need-last", 1, "2026-10-31T11:00:00Z")
            }
        }.map { it.get(10, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, results.count { it is OfferOutcome.Acknowledged })
        assertEquals(1, results.count { it is OfferOutcome.Rejected })
        assertEquals(0, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
    }

    @Test
    fun secretsStayOutOfDiagnosticsAndALimitedOfferDoesNotWrite() {
        val secret = "top-secret-value"
        val root = Files.createTempDirectory("generosity-service")
        val service = LocalGenerosityService(
            root,
            identity = AcceptingTestIdentity("account-1"),
            allowance = TestOfferAllowance(1),
            secrets = DeploymentSecrets(setOf(secret)),
            clock = { "2026-10-31T12:00:00Z" }
        )
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 4))
        service.offerGoods(secret, "op-secret", "need-a", 1, "2026-10-31T11:00:00Z")
        val limited = service.offerGoods(secret, "op-next", "need-a", 1, "2026-10-31T11:00:00Z")

        assertIs<OfferOutcome.Rejected>(limited)
        assertEquals("rate_limited", (limited as OfferOutcome.Rejected).code)
        assertEquals(null, service.record("op-next"))
        val leaked = Files.walk(root).use { paths ->
            paths.filter { Files.isRegularFile(it) }.anyMatch { Files.readString(it).contains(secret) }
        }
        assertFalse(leaked)
        assertFalse(service.health().values.any { secret in it })
    }

    @Test
    fun isolatedRestoreKeepsTheAcknowledgedReservationAndRejectsAnotherVersion() {
        val service = accepting()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 4))
        service.offerGoods("owner", "op-keep", "need-a", 1, "2026-10-31T11:00:00Z")
        val snapshot = Files.createTempDirectory("generosity-parent").resolve("snapshot")
        service.copyStoreTo(snapshot)
        service.offerGoods("owner", "op-later", "need-a", 1, "2026-10-31T11:00:00Z")

        service.restoreFrom(snapshot)

        assertEquals("account-1", service.record("op-keep")?.accountId)
        assertEquals(null, service.record("op-later"))
        assertEquals(3, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
        val upgraded = Files.createTempDirectory("generosity-parent").resolve("upgraded")
        service.copyStoreTo(upgraded)
        Files.writeString(upgraded.resolve("schema-version"), "2\n")
        val refused = LocalGenerosityService(upgraded)
        assertFailsWith<IllegalStateException> { refused.start() }
    }

    @Test
    fun ownerCanCancelOnceAndCannotConfirmACenterOffer() {
        val service = staffed()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 2))
        assertIs<OfferOutcome.Acknowledged>(
            service.offerGoods("owner", "op-1", "need-a", 1, "2026-10-31T11:00:00Z")
        )
        val selfConfirm = service.confirmOffer("owner", "confirm-1", "op-1", 1)
        val confirmed = service.confirmOffer("staff", "confirm-2", "op-1", 1)
        val cancelled = service.cancelOffer("owner", "cancel-1", "op-1", 2)
        val retried = service.cancelOffer("owner", "cancel-1", "op-1", 2)

        assertIs<TransitionOutcome.Rejected>(selfConfirm)
        assertEquals("forbidden", (selfConfirm as TransitionOutcome.Rejected).code)
        assertIs<TransitionOutcome.Acknowledged>(confirmed)
        assertIs<TransitionOutcome.Acknowledged>(cancelled)
        assertIs<TransitionOutcome.Acknowledged>(retried)
        assertEquals("cancelled", service.record("op-1")?.status)
        assertEquals(3, service.record("op-1")?.events?.size)
        assertEquals(2, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
    }

    @Test
    fun fulfilledOfferRejectsALaterCancellationWithoutReleasingTwice() {
        val service = staffed()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 2))
        service.offerGoods("owner", "op-1", "need-a", 1, "2026-10-31T11:00:00Z")
        service.confirmOffer("staff", "confirm-1", "op-1", 1)
        val fulfilled = service.fulfillOffer("staff", "fulfill-1", "op-1", 2)
        val cancelled = service.cancelOffer("owner", "cancel-late", "op-1", 3)

        assertIs<TransitionOutcome.Acknowledged>(fulfilled)
        assertIs<TransitionOutcome.Rejected>(cancelled)
        assertEquals("terminal", (cancelled as TransitionOutcome.Rejected).code)
        assertEquals(1, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
    }

    @Test
    fun cancellationAndFulfillmentOfTheSameVersionConflict() {
        val service = staffed()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 1))
        service.offerGoods("owner", "op-1", "need-a", 1, "2026-10-31T11:00:00Z")
        service.confirmOffer("staff", "confirm-1", "op-1", 1)
        val barrier = CyclicBarrier(2)
        val pool = Executors.newFixedThreadPool(2)
        val results = listOf(
            pool.submit<TransitionOutcome> {
                barrier.await()
                service.cancelOffer("owner", "cancel-race", "op-1", 2)
            },
            pool.submit<TransitionOutcome> {
                barrier.await()
                service.fulfillOffer("staff", "fulfill-race", "op-1", 2)
            }
        ).map { it.get(10, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, results.count { it is TransitionOutcome.Acknowledged })
        assertEquals(1, results.count { it is TransitionOutcome.Rejected && it.code == "conflict" })
        val status = service.record("op-1")?.status
        val remaining = service.publicCenters().flatMap { it.needs }.first().quantityRemaining
        if (status == "cancelled") assertEquals(1, remaining) else assertEquals(0, remaining)
    }

    @Test
    fun personalHelpStaysSelfReportedAndCannotEnterACenterNeed() {
        val service = accepting()
        service.start()
        service.importCatalog(oneCenter("center-a", "need-a", 2))
        val recorded = service.recordPersonalHelp("owner", "personal-1", "tutoring", 40, "2026-10-31T11:00:00Z")
        val completed = service.completePersonalHelp("owner", "personal-done", "personal-1", 1)
        val reassigned = service.reassignPersonalToNeed("owner", "personal-move", "personal-1", "need-a")

        assertIs<TransitionOutcome.Acknowledged>(recorded)
        assertEquals("self_reported", (recorded as TransitionOutcome.Acknowledged).record.provenance)
        assertEquals(null, recorded.record.needId)
        assertIs<TransitionOutcome.Acknowledged>(completed)
        assertEquals("self_reported", service.record("personal-1")?.provenance)
        assertEquals("fulfilled", service.record("personal-1")?.status)
        assertIs<TransitionOutcome.Rejected>(reassigned)
        assertEquals("personal_not_center", (reassigned as TransitionOutcome.Rejected).code)
        assertEquals(2, service.publicCenters().flatMap { it.needs }.first().quantityRemaining)
        val other = LocalGenerosityService(
            Files.createTempDirectory("generosity-service"),
            identity = MappedTestIdentity(mapOf("owner" to "account-1", "other" to "account-2")),
            allowance = TestOfferAllowance(4),
            clock = { "2026-10-31T12:00:00Z" }
        )
        other.start()
        other.recordPersonalHelp("owner", "personal-2", "cooking", 20, "2026-10-31T11:00:00Z")
        val denied = other.completePersonalHelp("other", "personal-2-done", "personal-2", 1)
        assertIs<TransitionOutcome.Rejected>(denied)
        assertEquals("forbidden", (denied as TransitionOutcome.Rejected).code)
        assertEquals("offered", other.record("personal-2")?.status)
    }

    private fun service() = LocalGenerosityService(Files.createTempDirectory("generosity-service"))

    private fun accepting(
        clock: () -> String = { "2026-10-31T12:00:00Z" },
        allowance: OfferAllowance = TestOfferAllowance(8)
    ) = LocalGenerosityService(
        Files.createTempDirectory("generosity-service"),
        identity = AcceptingTestIdentity("account-1"),
        allowance = allowance,
        clock = clock
    )

    private fun staffed() = LocalGenerosityService(
        Files.createTempDirectory("generosity-service"),
        identity = MappedTestIdentity(mapOf("owner" to "account-1", "staff" to "staff-1")),
        allowance = TestOfferAllowance(8),
        staff = AssignedStaff("staff-1", "center-a"),
        clock = { "2026-10-31T12:00:00Z" }
    )
}

private class AcceptingTestIdentity(private val accountId: String) : IdentityProvider {
    override fun validate(credential: String): IdentityResult = IdentityResult.Accepted(accountId)
}

private class MappedTestIdentity(private val accounts: Map<String, String>) : IdentityProvider {
    override fun validate(credential: String): IdentityResult =
        accounts[credential]?.let { IdentityResult.Accepted(it) } ?: IdentityResult.Rejected
}

private class TestOfferAllowance(private val maxAccepted: Int) : OfferAllowance {
    private var accepted = 0
    override fun permit(accountId: String): Allowance {
        if (accepted >= maxAccepted) return Allowance.LIMITED
        accepted += 1
        return Allowance.PERMITTED
    }
}

private class AssignedStaff(private val actorId: String, private val centerId: String) : StaffAssignment {
    override fun actorFor(credential: String, centerId: String): StaffAccess =
        if (credential == "staff" && centerId == this.centerId) StaffAccess.Assigned(actorId) else StaffAccess.Denied
}

private fun oneCenter(
    centerId: String,
    needId: String,
    required: Int,
    expires: String = "2026-12-31"
) = """{"schemaVersion":1,"centers":[${center(centerId, needId, required, expires)}]}"""

private fun duplicateCenterDocument() =
    """{"schemaVersion":1,"centers":[${center("center-a", "need-a", 1, "2026-12-31")},${center("center-a", "need-b", 1, "2026-12-31")}]}"""

private fun center(centerId: String, needId: String, required: Int, expires: String) = """
{"id":"$centerId","name":{"en":"Center","es":"Centro"},"description":{"en":"Help","es":"Ayuda"},"categories":["education"],"coordinates":{"latitude":19.43,"longitude":-99.13},"address":{"en":"Address","es":"Direccion"},"verificationStatus":"imported","needs":[{"id":"$needId","title":{"en":"Need","es":"Necesidad"},"description":{"en":"Goods","es":"Bienes"},"helpMethods":["goods"],"expiresOnIsoDate":"$expires","quantityRequired":$required}]}
""".trim()
