package com.yukiyoshi.smphdetox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.ui.theme.Smartphone_detoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Smartphone_detoxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = remember { HomeWifiSettings(context) }
    var homeSsids by remember { mutableStateOf(settings.homeSsids) }
    var newSsidInput by remember { mutableStateOf("") }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var statusText by remember { mutableStateOf("未確認") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    fun refreshStatus() {
        statusText = when {
            !hasLocationPermission -> "位置情報の許可が必要です"
            isHomeWifiConnected(context, homeSsids) -> "在宅中"
            else -> "非在宅（または対象Wi-Fi未接続）"
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "smartphone_detox")
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }) {
            Text(text = "アクセシビリティ設定を開く")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "自宅Wi-FiのSSID（複数登録可）")
        homeSsids.forEach { ssid ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = ssid)
                TextButton(onClick = {
                    settings.removeHomeSsid(ssid)
                    homeSsids = settings.homeSsids
                }) {
                    Text(text = "削除")
                }
            }
        }
        OutlinedTextField(
            value = newSsidInput,
            onValueChange = { newSsidInput = it },
            label = { Text(text = "SSIDを追加") },
        )
        Button(onClick = {
            settings.addHomeSsid(newSsidInput)
            homeSsids = settings.homeSsids
            newSsidInput = ""
        }) {
            Text(text = "追加")
        }

        if (!hasLocationPermission) {
            Button(onClick = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }) {
                Text(text = "位置情報の権限を許可する")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { refreshStatus() }) {
            Text(text = "在宅状態を確認")
        }
        Text(text = "状態: $statusText")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Smartphone_detoxTheme {
        HomeScreen()
    }
}
