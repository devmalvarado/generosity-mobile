package org.generosity.data

import org.generosity.domain.CenterCategory
import org.generosity.domain.CenterNeed
import org.generosity.domain.ContactInfo
import org.generosity.domain.Coordinates
import org.generosity.domain.HelpMethod
import org.generosity.domain.KindnessCenter
import org.generosity.domain.LocalizedText
import org.generosity.domain.NeedPriority
import org.generosity.domain.NeedStatus
import org.generosity.domain.SupportedLocale
import org.generosity.domain.VerificationStatus

sealed class CatalogImport {
    data class Accepted(val centers: List<KindnessCenter>) : CatalogImport()
    data class Rejected(val diagnostic: String) : CatalogImport()
}

/**
 * Version 1 of the documented kindness-center catalog. Unknown properties and
 * any other schema version are rejected so a later directory contract cannot
 * be partially applied.
 */
object CatalogJson {
    const val SCHEMA_VERSION: Int = 1

    fun parse(text: String): CatalogImport = try {
        validate(JsonReader(text).parse())
    } catch (error: CatalogJsonException) {
        CatalogImport.Rejected("Catalog import rejected: ${error.message}")
    } catch (error: IllegalArgumentException) {
        CatalogImport.Rejected("Catalog import rejected: ${error.message}")
    }

    private fun validate(root: JsonValue): CatalogImport {
        val document = root.asObject("catalog")
        val version = document.require("schemaVersion", "catalog").asInt("catalog.schemaVersion")
        if (version != SCHEMA_VERSION) {
            fail("unsupported schema version $version.")
        }
        val centers = document.require("centers", "catalog").asArray("catalog.centers").mapIndexed { index, value ->
            center(value, "catalog.centers[$index]")
        }
        val extra = document.keys - CENTER_DOCUMENT_KEYS
        if (extra.isNotEmpty()) fail("unknown property '${extra.first()}' at catalog.")
        rejectDuplicateIds(centers.map { it.id }, "center")
        rejectDuplicateIds(centers.flatMap { it.needs }.map { it.id }, "need")
        return CatalogImport.Accepted(centers)
    }

    private fun center(value: JsonValue, path: String): KindnessCenter {
        val objectValue = value.asObject(path)
        rejectUnknown(objectValue, CENTER_KEYS, path)
        val id = objectValue.require("id", path).asId("$path.id")
        val needs = objectValue.optional("needs")?.asArray("$path.needs").orEmpty().mapIndexed { index, need ->
            need(need, id, "$path.needs[$index]")
        }
        return KindnessCenter(
            id = id,
            name = localized(objectValue.require("name", path), "$path.name"),
            description = localized(objectValue.require("description", path), "$path.description"),
            categories = codes(objectValue.require("categories", path), "$path.categories", CenterCategory.entries, CenterCategory::code),
            coordinates = coordinates(objectValue.require("coordinates", path), "$path.coordinates"),
            address = localized(objectValue.require("address", path), "$path.address"),
            contactInfo = objectValue.optional("contact")?.let { contact(it, "$path.contact") } ?: ContactInfo(),
            verificationStatus = code(
                objectValue.require("verificationStatus", path),
                "$path.verificationStatus",
                VerificationStatus.entries,
                VerificationStatus::code
            ),
            needs = needs,
            importedAtIsoDate = objectValue.optional("importedAtIsoDate")?.asDateOrNull("$path.importedAtIsoDate")
        )
    }

