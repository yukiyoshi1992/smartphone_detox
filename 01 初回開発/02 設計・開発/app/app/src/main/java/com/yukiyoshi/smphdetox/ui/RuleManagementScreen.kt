package com.yukiyoshi.smphdetox.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType

private val TABS = listOf(
    RuleTargetType.APP_BLOCK to "アプリブロック",
    RuleTargetType.SITE_BLOCK to "サイトブロック",
    RuleTargetType.NOTIFICATION to "通知",
)

@Composable
fun RuleManagementScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onCreateRule: (RuleTargetType) -> Unit,
    onEditRule: (String) -> Unit,
) {
    val context = LocalContext.current
    val ruleSettings = remember { AppRuleSettings(context) }
    var rules by remember { mutableStateOf(ruleSettings.rules) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentType = TABS[selectedTab].first

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        BackButtonRow(onBack)
        Text(text = "ルール管理")

        Spacer(modifier = Modifier.height(8.dp))
        TabRow(selectedTabIndex = selectedTab) {
            TABS.forEachIndexed { index, (_, label) ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }) {
                    Text(text = label, modifier = Modifier.padding(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onCreateRule(currentType) }) {
            Text(text = "新規作成")
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            rules.filter { it.targetType == currentType }.forEach { rule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = rule.label,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onEditRule(rule.id) },
                    )
                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = {
                            ruleSettings.updateRule(rule.copy(enabled = it))
                            rules = ruleSettings.rules
                        },
                    )
                    TextButton(onClick = {
                        ruleSettings.removeRule(rule.id)
                        rules = ruleSettings.rules
                    }) {
                        Text(text = "削除")
                    }
                }
            }
        }
    }
}
