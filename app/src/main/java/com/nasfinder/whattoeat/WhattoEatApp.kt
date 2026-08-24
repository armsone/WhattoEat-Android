package com.nasfinder.whattoeat

import android.app.Application
import com.nasfinder.whattoeat.data.NotificationHelper
import com.nasfinder.whattoeat.data.ImageLoader

class WhattoEatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageLoader.installHttpCache(this)
        NotificationHelper.createNotificationChannel(this)
    }
}
