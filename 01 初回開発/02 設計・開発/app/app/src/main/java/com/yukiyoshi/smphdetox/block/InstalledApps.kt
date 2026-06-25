package com.yukiyoshi.smphdetox.block

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class InstalledApp(val label: String, val packageName: String)

/**
 * 起動可能なアプリ（ランチャーに表示されるアプリ）の一覧を取得する。
 * Android 11+のパッケージ可視性制限の対象外にするため、Manifestの<queries>に
 * ACTION_MAIN/CATEGORY_LAUNCHERのintentを宣言している（QUERY_ALL_PACKAGES権限は不要）。
 */
fun listLaunchableApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        .map { InstalledApp(label = it.loadLabel(pm).toString(), packageName = it.activityInfo.packageName) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}
