package com.yukiyoshi.smphdetox.rule

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

enum class RuleTargetType { APP_BLOCK, SITE_BLOCK, NOTIFICATION }

/**
 * 1つのルールが「何を」（ブロック対象アプリ・サイト、または通知マナーモード）
 * 「いつ」（曜日・時間帯・在宅条件・祝日）適用するかをすべて持つ。
 * targetsはAPP_BLOCKならパッケージ名、SITE_BLOCKならURLに含まれる文字列。
 * NOTIFICATIONではtargetsは使わない（時間帯になったら丸ごとマナーモードにする）。
 */
data class AppRule(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val targetType: RuleTargetType,
    val targets: Set<String> = emptySet(),
    val startTime: LocalTime,
    val endTime: LocalTime,
    val daysOfWeek: Set<DayOfWeek>,
    val requireHome: Boolean,
    val includeHolidays: Boolean = false,
    val enabled: Boolean = true,
) {
    fun encode(): String {
        return listOf(
            id,
            label,
            targetType.name,
            targets.joinToString(","),
            startTime.toString(),
            endTime.toString(),
            daysOfWeek.joinToString(",") { it.name },
            requireHome.toString(),
            includeHolidays.toString(),
            enabled.toString(),
        ).joinToString(";")
    }

    companion object {
        fun decode(raw: String): AppRule? {
            val parts = raw.split(";")
            if (parts.size != 10) return null
            return try {
                AppRule(
                    id = parts[0],
                    label = parts[1],
                    targetType = RuleTargetType.valueOf(parts[2]),
                    targets = parts[3].split(",").filter { it.isNotBlank() }.toSet(),
                    startTime = LocalTime.parse(parts[4]),
                    endTime = LocalTime.parse(parts[5]),
                    daysOfWeek = parts[6].split(",").filter { it.isNotBlank() }.map { DayOfWeek.valueOf(it) }.toSet(),
                    requireHome = parts[7].toBoolean(),
                    includeHolidays = parts[8].toBoolean(),
                    enabled = parts[9].toBoolean(),
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
