package com.yukiyoshi.smphdetox.rule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(label: String, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showDialog = true }) {
        Text(text = "$label: $time")
    }

    if (showDialog) {
        val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(shape = MaterialTheme.shapes.large) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TimePicker(state = state)
                    Row {
                        TextButton(onClick = { showDialog = false }) {
                            Text(text = "キャンセル")
                        }
                        TextButton(onClick = {
                            onTimeChange(LocalTime.of(state.hour, state.minute))
                            showDialog = false
                        }) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RuleSection() {
    val context = LocalContext.current
    val ruleSettings = remember { RuleSettings(context) }
    val homeWifiSettings = remember { HomeWifiSettings(context) }
    var rules by remember { mutableStateOf(ruleSettings.rules) }

    var label by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(6, 0)) }
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimePickerField(label = "開始", time = startTime) { startTime = it }
            TimePickerField(label = "終了", time = endTime) { endTime = it }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = requireHome, onCheckedChange = { requireHome = it })
            Text(text = "在宅時のみ有効")
        }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            DayOfWeek.entries.forEach { day ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedDays[day] == true,
                        onCheckedChange = { selectedDays[day] = it },
                    )
                    Text(text = day.name.take(3))
                }
            }
        }
        Button(onClick = {
            if (label.isNotBlank()) {
                val days = selectedDays.filterValues { it }.keys
                ruleSettings.addRule(
                    TimeRule(
                        label = label,
                        startTime = startTime,
                        endTime = endTime,
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
