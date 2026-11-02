package org.generosity.data

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import org.generosity.domain.*

data class ApiRequest(val method: String, val path: String, val body: String?, val operationId: String?, val accountId: String?)
data class ApiResponse(val status: Int, val body: String)

/** Platform adapters provide HTTPS, account-bound credentials and atomic protected storage. */
interface AppConnection {
    fun send(request: ApiRequest, completion: (ApiResponse) -> Unit)
    // Empty means absent; read failures must throw, never erase a queue.
    @Throws(Exception::class) fun read(key: String): String
    @Throws(Exception::class) fun write(key: String, value: String?)
    fun newOperationId(): String
}

data class PendingOperation(val id: String, val errorCode: String?)
class BackendFailure(val code: String) : RuntimeException(code)

class BackendStore(private val connection: AppConnection) : KindnessCenterRepository, NeedCatalog {
    private var catalog: List<KindnessCenter> = emptyList()
    private val mutex = Mutex()
    var updatedAt: String? = null
        private set

    init {
        runCatching { connection.read("catalog").takeIf { it.isNotEmpty() } }.getOrNull()?.let { raw ->
            runCatching { parseCatalog(raw) }.onSuccess { catalog = it; updatedAt = ApiJson.parse(raw).text("updatedAt") }
        }
    }

    override suspend fun centers() = catalog
    override suspend fun needById(id: String) = catalog.flatMap { it.needs }.firstOrNull { it.id == id }
    override suspend fun needsForCenter(centerId: String) = catalog.firstOrNull { it.id == centerId }?.needs.orEmpty()
    override suspend fun save(need: CenterNeed): Unit = error("Only the backend can mutate a remote need")

    suspend fun refreshCatalog() = mutex.withLock {
        val raw = request(ApiRequest("GET", "/api/v1/sync/catalog?category_version=2", null, null, null))
        val accepted = parseCatalog(raw)
        connection.write("catalog", raw)
        catalog = accepted
        updatedAt = ApiJson.parse(raw).text("updatedAt")
    }

    private fun parseCatalog(raw: String): List<KindnessCenter> {
        val document = ApiJson.parse(raw)
        val parsed = CatalogJson.parse(ApiJson.encode(document.objectValue("catalog")))
        return (parsed as? CatalogImport.Accepted)?.centers ?: throw BackendFailure("invalid_response")
    }

    fun history(userId: String): List<Contribution> = connection.read("account-$userId").takeIf { it.isNotEmpty() }?.let { parseHistory(it, userId) }.orEmpty()

    suspend fun refreshAccount(userId: String) = mutex.withLock {
        val raw = request(ApiRequest("GET", "/api/v1/sync/account", null, null, userId))
        parseHistory(raw, userId)
        connection.write("account-$userId", raw)
    }

    private fun parseHistory(raw: String, userId: String): List<Contribution> {
        val json = ApiJson.parse(raw)
        require(json.objectValue("user")["id"].toString() == userId)
        if (json.containsKey("visits")) json.objects("visits").forEach { require(it["user_id"].toString() == userId) }
        return json.objects("contributions").map { row ->
            require(row["user_id"].toString() == userId)
            val title = (row["title"] as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() }
                ?: mapOf("en" to "Contribution", "es" to "Aportación")
            val personal = row.text("provenance") == "self_reported"
            Contribution(
                id = requireNotNull(row.text("id")), userId = userId, centerId = row.text("center_id"),
                needId = row.text("need_id"), title = LocalizedText(title),
                helpMethod = HelpMethod.entries.first { it.code == row.text("method") },
                recordedAtIsoDateTime = requireNotNull(row.text("created_at")),
                status = ContributionStatus.entries.first { it.code == row.text("status") },
                quantity = row.int("quantity"), quantityUnit = row.text("quantity_unit"),
                activity = row.text("activity"), availability = row.text("availability"), note = row.text("notes"),
                plannedDurationMinutes = row.int("minutes"),
                actualDurationMinutes = row.int("actual_minutes"),
                provenance = if (personal) ContributionProvenance.SELF_REPORTED else ContributionProvenance.CENTER_OFFER,
                syncState = SyncState.ACKNOWLEDGED, version = requireNotNull(row.int("version"))
            )
        }
    }

    suspend fun offer(userId: String, needId: String?, offer: ContributionOffer) = mutex.withLock {
        require(offer.helpMethod != HelpMethod.MONEY)
        require((offer.note?.length ?: 0) <= 2000 && (offer.activity?.length ?: 0) <= 500 && (offer.availability?.length ?: 0) <= 500 && (offer.quantityUnit?.length ?: 0) <= 60)
        val body = mutableMapOf<String, Any?>("method" to offer.helpMethod.code)
        needId?.let { body["need_id"] = it }
        if (offer.helpMethod == HelpMethod.GOODS) {
            require((offer.quantity ?: 0) in 1..1_000_000)
            body["quantity"] = offer.quantity
            offer.quantityUnit?.let { body["quantity_unit"] = it }
        } else {
            require((offer.plannedDurationMinutes ?: 0) in 1..525600 && !offer.activity.isNullOrBlank())
            body["minutes"] = offer.plannedDurationMinutes
            body["activity"] = offer.activity
            offer.availability?.let { body["availability"] = it }
        }
        offer.note?.let { body["notes"] = it }
        enqueue(userId, "/api/v1/contributions", body)
    }

