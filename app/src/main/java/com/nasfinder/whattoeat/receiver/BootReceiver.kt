package com.nasfinder.whattoeat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nasfinder.whattoeat.data.NotificationHelper

class BootReceiver : BroadcastReceiver {
    constructor() : super()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.scheduleDailyAlarm(context)
        }
    }
}