    private fun need(value: JsonValue, parentCenterId: String, path: String): CenterNeed {
        val objectValue = value.asObject(path)
        rejectUnknown(objectValue, NEED_KEYS, path)
        val id = objectValue.require("id", path).asId("$path.id")
        val linkedCenter = objectValue.optional("centerId")?.asId("$path.centerId") ?: parentCenterId
        if (linkedCenter != parentCenterId) {
            fail("need '$id' is linked to center '$linkedCenter', not '$parentCenterId'.")
        }
        val urgent = objectValue.optional("urgent")?.asBoolean("$path.urgent") ?: false
        val priority = objectValue.optional("priority")?.let {
            code(it, "$path.priority", NeedPriority.entries, NeedPriority::code)
        } ?: if (urgent) NeedPriority.URGENT else NeedPriority.NORMAL
        if (objectValue.contains("urgent") && objectValue.contains("priority") && priority.isUrgent != urgent) {
            fail("need '$id' has urgent=$urgent and priority '${priority.code}'.")
        }
        val quantityRequired = objectValue.optional("quantityRequired")?.asInt("$path.quantityRequired")
        val quantityCommitted = objectValue.optional("quantityCommitted")?.asInt("$path.quantityCommitted") ?: 0
        if (quantityRequired == null && objectValue.contains("quantityCommitted") && quantityCommitted != 0) {
            fail("need '$id' has a committed quantity without a required quantity.")
        }
        if (quantityRequired != null && quantityRequired <= 0) {
            fail("need '$id' required quantity must be greater than zero.")
        }
        if (quantityRequired != null && quantityCommitted !in 0..quantityRequired) {
            fail("need '$id' committed quantity must stay between zero and the required quantity.")
        }
        return CenterNeed(
            id = id,
            centerId = parentCenterId,
            title = localized(objectValue.require("title", path), "$path.title"),
            description = localized(objectValue.require("description", path), "$path.description"),
            helpMethods = codes(objectValue.require("helpMethods", path), "$path.helpMethods", HelpMethod.entries, HelpMethod::code),
            urgent = urgent,
            expiresOnIsoDate = objectValue.optional("expiresOnIsoDate")?.asDateOrNull("$path.expiresOnIsoDate"),
            category = objectValue.optional("category")?.let {
                code(it, "$path.category", CenterCategory.entries, CenterCategory::code)
            },
            priority = priority,
            status = objectValue.optional("status")?.let {
                code(it, "$path.status", NeedStatus.entries, NeedStatus::code)
            } ?: NeedStatus.ACTIVE,
            quantityRequired = quantityRequired,
            quantityCommitted = quantityCommitted,
            publishedOnIsoDate = objectValue.optional("publishedOnIsoDate")?.asDateOrNull("$path.publishedOnIsoDate")
        )
    }

    private fun contact(value: JsonValue, path: String): ContactInfo {
        val objectValue = value.asObject(path)
        rejectUnknown(objectValue, CONTACT_KEYS, path)
        return ContactInfo(
            phone = objectValue.optional("phone")?.asStringOrNull("$path.phone"),
            email = objectValue.optional("email")?.asStringOrNull("$path.email"),
            website = objectValue.optional("website")?.asStringOrNull("$path.website")
        )
    }

    private fun coordinates(value: JsonValue, path: String): Coordinates {
        val objectValue = value.asObject(path)
        rejectUnknown(objectValue, COORDINATE_KEYS, path)
        val latitude = objectValue.require("latitude", path).asDouble("$path.latitude")
        val longitude = objectValue.require("longitude", path).asDouble("$path.longitude")
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            fail("coordinates at $path are outside the valid range.")
        }
        return Coordinates(latitude, longitude)
    }

    private fun localized(value: JsonValue, path: String): LocalizedText {
        val objectValue = value.asObject(path)
        val allowed = SupportedLocale.entries.map { it.code }.toSet()
        val unknown = objectValue.keys - allowed
        if (unknown.isNotEmpty()) fail("unsupported locale '${unknown.first()}' at $path.")
        val values = objectValue.mapValues { (key, item) -> item.asString("$path.$key") }
        if (values.values.none { it.isNotBlank() }) fail("localized field $path has no text.")
        return LocalizedText(values)
    }

    private fun <T> codes(
        value: JsonValue,
        path: String,
        entries: List<T>,
        codeOf: (T) -> String
    ): Set<T> {
        val items = value.asArray(path)
        val seen = mutableSetOf<String>()
        return items.map { item ->
            val raw = item.asString(path)
            if (!seen.add(raw)) fail("duplicate code '$raw' at $path.")
            code(item, path, entries, codeOf)
        }.toSet()
    }

    private fun <T> code(value: JsonValue, path: String, entries: List<T>, codeOf: (T) -> String): T {
        val raw = value.asString(path)
        return entries.firstOrNull { codeOf(it) == raw }
            ?: fail("unknown code '$raw' at $path.")
    }

    private fun rejectUnknown(value: Map<String, JsonValue>, allowed: Set<String>, path: String) {
        val extra = value.keys - allowed
        if (extra.isNotEmpty()) fail("unknown property '${extra.first()}' at $path.")
    }

    private fun rejectDuplicateIds(ids: List<String>, kind: String) {
        val seen = mutableSetOf<String>()
        ids.firstOrNull { !seen.add(it) }?.let { fail("duplicate $kind id '$it'.") }
    }

    private fun fail(message: String): Nothing = throw CatalogJsonException(message)

    private val CENTER_DOCUMENT_KEYS = setOf("schemaVersion", "centers")
    private val CENTER_KEYS = setOf(
        "id", "name", "description", "categories", "coordinates", "address",
        "contact", "verificationStatus", "needs", "importedAtIsoDate"
    )
    private val NEED_KEYS = setOf(
        "id", "centerId", "title", "description", "helpMethods", "urgent",
        "expiresOnIsoDate", "category", "priority", "status", "quantityRequired",
        "quantityCommitted", "publishedOnIsoDate"
    )
    private val CONTACT_KEYS = setOf("phone", "email", "website")
    private val COORDINATE_KEYS = setOf("latitude", "longitude")
}

