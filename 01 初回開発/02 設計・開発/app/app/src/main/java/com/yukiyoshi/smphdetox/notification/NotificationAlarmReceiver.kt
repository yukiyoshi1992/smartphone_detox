package com.yukiyoshi.smphdetox.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yukiyoshi.smphdetox.block.AppMasterSettings
import com.yukiyoshi.smphdetox.holiday.HolidayRepository
import com.yukiyoshi.smphdetox.home.HomeWifiSettings
import com.yukiyoshi.smphdetox.home.isHomeWifiConnected
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import com.yukiyoshi.smphdetox.rule.activeRules
import java.time.LocalDateTime

private const val TAG = "NotificationAlarm"

/** 通知ルールの開始/終了時刻ちょうどに発火し、リンガーモードを反映後、次の時刻を再スケジュールする。 */
class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (AppMasterSettings(context).enabled) {
            val ruleSettings = AppRuleSettings(context)
            val homeWifiSettings = HomeWifiSettings(context)
            val holidayRepository = HolidayRepository(context)
            val isHome = isHomeWifiConnected(context, homeWifiSettings.homeSsids)
            val holidayDates = holidayRepository.cachedHolidayDates()
            val notificationRules = ruleSettings.rules.filter { it.targetType == RuleTargetType.NOTIFICATION }
            val active = activeRules(notificationRules, LocalDateTime.now(), isHome, holidayDates)
            applyRingerMode(context, quiet = active.isNotEmpty())
            Log.d(TAG, "fired, active=${active.map { it.label }}")
        }
        NotificationAlarmScheduler.reschedule(context)
    }
}
