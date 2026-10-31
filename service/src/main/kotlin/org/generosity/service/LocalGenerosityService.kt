package org.generosity.service

import org.generosity.data.BundledKindnessCatalog
import org.generosity.data.CatalogImport
import org.generosity.data.CatalogJson
import org.generosity.domain.CenterNeed
import org.generosity.domain.KindnessCenter
import org.generosity.domain.PublicOrigin
import org.generosity.domain.currentUtcInstant
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.readText
import kotlin.io.path.writeText

data class AcknowledgedOffer(
    val operationId: String,
    val needId: String,
    val quantity: Int,
    val recordedAtUtc: String,
    val authorityUtc: String,
    val accountId: String = "",
    val centerId: String = ""
)

data class PublicCatalog(
    val revision: Int,
    val centers: List<KindnessCenter>
)

data class ServiceRecord(
    val id: String,
    val accountId: String,
    val centerId: String?,
    val needId: String?,
    val quantity: Int,
    val recordedAtUtc: String,
    val status: String,
    val version: Int,
    val provenance: String,
    val activity: String?,
    val events: List<String>
)

sealed class OfferOutcome {
    data class Acknowledged(val offer: AcknowledgedOffer) : OfferOutcome()
    data class Rejected(val code: String) : OfferOutcome()
    data object Unavailable : OfferOutcome()
}

sealed class TransitionOutcome {
    data class Acknowledged(val record: ServiceRecord) : TransitionOutcome()
    data class Rejected(val code: String, val record: ServiceRecord? = null) : TransitionOutcome()
    data object Unavailable : TransitionOutcome()
}

/**
 * Local runtime only. It is not a selected host, database vendor or identity
 * provider. Production credentials are neither read nor required.
 */
