package com.yukiyoshi.smphdetox.rule

import android.content.Context

private const val PREFS_NAME = "time_rules"
private const val KEY_RULES = "rules"

class RuleSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var rules: List<TimeRule>
        get() = prefs.getStringSet(KEY_RULES, emptySet())
            ?.mapNotNull { TimeRule.decode(it) }
            ?: emptyList()
        set(value) = prefs.edit().putStringSet(KEY_RULES, value.map { it.encode() }.toSet()).apply()

    fun addRule(rule: TimeRule) {
        rules = rules + rule
    }

    fun removeRule(id: String) {
        rules = rules.filterNot { it.id == id }
    }
}
