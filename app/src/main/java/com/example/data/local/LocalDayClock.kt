package com.example.data.local

import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * API-24 compatible local calendar day utilities.
 * Uses local device timezone midnight to compute consistent day indices without requiring API 26 java.time.
 */
object LocalDayClock {
  private const val MILLIS_PER_DAY = 86_400_000L

  fun currentEpochDay(nowMillis: Long = System.currentTimeMillis()): Long {
    val cal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val offset = cal.timeZone.getOffset(cal.timeInMillis)
    return (cal.timeInMillis + offset) / MILLIS_PER_DAY
  }

  fun currentHourOfDay(nowMillis: Long = System.currentTimeMillis()): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return cal.get(Calendar.HOUR_OF_DAY)
  }

  fun formatShortDate(millis: Long, languageCode: String): String {
    val locale = if (languageCode == "bn") Locale("bn", "BD") else Locale.ENGLISH
    return DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis))
  }

  fun formatDateTime(millis: Long, languageCode: String): String {
    val locale = if (languageCode == "bn") Locale("bn", "BD") else Locale.ENGLISH
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
      .format(Date(millis))
  }
}
