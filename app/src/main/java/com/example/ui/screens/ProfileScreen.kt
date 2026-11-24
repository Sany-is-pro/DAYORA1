package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LocalDayClock
import com.example.ui.components.CoinStatChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ModernSectionCard
import com.example.ui.components.ProfileAvatarCropDialog
import com.example.ui.components.ProfileAvatarImage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.RewardGold
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.DayoraAppUiState
import com.example.ui.viewmodel.DayoraViewModel

@Composable
fun ProfileScreen(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onOpenSettings: () -> Unit,
  onAfterResetAll: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI

  var showEditNameDialog by remember { mutableStateOf(false) }
  var showResetDialog by remember { mutableStateOf(false) }
  var showPhotoOptionsDialog by remember { mutableStateOf(false) }
  var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri ->
      if (uri != null) {
        pendingCropUri = uri
      }
    },
  )

  fun launchGalleryPicker() {
    photoPickerLauncher.launch(
      PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    )
  }

  val challengeCompleted = state.challengeDays.count { it.completedEpochDay != null }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Top Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.profileTitle,
          style = MaterialTheme.typography.headlineSmall,
          color = colors.mainText,
        )
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("profile_settings_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Settings,
            contentDescription = strings.openSettings,
            tint = colors.mainText,
          )
        }
      }
    }

    // Avatar & User Card
    item {
      ModernSectionCard {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier = Modifier
              .size(102.dp)
              .minimumInteractiveComponentSize()
              .clip(CircleShape)
              .clickable {
                if (state.prefs.profileImageUri.isBlank()) {
                  launchGalleryPicker()
                } else {
                  showPhotoOptionsDialog = true
                }
              }
              .testTag("profile_avatar_button"),
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(
                  width = 3.dp,
                  brush = Brush.linearGradient(listOf(colors.primaryAccent, colors.pinkAccent, RewardGold)),
                  shape = CircleShape,
                )
                .padding(4.dp)
                .clip(CircleShape),
            ) {
              ProfileAvatarImage(
                imageUri = state.prefs.profileImageUri,
                contentDescription = strings.profileTitle,
                modifier = Modifier
                  .fillMaxSize()
                  .clip(CircleShape),
              )
            }
            Surface(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp),
              shape = CircleShape,
              color = colors.primaryAccent,
              border = BorderStroke(2.dp, colors.cardSurface),
              shadowElevation = 4.dp,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Rounded.PhotoCamera,
                  contentDescription = strings.changeProfilePhoto,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            FilledTonalButton(
              onClick = { launchGalleryPicker() },
              shape = RoundedCornerShape(50),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("profile_change_photo_button"),
            ) {
              Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.changeProfilePhoto, style = MaterialTheme.typography.labelLarge)
            }
            if (state.prefs.profileImageUri.isNotBlank()) {
              TextButton(
                onClick = { viewModel.resetProfileAvatarToDefault() },
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("profile_reset_avatar_button"),
              ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.resetCropLabel)
              }
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = state.prefs.fullName.ifBlank { "DAYORA User" },
              style = MaterialTheme.typography.titleLarge,
              color = colors.mainText,
            )
            IconButton(
              onClick = { showEditNameDialog = true },
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("profile_edit_name_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = strings.editName,
                tint = colors.primaryAccent,
                modifier = Modifier.size(18.dp),
              )
            }
          }
          if (state.prefs.accountCreatedAtMillis > 0L) {
            Text(
              text = "${strings.memberSince} ${LocalDayClock.formatShortDate(state.prefs.accountCreatedAtMillis, state.prefs.language.code)}",
              style = MaterialTheme.typography.bodySmall,
              color = colors.secondaryText,
            )
          }
          Spacer(modifier = Modifier.height(16.dp))

          // Stats Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            ProfileStatTile(
              value = state.coinBalance.toString(),
              label = strings.totalCoinsStat,
              bg = colors.softPink,
              modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
              value = state.completions.size.toString(),
              label = strings.activitiesDoneStat,
              bg = colors.lavenderTint,
              modifier = Modifier.weight(1f),
            )
            ProfileStatTile(
              value = "$challengeCompleted/7",
              label = strings.challengeStreakStat,
              bg = colors.successMint.copy(alpha = 0.28f),
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // Quick Theme & Language Switchers
    item {
      ModernSectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = strings.quickPreferences,
            style = MaterialTheme.typography.titleMedium,
            color = colors.mainText,
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = state.prefs.themeMode == ThemeMode.LIGHT,
              onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
              label = { Text(strings.themeLight) },
            )
            FilterChip(
              selected = state.prefs.themeMode == ThemeMode.DARK,
              onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
              label = { Text(strings.themeDark) },
            )
            FilterChip(
              selected = state.prefs.themeMode == ThemeMode.SYSTEM,
              onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
              label = { Text(strings.themeSystem) },
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = state.prefs.language == AppLanguage.ENGLISH,
              onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
              label = { Text("English") },
            )
            FilterChip(
              selected = state.prefs.language == AppLanguage.BENGALI,
              onClick = { viewModel.setLanguage(AppLanguage.BENGALI) },
              label = { Text("বাংলা") },
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.openSettings)
          }
        }
      }
    }

    // Activity History Header
    item {
      Text(
        text = strings.activityHistory,
        style = MaterialTheme.typography.titleLarge,
        color = colors.mainText,
      )
    }

    if (state.completions.isEmpty()) {
      item {
        EmptyStateCard(title = strings.noHistoryYet, icon = Icons.Rounded.History)
      }
    } else {
      items(state.completions.take(25), key = { it.id }) { record ->
        ModernSectionCard {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = colors.successMintDeep,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isBn) record.activityTitleBn else record.activityTitleEn,
                style = MaterialTheme.typography.titleSmall,
                color = colors.mainText,
              )
              Text(
                text = LocalDayClock.formatDateTime(record.completedAtMillis, state.prefs.language.code),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
              )
            }
            CoinStatChip(amount = record.coinsEarned)
          }
        }
      }
    }

    // Reset Personal Data Button
    item {
      OutlinedButton(
        onClick = { showResetDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .minimumInteractiveComponentSize()
          .testTag("profile_reset_data_button"),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.danger),
      ) {
        Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.resetPersonalData)
      }
    }
  }

  if (showEditNameDialog) {
    EditNameDialog(
      currentName = state.prefs.fullName,
      onDismiss = { showEditNameDialog = false },
      onSave = { newName ->
        viewModel.saveFullName(newName) { showEditNameDialog = false }
      },
    )
  }

  if (showPhotoOptionsDialog) {
    AlertDialog(
      onDismissRequest = { showPhotoOptionsDialog = false },
      title = { Text(strings.changeProfilePhoto) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Button(
            onClick = {
              showPhotoOptionsDialog = false
              launchGalleryPicker()
            },
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.pickFromGalleryAndCrop)
          }
          OutlinedButton(
            onClick = {
              showPhotoOptionsDialog = false
              viewModel.resetProfileAvatarToDefault()
            },
            modifier = Modifier
              .fillMaxWidth()
              .minimumInteractiveComponentSize(),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.useDefaultCuteAvatar)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showPhotoOptionsDialog = false }) {
          Text(strings.cancel)
        }
      },
    )
  }

  pendingCropUri?.let { sourceUri ->
    ProfileAvatarCropDialog(
      sourceUri = sourceUri,
      onDismiss = { pendingCropUri = null },
      onCropped = { croppedBitmap ->
        viewModel.saveCroppedProfileAvatar(croppedBitmap) {
          pendingCropUri = null
        }
      },
    )
  }

  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      title = { Text(strings.resetPersonalData) },
      text = { Text(strings.resetPersonalDataConfirm) },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            viewModel.resetAllPersonalData(onAfterResetAll)
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
        ) {
          Text(strings.confirmReset)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetDialog = false }) {
          Text(strings.cancel)
        }
      },
    )
  }
}

@Composable
private fun ProfileStatTile(
  value: String,
  label: String,
  bg: Color,
  modifier: Modifier = Modifier,
) {
  val colors = LocalDayoraColors.current
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    color = bg.copy(alpha = if (colors.isDark) 0.55f else 0.9f),
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = colors.mainText,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = colors.secondaryText,
      )
    }
  }
}

@Composable
fun EditNameDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit,
) {
  val strings = LocalAppStrings.current
  var input by remember(currentName) { mutableStateOf(currentName) }
  var isError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.editName) },
    text = {
      Column {
        OutlinedTextField(
          value = input,
          onValueChange = {
            input = it
            isError = false
          },
          label = { Text(strings.fullNameLabel) },
          singleLine = true,
          isError = isError,
        )
        if (isError) {
          Text(
            text = strings.nameRequiredError,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 6.dp),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val trimmed = input.trim()
          if (trimmed.isEmpty()) isError = true else onSave(trimmed)
        }
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
  )
}
