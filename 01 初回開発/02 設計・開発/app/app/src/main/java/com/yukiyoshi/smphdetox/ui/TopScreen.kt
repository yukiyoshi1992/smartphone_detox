package com.yukiyoshi.smphdetox.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yukiyoshi.smphdetox.block.AppMasterSettings
import com.yukiyoshi.smphdetox.block.isBlockAccessibilityServiceEnabled
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.notification.hasNotificationPolicyAccess

@Composable
private fun StatusLine(label: String, value: String, isWarning: Boolean) {
    Text(
        text = "$label: $value",
        color = if (isWarning) Color.Red else Color.Unspecified,
        fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Normal,
    )
}

@Composable
fun TopScreen(
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit,
    onNavigateToRules: () -> Unit,
) {
    val context = LocalContext.current
    val masterSettings = remember { AppMasterSettings(context) }
    val homeWifiSettings = remember { HomeWifiSettings(context) }
    var masterEnabled by remember { mutableStateOf(masterSettings.enabled) }
    var homeStatusText by remember { mutableStateOf("未確認") }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var notificationAccessEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        accessibilityEnabled = isBlockAccessibilityServiceEnabled(context)
        notificationAccessEnabled = hasNotificationPolicyAccess(context)
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        homeStatusText = when {
            !hasLocationPermission -> "位置情報の許可が必要です（設定画面で許可してください）"
            isHomeWifiConnected(context, homeWifiSettings.homeSsids) -> "在宅中"
            else -> "非在宅（または対象Wi-Fi未接続）"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "smartphone_detox")

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "ブロック機能 全体ON/OFF")
            Switch(
                checked = masterEnabled,
                onCheckedChange = {
                    masterEnabled = it
                    masterSettings.enabled = it
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Text(text = "動作状況")
        StatusLine(label = "在宅状況", value = homeStatusText, isWarning = false)
        StatusLine(
            label = "アクセシビリティ",
            value = if (accessibilityEnabled) "許可済み" else "未許可（設定画面で許可してください）",
            isWarning = !accessibilityEnabled,
        )
        StatusLine(
            label = "通知へのアクセス",
            value = if (notificationAccessEnabled) "許可済み" else "未許可（設定画面で許可してください）",
            isWarning = !notificationAccessEnabled,
        )
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToSettings) {
            Text(text = "設定")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToRules) {
            Text(text = "ルール管理")
        }
    }
}
