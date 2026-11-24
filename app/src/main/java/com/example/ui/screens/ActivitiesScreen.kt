package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ActivityEntity
import com.example.ui.components.CoinPill
import com.example.ui.components.CoinStatChip
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.EmptyStateCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.viewmodel.DayoraAppUiState

@Composable
fun ActivitiesBrowseScreen(
  state: DayoraAppUiState,
  initialCategoryId: String? = null,
  onBack: (() -> Unit)? = null,
  onOpenActivity: (String) -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI

  var selectedCategoryId by remember(initialCategoryId) { mutableStateOf(initialCategoryId) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredActivities = remember(
    state.enabledActivities,
    selectedCategoryId,
    searchQuery,
    isBn,
  ) {
    val q = searchQuery.trim().lowercase()
    state.enabledActivities.filter { act ->
      val catMatch = selectedCategoryId == null || act.categoryId == selectedCategoryId
      val textMatch = q.isEmpty() ||
        act.titleEn.lowercase().contains(q) ||
        act.titleBn.lowercase().contains(q) ||
        act.descriptionEn.lowercase().contains(q) ||
        act.descriptionBn.lowercase().contains(q)
      catMatch && textMatch
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = if (onBack != null) 8.dp else 18.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        if (onBack != null) {
          IconButton(
            onClick = onBack,
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("activities_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = "Back",
              tint = colors.mainText,
            )
          }
        }
        val activeCategory = state.enabledCategories.firstOrNull { it.id == selectedCategoryId }
        Text(
          text = activeCategory?.let { if (isBn) it.nameBn else it.nameEn } ?: strings.navActivities,
          style = MaterialTheme.typography.headlineSmall,
          color = colors.mainText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      CoinPill(coins = state.coinBalance)
    }

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text(strings.searchActivitiesHint) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = null,
          tint = colors.primaryAccent,
        )
      },
      singleLine = true,
      shape = RoundedCornerShape(18.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryAccent,
        unfocusedBorderColor = colors.subtleBorder,
        focusedContainerColor = colors.cardSurface,
        unfocusedContainerColor = colors.cardSurface,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 6.dp)
        .testTag("activities_search_input"),
    )

    // Category Chips
    LazyRow(
      contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        FilterChip(
          selected = selectedCategoryId == null,
          onClick = { selectedCategoryId = null },
          label = { Text(strings.allCategories) },
        )
      }
      items(state.enabledCategories, key = { it.id }) { cat ->
        FilterChip(
          selected = selectedCategoryId == cat.id,
          onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
          label = { Text(if (isBn) cat.nameBn else cat.nameEn) },
        )
      }
    }

    // Activities List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (filteredActivities.isEmpty()) {
        item {
          EmptyStateCard(title = strings.noActivitiesFound)
        }
      } else {
        items(filteredActivities, key = { it.id }) { activity ->
          val stepCount = state.allSteps.count { it.activityId == activity.id }
          val progress = state.allProgress.firstOrNull { it.activityId == activity.id }
          val doneToday = state.completions.any {
            it.activityId == activity.id && it.completedEpochDay == state.todayEpochDay
          }
          ActivityBrowseRowCard(
            activity = activity,
            stepCount = stepCount,
            inProgress = progress?.isStarted == true,
            doneToday = doneToday,
            isBn = isBn,
            onClick = { onOpenActivity(activity.id) },
          )
        }
      }
    }
  }
}

@Composable
private fun ActivityBrowseRowCard(
  activity: ActivityEntity,
  stepCount: Int,
  inProgress: Boolean,
  doneToday: Boolean,
  isBn: Boolean,
  onClick: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val title = if (isBn) activity.titleBn else activity.titleEn
  val desc = if (isBn) activity.descriptionBn else activity.descriptionEn

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("activity_row_${activity.id}"),
    shape = RoundedCornerShape(22.dp),
    color = colors.cardSurface,
    border = BorderStroke(1.dp, colors.subtleBorder),
    shadowElevation = 2.dp,
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      DayoraIllustration(
        imageKey = activity.coverImageKey,
        contentDescription = title,
        modifier = Modifier
          .size(86.dp)
          .clip(RoundedCornerShape(16.dp)),
      )
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.mainText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(6.dp))
          CoinStatChip(amount = activity.coinReward)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = desc,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.Schedule,
              contentDescription = null,
              tint = colors.primaryAccent,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${activity.durationMinutes} ${strings.minShort}",
              style = MaterialTheme.typography.labelSmall,
              color = colors.secondaryText,
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.Layers,
              contentDescription = null,
              tint = colors.pinkAccent,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "$stepCount ${strings.stepsShort}",
              style = MaterialTheme.typography.labelSmall,
              color = colors.secondaryText,
            )
          }
          if (doneToday) {
            Surface(
              shape = RoundedCornerShape(50),
              color = colors.successMint.copy(alpha = 0.25f),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.CheckCircle,
                  contentDescription = null,
                  tint = colors.successMintDeep,
                  modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = strings.completedBadge,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.successMintDeep,
                )
              }
            }
          } else if (inProgress) {
            Surface(
              shape = RoundedCornerShape(50),
              color = colors.lavenderTint,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.PlayArrow,
                  contentDescription = null,
                  tint = colors.primaryAccent,
                  modifier = Modifier.size(12.dp),
                )
                Text(
                  text = strings.inProgressBadge,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.primaryAccent,
                )
              }
            }
          }
        }
      }
    }
  }
}
