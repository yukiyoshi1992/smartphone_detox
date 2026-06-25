package com.yukiyoshi.smphdetox.block

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.yukiyoshi.smphdetox.holiday.HolidayRepository
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import com.yukiyoshi.smphdetox.rule.isRuleActive
import java.time.LocalDateTime

private const val TAG = "BlockAccessibility"

/** Brave/ChromeそれぞれのアドレスバーのリソースID（どちらもChromiumベースのため同名）。 */
private val BROWSER_URL_BAR_IDS = mapOf(
    "com.android.chrome" to "com.android.chrome:id/url_bar",
    "com.brave.browser" to "com.brave.browser:id/url_bar",
)

/**
 * フォアグラウンドアプリ・Brave/ChromeのアドレスバーURLを検知し、
 * 現在有効なAPP_BLOCK/SITE_BLOCKルールに該当すればブロックする。
 * ルールの有効判定（曜日・時間帯・在宅条件・祝日）はrule.RuleEngineに委譲する。
 */
class BlockAccessibilityService : AccessibilityService() {

    private val masterSettings by lazy { AppMasterSettings(applicationContext) }
    private val ruleSettings by lazy { AppRuleSettings(applicationContext) }
    private val homeWifiSettings by lazy { HomeWifiSettings(applicationContext) }
    private val holidayRepository by lazy { HolidayRepository(applicationContext) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!masterSettings.enabled) return
        val packageName = event.packageName?.toString() ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            packageName in activeTargets(RuleTargetType.APP_BLOCK)
        ) {
            Log.d(TAG, "blocking app: $packageName")
            blockCurrentScreen()
            return
        }

        val urlBarId = BROWSER_URL_BAR_IDS[packageName] ?: return
        val isBrowserEvent = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        if (!isBrowserEvent) return

        val url = currentAddressBarText(urlBarId)
        val siteTargets = activeTargets(RuleTargetType.SITE_BLOCK)
        Log.d(TAG, "browser event pkg=$packageName url=$url activeSiteTargets=$siteTargets")
        if (url != null && siteTargets.any { url.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "blocking site: $url")
            blockCurrentScreen()
        }
    }

    /**
     * GLOBAL_ACTION_HOMEはYouTube等のPicture-in-Picture対応アプリでは
     * 「ホームに戻る」操作として認識され、アプリを閉じずに小窓(PiP)化させて
     * しまう。GLOBAL_ACTION_BACKはPiPを誘発しないため、これを使う。
     * 1回のBACKでルート画面まで戻りきれない場合は、その後も発生する
     * 画面変化イベントのたびにこの処理が呼ばれ、結果的に複数回backした
     * のと同じ動きになりアプリの外まで戻る。
     */
    private fun blockCurrentScreen() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun activeTargets(type: RuleTargetType): Set<String> {
        val isHome = isHomeWifiConnected(applicationContext, homeWifiSettings.homeSsids)
        val holidayDates = holidayRepository.cachedHolidayDates()
        val now = LocalDateTime.now()
        return ruleSettings.rules
            .filter { it.targetType == type && isRuleActive(it, now, isHome, holidayDates) }
            .flatMap { it.targets }
            .toSet()
    }

    private fun currentAddressBarText(urlBarId: String): String? {
        val root = rootInActiveWindow ?: return null
        return root.findAccessibilityNodeInfosByViewId(urlBarId).firstOrNull()?.text?.toString()
    }

    override fun onInterrupt() {}
}
