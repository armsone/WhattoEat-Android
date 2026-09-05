package com.nasfinder.whattoeat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nasfinder.whattoeat.data.NotificationHelper

class AlarmReceiver : BroadcastReceiver {
    constructor() : super()

    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val pending = goAsync()
        Thread {
            try {
                val store = com.nasfinder.whattoeat.data.ChoiceStore(appContext)
                if (store.lunchNotifyEnabled && store.lunchExcludeHolidays) {
                    com.nasfinder.whattoeat.data.KoreanHolidayService.refresh(appContext)
                }
                NotificationHelper.showLunchNotification(appContext)
            } finally {
                pending.finish()
            }
        }.start()
    }
}
