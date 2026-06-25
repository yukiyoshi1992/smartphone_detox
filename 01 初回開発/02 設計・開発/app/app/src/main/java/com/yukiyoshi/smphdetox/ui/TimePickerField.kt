package com.yukiyoshi.smphdetox.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
