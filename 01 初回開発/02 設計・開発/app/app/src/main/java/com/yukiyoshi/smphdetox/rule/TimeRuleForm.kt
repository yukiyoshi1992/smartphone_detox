package com.yukiyoshi.smphdetox.rule

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(label: String, time: LocalTime, onTimeChange: (LocalTime) -> Unit) {
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

/** ルール名・開始/終了時刻・曜日・在宅条件を入力して`TimeRule`を作成するフォーム。 */
@Composable
fun TimeRuleForm(homeCheckboxLabel: String = "在宅時のみ有効", onAddRule: (TimeRule) -> Unit) {
    var label by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(6, 0)) }
    var requireHome by remember { mutableStateOf(true) }
    val selectedDays = remember {
        mutableStateMapOf<DayOfWeek, Boolean>().apply {
            DayOfWeek.entries.forEach { put(it, true) }
        }
    }

    Column {
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
            Text(text = homeCheckboxLabel)
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
                onAddRule(
                    TimeRule(
                        label = label,
                        startTime = startTime,
                        endTime = endTime,
                        daysOfWeek = selectedDays.filterValues { it }.keys,
                        requireHome = requireHome,
                    )
                )
                label = ""
            }
        }) {
            Text(text = "ルールを追加")
        }
    }
}
