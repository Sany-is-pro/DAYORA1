package com.example.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ActivityEntity
import com.example.data.local.ActivityStepEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.RewardItemEntity
import com.example.ui.components.DayoraIllustration
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.viewmodel.DayoraViewModel
import java.util.UUID

val BuiltInIllustrationKeys = listOf(
  "img_selfcare_glow",
  "img_wellness_active",
  "img_evening_calm",
  "img_hero_dreamy",
  "img_reward_chest",
)

@Composable
fun ImageKeySelector(
  selectedKey: String,
  viewModel: DayoraViewModel,
  onSelected: (String) -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = { uri ->
      if (uri != null) {
        viewModel.importGalleryImage(uri) { savedPath ->
          if (savedPath != null) onSelected(savedPath)
        }
      }
    },
  )

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(strings.useBuiltInArt, style = MaterialTheme.typography.labelLarge)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      items(BuiltInIllustrationKeys) { key ->
        val isSel = selectedKey == key
        Surface(
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(
            width = if (isSel) 2.5.dp else 1.dp,
            color = if (isSel) colors.primaryAccent else colors.subtleBorder,
          ),
          modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelected(key) },
        ) {
          DayoraIllustration(imageKey = key, contentDescription = key)
        }
      }
    }
    OutlinedButton(
      onClick = {
        photoPickerLauncher.launch(
          PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
      },
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(strings.pickFromGallery)
    }
    if (selectedKey.startsWith("file://")) {
      DayoraIllustration(
        imageKey = selectedKey,
        contentDescription = "Selected image",
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .clip(RoundedCornerShape(12.dp)),
      )
    }
  }
}

