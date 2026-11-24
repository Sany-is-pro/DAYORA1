package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ActivityEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.RewardItemEntity
import com.example.data.local.WeeklyChallengeConfigEntity
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.ModernSectionCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.viewmodel.DayoraAppUiState
import com.example.ui.viewmodel.DayoraViewModel

@Composable
fun AdminScreen(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()

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
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("admin_back_button"),
        ) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = colors.mainText)
        }
        Text(strings.adminPanelTitle, style = MaterialTheme.typography.titleLarge, color = colors.mainText)
      }
      if (isUnlocked) {
        TextButton(onClick = { viewModel.lockAdmin() }) {
          Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Lock")
        }
      }
    }

    if (!isUnlocked) {
      AdminPasscodeGate(
        hasPasscode = state.prefs.hasAdminPasscode,
        viewModel = viewModel,
      )
    } else {
      AdminDashboardContent(state = state, viewModel = viewModel)
    }
  }
}

@Composable
private fun AdminPasscodeGate(
  hasPasscode: Boolean,
  viewModel: DayoraViewModel,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  var passcode by remember { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    ModernSectionCard(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Lock,
          contentDescription = null,
          tint = colors.primaryAccent,
          modifier = Modifier.size(36.dp),
        )
        Text(
          text = if (hasPasscode) strings.adminUnlockTitle else strings.adminSetupTitle,
          style = MaterialTheme.typography.titleLarge,
          color = colors.mainText,
        )
        Text(
          text = if (hasPasscode) strings.adminLocalNotice else strings.adminSetupDesc,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText,
        )
        OutlinedTextField(
          value = passcode,
          onValueChange = {
            passcode = it
            errorMsg = null
          },
          label = { Text(strings.adminPasscodeLabel) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          isError = errorMsg != null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_passcode_input"),
        )
        if (errorMsg != null) {
          Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = colors.danger)
        }
        Button(
          onClick = {
            val trimmed = passcode.trim()
            if (!hasPasscode) {
              if (trimmed.length < 4) {
                errorMsg = strings.adminPasscodeMinLength
              } else {
                viewModel.setAdminPasscodeAndUnlock(trimmed) {}
              }
            } else {
              viewModel.verifyAndUnlockAdmin(trimmed) { ok ->
                if (!ok) errorMsg = strings.adminWrongPasscode
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("admin_unlock_button"),
          shape = RoundedCornerShape(16.dp),
        ) {
          Text(if (hasPasscode) strings.adminUnlockBtn else strings.adminSetPasscodeBtn)
        }
      }
    }
  }
}

@Composable
private fun AdminDashboardContent(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI

  var tab by remember { mutableIntStateOf(0) } // 0: Cats, 1: Acts, 2: Rewards, 3: Challenge
  var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
  var creatingCategory by remember { mutableStateOf(false) }
  var deleteCatConfirm by remember { mutableStateOf<CategoryEntity?>(null) }

  var selectedActCatFilter by remember { mutableStateOf<String?>(null) }
  var editingActivity by remember { mutableStateOf<ActivityEntity?>(null) }
  var creatingActivity by remember { mutableStateOf(false) }
  var deleteActConfirm by remember { mutableStateOf<ActivityEntity?>(null) }
  var managingStepsActivity by remember { mutableStateOf<ActivityEntity?>(null) }

  var editingReward by remember { mutableStateOf<RewardItemEntity?>(null) }
  var creatingReward by remember { mutableStateOf(false) }
  var deleteRewardConfirm by remember { mutableStateOf<RewardItemEntity?>(null) }

  Column(modifier = Modifier.fillMaxSize()) {
    Surface(
      color = colors.lavenderTint.copy(alpha = 0.7f),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 4.dp),
    ) {
      Text(
        text = strings.adminLocalNotice,
        style = MaterialTheme.typography.bodySmall,
        color = colors.mainText,
        modifier = Modifier.padding(10.dp),
      )
    }

    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text(strings.adminTabCategories) })
      }
      item {
        FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text(strings.adminTabActivities) })
      }
      item {
        FilterChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text(strings.adminTabRewards) })
      }
      item {
        FilterChip(selected = tab == 3, onClick = { tab = 3 }, label = { Text(strings.adminTabChallenge) })
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      when (tab) {
        0 -> {
          item {
            Button(
              onClick = { creatingCategory = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.addNew)
            }
          }
          items(state.allCategories, key = { it.id }) { cat ->
            val actCount = state.allActivities.count { it.categoryId == cat.id }
            ModernSectionCard(
              modifier = Modifier.clickable {
                selectedActCatFilter = cat.id
                tab = 1
              }
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  DayoraIllustration(
                    imageKey = cat.imageKey,
                    contentDescription = cat.nameEn,
                    modifier = Modifier
                      .size(52.dp)
                      .clip(RoundedCornerShape(12.dp)),
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(if (isBn) cat.nameBn else cat.nameEn, style = MaterialTheme.typography.titleSmall, color = colors.mainText)
                    Text(
                      "$actCount ${strings.adminTabActivities} • #${cat.sortOrder} • ${if (cat.isEnabled) strings.enabledLabel else "Off"}",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText,
                    )
                  }
                  IconButton(
                    onClick = { editingCategory = cat },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(Icons.Rounded.Edit, contentDescription = strings.editLabel)
                  }
                  IconButton(
                    onClick = { deleteCatConfirm = cat },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(Icons.Rounded.Delete, contentDescription = strings.deleteLabel, tint = colors.danger)
                  }
                }
                FilledTonalButton(
                  onClick = {
                    selectedActCatFilter = cat.id
                    tab = 1
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("admin_category_open_${cat.id}"),
                  shape = RoundedCornerShape(14.dp),
                ) {
                  Icon(Icons.Rounded.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("${strings.openCategoryActivities} ($actCount)")
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        1 -> {
          item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { creatingActivity = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.addNew)
              }
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                  FilterChip(
                    selected = selectedActCatFilter == null,
                    onClick = { selectedActCatFilter = null },
                    label = { Text(strings.allCategories) },
                  )
                }
                items(state.allCategories, key = { it.id }) { cat ->
                  FilterChip(
                    selected = selectedActCatFilter == cat.id,
                    onClick = { selectedActCatFilter = if (selectedActCatFilter == cat.id) null else cat.id },
                    label = { Text(if (isBn) cat.nameBn else cat.nameEn) },
                  )
                }
              }
            }
          }
          val list = state.allActivities.filter {
            selectedActCatFilter == null || it.categoryId == selectedActCatFilter
          }
          items(list, key = { it.id }) { act ->
            val stepCount = state.allSteps.count { it.activityId == act.id }
            val catObj = state.allCategories.find { it.id == act.categoryId }
            val catLabel = catObj?.let { if (isBn) it.nameBn else it.nameEn }.orEmpty()
            ModernSectionCard(
              modifier = Modifier.clickable { managingStepsActivity = act }
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  DayoraIllustration(
                    imageKey = act.coverImageKey,
                    contentDescription = act.titleEn,
                    modifier = Modifier
                      .size(54.dp)
                      .clip(RoundedCornerShape(12.dp)),
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(if (isBn) act.titleBn else act.titleEn, style = MaterialTheme.typography.titleSmall, color = colors.mainText)
                    val prefix = if (catLabel.isNotEmpty()) "$catLabel • " else ""
                    Text("$prefix+${act.coinReward} • $stepCount ${strings.stepsShort}", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                  }
                  IconButton(
                    onClick = { editingActivity = act },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(Icons.Rounded.Edit, contentDescription = strings.editLabel)
                  }
                  IconButton(
                    onClick = { deleteActConfirm = act },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                  ) {
                    Icon(Icons.Rounded.Delete, contentDescription = strings.deleteLabel, tint = colors.danger)
                  }
                }
                FilledTonalButton(
                  onClick = { managingStepsActivity = act },
                  modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("admin_manage_steps_${act.id}"),
                  shape = RoundedCornerShape(14.dp),
                ) {
                  Icon(Icons.Rounded.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("${strings.manageSteps} ($stepCount) • ${strings.stepPrefix} 1 → ${strings.nextStep}")
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }

        2 -> {
          item {
            Button(
              onClick = { creatingReward = true },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.addNew)
            }
          }
          items(state.allRewards, key = { it.id }) { reward ->
            ModernSectionCard {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                DayoraIllustration(
                  imageKey = reward.imageKey,
                  contentDescription = reward.titleEn,
                  modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(if (isBn) reward.titleBn else reward.titleEn, style = MaterialTheme.typography.titleSmall, color = colors.mainText)
                  Text("${reward.coinCost} ${strings.coinsLabel}", style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
                }
                IconButton(onClick = { editingReward = reward }) {
                  Icon(Icons.Rounded.Edit, contentDescription = strings.editLabel)
                }
                IconButton(onClick = { deleteRewardConfirm = reward }) {
                  Icon(Icons.Rounded.Delete, contentDescription = strings.deleteLabel, tint = colors.danger)
                }
              }
            }
          }
        }

        else -> {
          item {
            val cfg = state.challengeConfig
            if (cfg != null) {
              ChallengeConfigEditor(
                config = cfg,
                onSave = { updated -> viewModel.upsertChallengeConfig(updated) },
              )
            }
          }
        }
      }
    }
  }

  // Category Modals
  if (creatingCategory || editingCategory != null) {
    CategoryEditDialog(
      initial = editingCategory,
      nextSortOrder = (state.allCategories.maxOfOrNull { it.sortOrder } ?: 0) + 1,
      viewModel = viewModel,
      onDismiss = {
        creatingCategory = false
        editingCategory = null
      },
      onSave = { cat ->
        viewModel.upsertCategory(cat)
        creatingCategory = false
        editingCategory = null
      },
    )
  }
  deleteCatConfirm?.let { cat ->
    AlertDialog(
      onDismissRequest = { deleteCatConfirm = null },
      title = { Text(strings.deleteLabel) },
      text = { Text("Delete '${cat.nameEn}' and its activities?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteCategory(cat.id)
            deleteCatConfirm = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
        ) { Text(strings.deleteLabel) }
      },
      dismissButton = { TextButton(onClick = { deleteCatConfirm = null }) { Text(strings.cancel) } },
    )
  }

  // Activity Modals
  if (creatingActivity || editingActivity != null) {
    ActivityEditDialog(
      initial = editingActivity,
      categories = state.allCategories,
      defaultCategoryId = selectedActCatFilter ?: state.allCategories.firstOrNull()?.id.orEmpty(),
      viewModel = viewModel,
      onDismiss = {
        creatingActivity = false
        editingActivity = null
      },
      onSave = { act ->
        val wasCreating = creatingActivity
        viewModel.upsertActivity(act, createDefaultStepIfNew = false)
        creatingActivity = false
        editingActivity = null
        if (wasCreating) {
          managingStepsActivity = act
        }
      },
    )
  }
  deleteActConfirm?.let { act ->
    AlertDialog(
      onDismissRequest = { deleteActConfirm = null },
      title = { Text(strings.deleteLabel) },
      text = { Text("Delete '${act.titleEn}'?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteActivity(act.id)
            deleteActConfirm = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
        ) { Text(strings.deleteLabel) }
      },
      dismissButton = { TextButton(onClick = { deleteActConfirm = null }) { Text(strings.cancel) } },
    )
  }
  managingStepsActivity?.let { act ->
    StepsManagerDialog(
      activity = act,
      steps = state.allSteps.filter { it.activityId == act.id },
      language = state.prefs.language,
      viewModel = viewModel,
      onDismiss = { managingStepsActivity = null },
    )
  }

  // Reward Modals
  if (creatingReward || editingReward != null) {
    RewardEditDialog(
      initial = editingReward,
      nextSortOrder = (state.allRewards.maxOfOrNull { it.sortOrder } ?: 0) + 1,
      viewModel = viewModel,
      onDismiss = {
        creatingReward = false
        editingReward = null
      },
      onSave = { rew ->
        viewModel.upsertReward(rew)
        creatingReward = false
        editingReward = null
      },
    )
  }
  deleteRewardConfirm?.let { rew ->
    AlertDialog(
      onDismissRequest = { deleteRewardConfirm = null },
      title = { Text(strings.deleteLabel) },
      text = { Text("Delete '${rew.titleEn}'?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteReward(rew.id)
            deleteRewardConfirm = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
        ) { Text(strings.deleteLabel) }
      },
      dismissButton = { TextButton(onClick = { deleteRewardConfirm = null }) { Text(strings.cancel) } },
    )
  }
}

@Composable
private fun ChallengeConfigEditor(
  config: WeeklyChallengeConfigEntity,
  onSave: (WeeklyChallengeConfigEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  var titleEn by remember(config) { mutableStateOf(config.titleEn) }
  var titleBn by remember(config) { mutableStateOf(config.titleBn) }
  var ruleEn by remember(config) { mutableStateOf(config.ruleDescriptionEn) }
  var ruleBn by remember(config) { mutableStateOf(config.ruleDescriptionBn) }
  var dailyCoins by remember(config) { mutableStateOf(config.dailyCoinReward.toString()) }
  var grandCoins by remember(config) { mutableStateOf(config.day7GrandCoins.toString()) }
  var grandTitleEn by remember(config) { mutableStateOf(config.day7GrandTitleEn) }
  var grandTitleBn by remember(config) { mutableStateOf(config.day7GrandTitleBn) }
  var resetOnMissed by remember(config) { mutableStateOf(config.resetOnMissedDay) }
  var savedToast by remember { mutableStateOf(false) }

  ModernSectionCard {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Title (EN)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("Title (BN)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = ruleEn, onValueChange = { ruleEn = it }, label = { Text("Rules (EN)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = ruleBn, onValueChange = { ruleBn = it }, label = { Text("Rules (BN)") }, modifier = Modifier.fillMaxWidth())
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = dailyCoins,
          onValueChange = { dailyCoins = it.filter(Char::isDigit) },
          label = { Text("Daily Coins") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = grandCoins,
          onValueChange = { grandCoins = it.filter(Char::isDigit) },
          label = { Text("Day 7 Grand Coins") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
      }
      OutlinedTextField(value = grandTitleEn, onValueChange = { grandTitleEn = it }, label = { Text("Grand Gift Title (EN)") }, modifier = Modifier.fillMaxWidth())
      OutlinedTextField(value = grandTitleBn, onValueChange = { grandTitleBn = it }, label = { Text("Grand Gift Title (BN)") }, modifier = Modifier.fillMaxWidth())
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text("Reset cycle on missed day", modifier = Modifier.weight(1f))
        Switch(checked = resetOnMissed, onCheckedChange = { resetOnMissed = it })
      }
      if (savedToast) {
        Text("Saved!", color = LocalDayoraColors.current.successMintDeep, style = MaterialTheme.typography.labelLarge)
      }
      Button(
        onClick = {
          onSave(
            config.copy(
              titleEn = titleEn.trim(),
              titleBn = titleBn.trim(),
              ruleDescriptionEn = ruleEn.trim(),
              ruleDescriptionBn = ruleBn.trim(),
              dailyCoinReward = (dailyCoins.toIntOrNull() ?: 20).coerceAtLeast(1),
              day7GrandCoins = (grandCoins.toIntOrNull() ?: 150).coerceAtLeast(1),
              day7GrandTitleEn = grandTitleEn.trim(),
              day7GrandTitleBn = grandTitleBn.trim(),
              resetOnMissedDay = resetOnMissed,
            )
          )
          savedToast = true
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
      ) {
        Text(strings.save)
      }
    }
  }
}
