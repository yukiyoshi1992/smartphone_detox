package com.yukiyoshi.smphdetox.rule

import android.content.Context

private const val DEFAULT_PREFS_NAME = "time_rules"
private const val KEY_RULES = "rules"

/** storeNameを変えることで、ブロック用ルールと通知用ルールなど別々のルール集合を保存できる。 */
class RuleSettings(context: Context, storeName: String = DEFAULT_PREFS_NAME) {
    private val prefs = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)

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
