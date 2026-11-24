package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.LocalDayClock
import com.example.data.local.RedeemRewardResult
import com.example.data.local.RewardItemEntity
import com.example.ui.components.CandyJellyButton
import com.example.ui.components.CoinPill
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.EmptyStateCard
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
fun RewardsScreen(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = state.prefs.language == AppLanguage.BENGALI

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Catalog, 1: Unlocked, 2: Coin Ledger
  var unlockedDialog by remember { mutableStateOf<RewardItemEntity?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    // Header & Balance Banner
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = strings.rewardsCenterTitle,
          style = MaterialTheme.typography.headlineSmall,
          color = colors.mainText,
        )
        CoinPill(coins = state.coinBalance)
      }
      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.cardSurface,
        border = BorderStroke(1.5.dp, RewardGold.copy(alpha = 0.65f)),
        shadowElevation = 4.dp,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.horizontalGradient(
                listOf(colors.lavenderTint.copy(alpha = 0.7f), colors.softPink.copy(alpha = 0.7f))
              )
            )
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          ShinyCoinIcon(size = 46.dp)
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.yourCoinBalance,
              style = MaterialTheme.typography.labelLarge,
              color = colors.secondaryText,
            )
            Text(
              text = "${state.coinBalance} ${strings.coinsLabel}",
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
              color = colors.mainText,
            )
          }
          DayoraIllustration(
            imageKey = "img_reward_chest",
            contentDescription = null,
            modifier = Modifier
              .size(58.dp)
              .clip(RoundedCornerShape(14.dp)),
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          label = { Text(strings.availableRewards) },
        )
        FilterChip(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          label = { Text(strings.unlockedHistory) },
        )
        FilterChip(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          label = { Text(strings.coinHistory) },
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      when (selectedTab) {
        0 -> {
          if (state.enabledRewards.isEmpty()) {
            item { EmptyStateCard(title = strings.noClaimsYet, icon = Icons.Rounded.CardGiftcard) }
          } else {
            items(state.enabledRewards, key = { it.id }) { reward ->
              val canUnlock = state.coinBalance >= reward.coinCost
              val ratio = if (reward.coinCost > 0) {
                (state.coinBalance.toFloat() / reward.coinCost.toFloat()).coerceIn(0f, 1f)
              } else 1f
              val rewardTitle = if (isBn) reward.titleBn else reward.titleEn
              val rewardDesc = if (isBn) reward.descriptionBn else reward.descriptionEn

              Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (canUnlock) colors.popupSurface else colors.cardSurface,
                border = BorderStroke(
                  width = if (canUnlock) 1.5.dp else 1.dp,
                  color = if (canUnlock) colors.pinkAccent else colors.subtleBorder,
                ),
                shadowElevation = if (canUnlock) 5.dp else 2.dp,
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    DayoraIllustration(
                      imageKey = reward.imageKey,
                      contentDescription = rewardTitle,
                      modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text(
                          text = rewardTitle,
                          style = MaterialTheme.typography.titleMedium,
                          color = colors.mainText,
                          modifier = Modifier.weight(1f),
                        )
                        Surface(
                          shape = RoundedCornerShape(50),
                          color = RewardGold.copy(alpha = 0.22f),
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                          ) {
                            ShinyCoinIcon(size = 15.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              text = "${reward.coinCost}",
                              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                              color = colors.mainText,
                            )
                          }
                        }
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = rewardDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))
                  LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(8.dp)
                      .clip(RoundedCornerShape(50)),
                    color = if (canUnlock) colors.successMintDeep else colors.primaryAccent,
                    trackColor = colors.lavenderTint,
                  )
                  Spacer(modifier = Modifier.height(12.dp))

                  if (canUnlock) {
                    CandyJellyButton(
                      text = strings.unlockReward,
                      onClick = {
                        viewModel.redeemReward(reward.id) { result ->
                          when (result) {
                            is RedeemRewardResult.Success -> unlockedDialog = reward
                            is RedeemRewardResult.InsufficientCoins -> {
                              errorMsg = "${result.missingCoins} ${strings.needMoreCoins}"
                            }
                            RedeemRewardResult.NotFound -> Unit
                          }
                        }
                      },
                      reducedMotion = state.prefs.reducedMotion,
                      leadingIcon = Icons.Rounded.CardGiftcard,
                      modifier = Modifier.fillMaxWidth(),
                      testTag = "unlock_reward_${reward.id}",
                    )
                  } else {
                    val missing = (reward.coinCost - state.coinBalance).coerceAtLeast(0)
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.Center,
                      modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.lavenderTint.copy(alpha = 0.5f))
                        .padding(vertical = 10.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(16.dp),
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "$missing ${strings.needMoreCoins}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.secondaryText,
                      )
                    }
                  }
                }
              }
            }
          }
        }

        1 -> {
          if (state.rewardClaims.isEmpty()) {
            item { EmptyStateCard(title = strings.noClaimsYet, icon = Icons.Rounded.CardGiftcard) }
          } else {
            items(state.rewardClaims, key = { it.id }) { claim ->
              ModernSectionCard {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  DayoraIllustration(
                    imageKey = claim.imageKey,
                    contentDescription = null,
                    modifier = Modifier
                      .size(54.dp)
                      .clip(RoundedCornerShape(14.dp)),
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (isBn) claim.rewardTitleBn else claim.rewardTitleEn,
                      style = MaterialTheme.typography.titleSmall,
                      color = colors.mainText,
                    )
                    Text(
                      text = LocalDayClock.formatDateTime(claim.claimedAtMillis, state.prefs.language.code),
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText,
                    )
                  }
                  if (claim.coinsSpent > 0) {
                    Text(
                      text = "-${claim.coinsSpent}",
                      style = MaterialTheme.typography.labelLarge,
                      color = colors.pinkAccent,
                    )
                  }
                }
              }
            }
          }
        }

        else -> {
          if (state.coinTransactions.isEmpty()) {
            item { EmptyStateCard(title = strings.noTransactionsYet, icon = Icons.Rounded.History) }
          } else {
            items(state.coinTransactions, key = { it.id }) { tx ->
              val positive = tx.amount >= 0
              ModernSectionCard {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(
                        if (positive) colors.successMint.copy(alpha = 0.25f)
                        else colors.softPink
                      ),
                    contentAlignment = Alignment.Center,
                  ) {
                    ShinyCoinIcon(size = 20.dp)
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (isBn) tx.titleBn else tx.titleEn,
                      style = MaterialTheme.typography.titleSmall,
                      color = colors.mainText,
                    )
                    Text(
                      text = LocalDayClock.formatDateTime(tx.createdAtMillis, state.prefs.language.code),
                      style = MaterialTheme.typography.bodySmall,
                      color = colors.secondaryText,
                    )
                  }
                  Text(
                    text = if (positive) "+${tx.amount}" else "${tx.amount}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (positive) colors.successMintDeep else colors.pinkAccent,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  unlockedDialog?.let { reward ->
    val title = if (isBn) reward.titleBn else reward.titleEn
    val desc = if (isBn) reward.descriptionBn else reward.descriptionEn
    AlertDialog(
      onDismissRequest = { unlockedDialog = null },
      title = { Text(strings.rewardUnlockedTitle) },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
          Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            StarCoinCelebrationBurst(
              reducedMotion = state.prefs.reducedMotion,
              modifier = Modifier.fillMaxSize(),
            )
            DayoraIllustration(
              imageKey = reward.imageKey,
              contentDescription = title,
              modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(20.dp)),
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
          )
        }
      },
      confirmButton = {
        Button(onClick = { unlockedDialog = null }) {
          Text(strings.continueLabel)
        }
      },
    )
  }

  errorMsg?.let { msg ->
    AlertDialog(
      onDismissRequest = { errorMsg = null },
      title = { Text(strings.rewardsCenterTitle) },
      text = { Text(msg) },
      confirmButton = {
        Button(onClick = { errorMsg = null }) {
          Text(strings.closeLabel)
        }
      },
    )
  }
}
