package com.yukiyoshi.smphdetox.rule

import android.content.Context

private const val PREFS_NAME = "app_rules"
private const val KEY_RULES = "rules"

/** アプリブロック・サイトブロック・通知ルールをすべて1つの集合として保存する。 */
class AppRuleSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var rules: List<AppRule>
        get() = prefs.getStringSet(KEY_RULES, emptySet())
            ?.mapNotNull { AppRule.decode(it) }
            ?: emptyList()
        set(value) = prefs.edit().putStringSet(KEY_RULES, value.map { it.encode() }.toSet()).apply()

    fun rule(id: String): AppRule? = rules.find { it.id == id }

    fun addRule(rule: AppRule) {
        rules = rules + rule
    }

    fun removeRule(id: String) {
        rules = rules.filterNot { it.id == id }
    }

    fun updateRule(rule: AppRule) {
        rules = rules.map { if (it.id == rule.id) rule else it }
    }
}
