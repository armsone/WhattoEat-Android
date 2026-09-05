package com.nasfinder.whattoeat

import android.app.Application
import com.nasfinder.whattoeat.data.NotificationHelper
import com.nasfinder.whattoeat.data.ImageLoader
import com.nasfinder.whattoeat.update.DirectUpdateManager

class WhattoEatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageLoader.installHttpCache(this)
        NotificationHelper.createNotificationChannel(this)
        val store = com.nasfinder.whattoeat.data.ChoiceStore(this)
        if (store.lunchNotifyEnabled && store.lunchExcludeHolidays) {
            Thread { com.nasfinder.whattoeat.data.KoreanHolidayService.refresh(this) }.start()
        }
        DirectUpdateManager.get(this).start()
    }
}
