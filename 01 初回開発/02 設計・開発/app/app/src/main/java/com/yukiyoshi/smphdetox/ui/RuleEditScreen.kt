package com.yukiyoshi.smphdetox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.yukiyoshi.smphdetox.block.listLaunchableApps
import com.yukiyoshi.smphdetox.rule.AppRule
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun RuleEditScreen(
    modifier: Modifier = Modifier,
    ruleId: String?,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val ruleSettings = remember { AppRuleSettings(context) }
    val existing = remember { ruleId?.let { ruleSettings.rule(it) } }

    var label by remember { mutableStateOf(existing?.label ?: "") }
    var targetType by remember { mutableStateOf(existing?.targetType ?: RuleTargetType.APP_BLOCK) }
    var targets by remember { mutableStateOf(existing?.targets ?: emptySet()) }
    var domainInput by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(existing?.startTime ?: LocalTime.of(0, 0)) }
    var endTime by remember { mutableStateOf(existing?.endTime ?: LocalTime.of(23, 59)) }
    var requireHome by remember { mutableStateOf(existing?.requireHome ?: false) }
    var includeHolidays by remember { mutableStateOf(existing?.includeHolidays ?: false) }
    val selectedDays = remember {
        mutableStateMapOf<DayOfWeek, Boolean>().apply {
            DayOfWeek.entries.forEach { put(it, existing?.daysOfWeek?.contains(it) ?: true) }
        }
    }
    var showAppPicker by remember { mutableStateOf(false) }
    val installedApps = remember { listLaunchableApps(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(text = if (existing == null) "ルールを新規作成" else "ルールを編集")

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text(text = "ルール名") })

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "種類")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RuleTargetType.entries.forEach { type ->
                FilterChip(
                    selected = targetType == type,
                    onClick = { targetType = type },
                    label = { Text(text = ruleTypeLabel(type)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (targetType) {
            RuleTargetType.APP_BLOCK -> {
                Text(text = "対象アプリ")
                targets.forEach { pkg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = pkg, modifier = Modifier.weight(1f))
                        TextButton(onClick = { targets = targets - pkg }) {
                            Text(text = "削除")
                        }
                    }
                }
                Button(onClick = { showAppPicker = true }) {
                    Text(text = "アプリを選んで追加")
                }
            }
            RuleTargetType.SITE_BLOCK -> {
                Text(text = "対象サイト（URLに含まれる文字列、例：youtube.com）")
                targets.forEach { domain ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = domain, modifier = Modifier.weight(1f))
                        TextButton(onClick = { targets = targets - domain }) {
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
                    if (domainInput.isNotBlank()) {
                        targets = targets + domainInput.trim()
                        domainInput = ""
                    }
                }) {
                    Text(text = "追加")
                }
            }
            RuleTargetType.NOTIFICATION -> {
                Text(text = "下記の時間帯になると、自動でマナーモード（バイブ有効）に切り替わります")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Text(text = "適用する時間帯")
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeHolidays, onCheckedChange = { includeHolidays = it })
            Text(text = "祝日も含める（曜日指定に関わらず祝日なら適用）")
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

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            if (label.isNotBlank()) {
                val rule = AppRule(
                    id = existing?.id ?: java.util.UUID.randomUUID().toString(),
                    label = label,
                    targetType = targetType,
                    targets = targets,
                    startTime = startTime,
                    endTime = endTime,
                    daysOfWeek = selectedDays.filterValues { it }.keys,
                    requireHome = requireHome,
                    includeHolidays = includeHolidays,
                    enabled = existing?.enabled ?: true,
                )
                if (existing == null) {
                    ruleSettings.addRule(rule)
                } else {
                    ruleSettings.updateRule(rule)
                }
                onDone()
            }
        }) {
            Text(text = "保存")
        }

        if (existing != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                ruleSettings.removeRule(existing.id)
                onDone()
            }) {
                Text(text = "このルールを削除")
            }
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
                                    targets = if (app.packageName in targets) {
                                        targets - app.packageName
                                    } else {
                                        targets + app.packageName
                                    }
                                }
                                .padding(8.dp),
                        ) {
                            val marker = if (app.packageName in targets) "✓ " else ""
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
