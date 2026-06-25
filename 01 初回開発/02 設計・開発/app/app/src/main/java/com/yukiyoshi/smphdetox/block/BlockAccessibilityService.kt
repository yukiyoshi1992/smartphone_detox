package com.yukiyoshi.smphdetox.block

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/** Brave/ChromeそれぞれのアドレスバーのリソースID（どちらもChromiumベースのため同名）。 */
private val BROWSER_URL_BAR_IDS = mapOf(
    "com.android.chrome" to "com.android.chrome:id/url_bar",
    "com.brave.browser" to "com.brave.browser:id/url_bar",
)

/**
 * フォアグラウンドアプリ・Brave/ChromeのアドレスバーURLを検知し、
 * 設定済みのブロック対象（アプリ・サイト）に該当すればホーム画面に戻す。
 */
class BlockAccessibilityService : AccessibilityService() {

    private val settings by lazy { BlockSettings(applicationContext) }
    private val masterSettings by lazy { AppMasterSettings(applicationContext) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!masterSettings.enabled) return
        val packageName = event.packageName?.toString() ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            packageName in settings.blockedPackages
        ) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }

        val urlBarId = BROWSER_URL_BAR_IDS[packageName] ?: return
        val isBrowserEvent = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        if (!isBrowserEvent) return

        val url = currentAddressBarText(urlBarId) ?: return
        if (settings.blockedDomains.any { url.contains(it, ignoreCase = true) }) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun currentAddressBarText(urlBarId: String): String? {
        val root = rootInActiveWindow ?: return null
        return root.findAccessibilityNodeInfosByViewId(urlBarId).firstOrNull()?.text?.toString()
    }

    override fun onInterrupt() {}
}
