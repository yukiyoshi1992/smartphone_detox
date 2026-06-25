package com.yukiyoshi.smphdetox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.yukiyoshi.smphdetox.notification.NotificationAlarmScheduler
import com.yukiyoshi.smphdetox.rule.AppRule
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.HolidayMode
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import java.time.DayOfWeek
import java.time.LocalTime

private fun holidayModeLabel(mode: HolidayMode): String = when (mode) {
    HolidayMode.NORMAL -> "通常（曜日のみ）"
    HolidayMode.INCLUDE -> "祝日も含める"
    HolidayMode.EXCLUDE -> "祝日は除く"
    HolidayMode.ONLY -> "祝日のみ"
}

private val ALWAYS_START = LocalTime.of(0, 0)
private val ALWAYS_END = LocalTime.of(23, 59)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    modifier: Modifier = Modifier,
    ruleId: String?,
    createType: RuleTargetType?,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val ruleSettings = remember { AppRuleSettings(context) }
    val existing = remember { ruleId?.let { ruleSettings.rule(it) } }
    val targetType = existing?.targetType ?: createType ?: RuleTargetType.APP_BLOCK

    var label by remember { mutableStateOf(existing?.label ?: "") }
    var targets by remember { mutableStateOf(existing?.targets ?: emptySet()) }
    var domainInput by remember { mutableStateOf("") }
    var alwaysOn by remember {
        mutableStateOf(existing == null || (existing.startTime == ALWAYS_START && existing.endTime == ALWAYS_END))
    }
    var startTime by remember { mutableStateOf(existing?.startTime ?: ALWAYS_START) }
    var endTime by remember { mutableStateOf(existing?.endTime ?: ALWAYS_END) }
    var requireHome by remember { mutableStateOf(existing?.requireHome ?: false) }
    var holidayMode by remember { mutableStateOf(existing?.holidayMode ?: HolidayMode.NORMAL) }
    val selectedDays = remember {
        mutableStateMapOf<DayOfWeek, Boolean>().apply {
            DayOfWeek.entries.forEach { put(it, existing?.daysOfWeek?.contains(it) ?: true) }
        }
    }
    var showAppPicker by remember { mutableStateOf(false) }
    var appSearch by remember { mutableStateOf("") }
    val installedApps = remember { listLaunchableApps(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        BackButtonRow(onBack)
        Text(
            text = (if (existing == null) "ルールを新規作成" else "ルールを編集") + "（${ruleTypeLabel(targetType)}）",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text(text = "ルール名") })

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
        Text(text = "適用する時間帯", style = MaterialTheme.typography.titleSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = alwaysOn, onCheckedChange = { alwaysOn = it })
            Text(text = "常時適用（時間帯を指定しない）")
        }
        if (!alwaysOn) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimePickerField(label = "開始", time = startTime) { startTime = it }
                TimePickerField(label = "終了", time = endTime) { endTime = it }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = requireHome, onCheckedChange = { requireHome = it })
            Text(text = "在宅時のみ有効")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "対象曜日", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = selectedDays[day] == true,
                    onClick = { selectedDays[day] = selectedDays[day] != true },
                    label = { Text(text = day.name.take(3)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "祝日の扱い", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            HolidayMode.entries.forEach { mode ->
                FilterChip(
                    selected = holidayMode == mode,
                    onClick = { holidayMode = mode },
                    label = { Text(text = holidayModeLabel(mode)) },
                )
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
                    startTime = if (alwaysOn) ALWAYS_START else startTime,
                    endTime = if (alwaysOn) ALWAYS_END else endTime,
                    daysOfWeek = selectedDays.filterValues { it }.keys,
                    requireHome = requireHome,
                    holidayMode = holidayMode,
                    enabled = existing?.enabled ?: true,
                )
                if (existing == null) {
                    ruleSettings.addRule(rule)
                } else {
                    ruleSettings.updateRule(rule)
                }
                NotificationAlarmScheduler.reschedule(context)
                onDone()
            }
        }) {
            Text(text = "保存")
        }

        if (existing != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                ruleSettings.removeRule(existing.id)
                NotificationAlarmScheduler.reschedule(context)
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
                Column {
                    OutlinedTextField(
                        value = appSearch,
                        onValueChange = { appSearch = it },
                        label = { Text(text = "検索") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val filtered = installedApps.filter {
                        appSearch.isBlank() ||
                            it.label.contains(appSearch, ignoreCase = true) ||
                            it.packageName.contains(appSearch, ignoreCase = true)
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(filtered) { app ->
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
