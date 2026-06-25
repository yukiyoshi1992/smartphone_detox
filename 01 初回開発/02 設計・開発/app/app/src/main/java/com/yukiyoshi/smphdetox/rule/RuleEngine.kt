package com.yukiyoshi.smphdetox.rule

import java.time.LocalDateTime

/**
 * ルールが今この瞬間に有効かどうかを判定する。
 * 終了時刻が開始時刻より前（例：22:00-06:00）の場合は日付をまたぐ時間帯として扱う。
 */
fun isRuleActive(rule: TimeRule, now: LocalDateTime, isHome: Boolean): Boolean {
    if (rule.requireHome && !isHome) return false

    val time = now.toLocalTime()
    val overnight = rule.endTime < rule.startTime

    return if (!overnight) {
        now.dayOfWeek in rule.daysOfWeek && time >= rule.startTime && time < rule.endTime
    } else {
        val inLateSegment = time >= rule.startTime && now.dayOfWeek in rule.daysOfWeek
        val inEarlySegment = time < rule.endTime && now.minusDays(1).dayOfWeek in rule.daysOfWeek
        inLateSegment || inEarlySegment
    }
}

fun activeRules(rules: List<TimeRule>, now: LocalDateTime, isHome: Boolean): List<TimeRule> =
    rules.filter { isRuleActive(it, now, isHome) }
