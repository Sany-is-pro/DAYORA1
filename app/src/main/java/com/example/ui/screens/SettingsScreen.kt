package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.ui.components.ModernSectionCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.DayoraAppUiState
import com.example.ui.viewmodel.DayoraViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onBack: () -> Unit,
  onOpenAdmin: () -> Unit,
  onResetOnboardingDone: () -> Unit,
  onResetAllDone: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val context = LocalContext.current

  var showEditName by remember { mutableStateOf(false) }
  var showTimePicker by remember { mutableStateOf(false) }
  var showResetOnboardingConfirm by remember { mutableStateOf(false) }
  var showClearHistoryConfirm by remember { mutableStateOf(false) }
  var showResetAllConfirm by remember { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
      if (granted) {
        viewModel.updateReminders(
          enabled = true,
          hour = state.prefs.reminderHour,
          minute = state.prefs.reminderMinute,
        )
      }
    },
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp, end = 18.dp, top = 10.dp, bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .testTag("settings_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Back",
          tint = colors.mainText,
        )
      }
      Text(
        text = strings.settingsTitle,
        style = MaterialTheme.typography.headlineSmall,
        color = colors.mainText,
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // 1. Theme Settings
      item {
        ModernSectionCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.themeSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = state.prefs.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                label = { Text(strings.themeLight) },
                modifier = Modifier.testTag("settings_theme_light"),
              )
              FilterChip(
                selected = state.prefs.themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                label = { Text(strings.themeDark) },
                modifier = Modifier.testTag("settings_theme_dark"),
              )
              FilterChip(
                selected = state.prefs.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                label = { Text(strings.themeSystem) },
                modifier = Modifier.testTag("settings_theme_system"),
              )
            }
          }
        }
      }

      // 2. Language Settings
      item {
        ModernSectionCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.languageSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              FilterChip(
                selected = state.prefs.language == AppLanguage.ENGLISH,
                onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                label = { Text("English") },
                modifier = Modifier.testTag("settings_lang_en"),
              )
              FilterChip(
                selected = state.prefs.language == AppLanguage.BENGALI,
                onClick = { viewModel.setLanguage(AppLanguage.BENGALI) },
                label = { Text("বাংলা") },
                modifier = Modifier.testTag("settings_lang_bn"),
              )
            }
          }
        }
      }

      // 3. Notifications & Motion
      item {
        ModernSectionCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.notificationsSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = strings.dailyRemindersLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mainText,
                modifier = Modifier.weight(1f),
              )
              Switch(
                checked = state.prefs.remindersEnabled,
                onCheckedChange = { enable ->
                  if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
                  ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                  } else {
                    viewModel.updateReminders(enable, state.prefs.reminderHour, state.prefs.reminderMinute)
                  }
                },
                modifier = Modifier.testTag("settings_reminders_switch"),
              )
            }
            if (state.prefs.remindersEnabled) {
              Spacer(modifier = Modifier.height(8.dp))
              val formattedTime = String.format(
                Locale.US,
                "%02d:%02d",
                state.prefs.reminderHour,
                state.prefs.reminderMinute,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("${strings.reminderTimeLabel}: $formattedTime", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                TextButton(onClick = { showTimePicker = true }) {
                  Text(strings.editLabel)
                }
              }
              OutlinedButton(
                onClick = { viewModel.sendTestReminder() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.testNotification)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = strings.reducedMotionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mainText,
                modifier = Modifier.weight(1f),
              )
              Switch(
                checked = state.prefs.reducedMotion,
                onCheckedChange = { viewModel.setReducedMotion(it) },
              )
            }
          }
        }
      }

      // 4. Profile Settings
      item {
        ModernSectionCard {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(strings.profileSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
              Text(state.prefs.fullName, style = MaterialTheme.typography.bodyMedium, color = colors.secondaryText)
            }
            OutlinedButton(onClick = { showEditName = true }) {
              Text(strings.editName)
            }
          }
        }
      }

      // 5. Data Management
      item {
        ModernSectionCard {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(strings.dataManagementSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            OutlinedButton(
              onClick = { showResetOnboardingConfirm = true },
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(14.dp),
            ) {
              Text(strings.resetOnboarding)
            }
            OutlinedButton(
              onClick = { showClearHistoryConfirm = true },
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(14.dp),
            ) {
              Text(strings.clearActivityHistory)
            }
            OutlinedButton(
              onClick = { showResetAllConfirm = true },
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.6f)),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
            ) {
              Text(strings.resetAllData)
            }
          }
        }
      }

      // 6. About DAYORA
      item {
        ModernSectionCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.aboutSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${strings.appName} v${BuildConfig.VERSION_NAME}",
              style = MaterialTheme.typography.labelLarge,
              color = colors.primaryAccent,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = strings.aboutDescription,
              style = MaterialTheme.typography.bodySmall,
              color = colors.secondaryText,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = { openYouTubeChannel(context) },
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize(),
              shape = RoundedCornerShape(14.dp),
            ) {
              Icon(Icons.Rounded.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(strings.openYoutubeChannel)
            }
          }
        }
      }

      // 7. Admin Access
      item {
        ModernSectionCard(containerColor = colors.lavenderTint.copy(alpha = 0.6f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.adminAccessSection, style = MaterialTheme.typography.titleMedium, color = colors.mainText)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = onOpenAdmin,
              modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("settings_open_admin_button"),
              shape = RoundedCornerShape(16.dp),
            ) {
              Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(strings.openAdminPanel)
            }
          }
        }
      }
    }
  }

  if (showEditName) {
    EditNameDialog(
      currentName = state.prefs.fullName,
      onDismiss = { showEditName = false },
      onSave = { newName -> viewModel.saveFullName(newName) { showEditName = false } },
    )
  }

  if (showTimePicker) {
    val pickerState = rememberTimePickerState(
      initialHour = state.prefs.reminderHour,
      initialMinute = state.prefs.reminderMinute,
    )
    AlertDialog(
      onDismissRequest = { showTimePicker = false },
      title = { Text(strings.reminderTimeLabel) },
      text = { TimePicker(state = pickerState) },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateReminders(true, pickerState.hour, pickerState.minute)
            showTimePicker = false
          }
        ) { Text(strings.save) }
      },
      dismissButton = {
        TextButton(onClick = { showTimePicker = false }) { Text(strings.cancel) }
      },
    )
  }

  if (showResetOnboardingConfirm) {
    AlertDialog(
      onDismissRequest = { showResetOnboardingConfirm = false },
      title = { Text(strings.resetOnboarding) },
      text = { Text(strings.resetOnboardingConfirm) },
      confirmButton = {
        Button(
          onClick = {
            showResetOnboardingConfirm = false
            viewModel.resetOnboarding(onResetOnboardingDone)
          }
        ) { Text(strings.confirmReset) }
      },
      dismissButton = {
        TextButton(onClick = { showResetOnboardingConfirm = false }) { Text(strings.cancel) }
      },
    )
  }

  if (showClearHistoryConfirm) {
    AlertDialog(
      onDismissRequest = { showClearHistoryConfirm = false },
      title = { Text(strings.clearActivityHistory) },
      text = { Text(strings.clearHistoryConfirm) },
      confirmButton = {
        Button(
          onClick = {
            showClearHistoryConfirm = false
            viewModel.clearActivityHistory()
          }
        ) { Text(strings.confirmReset) }
      },
      dismissButton = {
        TextButton(onClick = { showClearHistoryConfirm = false }) { Text(strings.cancel) }
      },
    )
  }

  if (showResetAllConfirm) {
    AlertDialog(
      onDismissRequest = { showResetAllConfirm = false },
      title = { Text(strings.resetAllData) },
      text = { Text(strings.resetPersonalDataConfirm) },
      confirmButton = {
        Button(
          onClick = {
            showResetAllConfirm = false
            viewModel.resetAllPersonalData(onResetAllDone)
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
        ) { Text(strings.confirmReset) }
      },
      dismissButton = {
        TextButton(onClick = { showResetAllConfirm = false }) { Text(strings.cancel) }
      },
    )
  }
}
