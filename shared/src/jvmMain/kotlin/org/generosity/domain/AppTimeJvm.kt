package org.generosity.domain

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

actual fun currentUtcInstant(): String =
    Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

actual fun zoneOffsetsAt(zoneId: String, localDateTime: String): List<Int> {
    val zone = runCatching { ZoneId.of(zoneId) }.getOrNull() ?: return emptyList()
    val local = runCatching { LocalDateTime.parse(localDateTime) }.getOrNull() ?: return emptyList()
    return zone.rules.getValidOffsets(local).map { it.totalSeconds / 60 }
}

actual fun formatInstantInZone(utcInstant: String, zoneId: String): String {
    val instant = Instant.parse(utcInstant)
    val zone = ZoneId.of(zoneId)
    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(zone).format(instant)
}
