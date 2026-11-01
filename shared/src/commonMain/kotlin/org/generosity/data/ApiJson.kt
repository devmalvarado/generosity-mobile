package org.generosity.data

/** Reuses the catalog's strict JSON reader for small API and durable-outbox documents. */
internal object ApiJson {
    @Suppress("UNCHECKED_CAST")
    fun parse(text: String): Map<String, Any?> = value(JsonReader(text).parse()) as? Map<String, Any?>
        ?: error("Expected JSON object")

    private fun value(json: JsonValue): Any? = when (json) {
        is JsonValue.Obj -> json.fields.mapValues { value(it.value) }
        is JsonValue.Arr -> json.items.map(::value)
        is JsonValue.Str -> json.value
        is JsonValue.Num -> json.raw.toLongOrNull() ?: json.raw.toDouble()
        is JsonValue.Bool -> json.value
        JsonValue.Null -> null
    }

    fun encode(value: Any?): String = when (value) {
        null -> "null"
        is String -> buildString {
            append('"')
            for (c in value) when (c) {
                '"' -> append("\\\""); '\\' -> append("\\\\")
                else -> if (c < ' ') append("\\u" + c.code.toString(16).padStart(4, '0')) else append(c)
            }
            append('"')
        }
        is Boolean, is Int, is Long -> value.toString()
        is Double -> { require(value.isFinite()); value.toString() }
        is Map<*, *> -> value.entries.joinToString(",", "{", "}") { encode(it.key as String) + ":" + encode(it.value) }
        is List<*> -> value.joinToString(",", "[", "]", transform = ::encode)
        else -> error("Unsupported JSON value")
    }
}

internal fun Map<String, Any?>.text(key: String): String? = this[key] as? String
internal fun Map<String, Any?>.int(key: String): Int? {
    val value = this[key] ?: return null
    require(value is Long && value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
    return value.toInt()
}
@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.objectValue(key: String): Map<String, Any?> = this[key] as? Map<String, Any?> ?: error("Missing object: $key")
@Suppress("UNCHECKED_CAST")
internal fun Map<String, Any?>.objects(key: String): List<Map<String, Any?>> = (this[key] as? List<*>)?.map { it as? Map<String, Any?> ?: error("Invalid row") } ?: error("Missing list: $key")