/**
 * Keeps the last accepted catalog when a file is missing or rejected.
 * A first failed load stays empty and never substitutes demonstration centers.
 */
class ValidatingCatalogDataSource(
    private val readText: () -> String?,
    private var published: List<KindnessCenter> = emptyList()
) : ImportedCatalogDataSource {
    var lastDiagnostic: String? = null
        private set

    override fun loadCenters(): List<KindnessCenter> {
        val text = readText()
        if (text == null) {
            lastDiagnostic = "Catalog file is missing."
            return published
        }
        return when (val result = CatalogJson.parse(text)) {
            is CatalogImport.Accepted -> {
                lastDiagnostic = null
                published = result.centers
                published
            }
            is CatalogImport.Rejected -> {
                lastDiagnostic = result.diagnostic
                published
            }
        }
    }
}

private class CatalogJsonException(message: String) : RuntimeException(message)

private sealed class JsonValue {
    data class Obj(val fields: Map<String, JsonValue>) : JsonValue()
    data class Arr(val items: List<JsonValue>) : JsonValue()
    data class Str(val value: String) : JsonValue()
    data class Num(val raw: String) : JsonValue()
    data class Bool(val value: Boolean) : JsonValue()
    data object Null : JsonValue()
}

private fun Map<String, JsonValue>.require(key: String, path: String): JsonValue =
    this[key] ?: throw CatalogJsonException("missing $key at $path.")

private fun Map<String, JsonValue>.optional(key: String): JsonValue? = this[key]?.takeUnless { it is JsonValue.Null }

private fun JsonValue.asObject(path: String): Map<String, JsonValue> =
    (this as? JsonValue.Obj)?.fields ?: throw CatalogJsonException("expected an object at $path.")

private fun JsonValue.asArray(path: String): List<JsonValue> =
    (this as? JsonValue.Arr)?.items ?: throw CatalogJsonException("expected an array at $path.")

private fun JsonValue.asString(path: String): String =
    (this as? JsonValue.Str)?.value ?: throw CatalogJsonException("expected a string at $path.")

private fun JsonValue.asStringOrNull(path: String): String? = when (this) {
    is JsonValue.Null -> null
    is JsonValue.Str -> value
    else -> throw CatalogJsonException("expected a string at $path.")
}

private fun JsonValue.asBoolean(path: String): Boolean =
    (this as? JsonValue.Bool)?.value ?: throw CatalogJsonException("expected a boolean at $path.")

private fun JsonValue.asDouble(path: String): Double {
    val raw = (this as? JsonValue.Num)?.raw ?: throw CatalogJsonException("expected a number at $path.")
    val value = raw.toDoubleOrNull() ?: throw CatalogJsonException("invalid number at $path.")
    if (!value.isFinite()) throw CatalogJsonException("invalid number at $path.")
    return value
}

private fun JsonValue.asInt(path: String): Int {
    val raw = (this as? JsonValue.Num)?.raw ?: throw CatalogJsonException("expected an integer at $path.")
    if (raw.any { it == '.' || it == 'e' || it == 'E' }) {
        throw CatalogJsonException("expected an integer at $path.")
    }
    return raw.toIntOrNull() ?: throw CatalogJsonException("expected an integer at $path.")
}

private fun JsonValue.asId(path: String): String {
    val value = asString(path)
    if (value.isBlank() || value != value.trim()) throw CatalogJsonException("invalid identifier at $path.")
    return value
}

private fun JsonValue.asDateOrNull(path: String): String? {
    val value = asStringOrNull(path) ?: return null
    if (!isIsoDate(value)) throw CatalogJsonException("invalid date at $path.")
    return value
}

