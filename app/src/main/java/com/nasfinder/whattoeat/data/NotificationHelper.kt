package com.nasfinder.whattoeat.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nasfinder.whattoeat.MainActivity
import com.nasfinder.whattoeat.receiver.AlarmReceiver
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "whattoeat.lunch.daily"
    const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "점심 알림"
            val descriptionText = "점심시간 전에 오늘의 추천을 알려드려요."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyAlarm(context: Context) {
        val store = ChoiceStore(context)
        if (!store.lunchNotifyEnabled) {
            cancelDailyAlarm(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hour = store.lunchHour
        val minute = store.lunchMinute
        val lead = store.lunchLeadMinutes

        var totalMinutes = hour * 60 + minute - lead
        if (totalMinutes < 0) {
            totalMinutes += 24 * 60
        }
        val targetHour = totalMinutes / 60
        val targetMinute = totalMinutes % 60

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            // Alarm scheduling error fallback
        }
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showLunchNotification(context: Context) {
        createNotificationChannel(context)
        val store = ChoiceStore(context)
        val region = store.manualResolvedName.ifEmpty { "내 주변" }
        val lastMenu = store.lastTopMenu

        val title = "곧 점심시간이에요"
        val body = if (lastMenu.isNotEmpty()) {
            "$region 근처에서 마지막으로 본 추천 후보는 ‘$lastMenu’였어요. 앱을 열면 주변 후보를 다시 찾아 드려요."
        } else {
            "$region 근처 점심 후보를 앱에서 찾아보세요."
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
