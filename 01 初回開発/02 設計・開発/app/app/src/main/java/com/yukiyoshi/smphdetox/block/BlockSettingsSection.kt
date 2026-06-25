package com.yukiyoshi.smphdetox.block

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BlockSettingsSection() {
    val context = LocalContext.current
    val settings = remember { BlockSettings(context) }
    var blockedPackages by remember { mutableStateOf(settings.blockedPackages) }
    var blockedDomains by remember { mutableStateOf(settings.blockedDomains) }
    var domainInput by remember { mutableStateOf("") }
    var showAppPicker by remember { mutableStateOf(false) }
    val installedApps = remember { listLaunchableApps(context) }

    Column {
        Text(text = "ブロック対象アプリ")
        blockedPackages.forEach { pkg ->
            Row {
                Text(text = pkg)
                TextButton(onClick = {
                    settings.removeBlockedPackage(pkg)
                    blockedPackages = settings.blockedPackages
                }) {
                    Text(text = "削除")
                }
            }
        }
        Button(onClick = { showAppPicker = true }) {
            Text(text = "アプリを選んで追加")
        }

        Text(text = "ブロック対象サイト（URLに含まれる文字列、例：youtube.com）")
        blockedDomains.forEach { domain ->
            Row {
                Text(text = domain)
                TextButton(onClick = {
                    settings.removeBlockedDomain(domain)
                    blockedDomains = settings.blockedDomains
                }) {
                    Text(text = "削除")
                }
            }
        }
        OutlinedTextField(
            value = domainInput,
            onValueChange = { domainInput = it },
            label = { Text(text = "ドメイン/文字列を追加") },
        )
        Button(onClick = {
            settings.addBlockedDomain(domainInput)
            blockedDomains = settings.blockedDomains
            domainInput = ""
        }) {
            Text(text = "追加")
        }
    }

    if (showAppPicker) {
        AlertDialog(
            onDismissRequest = { showAppPicker = false },
            title = { Text(text = "アプリを選択") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(installedApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settings.addBlockedPackage(app.packageName)
                                    blockedPackages = settings.blockedPackages
                                }
                                .padding(8.dp),
                        ) {
                            val marker = if (app.packageName in blockedPackages) "✓ " else ""
                            Text(text = "$marker${app.label}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppPicker = false }) {
                    Text(text = "閉じる")
                }
            },
        )
    }
}