    suspend fun transition(userId: String, id: String, action: String, version: Int) = mutex.withLock {
        require(action in listOf("cancel", "complete"))
        require(id.matches(Regex("[A-Za-z0-9-]{1,100}")))
        enqueue(userId, "/api/v1/contributions/$id/transition", mapOf("action" to action, "version" to version))
    }

    suspend fun recordVisit(userId: String?, centerId: String, at: String) = mutex.withLock {
        require(catalog.any { it.id == centerId })
        val body = mapOf("center_id" to centerId, "occurred_at" to at, "explicit_action" to true)
        if (userId != null) enqueue(userId, "/api/v1/visits", body)
        else {
            val visits = visitedCenters(null) + centerId
            connection.write("guest-visits", ApiJson.encode(mapOf("visits" to visits.map { mapOf("center_id" to it) })))
        }
    }

    fun visitedCenters(userId: String?): Set<String> {
        val raw = connection.read(if (userId == null) "guest-visits" else "account-$userId")
        val document = raw.takeIf { it.isNotEmpty() }?.let(ApiJson::parse)
        if (userId != null && document != null) require(document.objectValue("user")["id"].toString() == userId)
        val received = if (document?.containsKey("visits") == true) document.objects("visits").mapNotNull { it.text("center_id") } else emptyList()
        val pending = if (userId == null) emptyList() else queue(userId).filter { it.text("path") == "/api/v1/visits" && it.text("error") == null }.mapNotNull { it.objectValue("body").text("center_id") }
        return (received + pending).toSet()
    }

    fun preferences(userId: String?): NotificationPreference {
        val raw = connection.read("preferences-${userId ?: "guest"}")
        if (raw.isEmpty()) return NotificationPreference()
        val json = ApiJson.parse(raw)
        return NotificationPreference(nearbyCentersEnabled = json["nearby"] == true, urgentNeedsEnabled = json["urgent"] == true, remindersEnabled = json["reminders"] == true, allCategoryInterests = json["all"] == true, categoryInterests = (json["categories"] as? List<*>)?.mapNotNull { code -> CenterCategory.entries.firstOrNull { it.code == code } }?.toSet().orEmpty())
    }

    fun savePreferences(userId: String?, preferences: NotificationPreference) = connection.write("preferences-${userId ?: "guest"}", ApiJson.encode(mapOf(
        "nearby" to preferences.nearbyCentersEnabled, "urgent" to preferences.urgentNeedsEnabled, "reminders" to preferences.remindersEnabled, "all" to preferences.allCategoryInterests, "categories" to preferences.categoryInterests.map { it.code }
    )))

    private fun enqueue(userId: String, path: String, body: Map<String, Any?>) {
        val rows = queue(userId).toMutableList()
        if (rows.size >= 100) throw BackendFailure("queue_full")
        rows += mapOf("id" to connection.newOperationId(), "path" to path, "body" to body)
        saveQueue(userId, rows)
    }

    fun pending(userId: String): List<PendingOperation> = queue(userId).map { PendingOperation(requireNotNull(it.text("id")), it.text("error")) }

    suspend fun discard(userId: String, operationId: String) = mutex.withLock {
        // Unknown outcomes must retain their idempotency key until reconciled.
        saveQueue(userId, queue(userId).filterNot { it.text("id") == operationId && it.text("error") != null })
    }

    suspend fun flush(userId: String, stillCurrentAccount: () -> Boolean) = mutex.withLock {
        val rows = queue(userId).toMutableList()
        for (row in rows.toList()) {
            if (!stillCurrentAccount()) return@withLock
            if (row.text("error") != null) continue
            val response = send(ApiRequest("POST", requireNotNull(row.text("path")), ApiJson.encode(row.objectValue("body")), row.text("id"), userId))
            if (response.status in 200..299) rows.remove(row)
            else if (response.status in listOf(400, 403, 404, 409, 422)) {
                rows[rows.indexOf(row)] = row + ("error" to errorCode(response))
            } else throw BackendFailure(errorCode(response))
            saveQueue(userId, rows)
        }
    }

    private fun queue(userId: String): List<Map<String, Any?>> = connection.read("queue-$userId").takeIf { it.isNotEmpty() }?.let { ApiJson.parse(it).objects("operations") }.orEmpty()
    private fun saveQueue(userId: String, rows: List<Map<String, Any?>>) = connection.write("queue-$userId", ApiJson.encode(mapOf("operations" to rows)))
    private suspend fun request(request: ApiRequest): String {
        val response = send(request)
        if (response.status !in 200..299) throw BackendFailure(errorCode(response))
        return response.body
    }
    private suspend fun send(request: ApiRequest): ApiResponse = suspendCancellableCoroutine { continuation ->
        connection.send(request) { if (continuation.isActive) continuation.resume(it) }
    }
    private fun errorCode(response: ApiResponse) = when (response.status) {
        0 -> "offline"
        401 -> "unauthenticated"
        else -> runCatching { ApiJson.parse(response.body).objectValue("error").text("code") }.getOrNull() ?: "service_unavailable"
    }
}
