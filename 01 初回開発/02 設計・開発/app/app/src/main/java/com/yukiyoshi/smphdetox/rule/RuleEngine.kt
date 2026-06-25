package com.yukiyoshi.smphdetox.rule

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ルールが今この瞬間に有効かどうかを判定する。
 * 終了時刻が開始時刻より前（例：22:00-06:00）の場合は日付をまたぐ時間帯として扱う。
 * holidayDatesはHolidayRepository.cachedHolidayDates()を渡す想定（祝日を考慮しない場合は空集合でよい）。
 */
fun isRuleActive(
    rule: AppRule,
    now: LocalDateTime,
    isHome: Boolean,
    holidayDates: Set<LocalDate> = emptySet(),
): Boolean {
    if (!rule.enabled) return false
    if (rule.requireHome && !isHome) return false

    fun dayMatches(date: LocalDate): Boolean {
        val isHoliday = date in holidayDates
        val dayOfWeekMatches = date.dayOfWeek in rule.daysOfWeek
        return when (rule.holidayMode) {
            HolidayMode.NORMAL -> dayOfWeekMatches
            HolidayMode.INCLUDE -> dayOfWeekMatches || isHoliday
            HolidayMode.EXCLUDE -> dayOfWeekMatches && !isHoliday
            HolidayMode.ONLY -> isHoliday
        }
    }

    val time = now.toLocalTime()
    val today = now.toLocalDate()
    val overnight = rule.endTime < rule.startTime

    return if (!overnight) {
        dayMatches(today) && time >= rule.startTime && time < rule.endTime
    } else {
        val inLateSegment = time >= rule.startTime && dayMatches(today)
        val inEarlySegment = time < rule.endTime && dayMatches(today.minusDays(1))
        inLateSegment || inEarlySegment
    }
}

fun activeRules(
    rules: List<AppRule>,
    now: LocalDateTime,
    isHome: Boolean,
    holidayDates: Set<LocalDate> = emptySet(),
): List<AppRule> = rules.filter { isRuleActive(it, now, isHome, holidayDates) }
