package org.generosity.domain

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.dateByAddingTimeInterval
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.timeZoneForSecondsFromGMT
import platform.Foundation.timeZoneWithName

actual fun currentUtcInstant(): String {
    val formatter = utcFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    return formatter.stringFromDate(NSDate())
}

actual fun zoneOffsetsAt(zoneId: String, localDateTime: String): List<Int> {
    val zone = NSTimeZone.timeZoneWithName(zoneId) ?: return emptyList()
    val formatter = utcFormatter()
    formatter.timeZone = zone
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm"
    val parsed = formatter.dateFromString(localDateTime) ?: return emptyList()
    if (formatter.stringFromDate(parsed) != localDateTime) return emptyList()
    val primary = (zone.secondsFromGMTForDate(parsed) / 60).toInt()
    val alternatives = listOf(3600.0, -3600.0).mapNotNull { delta ->
        val other = parsed.dateByAddingTimeInterval(delta)
        if (formatter.stringFromDate(other) != localDateTime) return@mapNotNull null
        val offset = (zone.secondsFromGMTForDate(other) / 60).toInt()
        offset.takeIf { it != primary }
    }
    return listOf(primary) + alternatives
}

actual fun formatInstantInZone(utcInstant: String, zoneId: String): String {
    val zone = NSTimeZone.timeZoneWithName(zoneId) ?: return utcInstant
    val parser = utcFormatter()
    parser.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    val date = parser.dateFromString(utcInstant) ?: return utcInstant
    val formatter = utcFormatter()
    formatter.timeZone = zone
    formatter.dateFormat = "yyyy-MM-dd HH:mm"
    return formatter.stringFromDate(date)
}

private fun utcFormatter(): NSDateFormatter = NSDateFormatter().apply {
    locale = NSLocale.localeWithLocaleIdentifier("en_US_POSIX")
    timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
}
