package com.yukiyoshi.smphdetox.rule

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

/**
 * 時間帯ルール。曜日（平日のみ・休日のみ等もdaysOfWeekの組み合わせとして表現）と
 * 時間帯、在宅条件を持つ。祝日対応は後続ステップ（祝日API連携）で評価時に組み込む。
 */
data class TimeRule(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,
    val requireHome: Boolean,
) {
    fun encode(): String {
        val days = daysOfWeek.joinToString(",") { it.name }
        return listOf(id, label, startTime.toString(), endTime.toString(), days, requireHome.toString())
            .joinToString(";")
    }

    companion object {
        fun decode(raw: String): TimeRule? {
            val parts = raw.split(";")
            if (parts.size != 6) return null
            return try {
                TimeRule(
                    id = parts[0],
                    label = parts[1],
                    startTime = LocalTime.parse(parts[2]),
                    endTime = LocalTime.parse(parts[3]),
                    daysOfWeek = parts[4].split(",").filter { it.isNotBlank() }.map { DayOfWeek.valueOf(it) }.toSet(),
                    requireHome = parts[5].toBoolean(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
