package com.yukiyoshi.smphdetox.notification

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.yukiyoshi.smphdetox.holiday.HolidayRepository
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.rule.RuleSettings
import com.yukiyoshi.smphdetox.rule.TimeRuleForm
import com.yukiyoshi.smphdetox.rule.activeRules
import java.time.LocalDateTime

private const val NOTIFICATION_RULES_STORE = "notification_rules"

@Composable
fun NotificationRuleSection() {
    val context = LocalContext.current
    val ruleSettings = remember { RuleSettings(context, NOTIFICATION_RULES_STORE) }
    val homeWifiSettings = remember { HomeWifiSettings(context) }
    val holidayRepository = remember { HolidayRepository(context) }
    var rules by remember { mutableStateOf(ruleSettings.rules) }
    var statusText by remember { mutableStateOf("未確認") }
    var hasPolicyAccess by remember { mutableStateOf(hasNotificationPolicyAccess(context)) }

    LaunchedEffect(Unit) { holidayRepository.refreshIfStale() }

    Column {
        Text(text = "通知ルール（時間帯でマナーモードに自動切替）")

        if (!hasPolicyAccess) {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }) {
                Text(text = "通知へのアクセスを許可する")
            }
        }

        rules.forEach { rule ->
            Row {
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

        TimeRuleForm(homeCheckboxLabel = "在宅時のみマナーモード") { newRule ->
            ruleSettings.addRule(newRule)
            rules = ruleSettings.rules
        }

        Button(onClick = {
            hasPolicyAccess = hasNotificationPolicyAccess(context)
            val isHome = isHomeWifiConnected(context, homeWifiSettings.homeSsids)
            val holidayDates = holidayRepository.cachedHolidayDates()
            val active = activeRules(rules, LocalDateTime.now(), isHome, holidayDates)
            val applied = applyRingerMode(context, quiet = active.isNotEmpty())
            statusText = when {
                !applied -> "通知へのアクセス権限が必要です"
                active.isEmpty() -> "対象ルールなし→通常モードに設定"
                else -> "マナーモードに設定（該当: ${active.joinToString(", ") { it.label }}）"
            }
        }) {
            Text(text = "今のルールを適用")
        }
        Text(text = statusText)
    }
}
