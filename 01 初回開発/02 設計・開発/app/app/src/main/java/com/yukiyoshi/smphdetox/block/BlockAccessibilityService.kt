package com.yukiyoshi.smphdetox.block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * 最小動作確認用：フォアグラウンドに来たパッケージを検知し、
 * ブロック対象リストに該当すれば即座にホーム画面に戻す。
 * 対象リストは後でルールエンジンから渡される想定だが、今はハードコードで動作確認する。
 */
class BlockAccessibilityService : AccessibilityService() {

    private val blockedPackages = setOf(
        "com.google.android.youtube",
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        if (packageName in blockedPackages) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
