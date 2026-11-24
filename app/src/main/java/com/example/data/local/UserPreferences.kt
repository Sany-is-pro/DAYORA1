package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dayoraDataStore: DataStore<Preferences> by preferencesDataStore(
  name = "dayora_user_prefs"
)

data class UserPrefsSnapshot(
  val fullName: String = "",
  val onboardingCompleted: Boolean = false,
  val youtubeConfirmed: Boolean = false,
  val accountCreatedAtMillis: Long = 0L,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val remindersEnabled: Boolean = false,
  val reminderHour: Int = 8,
  val reminderMinute: Int = 30,
  val reducedMotion: Boolean = false,
  val hasAdminPasscode: Boolean = false,
  val profileImageUri: String = "",
)

class UserPreferencesStore(private val context: Context) {
  private object Keys {
    val FULL_NAME = stringPreferencesKey("full_name")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val YOUTUBE_CONFIRMED = booleanPreferencesKey("youtube_confirmed")
    val ACCOUNT_CREATED_AT = longPreferencesKey("account_created_at")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LANGUAGE = stringPreferencesKey("language")
    val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    val ADMIN_PASSCODE_SALT = stringPreferencesKey("admin_passcode_salt")
    val ADMIN_PASSCODE_HASH = stringPreferencesKey("admin_passcode_hash")
    val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
  }

  val prefsFlow: Flow<UserPrefsSnapshot> = context.dayoraDataStore.data.map { prefs ->
    UserPrefsSnapshot(
      fullName = prefs[Keys.FULL_NAME].orEmpty(),
      onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
      youtubeConfirmed = prefs[Keys.YOUTUBE_CONFIRMED] ?: false,
      accountCreatedAtMillis = prefs[Keys.ACCOUNT_CREATED_AT] ?: 0L,
      themeMode = ThemeMode.fromStorage(prefs[Keys.THEME_MODE]),
      language = AppLanguage.fromCode(prefs[Keys.LANGUAGE]),
      remindersEnabled = prefs[Keys.REMINDERS_ENABLED] ?: false,
      reminderHour = prefs[Keys.REMINDER_HOUR] ?: 8,
      reminderMinute = prefs[Keys.REMINDER_MINUTE] ?: 30,
      reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
      hasAdminPasscode = !prefs[Keys.ADMIN_PASSCODE_HASH].isNullOrEmpty(),
      profileImageUri = prefs[Keys.PROFILE_IMAGE_URI].orEmpty(),
    )
  }

  suspend fun saveFullName(name: String) {
    val trimmed = name.trim()
    context.dayoraDataStore.edit { prefs ->
      prefs[Keys.FULL_NAME] = trimmed
      if ((prefs[Keys.ACCOUNT_CREATED_AT] ?: 0L) == 0L) {
        prefs[Keys.ACCOUNT_CREATED_AT] = System.currentTimeMillis()
      }
    }
  }

  suspend fun confirmYoutubeAndCompleteOnboarding() {
    context.dayoraDataStore.edit { prefs ->
      prefs[Keys.YOUTUBE_CONFIRMED] = true
      prefs[Keys.ONBOARDING_COMPLETED] = true
      if ((prefs[Keys.ACCOUNT_CREATED_AT] ?: 0L) == 0L) {
        prefs[Keys.ACCOUNT_CREATED_AT] = System.currentTimeMillis()
      }
    }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dayoraDataStore.edit { it[Keys.THEME_MODE] = mode.storageValue }
  }

  suspend fun setLanguage(language: AppLanguage) {
    context.dayoraDataStore.edit { it[Keys.LANGUAGE] = language.code }
  }

  suspend fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
    context.dayoraDataStore.edit {
      it[Keys.REMINDERS_ENABLED] = enabled
      it[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
      it[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
    }
  }

  suspend fun setReducedMotion(reduced: Boolean) {
    context.dayoraDataStore.edit { it[Keys.REDUCED_MOTION] = reduced }
  }

  suspend fun setProfileImageUri(uri: String) {
    context.dayoraDataStore.edit { it[Keys.PROFILE_IMAGE_URI] = uri }
  }

  suspend fun resetOnboarding() {
    context.dayoraDataStore.edit {
      it[Keys.ONBOARDING_COMPLETED] = false
      it[Keys.YOUTUBE_CONFIRMED] = false
    }
  }

  suspend fun resetAllPersonalPreferences() {
    context.dayoraDataStore.edit { prefs ->
      val keepSalt = prefs[Keys.ADMIN_PASSCODE_SALT]
      val keepHash = prefs[Keys.ADMIN_PASSCODE_HASH]
      prefs.clear()
      if (keepSalt != null && keepHash != null) {
        prefs[Keys.ADMIN_PASSCODE_SALT] = keepSalt
        prefs[Keys.ADMIN_PASSCODE_HASH] = keepHash
      }
    }
  }

  suspend fun setAdminPasscode(passcode: String) {
    val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
    val saltHex = saltBytes.joinToString("") { "%02x".format(it) }
    val hashHex = sha256("$saltHex:${passcode.trim()}")
    context.dayoraDataStore.edit {
      it[Keys.ADMIN_PASSCODE_SALT] = saltHex
      it[Keys.ADMIN_PASSCODE_HASH] = hashHex
    }
  }

  suspend fun verifyAdminPasscode(passcode: String): Boolean {
    val prefs = context.dayoraDataStore.data.first()
    val salt = prefs[Keys.ADMIN_PASSCODE_SALT] ?: return false
    val expectedHash = prefs[Keys.ADMIN_PASSCODE_HASH] ?: return false
    return sha256("$salt:${passcode.trim()}") == expectedHash
  }

  private fun sha256(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }
}
