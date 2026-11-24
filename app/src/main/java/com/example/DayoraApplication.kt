package com.example

import android.app.Application
import com.example.data.local.DayoraDatabase
import com.example.data.local.UserPreferencesStore
import com.example.data.repository.DayoraRepository
import com.example.data.repository.LocalDayoraRepository
import com.example.notifications.DailyReminderScheduler

class DayoraApplication : Application() {
  val database: DayoraDatabase by lazy { DayoraDatabase.getInstance(this) }
  val prefsStore: UserPreferencesStore by lazy { UserPreferencesStore(this) }
  val repository: DayoraRepository by lazy {
    LocalDayoraRepository(
      context = applicationContext,
      dao = database.dayoraDao(),
      prefsStore = prefsStore,
    )
  }

  override fun onCreate() {
    super.onCreate()
    DailyReminderScheduler.ensureChannel(this)
  }
}