class LocalGenerosityService(
    private val root: Path,
    private val identity: IdentityProvider = UnconfiguredIdentityProvider,
    private val payments: PaymentGate = UnconfiguredPaymentGate,
    private val delivery: NotificationDelivery = UnconfiguredNotificationDelivery,
    private val allowance: OfferAllowance = UnconfiguredOfferAllowance,
    private val staff: StaffAssignment = UnconfiguredStaffAssignment,
    private val secrets: DeploymentSecrets = DeploymentSecrets(emptySet()),
    private val publicOrigin: String? = PublicOrigin.resolve(),
    private val clock: () -> String = ::currentUtcInstant
) {
    var ready: Boolean = false
        private set

    private val mutation = Any()
    private val diagnostics = mutableListOf<String>()

    fun start() {
        synchronized(mutation) {
            Files.createDirectories(root)
            val version = readVersion()
            if (version == null) {
                writeVersion(SCHEMA_VERSION)
            } else if (version != SCHEMA_VERSION) {
                error("Unsupported local store version $version.")
            }
            if (!Files.exists(catalogPath())) Files.writeString(catalogPath(), "")
            if (!Files.exists(operationsPath())) Files.writeString(operationsPath(), "")
            if (!Files.exists(revisionPath())) writeRevision(0)
            diagnostics.clear()
            if (Files.exists(diagnosticsPath())) {
                diagnostics += diagnosticsPath().readText().lineSequence().filter { it.isNotBlank() }
            }
            ready = true
        }
    }

    fun health(): Map<String, String> = mapOf(
        "status" to if (ready) "ready" else "starting",
        "identity" to if (identity is UnconfiguredIdentityProvider) "unconfigured" else "configured",
        "payments" to if (payments.configured) "configured" else "unavailable",
        "delivery" to if (delivery.configured) "configured" else "unavailable",
        "allowance" to if (allowance is UnconfiguredOfferAllowance) "unconfigured" else "configured",
        "staff" to if (staff is UnconfiguredStaffAssignment) "unconfigured" else "configured",
        "origin" to (publicOrigin ?: "unconfigured")
    )

    fun diagnostics(): List<String> = synchronized(mutation) { diagnostics.toList() }

    fun importCatalog(text: String): CatalogImport = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        val parsed = CatalogJson.parse(text)
        if (parsed is CatalogImport.Rejected) {
            diagnose("import_rejected")
            return parsed
        }
        replace(catalogPath(), text.trim() + "\n")
        writeRevision(readRevision() + 1)
        diagnose("import_accepted")
        parsed
    }

    fun publicCatalog(): PublicCatalog = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        PublicCatalog(readRevision(), readCenters())
    }

    fun publicCenters(): List<KindnessCenter> = publicCatalog().centers

    fun offerGoods(
        credential: String,
        operationId: String,
        needId: String,
        quantity: Int,
        recordedAtUtc: String
    ): OfferOutcome = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        val accountId = acceptedAccount(credential) ?: return OfferOutcome.Unavailable
        val incoming = journalLine(
            kind = "goods",
            operationId = operationId,
            actorId = accountId,
            targetId = operationId,
            centerId = "",
            needId = needId,
            quantity = quantity,
            recordedAtUtc = recordedAtUtc,
            expectedVersion = 0,
            extra = ""
        )
        val existing = readLines().firstOrNull { it.operationId == operationId }
        if (existing != null) {
            return if (sameRequest(existing, incoming)) {
                OfferOutcome.Acknowledged(existing.toOffer())
            } else {
                diagnose("operation_conflict")
                OfferOutcome.Rejected("operation_conflict")
            }
        }
        when (allowance.permit(accountId)) {
            Allowance.UNCONFIGURED -> {
                diagnose("allowance_unconfigured")
                return OfferOutcome.Unavailable
            }
            Allowance.LIMITED -> {
                diagnose("rate_limited")
                return OfferOutcome.Rejected("rate_limited")
            }
            Allowance.PERMITTED -> Unit
        }
        if (quantity < 1) {
            diagnose("quantity_required")
            return OfferOutcome.Rejected("quantity_required")
        }
        val authority = clock()
        val located = locateNeed(needId) ?: return OfferOutcome.Rejected("need_not_found").also {
            diagnose("need_not_found")
        }
        if (located.need.isExpiredAt(authority) || !located.need.status.isOpen) {
            diagnose("need_not_open")
            return OfferOutcome.Rejected("need_not_open")
        }
        if (located.need.commitGoods(quantity) == null) {
            diagnose("quantity_exceeds_remaining")
            return OfferOutcome.Rejected("quantity_exceeds_remaining")
        }
        val stored = incoming.copy(centerId = located.centerId, authorityUtc = authority)
        append(stored)
        diagnose("offer_accepted")
        OfferOutcome.Acknowledged(stored.toOffer())
    }

    fun cancelOffer(
        credential: String,
        operationId: String,
        targetId: String,
        expectedVersion: Int
    ): TransitionOutcome = transition(
        credential = credential,
        kind = "cancel",
        operationId = operationId,
        targetId = targetId,
        expectedVersion = expectedVersion,
        staffOnly = false
    )

    fun confirmOffer(
        credential: String,
        operationId: String,
        targetId: String,
        expectedVersion: Int
    ): TransitionOutcome = transition(
        credential = credential,
        kind = "confirm",
        operationId = operationId,
        targetId = targetId,
        expectedVersion = expectedVersion,
        staffOnly = true
    )

    fun fulfillOffer(
        credential: String,
        operationId: String,
        targetId: String,
        expectedVersion: Int
    ): TransitionOutcome = transition(
        credential = credential,
        kind = "fulfill",
        operationId = operationId,
        targetId = targetId,
        expectedVersion = expectedVersion,
        staffOnly = true
    )

    fun recordPersonalHelp(
        credential: String,
        operationId: String,
        activity: String,
        durationMinutes: Int,
        recordedAtUtc: String
    ): TransitionOutcome = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        val accountId = acceptedAccount(credential) ?: return TransitionOutcome.Unavailable
        if (activity.isBlank() || durationMinutes < 1) {
            diagnose("personal_invalid")
            return TransitionOutcome.Rejected("personal_invalid")
        }
        val incoming = journalLine(
            kind = "personal",
            operationId = operationId,
            actorId = accountId,
            targetId = operationId,
            centerId = "",
            needId = "",
            quantity = durationMinutes,
            recordedAtUtc = recordedAtUtc,
            expectedVersion = 0,
            extra = activity
        )
        val existing = readLines().firstOrNull { it.operationId == operationId }
        if (existing != null) {
            return if (sameRequest(existing, incoming)) {
                TransitionOutcome.Acknowledged(recordAfter(operationId)!!)
            } else {
                diagnose("operation_conflict")
                TransitionOutcome.Rejected("operation_conflict")
            }
        }
        when (allowance.permit(accountId)) {
            Allowance.PERMITTED -> Unit
            Allowance.LIMITED -> {
                diagnose("rate_limited")
                return TransitionOutcome.Rejected("rate_limited")
            }
            Allowance.UNCONFIGURED -> {
                diagnose("allowance_unconfigured")
                return TransitionOutcome.Unavailable
            }
        }
        val stored = incoming.copy(authorityUtc = clock())
        append(stored)
        diagnose("personal_accepted")
        TransitionOutcome.Acknowledged(recordAfter(operationId)!!)
    }

    fun completePersonalHelp(
        credential: String,
        operationId: String,
        targetId: String,
        expectedVersion: Int
    ): TransitionOutcome = transition(
        credential = credential,
        kind = "personal_complete",
        operationId = operationId,
        targetId = targetId,
        expectedVersion = expectedVersion,
        staffOnly = false
    )

    fun reassignPersonalToNeed(
        credential: String,
        operationId: String,
        targetId: String,
        needId: String
    ): TransitionOutcome = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        if (acceptedAccount(credential) == null) return TransitionOutcome.Unavailable
        val current = replay().firstOrNull { it.id == targetId }
            ?: return TransitionOutcome.Rejected("need_not_found").also { diagnose("need_not_found") }
        if (current.provenance == SELF_REPORTED || current.needId == null) {
            diagnose("personal_not_center")
            return TransitionOutcome.Rejected("personal_not_center", current)
        }
        diagnose("personal_not_center")
        TransitionOutcome.Rejected("personal_not_center", current)
    }

    fun record(id: String): ServiceRecord? = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        replay().firstOrNull { it.id == id }
    }

    fun copyStoreTo(destination: Path) {
        synchronized(mutation) {
            check(ready) { "Service is not ready." }
            if (Files.exists(destination)) error("Backup destination already exists.")
            Files.createDirectories(destination)
            STORE_FILES.forEach { name ->
                val source = root.resolve(name)
                if (Files.exists(source)) {
                    Files.copy(source, destination.resolve(name))
                }
            }
        }
    }

    fun restoreFrom(snapshot: Path) {
        synchronized(mutation) {
            check(ready) { "Service is not ready." }
            val version = snapshot.resolve("schema-version").readText().trim()
            if (version != SCHEMA_VERSION.toString()) error("Unsupported local store version $version.")
            STORE_FILES.forEach { name ->
                val source = snapshot.resolve(name)
                if (Files.exists(source)) replace(root.resolve(name), source.readText())
            }
            diagnostics.clear()
            if (Files.exists(diagnosticsPath())) {
                diagnostics += diagnosticsPath().readText().lineSequence().filter { it.isNotBlank() }
            }
        }
    }

    private fun transition(
        credential: String,
        kind: String,
        operationId: String,
        targetId: String,
        expectedVersion: Int,
        staffOnly: Boolean
    ): TransitionOutcome = synchronized(mutation) {
        check(ready) { "Service is not ready." }
        val accountId = acceptedAccount(credential) ?: return TransitionOutcome.Unavailable
        val current = replay().firstOrNull { it.id == targetId }
            ?: return TransitionOutcome.Rejected("need_not_found").also { diagnose("need_not_found") }
        val actorId = if (staffOnly) {
            when (val access = staff.actorFor(credential, current.centerId.orEmpty())) {
                is StaffAccess.Assigned -> access.actorId
                StaffAccess.Denied -> {
                    diagnose("forbidden")
                    return TransitionOutcome.Rejected("forbidden", current)
                }
                StaffAccess.Unavailable -> {
                    diagnose("staff_unconfigured")
                    return TransitionOutcome.Unavailable
                }
            }
        } else {
            accountId
        }
        if (!staffOnly && accountId != current.accountId) {
            diagnose("forbidden")
            return TransitionOutcome.Rejected("forbidden", current)
        }
        val incoming = journalLine(
            kind = kind,
            operationId = operationId,
            actorId = actorId,
            targetId = targetId,
            centerId = current.centerId.orEmpty(),
            needId = current.needId.orEmpty(),
            quantity = current.quantity,
            recordedAtUtc = current.recordedAtUtc,
            expectedVersion = expectedVersion,
            extra = ""
        )
        val existing = readLines().firstOrNull { it.operationId == operationId }
        if (existing != null) {
            return if (sameRequest(existing, incoming)) {
                TransitionOutcome.Acknowledged(recordAfter(operationId)!!)
            } else {
                diagnose("operation_conflict")
                TransitionOutcome.Rejected("operation_conflict", current)
            }
        }
        if (current.provenance == SELF_REPORTED && kind != "personal_complete" && kind != "cancel") {
            diagnose("personal_not_center")
            return TransitionOutcome.Rejected("personal_not_center", current)
        }
        if (kind == "personal_complete" && current.provenance != SELF_REPORTED) {
            diagnose("forbidden")
            return TransitionOutcome.Rejected("forbidden", current)
        }
        if (current.version != expectedVersion) {
            diagnose("conflict")
            return TransitionOutcome.Rejected("conflict", current)
        }
        if (current.status == "cancelled" || current.status == "fulfilled") {
            diagnose("terminal")
            return TransitionOutcome.Rejected("terminal", current)
        }
        val nextStatus = when (kind) {
            "cancel" -> if (current.status == "offered" || current.status == "confirmed") "cancelled" else null
            "confirm" -> if (current.status == "offered" && current.provenance != SELF_REPORTED) "confirmed" else null
            "fulfill" -> if (current.status == "confirmed" && current.provenance != SELF_REPORTED) "fulfilled" else null
            "personal_complete" -> if (current.status == "offered") "fulfilled" else null
            else -> null
        }
        if (nextStatus == null) {
            diagnose("forbidden")
            return TransitionOutcome.Rejected("forbidden", current)
        }
        when (allowance.permit(accountId)) {
            Allowance.PERMITTED -> Unit
            Allowance.LIMITED -> {
                diagnose("rate_limited")
                return TransitionOutcome.Rejected("rate_limited", current)
            }
            Allowance.UNCONFIGURED -> {
                diagnose("allowance_unconfigured")
                return TransitionOutcome.Unavailable
            }
        }
        append(incoming.copy(authorityUtc = clock()))
        diagnose("${kind}_accepted")
        TransitionOutcome.Acknowledged(recordAfter(operationId)!!)
    }

    private fun acceptedAccount(credential: String): String? =
        when (val result = identity.validate(credential)) {
            is IdentityResult.Accepted -> result.accountId
            IdentityResult.Rejected, IdentityResult.Unavailable -> {
                diagnose("identity_unavailable", credential)
                null
            }
        }

    private fun diagnose(code: String, sensitive: String = "") {
        val clockText = runCatching { clock() }.getOrDefault("unknown-time")
        val line = secrets.redact("$clockText\t$code")
        diagnostics += if (sensitive.isNotEmpty() && sensitive in line) "$clockText\t$code" else line
        replace(diagnosticsPath(), diagnostics.joinToString("\n") + "\n")
    }

    private fun readCenters(): List<KindnessCenter> {
        val text = catalogPath().readText()
        val imported = if (text.isBlank()) {
            emptyList()
        } else {
            when (val parsed = CatalogJson.parse(text)) {
                is CatalogImport.Accepted -> parsed.centers
                is CatalogImport.Rejected -> emptyList()
            }
        }
        val active = replay().filter { it.provenance == CENTER_OFFER && it.status != "cancelled" && it.needId != null }
        return imported.map { center ->
            center.copy(needs = center.needs.map { need ->
                active.filter { it.needId == need.id }.fold(need) { current, record ->
                    current.commitGoods(record.quantity) ?: current
                }
            })
        }
    }

    private fun locateNeed(needId: String): LocatedNeed? {
        readCenters().forEach { center ->
            center.needs.firstOrNull { it.id == needId }?.let { return LocatedNeed(center.id, it) }
        }
        return null
    }

    private fun replay(): List<ServiceRecord> {
        val records = linkedMapOf<String, MutableRecord>()
        readLines().forEach { line -> applyLine(records, line) }
        return records.values.map { it.toRecord() }
    }

    private fun recordAfter(operationId: String): ServiceRecord? {
        val lines = readLines()
        val index = lines.indexOfFirst { it.operationId == operationId }
        if (index < 0) return null
        val records = linkedMapOf<String, MutableRecord>()
        lines.take(index + 1).forEach { applyLine(records, it) }
        val line = lines[index]
        val id = if (line.kind == "goods" || line.kind == "personal") line.operationId else line.targetId
        return records[id]?.toRecord()
    }

    private fun applyLine(records: MutableMap<String, MutableRecord>, line: JournalLine) {
        when (line.kind) {
            "goods" -> records[line.operationId] = mutableRecord(line, CENTER_OFFER, "offered")
            "personal" -> records[line.operationId] = mutableRecord(line, SELF_REPORTED, "offered")
            else -> {
                val current = records[line.targetId] ?: return
                val next = when (line.kind) {
                    "cancel" -> "cancelled"
                    "confirm" -> "confirmed"
                    "fulfill", "personal_complete" -> "fulfilled"
                    else -> return
                }
                current.status = next
                current.version += 1
                current.events += event(current.version, next, line.actorId, line.authorityUtc)
            }
        }
    }

    private fun mutableRecord(line: JournalLine, provenance: String, status: String) = MutableRecord(
        id = line.operationId,
        accountId = line.actorId,
        centerId = line.centerId.ifEmpty { null },
        needId = line.needId.ifEmpty { null },
        quantity = line.quantity,
        recordedAtUtc = line.recordedAtUtc,
        status = status,
        version = 1,
        provenance = provenance,
        activity = line.extra.ifEmpty { null },
        events = mutableListOf(event(1, status, line.actorId, line.authorityUtc))
    )

    private fun event(version: Int, status: String, actorId: String, atUtc: String): String =
        "$version $status $actorId $atUtc"

    private fun readLines(): List<JournalLine> =
        operationsPath().readText().lineSequence().filter { it.isNotBlank() }.map { decodeLine(it) }.toList()

    private fun append(line: JournalLine) {
        val lines = readLines() + line
        replace(operationsPath(), lines.joinToString("\n") { it.encode() } + "\n")
    }

    private fun readRevision(): Int =
        revisionPath().takeIf { Files.exists(it) }?.readText()?.trim()?.toIntOrNull() ?: 0

    private fun writeRevision(revision: Int) = replace(revisionPath(), "$revision\n")

    private fun readVersion(): Int? =
        versionPath().takeIf { Files.exists(it) }?.readText()?.trim()?.toIntOrNull()

    private fun writeVersion(version: Int) = replace(versionPath(), "$version\n")

    private fun versionPath() = root.resolve("schema-version")
    private fun catalogPath() = root.resolve("catalog.json")
    private fun operationsPath() = root.resolve("operations.log")
    private fun revisionPath() = root.resolve("catalog-revision")
    private fun diagnosticsPath() = root.resolve("diagnostics.log")

    private fun replace(path: Path, text: String) {
        val temporary = path.resolveSibling("${path.fileName}.tmp")
        temporary.writeText(text)
        try {
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (error: AtomicMoveNotSupportedException) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    companion object {
        const val SCHEMA_VERSION = 1
        private val STORE_FILES = listOf(
            "schema-version",
            "catalog.json",
            "operations.log",
            "catalog-revision",
            "diagnostics.log"
        )

        fun labeledDemonstrationCatalog(): String = BundledKindnessCatalog.JSON
    }
}

private const val CENTER_OFFER = "center_offer"
private const val SELF_REPORTED = "self_reported"

private data class LocatedNeed(val centerId: String, val need: CenterNeed)

private data class MutableRecord(
    val id: String,
    val accountId: String,
    val centerId: String?,
    val needId: String?,
    val quantity: Int,
    val recordedAtUtc: String,
    var status: String,
    var version: Int,
    val provenance: String,
    val activity: String?,
    val events: MutableList<String>
) {
    fun toRecord(): ServiceRecord = ServiceRecord(
        id = id,
        accountId = accountId,
        centerId = centerId,
        needId = needId,
        quantity = quantity,
        recordedAtUtc = recordedAtUtc,
        status = status,
        version = version,
        provenance = provenance,
        activity = activity,
        events = events.toList()
    )
}

private data class JournalLine(
    val kind: String,
    val operationId: String,
    val actorId: String,
    val targetId: String,
    val centerId: String,
    val needId: String,
    val quantity: Int,
    val recordedAtUtc: String,
    val authorityUtc: String,
    val expectedVersion: Int,
    val extra: String
) {
    fun encode(): String = listOf(
        kind,
        operationId,
        actorId,
        targetId,
        centerId,
        needId,
        quantity.toString(),
        recordedAtUtc,
        authorityUtc,
        expectedVersion.toString(),
        extra
    ).joinToString("\t")

    fun toOffer(): AcknowledgedOffer = AcknowledgedOffer(
        operationId = operationId,
        needId = needId,
        quantity = quantity,
        recordedAtUtc = recordedAtUtc,
        authorityUtc = authorityUtc,
        accountId = actorId,
        centerId = centerId
    )
}

private fun journalLine(
    kind: String,
    operationId: String,
    actorId: String,
    targetId: String,
    centerId: String,
    needId: String,
    quantity: Int,
    recordedAtUtc: String,
    expectedVersion: Int,
    extra: String
): JournalLine = JournalLine(
    kind = kind,
    operationId = operationId,
    actorId = actorId,
    targetId = targetId,
    centerId = centerId,
    needId = needId,
    quantity = quantity,
    recordedAtUtc = recordedAtUtc,
    authorityUtc = "",
    expectedVersion = expectedVersion,
    extra = extra
)

private fun sameRequest(existing: JournalLine, incoming: JournalLine): Boolean {
    if (existing.kind != incoming.kind || existing.operationId != incoming.operationId) return false
    if (existing.actorId.isNotEmpty() && existing.actorId != incoming.actorId) return false
    return existing.targetId == incoming.targetId &&
        existing.needId == incoming.needId &&
        existing.quantity == incoming.quantity &&
        existing.recordedAtUtc == incoming.recordedAtUtc &&
        existing.expectedVersion == incoming.expectedVersion &&
        existing.extra == incoming.extra
}

private fun decodeLine(line: String): JournalLine {
    val parts = line.split('\t')
    if (parts.size == 5 || parts.size == 6) {
        return JournalLine(
            kind = "goods",
            operationId = parts[0],
            actorId = parts.getOrElse(5) { "" },
            targetId = parts[0],
            centerId = "",
            needId = parts[1],
            quantity = parts[2].toInt(),
            recordedAtUtc = parts[3],
            authorityUtc = parts[4],
            expectedVersion = 0,
            extra = ""
        )
    }
    require(parts.size == 11) { "Operation record is incomplete." }
    return JournalLine(
        kind = parts[0],
        operationId = parts[1],
        actorId = parts[2],
        targetId = parts[3],
        centerId = parts[4],
        needId = parts[5],
        quantity = parts[6].toInt(),
        recordedAtUtc = parts[7],
        authorityUtc = parts[8],
        expectedVersion = parts[9].toInt(),
        extra = parts[10]
    )
}
