package com.yukiyoshi.smphdetox.rule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun RuleSection() {
    val context = LocalContext.current
    val ruleSettings = remember { RuleSettings(context) }
    val homeWifiSettings = remember { HomeWifiSettings(context) }
    var rules by remember { mutableStateOf(ruleSettings.rules) }

    var label by remember { mutableStateOf("") }
    var startInput by remember { mutableStateOf("22:00") }
    var endInput by remember { mutableStateOf("06:00") }
    var requireHome by remember { mutableStateOf(true) }
    val selectedDays = remember {
        mutableStateMapOf<DayOfWeek, Boolean>().apply {
            DayOfWeek.entries.forEach { put(it, true) }
        }
    }
    var statusText by remember { mutableStateOf("未確認") }

    Column {
        Text(text = "時間帯ルール")
        rules.forEach { rule ->
            Row {
                val days = rule.daysOfWeek.joinToString(",") { it.name.take(3) }
                val homeLabel = if (rule.requireHome) "在宅時のみ" else "在宅問わず"
                Text(text = "${rule.label}: ${rule.startTime}-${rule.endTime} [$homeLabel] [$days]")
                TextButton(onClick = {
                    ruleSettings.removeRule(rule.id)
                    rules = ruleSettings.rules
                }) {
                    Text(text = "削除")
                }
            }
        }

        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text(text = "ルール名") })
        Row {
            OutlinedTextField(
                value = startInput,
                onValueChange = { startInput = it },
                label = { Text(text = "開始 HH:mm") },
            )
            OutlinedTextField(
                value = endInput,
                onValueChange = { endInput = it },
                label = { Text(text = "終了 HH:mm") },
            )
        }
        Row {
            Checkbox(checked = requireHome, onCheckedChange = { requireHome = it })
            Text(text = "在宅時のみ有効")
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            DayOfWeek.entries.forEach { day ->
                Row {
                    Checkbox(
                        checked = selectedDays[day] == true,
                        onCheckedChange = { selectedDays[day] = it },
                    )
                    Text(text = day.name.take(3))
                }
            }
        }
        Button(onClick = {
            val start = runCatching { LocalTime.parse(startInput) }.getOrNull()
            val end = runCatching { LocalTime.parse(endInput) }.getOrNull()
            if (label.isNotBlank() && start != null && end != null) {
                val days = selectedDays.filterValues { it }.keys
                ruleSettings.addRule(
                    TimeRule(
                        label = label,
                        startTime = start,
                        endTime = end,
                        daysOfWeek = days,
                        requireHome = requireHome,
                    )
                )
                rules = ruleSettings.rules
                label = ""
            }
        }) {
            Text(text = "ルールを追加")
        }

        Button(onClick = {
            val isHome = isHomeWifiConnected(context, homeWifiSettings.homeSsids)
            val active = activeRules(rules, LocalDateTime.now(), isHome)
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
