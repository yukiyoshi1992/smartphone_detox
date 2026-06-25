package com.yukiyoshi.smphdetox.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yukiyoshi.smphdetox.rule.AppRule
import com.yukiyoshi.smphdetox.rule.AppRuleSettings
import com.yukiyoshi.smphdetox.rule.RuleTargetType
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 通知ルールの開始/終了時刻ちょうどに端末を起こして反映するための厳密なアラーム。
 * 1回限りのアラームなので、発火後（NotificationAlarmReceiver側で）次の時刻を
 * 再計算してまた1回スケジュールし直す、を繰り返す。
 */
object NotificationAlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        val am = context.getSystemService(AlarmManager::class.java) ?: return false
        return am.canScheduleExactAlarms()
    }

    fun reschedule(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val rules = AppRuleSettings(context).rules
            .filter { it.targetType == RuleTargetType.NOTIFICATION && it.enabled }
        val triggerMillis = nextTriggerMillis(rules, LocalDateTime.now()) ?: return
        val pendingIntent = alarmPendingIntent(context)
        if (am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun alarmPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationAlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /**
     * 有効な通知ルールの開始/終了時刻（時:分）のうち、今より後で最も早く来るものを返す。
     * 曜日・在宅・祝日の判定はアラーム発火時にRuleEngine側で正しく行うため、
     * ここでは「起こすべき時:分」の集合だけを見て次のタイミングを決める。
     */
    internal fun nextTriggerMillis(rules: List<AppRule>, now: LocalDateTime): Long? {
        val times = rules.flatMap { listOf(it.startTime, it.endTime) }.distinct()
        if (times.isEmpty()) return null
        val today = now.toLocalDate()
        val candidates = times.map { time ->
            val candidate = LocalDateTime.of(today, time)
            if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
        }
        return candidates.min().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
