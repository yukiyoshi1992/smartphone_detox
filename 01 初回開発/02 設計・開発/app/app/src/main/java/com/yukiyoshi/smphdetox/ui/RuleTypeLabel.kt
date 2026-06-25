package com.yukiyoshi.smphdetox.ui

import com.yukiyoshi.smphdetox.rule.RuleTargetType

fun ruleTypeLabel(type: RuleTargetType): String = when (type) {
    RuleTargetType.APP_BLOCK -> "アプリブロック"
    RuleTargetType.SITE_BLOCK -> "サイトブロック"
    RuleTargetType.NOTIFICATION -> "通知（マナーモード）"
}
