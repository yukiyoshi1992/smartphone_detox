package com.yukiyoshi.smphdetox.rule

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

/**
 * 時間帯ルール。曜日（平日のみ・休日のみ等もdaysOfWeekの組み合わせとして表現）と
 * 時間帯、在宅条件を持つ。includeHolidaysをONにすると、daysOfWeekに含まれない
 * 曜日でも祝日であればルールが適用される（祝日データはHolidayRepository経由）。
 */
data class TimeRule(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,
    val requireHome: Boolean,
    val includeHolidays: Boolean = false,
) {
    fun encode(): String {
        val days = daysOfWeek.joinToString(",") { it.name }
        return listOf(
            id, label, startTime.toString(), endTime.toString(), days,
            requireHome.toString(), includeHolidays.toString(),
        ).joinToString(";")
    }

    companion object {
        fun decode(raw: String): TimeRule? {
            val parts = raw.split(";")
            if (parts.size != 7) return null
            return try {
                TimeRule(
                    id = parts[0],
                    label = parts[1],
                    startTime = LocalTime.parse(parts[2]),
                    endTime = LocalTime.parse(parts[3]),
                    daysOfWeek = parts[4].split(",").filter { it.isNotBlank() }.map { DayOfWeek.valueOf(it) }.toSet(),
                    requireHome = parts[5].toBoolean(),
                    includeHolidays = parts[6].toBoolean(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
