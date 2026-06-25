package com.yukiyoshi.smphdetox.ui

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun BackButtonRow(onBack: () -> Unit) {
    TextButton(onClick = onBack) {
        Text(text = "← 戻る")
    }
}
