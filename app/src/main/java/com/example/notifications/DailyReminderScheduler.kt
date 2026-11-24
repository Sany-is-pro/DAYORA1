package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object DailyReminderScheduler {
  const val CHANNEL_ID = "dayora_daily_reminders"
  private const val REQUEST_CODE = 4201
  private const val NOTIFICATION_ID = 4202
  const val EXTRA_LANG = "extra_lang"

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Daily Self-Care Reminders",
        NotificationManager.IMPORTANCE_DEFAULT,
      ).apply {
        description = "Gentle daily reminders for your DAYORA lifestyle routines."
      }
      manager.createNotificationChannel(channel)
    }
  }

  fun scheduleDaily(context: Context, hour: Int, minute: Int, languageCode: String) {
    ensureChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, DailyReminderReceiver::class.java).apply {
      putExtra(EXTRA_LANG, languageCode)
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val cal = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }
    alarmManager.setInexactRepeating(
      AlarmManager.RTC_WAKEUP,
      cal.timeInMillis,
      AlarmManager.INTERVAL_DAY,
      pendingIntent,
    )
  }

  fun cancelDaily(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, DailyReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    alarmManager.cancel(pendingIntent)
  }

  fun showImmediateReminder(context: Context, languageCode: String) {
    ensureChannel(context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
      PackageManager.PERMISSION_GRANTED
    ) {
      return
    }
    val launchIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentIntent = PendingIntent.getActivity(
      context,
      0,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val isBn = languageCode == "bn"
    val title = if (isBn) "DAYORA দৈনিক যত্ন" else "DAYORA Self-Care Spark"
    val body = if (isBn) {
      "আজকের একটি সুন্দর কার্যক্রম সম্পন্ন করে কয়েন সংগ্রহ করুন!"
    } else {
      "Ready to make today a little more magical? Complete a quick routine!"
    }
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(body)
      .setContentIntent(contentIntent)
      .setAutoCancel(true)
      .build()
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
  }
}

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val lang = intent.getStringExtra(DailyReminderScheduler.EXTRA_LANG) ?: "en"
    DailyReminderScheduler.showImmediateReminder(context, lang)
  }
}
