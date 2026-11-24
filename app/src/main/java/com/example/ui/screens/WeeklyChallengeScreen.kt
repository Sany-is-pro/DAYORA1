package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.ClaimGrandRewardResult
import com.example.data.local.LocalDayClock
import com.example.ui.components.CandyJellyButton
import com.example.ui.components.CoinPill
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.ModernSectionCard
import com.example.ui.components.ShinyCoinIcon
import com.example.ui.components.StarCoinCelebrationBurst
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.RewardGold
import com.example.ui.viewmodel.DayoraAppUiState
import com.example.ui.viewmodel.DayoraViewModel

@Composable
fun WeeklyChallengeScreen(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI
  val config = state.challengeConfig

  val completedDays = state.challengeDays.count { it.completedEpochDay != null }
  val day7 = state.challengeDays.firstOrNull { it.dayNumber == 7 }
  val isDay7ReadyToClaim = day7?.completedEpochDay != null && day7.isGrandRewardClaimed == false
  val todayCompleted = state.challengeDays.any { it.completedEpochDay == state.todayEpochDay }

  var celebrationResult by remember { mutableStateOf<ClaimGrandRewardResult.Success?>(null) }

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
            .testTag("weekly_challenge_back"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = colors.mainText,
          )
        }
        Text(
          text = config?.let { if (isBn) it.titleBn else it.titleEn } ?: strings.weeklyChallengeTitle,
          style = MaterialTheme.typography.titleLarge,
          color = colors.mainText,
        )
      }
      CoinPill(coins = state.coinBalance)
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Hero Treasure Chest Banner
      item {
        Surface(
          shape = RoundedCornerShape(28.dp),
          color = colors.popupSurface,
          border = BorderStroke(2.dp, RewardGold.copy(alpha = 0.7f)),
          shadowElevation = 6.dp,
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.verticalGradient(
                  listOf(colors.lavenderTint.copy(alpha = 0.8f), colors.softPink.copy(alpha = 0.6f))
                )
              )
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            DayoraIllustration(
              imageKey = "img_reward_chest",
              contentDescription = strings.weeklyChallengeTitle,
              modifier = Modifier
                .size(136.dp)
                .clip(RoundedCornerShape(24.dp)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (isDay7ReadyToClaim) strings.sevenDaysCompletedTitle else "$completedDays ${strings.dayOf7}",
              style = MaterialTheme.typography.headlineSmall,
              color = colors.mainText,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (isDay7ReadyToClaim) {
                strings.sevenDaysCompletedSubtitle
              } else if (todayCompleted) {
                strings.todayDoneHint
              } else {
                strings.completeTodayHint
              },
              style = MaterialTheme.typography.bodyMedium,
              color = colors.secondaryText,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
              progress = { (completedDays / 7f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50)),
              color = RewardGold,
              trackColor = colors.cardSurface,
            )
            if (isDay7ReadyToClaim) {
              Spacer(modifier = Modifier.height(16.dp))
              CandyJellyButton(
                text = strings.claimGrandReward,
                onClick = {
                  viewModel.claimDay7GrandReward { res ->
                    if (res is ClaimGrandRewardResult.Success) {
                      celebrationResult = res
                    }
                  }
                },
                reducedMotion = state.prefs.reducedMotion,
                topColor = Color(0xFFF5C75E),
                bottomColor = Color(0xFFE59B22),
                lipColor = Color(0xFFB3700E),
                leadingIcon = Icons.Rounded.EmojiEvents,
                modifier = Modifier.fillMaxWidth(),
                testTag = "claim_day7_grand_button",
              )
            }
          }
        }
      }

      // 7-Day Jelly Cards Sequence
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          for (dayNum in 1..7) {
            val dayRecord = state.challengeDays.firstOrNull { it.dayNumber == dayNum }
            val isDone = dayRecord?.completedEpochDay != null
            val isGrand = dayNum == 7
            val dailyCoins = config?.dailyCoinReward ?: 20
            val grandCoins = config?.day7GrandCoins ?: 150

            Surface(
              shape = RoundedCornerShape(22.dp),
              color = when {
                isDone -> colors.successMint.copy(alpha = if (colors.isDark) 0.2f else 0.25f)
                isGrand -> colors.softPink
                else -> colors.cardSurface
              },
              border = BorderStroke(
                width = if (isGrand) 1.5.dp else 1.dp,
                color = if (isDone) colors.successMintDeep else if (isGrand) RewardGold else colors.subtleBorder,
              ),
              shadowElevation = if (isGrand) 4.dp else 1.dp,
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                      when {
                        isDone -> colors.successMintDeep
                        isGrand -> RewardGold
                        else -> colors.lavenderTint
                      }
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = when {
                      isDone -> Icons.Rounded.CheckCircle
                      isGrand -> Icons.Rounded.EmojiEvents
                      else -> Icons.Rounded.Star
                    },
                    contentDescription = null,
                    tint = if (isDone || isGrand) Color.White else colors.primaryAccent,
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (isGrand) {
                      "${strings.dayLabel} 7 • ${if (isBn) config?.day7GrandTitleBn ?: "" else config?.day7GrandTitleEn ?: ""}"
                    } else {
                      "${strings.dayLabel} $dayNum"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.mainText,
                  )
                  val dateText = dayRecord?.claimedAtMillis?.let {
                    LocalDayClock.formatShortDate(it, state.prefs.language.code)
                  }
                  if (dateText != null) {
                    Text(
                      text = "${strings.completedBadge} • $dateText",
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.successMintDeep,
                    )
                  }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  ShinyCoinIcon(size = 20.dp)
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = if (isGrand) "+$dailyCoins (+$grandCoins)" else "+$dailyCoins",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.mainText,
                  )
                }
              }
            }
          }
        }
      }

      // Rules Card
      if (config != null) {
        item {
          ModernSectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Rounded.AutoAwesome,
                  contentDescription = null,
                  tint = colors.primaryAccent,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = strings.challengeRulesTitle,
                  style = MaterialTheme.typography.titleMedium,
                  color = colors.mainText,
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (isBn) config.ruleDescriptionBn else config.ruleDescriptionEn,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
              )
            }
          }
        }
      }
    }
  }

  celebrationResult?.let { res ->
    AlertDialog(
      onDismissRequest = { celebrationResult = null },
      title = {
        Text(
          text = strings.sevenDaysCompletedTitle,
          style = MaterialTheme.typography.headlineSmall,
        )
      },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
            StarCoinCelebrationBurst(
              reducedMotion = state.prefs.reducedMotion,
              modifier = Modifier.fillMaxSize(),
            )
            DayoraIllustration(
              imageKey = "img_reward_chest",
              contentDescription = null,
              modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(18.dp)),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "+${res.grandCoins} ${strings.coinsLabel}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.rewardGoldDeep,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = strings.grandRewardClaimedMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
          )
        }
      },
      confirmButton = {
        Button(onClick = { celebrationResult = null }) {
          Text(strings.continueLabel)
        }
      },
    )
  }
}
