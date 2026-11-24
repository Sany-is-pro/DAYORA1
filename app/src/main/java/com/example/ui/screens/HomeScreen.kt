package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ActivityEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.LocalDayClock
import com.example.ui.components.CoinPill
import com.example.ui.components.CoinStatChip
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.ModernSectionCard
import com.example.ui.components.ProfileAvatarImage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.RewardGold
import com.example.ui.viewmodel.DayoraAppUiState

@Composable
fun HomeScreen(
  state: DayoraAppUiState,
  onOpenWeeklyChallenge: () -> Unit,
  onOpenCategory: (String) -> Unit,
  onOpenActivity: (String) -> Unit,
  onNavigateTabRewards: () -> Unit,
  onNavigateTabProfile: () -> Unit,
  onNavigateTabActivities: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI

  val firstName = remember(state.prefs.fullName) {
    state.prefs.fullName.trim().substringBefore(" ").ifEmpty { "Friend" }
  }
  val greetingPrefix = remember(strings) {
    val hour = LocalDayClock.currentHourOfDay()
    when {
      hour < 12 -> strings.greetingMorning
      hour < 17 -> strings.greetingAfternoon
      else -> strings.greetingEvening
    }
  }

  val featured = remember(state.enabledActivities) {
    state.enabledActivities.filter { it.isFeatured }.ifEmpty {
      state.enabledActivities.take(4)
    }
  }
  val completedDaysCount = remember(state.challengeDays) {
    state.challengeDays.count { it.completedEpochDay != null }
  }
  val dailyTarget = 3
  val dailyRatio = (state.todayCompletedCount.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    // Top Brand & Coin Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Image(
            painter = painterResource(id = R.drawable.dayora_logo),
            contentDescription = "DAYORA",
            modifier = Modifier.size(38.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = strings.appName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.mainText,
          )
        }
        CoinPill(
          coins = state.coinBalance,
          onClick = onNavigateTabRewards,
          modifier = Modifier.testTag("home_coin_pill"),
        )
      }
    }

    // Personalized Greeting & Hero Banner
    item {
      Surface(
        shape = RoundedCornerShape(26.dp),
        color = colors.cardSurface,
        border = BorderStroke(1.dp, colors.subtleBorder),
        shadowElevation = 4.dp,
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_dreamy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = if (colors.isDark) 0.25f else 0.42f,
            modifier = Modifier.matchParentSize(),
          )
          Column(modifier = Modifier.padding(20.dp)) {
            Text(
              text = "$greetingPrefix, $firstName!",
              style = MaterialTheme.typography.headlineMedium,
              color = colors.mainText,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = strings.homeSubtitle,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.mainText.copy(alpha = 0.82f),
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Daily Progress Summary
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = colors.cardSurface.copy(alpha = 0.9f),
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = strings.dailyProgress,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.mainText,
                  )
                  Text(
                    text = "${state.todayCompletedCount} ${strings.completedTodaySuffix}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                  progress = { dailyRatio },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50)),
                  color = colors.successMintDeep,
                  trackColor = colors.lavenderTint,
                )
              }
            }
          }
        }
      }
    }

    // Weekly Challenge Card
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(24.dp))
          .clickable(onClick = onOpenWeeklyChallenge)
          .testTag("home_weekly_challenge_card"),
        shape = RoundedCornerShape(24.dp),
        color = colors.cardSurface,
        border = BorderStroke(1.5.dp, RewardGold.copy(alpha = 0.55f)),
        shadowElevation = 3.dp,
      ) {
        Row(
          modifier = Modifier
            .background(
              Brush.horizontalGradient(
                listOf(colors.lavenderTint.copy(alpha = 0.65f), colors.softPink.copy(alpha = 0.65f))
              )
            )
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(RewardGold.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.EmojiEvents,
              contentDescription = strings.weeklyChallenge,
              tint = colors.rewardGoldDeep,
              modifier = Modifier.size(30.dp),
            )
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.weeklyChallenge,
              style = MaterialTheme.typography.titleMedium,
              color = colors.mainText,
            )
            Text(
              text = "$completedDaysCount ${strings.dayOf7} • ${strings.tapToOpenChallenge}",
              style = MaterialTheme.typography.bodySmall,
              color = colors.secondaryText,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
              for (d in 1..7) {
                val done = state.challengeDays.any { it.dayNumber == d && it.completedEpochDay != null }
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (done) colors.primaryAccent else colors.cardSurface.copy(alpha = 0.7f))
                )
              }
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = colors.primaryAccent,
          )
        }
      }
    }

    // Quick Access Pills (Rewards & Profile)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        QuickAccessTile(
          title = strings.quickRewards,
          subtitle = "${state.enabledRewards.size} ${strings.availableRewards}",
          icon = Icons.Rounded.CardGiftcard,
          tint = colors.pinkAccent,
          bgColor = colors.softPink,
          onClick = onNavigateTabRewards,
          modifier = Modifier
            .weight(1f)
            .testTag("home_quick_rewards"),
        )
        QuickAccessTile(
          title = strings.quickProfile,
          subtitle = state.prefs.fullName,
          icon = Icons.Rounded.Person,
          tint = colors.primaryAccent,
          bgColor = colors.lavenderTint,
          avatarUri = state.prefs.profileImageUri,
          onClick = onNavigateTabProfile,
          modifier = Modifier
            .weight(1f)
            .testTag("home_quick_profile"),
        )
      }
    }

    // Daily Motivation Card
    item {
      ModernSectionCard(containerColor = colors.cardSurface) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(colors.primaryAccent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.AutoAwesome,
              contentDescription = null,
              tint = colors.primaryAccent,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = strings.dailyMotivationTitle,
              style = MaterialTheme.typography.labelLarge,
              color = colors.primaryAccent,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = strings.dailyMotivationQuote,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.mainText,
            )
          }
        }
      }
    }

    // Featured Activities Carousel
    if (featured.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = strings.featuredActivities,
            style = MaterialTheme.typography.titleLarge,
            color = colors.mainText,
          )
          TextButton(onClick = onNavigateTabActivities) {
            Text(strings.viewAll)
          }
        }
      }

      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(end = 4.dp),
        ) {
          items(featured, key = { it.id }) { activity ->
            val doneToday = state.completions.any {
              it.activityId == activity.id && it.completedEpochDay == state.todayEpochDay
            }
            FeaturedActivityCard(
              activity = activity,
              isBn = isBn,
              doneToday = doneToday,
              onClick = { onOpenActivity(activity.id) },
            )
          }
        }
      }
    }

    // Lifestyle Categories Grid (2-column pairs inside LazyColumn)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = strings.lifestyleCategories,
          style = MaterialTheme.typography.titleLarge,
          color = colors.mainText,
        )
        TextButton(onClick = onNavigateTabActivities) {
          Text(strings.viewAll)
        }
      }
    }

    val pairs = state.enabledCategories.chunked(2)
    items(pairs) { rowCats ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        rowCats.forEach { cat ->
          val actCount = state.enabledActivities.count { it.categoryId == cat.id }
          CategoryGridCard(
            category = cat,
            activityCount = actCount,
            isBn = isBn,
            onClick = { onOpenCategory(cat.id) },
            modifier = Modifier.weight(1f),
          )
        }
        if (rowCats.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun QuickAccessTile(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  bgColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  avatarUri: String? = null,
) {
  val colors = LocalDayoraColors.current
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(20.dp),
    color = bgColor.copy(alpha = if (colors.isDark) 0.7f else 0.85f),
    border = BorderStroke(1.dp, colors.subtleBorder),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(colors.cardSurface),
        contentAlignment = Alignment.Center,
      ) {
        if (avatarUri != null) {
          ProfileAvatarImage(
            imageUri = avatarUri,
            contentDescription = title,
            modifier = Modifier
              .fillMaxSize()
              .clip(CircleShape),
          )
        } else {
          Icon(imageVector = icon, contentDescription = title, tint = tint)
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = colors.mainText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun FeaturedActivityCard(
  activity: ActivityEntity,
  isBn: Boolean,
  doneToday: Boolean,
  onClick: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val title = if (isBn) activity.titleBn else activity.titleEn
  val desc = if (isBn) activity.descriptionBn else activity.descriptionEn

  Surface(
    modifier = Modifier
      .width(232.dp)
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("featured_activity_${activity.id}"),
    shape = RoundedCornerShape(22.dp),
    color = colors.cardSurface,
    border = BorderStroke(1.dp, colors.subtleBorder),
    shadowElevation = 3.dp,
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(118.dp),
      ) {
        DayoraIllustration(
          imageKey = activity.coverImageKey,
          contentDescription = title,
          modifier = Modifier.fillMaxSize(),
        )
        CoinStatChip(
          amount = activity.coinReward,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp),
        )
        if (doneToday) {
          Surface(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(10.dp),
            shape = RoundedCornerShape(50),
            color = colors.successMint,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF0E4A36),
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = strings.completedBadge,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF0E4A36),
              )
            }
          }
        }
      }

      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = colors.mainText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = desc,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${activity.durationMinutes} ${strings.minShort}",
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryGridCard(
  category: CategoryEntity,
  activityCount: Int,
  isBn: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = LocalDayoraColors.current
  val name = if (isBn) category.nameBn else category.nameEn
  val desc = if (isBn) category.descriptionBn else category.descriptionEn

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick)
      .testTag("category_card_${category.id}"),
    shape = RoundedCornerShape(22.dp),
    color = colors.cardSurface,
    border = BorderStroke(1.dp, colors.subtleBorder),
    shadowElevation = 2.dp,
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(92.dp),
      ) {
        DayoraIllustration(
          imageKey = category.imageKey,
          contentDescription = name,
          modifier = Modifier.fillMaxSize(),
        )
        Surface(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp),
          shape = RoundedCornerShape(50),
          color = colors.cardSurface.copy(alpha = 0.92f),
        ) {
          Text(
            text = "$activityCount",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryAccent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }
      Column(modifier = Modifier.padding(12.dp)) {
        Text(
          text = name,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = colors.mainText,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = desc,
          style = MaterialTheme.typography.bodySmall,
          color = colors.secondaryText,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