@Composable
fun CategoryEditDialog(
  initial: CategoryEntity?,
  nextSortOrder: Int,
  viewModel: DayoraViewModel,
  onDismiss: () -> Unit,
  onSave: (CategoryEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  var nameEn by remember { mutableStateOf(initial?.nameEn ?: "") }
  var nameBn by remember { mutableStateOf(initial?.nameBn ?: "") }
  var descEn by remember { mutableStateOf(initial?.descriptionEn ?: "") }
  var descBn by remember { mutableStateOf(initial?.descriptionBn ?: "") }
  var sortOrder by remember { mutableStateOf((initial?.sortOrder ?: nextSortOrder).toString()) }
  var imageKey by remember { mutableStateOf(initial?.imageKey ?: "img_selfcare_glow") }
  var enabled by remember { mutableStateOf(initial?.isEnabled ?: true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) strings.addNew else strings.editLabel) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Name (EN)") }, singleLine = true)
        OutlinedTextField(value = nameBn, onValueChange = { nameBn = it }, label = { Text("Name (BN)") }, singleLine = true)
        OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") })
        OutlinedTextField(value = descBn, onValueChange = { descBn = it }, label = { Text("Description (BN)") })
        OutlinedTextField(
          value = sortOrder,
          onValueChange = { sortOrder = it.filter(Char::isDigit) },
          label = { Text("Sort Order") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )
        ImageKeySelector(selectedKey = imageKey, viewModel = viewModel, onSelected = { imageKey = it })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(strings.enabledLabel)
          Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
      }
    },
    confirmButton = {
      Button(
        enabled = nameEn.trim().isNotEmpty() || nameBn.trim().isNotEmpty(),
        onClick = {
          val resolvedEn = nameEn.trim().ifEmpty { nameBn.trim() }
          val resolvedBn = nameBn.trim().ifEmpty { nameEn.trim() }
          val resolvedDescEn = descEn.trim().ifEmpty { descBn.trim() }
          val resolvedDescBn = descBn.trim().ifEmpty { descEn.trim() }
          onSave(
            CategoryEntity(
              id = initial?.id ?: "cat_${UUID.randomUUID()}",
              nameEn = resolvedEn,
              nameBn = resolvedBn,
              descriptionEn = resolvedDescEn,
              descriptionBn = resolvedDescBn,
              imageKey = imageKey,
              accentColorHex = initial?.accentColorHex ?: 0xFFA77BEA,
              sortOrder = sortOrder.toIntOrNull() ?: nextSortOrder,
              isEnabled = enabled,
            )
          )
        },
      ) { Text(strings.save) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}

@Composable
fun ActivityEditDialog(
  initial: ActivityEntity?,
  categories: List<CategoryEntity>,
  defaultCategoryId: String,
  viewModel: DayoraViewModel,
  onDismiss: () -> Unit,
  onSave: (ActivityEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  var categoryId by remember { mutableStateOf(initial?.categoryId ?: defaultCategoryId) }
  var titleEn by remember { mutableStateOf(initial?.titleEn ?: "") }
  var titleBn by remember { mutableStateOf(initial?.titleBn ?: "") }
  var descEn by remember { mutableStateOf(initial?.descriptionEn ?: "") }
  var descBn by remember { mutableStateOf(initial?.descriptionBn ?: "") }
  var duration by remember { mutableStateOf((initial?.durationMinutes ?: 5).toString()) }
  var coins by remember { mutableStateOf((initial?.coinReward ?: 20).toString()) }
  var imageKey by remember { mutableStateOf(initial?.coverImageKey ?: "img_wellness_active") }
  var featured by remember { mutableStateOf(initial?.isFeatured ?: false) }
  var enabled by remember { mutableStateOf(initial?.isEnabled ?: true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) strings.addNew else strings.editLabel) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (categories.isNotEmpty()) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories, key = { it.id }) { cat ->
              val sel = cat.id == categoryId
              OutlinedButton(
                onClick = { categoryId = cat.id },
                border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
              ) { Text(cat.nameEn) }
            }
          }
        }
        OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Title (EN)") }, singleLine = true)
        OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("Title (BN)") }, singleLine = true)
        OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") })
        OutlinedTextField(value = descBn, onValueChange = { descBn = it }, label = { Text("Description (BN)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = duration,
            onValueChange = { duration = it.filter(Char::isDigit) },
            label = { Text("Minutes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
          )
          OutlinedTextField(
            value = coins,
            onValueChange = { coins = it.filter(Char::isDigit) },
            label = { Text("Coins") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
          )
        }
        ImageKeySelector(selectedKey = imageKey, viewModel = viewModel, onSelected = { imageKey = it })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(strings.featuredLabel)
          Switch(checked = featured, onCheckedChange = { featured = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(strings.enabledLabel)
          Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
      }
    },
    confirmButton = {
      Button(
        enabled = (titleEn.trim().isNotEmpty() || titleBn.trim().isNotEmpty()) && categoryId.isNotEmpty(),
        onClick = {
          val resolvedEn = titleEn.trim().ifEmpty { titleBn.trim() }
          val resolvedBn = titleBn.trim().ifEmpty { titleEn.trim() }
          val resolvedDescEn = descEn.trim().ifEmpty { descBn.trim() }
          val resolvedDescBn = descBn.trim().ifEmpty { descEn.trim() }
          onSave(
            ActivityEntity(
              id = initial?.id ?: "act_${UUID.randomUUID()}",
              categoryId = categoryId,
              titleEn = resolvedEn,
              titleBn = resolvedBn,
              descriptionEn = resolvedDescEn,
              descriptionBn = resolvedDescBn,
              coverImageKey = imageKey,
              durationMinutes = (duration.toIntOrNull() ?: 5).coerceAtLeast(1),
              coinReward = (coins.toIntOrNull() ?: 20).coerceAtLeast(1),
              isFeatured = featured,
              isEnabled = enabled,
              sortOrder = initial?.sortOrder ?: 99,
            )
          )
        },
      ) { Text(strings.save) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}

/**
 * Interactive Step-by-Step Flow Editor for any activity in any category.
 * Lets the admin directly configure Step 1 (text + image), then tap Next to configure Step 2, Step 3, etc.,
 * or tap "+ Add Next Step" to append new steps in sequence.
 */
@Composable
fun StepsManagerDialog(
  activity: ActivityEntity,
  steps: List<ActivityStepEntity>,
  language: AppLanguage,
  viewModel: DayoraViewModel,
  onDismiss: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = language == AppLanguage.BENGALI
  val sortedSteps = remember(steps) { steps.sortedBy { it.stepNumber } }

  var selectedIndex by remember(activity.id) { mutableIntStateOf(0) }
  var pendingJumpToNew by remember(activity.id) { mutableStateOf(false) }

  LaunchedEffect(sortedSteps.size) {
    if (sortedSteps.isEmpty()) {
      // Auto-create Step 1 if an activity currently has zero steps
      viewModel.upsertStep(
        ActivityStepEntity(
          id = "step_${UUID.randomUUID()}",
          activityId = activity.id,
          stepNumber = 1,
          titleEn = "Step 1",
          titleBn = "ধাপ ১",
          instructionEn = "Write Step 1 instructions here.",
          instructionBn = "১ম ধাপের নির্দেশনা এখানে লিখুন।",
          imageKey = activity.coverImageKey,
        )
      )
    } else if (pendingJumpToNew) {
      selectedIndex = sortedSteps.lastIndex
      pendingJumpToNew = false
    } else if (selectedIndex >= sortedSteps.size) {
      selectedIndex = (sortedSteps.size - 1).coerceAtLeast(0)
    }
  }

  val safeIndex = selectedIndex.coerceIn(0, (sortedSteps.size - 1).coerceAtLeast(0))
  val currentStep = sortedSteps.getOrNull(safeIndex)

  var titleEn by remember(currentStep?.id) { mutableStateOf(currentStep?.titleEn ?: "") }
  var titleBn by remember(currentStep?.id) { mutableStateOf(currentStep?.titleBn ?: "") }
  var instEn by remember(currentStep?.id) { mutableStateOf(currentStep?.instructionEn ?: "") }
  var instBn by remember(currentStep?.id) { mutableStateOf(currentStep?.instructionBn ?: "") }
  var imageKey by remember(currentStep?.id) { mutableStateOf(currentStep?.imageKey ?: activity.coverImageKey) }
  var showSavedBanner by remember(currentStep?.id) { mutableStateOf(false) }

  fun saveCurrentStepIfValid(): Boolean {
    val step = currentStep ?: return false
    val hasTitle = titleEn.trim().isNotEmpty() || titleBn.trim().isNotEmpty()
    val hasInst = instEn.trim().isNotEmpty() || instBn.trim().isNotEmpty()
    if (!hasTitle && !hasInst) return false

    val resolvedTitleEn = titleEn.trim().ifEmpty { titleBn.trim() }.ifEmpty { "Step ${safeIndex + 1}" }
    val resolvedTitleBn = titleBn.trim().ifEmpty { titleEn.trim() }.ifEmpty { "ধাপ ${safeIndex + 1}" }
    val resolvedInstEn = instEn.trim().ifEmpty { instBn.trim() }
    val resolvedInstBn = instBn.trim().ifEmpty { instEn.trim() }

    viewModel.upsertStep(
      step.copy(
        titleEn = resolvedTitleEn,
        titleBn = resolvedTitleBn,
        instructionEn = resolvedInstEn,
        instructionBn = resolvedInstBn,
        imageKey = imageKey,
      )
    )
    showSavedBanner = true
    return true
  }

  fun appendNextStep() {
    saveCurrentStepIfValid()
    val nextNum = (sortedSteps.maxOfOrNull { it.stepNumber } ?: 0) + 1
    val displayNum = sortedSteps.size + 1
    pendingJumpToNew = true
    viewModel.upsertStep(
      ActivityStepEntity(
        id = "step_${UUID.randomUUID()}",
        activityId = activity.id,
        stepNumber = nextNum,
        titleEn = "Step $displayNum",
        titleBn = "ধাপ $displayNum",
        instructionEn = "",
        instructionBn = "",
        imageKey = imageKey,
      )
    )
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 20.dp)
        .heightIn(max = 760.dp),
      shape = RoundedCornerShape(28.dp),
      color = colors.popupSurface,
      border = BorderStroke(1.5.dp, colors.primaryAccent.copy(alpha = 0.35f)),
      shadowElevation = 10.dp,
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Header Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(colors.lavenderTint)
            .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isBn) activity.titleBn else activity.titleEn,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = colors.mainText,
            )
            Text(
              text = strings.stepFlowSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = colors.secondaryText,
            )
          }
          IconButton(onClick = {
            saveCurrentStepIfValid()
            onDismiss()
          }) {
            Icon(Icons.Rounded.Close, contentDescription = strings.closeLabel, tint = colors.mainText)
          }
        }

        // Step Chips Strip: Step 1 -> Step 2 -> Step 3 -> + Add Next Step
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          items(sortedSteps.size) { index ->
            FilterChip(
              selected = index == safeIndex,
              onClick = {
                saveCurrentStepIfValid()
                selectedIndex = index
              },
              label = { Text("${strings.stepPrefix} ${index + 1}") },
              modifier = Modifier.testTag("admin_step_chip_$index"),
            )
          }
          item {
            OutlinedButton(
              onClick = { appendNextStep() },
              shape = RoundedCornerShape(50),
              modifier = Modifier.testTag("admin_add_next_step_chip"),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.addNextStepLabel)
            }
          }
        }

        // Editable Current Step Body
        if (currentStep != null) {
          Column(
            modifier = Modifier
              .weight(1f, fill = false)
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Step Header + Order Controls
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Surface(
                shape = RoundedCornerShape(50),
                color = colors.primaryAccent.copy(alpha = 0.16f),
              ) {
                Text(
                  text = "${strings.stepPrefix} ${safeIndex + 1} ${strings.ofPrefix} ${sortedSteps.size}",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = colors.primaryAccent,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                if (safeIndex > 0) {
                  IconButton(
                    onClick = {
                      saveCurrentStepIfValid()
                      viewModel.swapSteps(currentStep, sortedSteps[safeIndex - 1])
                      selectedIndex = safeIndex - 1
                    }
                  ) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Earlier")
                  }
                }
                if (safeIndex < sortedSteps.lastIndex) {
                  IconButton(
                    onClick = {
                      saveCurrentStepIfValid()
                      viewModel.swapSteps(currentStep, sortedSteps[safeIndex + 1])
                      selectedIndex = safeIndex + 1
                    }
                  ) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Later")
                  }
                }
                if (sortedSteps.size > 1) {
                  IconButton(
                    onClick = {
                      viewModel.deleteStep(currentStep.id)
                      selectedIndex = (safeIndex - 1).coerceAtLeast(0)
                    }
                  ) {
                    Icon(Icons.Rounded.Delete, contentDescription = strings.deleteLabel, tint = colors.danger)
                  }
                }
              }
            }

            // Image Preview & Picker
            Text(strings.stepImageLabel, style = MaterialTheme.typography.labelLarge, color = colors.mainText)
            DayoraIllustration(
              imageKey = imageKey,
              contentDescription = titleEn,
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(18.dp)),
            )
            ImageKeySelector(
              selectedKey = imageKey,
              viewModel = viewModel,
              onSelected = {
                imageKey = it
                showSavedBanner = false
              },
            )

            // Text Inputs for Step Title & Instruction (EN & BN)
            OutlinedTextField(
              value = titleEn,
              onValueChange = {
                titleEn = it
                showSavedBanner = false
              },
              label = { Text(strings.stepTitleEnLabel) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_step_title_en"),
            )
            OutlinedTextField(
              value = titleBn,
              onValueChange = {
                titleBn = it
                showSavedBanner = false
              },
              label = { Text(strings.stepTitleBnLabel) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_step_title_bn"),
            )
            OutlinedTextField(
              value = instEn,
              onValueChange = {
                instEn = it
                showSavedBanner = false
              },
              label = { Text(strings.stepTextEnLabel) },
              minLines = 2,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_step_text_en"),
            )
            OutlinedTextField(
              value = instBn,
              onValueChange = {
                instBn = it
                showSavedBanner = false
              },
              label = { Text(strings.stepTextBnLabel) },
              minLines = 2,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_step_text_bn"),
            )

            if (showSavedBanner) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.successMint.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.successMintDeep,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = strings.stepSavedBadge,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.successMintDeep,
                  )
                }
              }
            }

            Button(
              onClick = { saveCurrentStepIfValid() },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_save_step_button"),
              shape = RoundedCornerShape(16.dp),
            ) {
              Text(strings.saveStepLabel)
            }
          }
        }

        // Bottom Previous / Next Step / + Add Next Step Navigation Bar
        Surface(
          color = colors.cardSurface,
          tonalElevation = 4.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (safeIndex > 0) {
              OutlinedButton(
                onClick = {
                  saveCurrentStepIfValid()
                  selectedIndex = safeIndex - 1
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
              ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.previousStep)
              }
            }

            val isLast = safeIndex >= sortedSteps.lastIndex
            if (!isLast) {
              Button(
                onClick = {
                  saveCurrentStepIfValid()
                  selectedIndex = safeIndex + 1
                },
                modifier = Modifier
                  .weight(1.2f)
                  .testTag("admin_step_next_button"),
                shape = RoundedCornerShape(16.dp),
              ) {
                Text("${strings.nextStep} (${strings.stepPrefix} ${safeIndex + 2})")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
              }
            } else {
              Button(
                onClick = { appendNextStep() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.pinkAccent),
                modifier = Modifier
                  .weight(1.2f)
                  .testTag("admin_step_add_next_button"),
                shape = RoundedCornerShape(16.dp),
              ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${strings.nextStep}: ${strings.addNextStepLabel}")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun RewardEditDialog(
  initial: RewardItemEntity?,
  nextSortOrder: Int,
  viewModel: DayoraViewModel,
  onDismiss: () -> Unit,
  onSave: (RewardItemEntity) -> Unit,
) {
  val strings = LocalAppStrings.current
  var titleEn by remember { mutableStateOf(initial?.titleEn ?: "") }
  var titleBn by remember { mutableStateOf(initial?.titleBn ?: "") }
  var descEn by remember { mutableStateOf(initial?.descriptionEn ?: "") }
  var descBn by remember { mutableStateOf(initial?.descriptionBn ?: "") }
  var cost by remember { mutableStateOf((initial?.coinCost ?: 100).toString()) }
  var imageKey by remember { mutableStateOf(initial?.imageKey ?: "img_reward_chest") }
  var isGrand by remember { mutableStateOf(initial?.isDay7GrandReward ?: false) }
  var enabled by remember { mutableStateOf(initial?.isEnabled ?: true) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial == null) strings.addNew else strings.editLabel) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Reward Title (EN)") }, singleLine = true)
        OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("Reward Title (BN)") }, singleLine = true)
        OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") })
        OutlinedTextField(value = descBn, onValueChange = { descBn = it }, label = { Text("Description (BN)") })
        OutlinedTextField(
          value = cost,
          onValueChange = { cost = it.filter(Char::isDigit) },
          label = { Text("Coin Cost") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )
        ImageKeySelector(selectedKey = imageKey, viewModel = viewModel, onSelected = { imageKey = it })
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text("Day 7 Grand Reward")
          Switch(checked = isGrand, onCheckedChange = { isGrand = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
          Text(strings.enabledLabel)
          Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
      }
    },
    confirmButton = {
      Button(
        enabled = titleEn.trim().isNotEmpty() || titleBn.trim().isNotEmpty(),
        onClick = {
          val resolvedEn = titleEn.trim().ifEmpty { titleBn.trim() }
          val resolvedBn = titleBn.trim().ifEmpty { titleEn.trim() }
          val resolvedDescEn = descEn.trim().ifEmpty { descBn.trim() }
          val resolvedDescBn = descBn.trim().ifEmpty { descEn.trim() }
          onSave(
            RewardItemEntity(
              id = initial?.id ?: "rew_${UUID.randomUUID()}",
              titleEn = resolvedEn,
              titleBn = resolvedBn,
              descriptionEn = resolvedDescEn,
              descriptionBn = resolvedDescBn,
              imageKey = imageKey,
              coinCost = (cost.toIntOrNull() ?: 100).coerceAtLeast(1),
              isDay7GrandReward = isGrand,
              isEnabled = enabled,
              sortOrder = initial?.sortOrder ?: nextSortOrder,
            )
          )
        },
      ) { Text(strings.save) }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } },
  )
}
