package com.yukiyoshi.smphdetox.rule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.yukiyoshi.smphdetox.holiday.HolidayRepository
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import java.time.LocalDateTime

@Composable
fun RuleSection() {
    val context = LocalContext.current
    val ruleSettings = remember { RuleSettings(context) }
    val homeWifiSettings = remember { HomeWifiSettings(context) }
    val holidayRepository = remember { HolidayRepository(context) }
    var rules by remember { mutableStateOf(ruleSettings.rules) }
    var statusText by remember { mutableStateOf("未確認") }

    LaunchedEffect(Unit) { holidayRepository.refreshIfStale() }

    Column {
        Text(text = "時間帯ルール（ブロック対象アプリ・サイトに適用）")
        rules.forEach { rule ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.enabled,
                    onCheckedChange = {
                        ruleSettings.updateRule(rule.copy(enabled = it))
                        rules = ruleSettings.rules
                    },
                )
                val days = rule.daysOfWeek.joinToString(",") { it.name.take(3) }
                val homeLabel = if (rule.requireHome) "在宅時のみ" else "在宅問わず"
                val holidayLabel = if (rule.includeHolidays) " [祝日含む]" else ""
                Text(text = "${rule.label}: ${rule.startTime}-${rule.endTime} [$homeLabel] [$days]$holidayLabel")
                TextButton(onClick = {
                    ruleSettings.removeRule(rule.id)
                    rules = ruleSettings.rules
                }) {
                    Text(text = "削除")
                }
            }
        }

        TimeRuleForm { newRule ->
            ruleSettings.addRule(newRule)
            rules = ruleSettings.rules
        }

        Button(onClick = {
            val isHome = isHomeWifiConnected(context, homeWifiSettings.homeSsids)
            val holidayDates = holidayRepository.cachedHolidayDates()
            val active = activeRules(rules, LocalDateTime.now(), isHome, holidayDates)
            statusText = if (active.isEmpty()) {
                "有効なルールなし"
            } else {
                "有効: ${active.joinToString(", ") { it.label }}"
            }
        }) {
            Text(text = "現在有効なルールを確認")
        }
        Text(text = statusText)
    }
}
