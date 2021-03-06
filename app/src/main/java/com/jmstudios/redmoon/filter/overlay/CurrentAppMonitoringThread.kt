/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.filter.overlay

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.ContextWrapper

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.util.*

import java.lang.Thread
import java.util.TreeMap

class CurrentAppMonitoringThread(private val mContext: Context) : Thread() {

    init {
        Log.d("CurrentAppMonitoringThread created")
    }

    override fun run() {
        Log.i("CurrentAppMonitoringThread running")

        try {
            while (!Thread.interrupted()) {
                val currentApp = getCurrentApp(mContext)
                Log.d("Current app is: $currentApp")
                handleApp(currentApp)
                Thread.sleep(1000)
            }
        } catch (e: InterruptedException) {
        }

        Log.i("Shutting down CurrentAppMonitoringThread")
    }

    private fun handleApp(app: String) {
        when(app) {
            "com.android.packageinstaller",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "me.phh.superuser",
            "com.owncloud.android",
            "com.google.android.packageinstaller" -> Command.SUSPEND.send()
            // Opening the notification shade shows red moon as the active
            // package. Closing it does not restore the previous package, so
            // there's no chance to re-pause, so we should not unpause.
            "com.jmstudios.redmoon", "com.jmstudios.redmoon.debug" -> {}
            else -> Command.RESUME.send()
        }
    }

    companion object : Logger(false) {

        fun isAppMonitoringWorking(context: Context): Boolean {
            return getCurrentApp(context) != ""
        }

        private fun getCurrentApp(context: Context): String {
            // http://stackoverflow.com/q/33581311
            if (atLeastAPI(21)) {
                return getCurrentAppUsingUsageStats(context)
            } else {
                return getCurrentAppUsingActivityManager(context)
            }
        }

        private fun getCurrentAppUsingUsageStats(context: Context): String {
            try {
                if (atLeastAPI(21)) @TargetApi(21) {
                    // Although the UsageStatsManager was added in API 21, the
                    // constant to specify it wasn't added until API 22.
                    // So we use the value of that constant on API 21.
                    val usageStatsServiceString =
                        if (atLeastAPI(22)) Context.USAGE_STATS_SERVICE
                        else "usagestats"
                    val usm = context.getSystemService(usageStatsServiceString) as UsageStatsManager
                    val time = System.currentTimeMillis()
                    val appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                            time - 1000 * 1000, time)

                    if (appList != null && appList.size > 0) {
                        val mySortedMap = TreeMap<Long, UsageStats>()
                        for (usageStats in appList) {
                            mySortedMap.put(usageStats.lastTimeUsed,
                                    usageStats)
                        }
                        if (!mySortedMap.isEmpty()) {
                            val packageName = mySortedMap[mySortedMap.lastKey()]!!.packageName
                            return packageName ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore exceptions to allow the user to determine if it
                // works him/herself
            }

            return ""
        }

        private fun getCurrentAppUsingActivityManager(context: Context): String {
            if (belowAPI(21)) @Suppress("DEPRECATION") {
                val am = ContextWrapper(context).baseContext
                          .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                return am.getRunningTasks(1)[0].topActivity.packageName
            }
            return ""
        }
    }
}
