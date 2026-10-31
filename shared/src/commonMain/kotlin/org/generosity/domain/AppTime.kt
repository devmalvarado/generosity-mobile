package org.generosity.domain

/**
 * Date-only expiry is inclusive through that UTC date and expired at the next
 * UTC midnight. Reminder wall times resolve to one instant, or need a choice
 * when the local time is missing or repeated.
 */
sealed class ReminderResolution {
    data class Saved(val utcInstant: String) : ReminderResolution()
    data object NeedsChoice : ReminderResolution()
}

expect fun currentUtcInstant(): String

expect fun zoneOffsetsAt(zoneId: String, localDateTime: String): List<Int>

expect fun formatInstantInZone(utcInstant: String, zoneId: String): String

fun isDateOnlyExpired(expiresOnIsoDate: String, authorityUtcInstant: String): Boolean {
    val authorityDate = utcDate(authorityUtcInstant)
    return expiresOnIsoDate < authorityDate
}

fun utcDate(authorityUtcInstant: String): String {
    requireUtcInstant(authorityUtcInstant)
    return authorityUtcInstant.take(10)
}

fun plusUtcMinutes(utcInstant: String, minutes: Int): String =
    formatUtcMinutes(utcMinutes(utcInstant) + minutes)

private fun requireUtcInstant(utcInstant: String) {
    require(utcInstant.endsWith("Z") && utcInstant.length >= 20) {
        "Authority time must be a UTC instant."
    }
}

private fun utcMinutes(utcInstant: String): Long {
    requireUtcInstant(utcInstant)
    val year = utcInstant.substring(0, 4).toInt()
    val month = utcInstant.substring(5, 7).toInt()
    val day = utcInstant.substring(8, 10).toInt()
    val hour = utcInstant.substring(11, 13).toInt()
    val minute = utcInstant.substring(14, 16).toInt()
    return daysFromCivil(year, month, day) * 1_440 + hour * 60 + minute
}

fun resolveReminder(date: String, time: String, zoneId: String): ReminderResolution {
    if (!ISO_DATE.matches(date) || !LOCAL_TIME.matches(time)) return ReminderResolution.NeedsChoice
    val offsets = zoneOffsetsAt(zoneId, "${date}T$time")
    val offset = offsets.singleOrNull() ?: return ReminderResolution.NeedsChoice
    return ReminderResolution.Saved(utcFromLocal(date, time, offset))
}

internal fun utcFromLocal(date: String, time: String, offsetMinutes: Int): String {
    val year = date.substring(0, 4).toInt()
    val month = date.substring(5, 7).toInt()
    val day = date.substring(8, 10).toInt()
    val hour = time.substring(0, 2).toInt()
    val minute = time.substring(3, 5).toInt()
    val localMinutes = daysFromCivil(year, month, day) * 1_440 + hour * 60 + minute
    return formatUtcMinutes(localMinutes - offsetMinutes)
}

private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val y = year.toLong() - if (month <= 2) 1 else 0
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (month + if (month > 2) -3 else 9) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private fun formatUtcMinutes(totalMinutes: Long): String {
    var days = totalMinutes.floorDiv(1_440)
    var minutes = (totalMinutes - days * 1_440).toInt()
    if (minutes < 0) {
        days -= 1
        minutes += 1_440
    }
    val (year, month, day) = civilFromDays(days)
    val hour = minutes / 60
    val minute = minutes % 60
    return "${pad(year, 4)}-${pad(month, 2)}-${pad(day, 2)}T${pad(hour, 2)}:${pad(minute, 2)}:00Z"
}

private fun civilFromDays(days: Long): Triple<Int, Int, Int> {
    var z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = (z - era * 146097).toInt()
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = doy - (153 * mp + 2) / 5 + 1
    val month = mp + if (mp < 10) 3 else -9
    val year = (y + if (month <= 2) 1 else 0).toInt()
    return Triple(year, month, day)
}

private fun pad(value: Int, width: Int): String = value.toString().padStart(width, '0')

private val ISO_DATE = Regex("""\d{4}-\d{2}-\d{2}""")
private val LOCAL_TIME = Regex("""\d{2}:\d{2}""")