private fun isIsoDate(value: String): Boolean {
    if (!ISO_DATE.matches(value)) return false
    val year = value.substring(0, 4).toInt()
    val month = value.substring(5, 7).toInt()
    val day = value.substring(8, 10).toInt()
    if (month !in 1..12) return false
    val maxDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day in 1..maxDay
}

private val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")

private class JsonReader(private val text: String) {
    private var index = 0

    fun parse(): JsonValue {
        val value = readValue()
        skipWs()
        if (index != text.length) fail("malformed JSON.")
        return value
    }

    private fun readValue(): JsonValue {
        skipWs()
        if (index >= text.length) fail("malformed JSON.")
        return when (val char = text[index]) {
            '{' -> readObject()
            '[' -> readArray()
            '"' -> JsonValue.Str(readString())
            't' -> literal("true", JsonValue.Bool(true))
            'f' -> literal("false", JsonValue.Bool(false))
            'n' -> literal("null", JsonValue.Null)
            else -> if (char == '-' || char.isDigit()) readNumber() else fail("malformed JSON.")
        }
    }

    private fun readObject(): JsonValue.Obj {
        expect('{')
        skipWs()
        val fields = linkedMapOf<String, JsonValue>()
        if (peek('}')) {
            index++
            return JsonValue.Obj(fields)
        }
        while (true) {
            skipWs()
            if (index >= text.length || text[index] != '"') fail("malformed JSON.")
            val key = readString()
            if (key in fields) fail("duplicate property '$key'.")
            skipWs()
            expect(':')
            fields[key] = readValue()
            skipWs()
            when {
                peek(',') -> index++
                peek('}') -> {
                    index++
                    return JsonValue.Obj(fields)
                }
                else -> fail("malformed JSON.")
            }
        }
    }

    private fun readArray(): JsonValue.Arr {
        expect('[')
        skipWs()
        val items = mutableListOf<JsonValue>()
        if (peek(']')) {
            index++
            return JsonValue.Arr(items)
        }
        while (true) {
            items += readValue()
            skipWs()
            when {
                peek(',') -> index++
                peek(']') -> {
                    index++
                    return JsonValue.Arr(items)
                }
                else -> fail("malformed JSON.")
            }
        }
    }

    private fun readString(): String {
        expect('"')
        val out = StringBuilder()
        while (index < text.length) {
            when (val char = text[index++]) {
                '"' -> return out.toString()
                '\\' -> out.append(escape())
                else -> {
                    if (char < ' ') fail("malformed JSON.")
                    out.append(char)
                }
            }
        }
        fail("malformed JSON.")
    }

    private fun escape(): Char {
        if (index >= text.length) fail("malformed JSON.")
        return when (val char = text[index++]) {
            '"', '\\', '/' -> char
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> {
                if (index + 4 > text.length) fail("malformed JSON.")
                val hex = text.substring(index, index + 4)
                index += 4
                hex.toIntOrNull(16)?.toChar() ?: fail("malformed JSON.")
            }
            else -> fail("malformed JSON.")
        }
    }

    private fun readNumber(): JsonValue.Num {
        val start = index
        if (peek('-')) index++
        if (index >= text.length || !text[index].isDigit()) fail("malformed JSON.")
        if (text[index] == '0') {
            index++
        } else {
            while (index < text.length && text[index].isDigit()) index++
        }
        if (peek('.')) {
            index++
            if (index >= text.length || !text[index].isDigit()) fail("malformed JSON.")
            while (index < text.length && text[index].isDigit()) index++
        }
        if (index < text.length && (text[index] == 'e' || text[index] == 'E')) {
            index++
            if (index < text.length && (text[index] == '+' || text[index] == '-')) index++
            if (index >= text.length || !text[index].isDigit()) fail("malformed JSON.")
            while (index < text.length && text[index].isDigit()) index++
        }
        return JsonValue.Num(text.substring(start, index))
    }

    private fun literal(expected: String, value: JsonValue): JsonValue {
        if (!text.startsWith(expected, index)) fail("malformed JSON.")
        index += expected.length
        return value
    }

    private fun expect(char: Char) {
        if (index >= text.length || text[index] != char) fail("malformed JSON.")
        index++
    }

    private fun peek(char: Char): Boolean = index < text.length && text[index] == char

    private fun skipWs() {
        while (index < text.length && text[index].isWhitespace()) index++
    }

    private fun fail(message: String): Nothing = throw CatalogJsonException(message)
}
