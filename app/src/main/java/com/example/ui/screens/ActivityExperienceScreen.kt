package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ClaimActivityResult
import com.example.ui.components.CandyCloudHeader
import com.example.ui.components.CandyJellyButton
import com.example.ui.components.CoinPill
import com.example.ui.components.DayoraIllustration
import com.example.ui.components.ShinyCoinIcon
import com.example.ui.components.StarCoinCelebrationBurst
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.RewardGold
import com.example.ui.viewmodel.DayoraViewModel
import java.util.UUID

private enum class ActivityStage {
  POPUP_INTRO,
  STEPS,
  COMPLETION,
  CLAIMED_SUCCESS,
}

@Composable
fun ActivityExperienceScreen(
  activityId: String,
  viewModel: DayoraViewModel,
  language: AppLanguage,
  coinBalance: Int,
  reducedMotion: Boolean,
  onClose: () -> Unit,
  onOpenWeeklyChallenge: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val isBn = language == AppLanguage.BENGALI

  val activity by viewModel.observeActivity(activityId).collectAsStateWithLifecycle(initialValue = null)
  val steps by viewModel.observeSteps(activityId).collectAsStateWithLifecycle(initialValue = emptyList())
  val savedProgress by viewModel.observeProgress(activityId).collectAsStateWithLifecycle(initialValue = null)

  var stage by rememberSaveable(activityId) { mutableStateOf(ActivityStage.POPUP_INTRO) }
  var currentStepIndex by rememberSaveable(activityId) { mutableIntStateOf(0) }
  var sessionToken by rememberSaveable(activityId) { mutableStateOf(UUID.randomUUID().toString()) }
  var initializedProgress by remember(activityId) { mutableStateOf(false) }
  var isClaiming by remember { mutableStateOf(false) }
  var claimResult by remember { mutableStateOf<ClaimActivityResult.Success?>(null) }
  var claimError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(savedProgress, steps.size) {
    if (!initializedProgress && savedProgress != null && steps.isNotEmpty()) {
      val prog = savedProgress!!
      currentStepIndex = prog.currentStepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))
      if (prog.sessionToken.isNotBlank()) {
        sessionToken = prog.sessionToken
      }
      initializedProgress = true
    }
  }

  BackHandler {
    when (stage) {
      ActivityStage.STEPS -> {
        if (currentStepIndex > 0) {
          currentStepIndex -= 1
          viewModel.saveStepProgress(activityId, currentStepIndex, steps.size, sessionToken)
        } else {
          stage = ActivityStage.POPUP_INTRO
        }
      }
      ActivityStage.COMPLETION -> stage = ActivityStage.STEPS
      ActivityStage.POPUP_INTRO, ActivityStage.CLAIMED_SUCCESS -> onClose()
    }
  }

  val act = activity
  if (act == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              colors.lavenderTint,
              MaterialTheme.colorScheme.background,
              colors.softPink.copy(alpha = 0.6f),
            )
          )
        ),
      contentAlignment = Alignment.Center,
    ) {
      androidx.compose.material3.CircularProgressIndicator(color = colors.primaryAccent)
    }
    return
  }
  val title = if (isBn) act.titleBn else act.titleEn
  val desc = if (isBn) act.descriptionBn else act.descriptionEn

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            colors.lavenderTint,
            MaterialTheme.colorScheme.background,
            colors.softPink.copy(alpha = 0.6f),
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier = Modifier
        .widthIn(max = 560.dp)
        .fillMaxWidth(),
      shape = RoundedCornerShape(30.dp),
      color = colors.popupSurface,
      border = BorderStroke(2.dp, colors.primaryAccent.copy(alpha = 0.35f)),
      shadowElevation = 12.dp,
    ) {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        CandyCloudHeader(
          title = title,
          subtitle = "${act.durationMinutes} ${strings.minShort} • ${steps.size} ${strings.stepsShort}",
          onClose = onClose,
        )

        AnimatedContent(
          targetState = stage,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "activity_stage",
        ) { currentStage ->
          when (currentStage) {
            ActivityStage.POPUP_INTRO -> {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                DayoraIllustration(
                  imageKey = act.coverImageKey,
                  contentDescription = title,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(22.dp)),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                  text = desc,
                  style = MaterialTheme.typography.bodyLarge,
                  color = colors.mainText,
                  textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Info Pills
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                  InfoBadgePill(
                    icon = Icons.Rounded.Schedule,
                    text = "${act.durationMinutes} ${strings.minShort}",
                    tint = colors.primaryAccent,
                  )
                  InfoBadgePill(
                    icon = Icons.Rounded.Layers,
                    text = "${steps.size} ${strings.stepsShort}",
                    tint = colors.pinkAccent,
                  )
                  Surface(
                    shape = RoundedCornerShape(50),
                    color = RewardGold.copy(alpha = 0.22f),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      ShinyCoinIcon(size = 18.dp)
                      Spacer(modifier = Modifier.width(5.dp))
                      Text(
                        text = "+${act.coinReward}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.mainText,
                      )
                    }
                  }
                }

                // Resume Progress Indicator
                if (savedProgress?.isStarted == true && steps.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(16.dp))
                  val ratio = ((currentStepIndex + 1).toFloat() / steps.size.toFloat()).coerceIn(0f, 1f)
                  Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                      text = "${strings.stepPrefix} ${currentStepIndex + 1} ${strings.ofPrefix} ${steps.size}",
                      style = MaterialTheme.typography.labelMedium,
                      color = colors.primaryAccent,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                      progress = { ratio },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(50)),
                      color = colors.pinkAccent,
                      trackColor = colors.lavenderTint,
                    )
                  }
                }

                Spacer(modifier = Modifier.height(22.dp))

                val startText = if (savedProgress?.isStarted == true) {
                  strings.resumeActivity
                } else {
                  strings.startActivity
                }
                CandyJellyButton(
                  text = startText,
                  onClick = {
                    if (steps.isEmpty()) {
                      stage = ActivityStage.COMPLETION
                    } else {
                      viewModel.saveStepProgress(activityId, currentStepIndex, steps.size, sessionToken)
                      stage = ActivityStage.STEPS
                    }
                  },
                  reducedMotion = reducedMotion,
                  leadingIcon = Icons.Rounded.PlayArrow,
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "activity_start_button",
                )
              }
            }

            ActivityStage.STEPS -> {
              val safeIndex = currentStepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))
              val step = steps.getOrNull(safeIndex)
              if (step != null) {
                val stepTitle = if (isBn) step.titleBn else step.titleEn
                val stepInstruction = if (isBn) step.instructionBn else step.instructionEn
                val progress = ((safeIndex + 1).toFloat() / steps.size.toFloat()).coerceIn(0f, 1f)

                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  // Step Counter & Progress
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Surface(
                      shape = RoundedCornerShape(50),
                      color = colors.primaryAccent.copy(alpha = 0.16f),
                    ) {
                      Text(
                        text = "${strings.stepPrefix} ${safeIndex + 1} ${strings.ofPrefix} ${steps.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primaryAccent,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                      )
                    }
                    CoinPill(coins = coinBalance)
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(10.dp)
                      .clip(RoundedCornerShape(50)),
                    color = colors.pinkAccent,
                    trackColor = colors.lavenderTint,
                  )
                  Spacer(modifier = Modifier.height(18.dp))

                  DayoraIllustration(
                    imageKey = step.imageKey,
                    contentDescription = stepTitle,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(175.dp)
                      .clip(RoundedCornerShape(22.dp)),
                  )
                  Spacer(modifier = Modifier.height(18.dp))

                  Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.mainText,
                    textAlign = TextAlign.Center,
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(1.dp, colors.subtleBorder),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Text(
                      text = stepInstruction,
                      style = MaterialTheme.typography.bodyLarge,
                      color = colors.mainText,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(18.dp),
                    )
                  }

                  Spacer(modifier = Modifier.height(22.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    if (safeIndex > 0) {
                      OutlinedButton(
                        onClick = {
                          val prev = safeIndex - 1
                          currentStepIndex = prev
                          viewModel.saveStepProgress(activityId, prev, steps.size, sessionToken)
                        },
                        modifier = Modifier
                          .weight(0.9f)
                          .minimumInteractiveComponentSize()
                          .testTag("step_previous_button"),
                        shape = RoundedCornerShape(22.dp),
                      ) {
                        Icon(
                          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                          contentDescription = null,
                          modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.previousStep)
                      }
                    }

                    val isLast = safeIndex >= steps.size - 1
                    CandyJellyButton(
                      text = if (isLast) strings.finishActivity else strings.nextStep,
                      onClick = {
                        if (isLast) {
                          stage = ActivityStage.COMPLETION
                        } else {
                          val next = safeIndex + 1
                          currentStepIndex = next
                          viewModel.saveStepProgress(activityId, next, steps.size, sessionToken)
                        }
                      },
                      reducedMotion = reducedMotion,
                      leadingIcon = if (isLast) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.ArrowForward,
                      modifier = Modifier.weight(1.2f),
                      testTag = "step_next_button",
                    )
                  }
                }
              }
            }

            ActivityStage.COMPLETION -> {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Box(
                  modifier = Modifier.size(180.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  StarCoinCelebrationBurst(
                    reducedMotion = reducedMotion,
                    modifier = Modifier.fillMaxSize(),
                  )
                  DayoraIllustration(
                    imageKey = act.coverImageKey,
                    contentDescription = title,
                    modifier = Modifier
                      .size(124.dp)
                      .clip(CircleShape),
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = strings.activityCompletedTitle,
                  style = MaterialTheme.typography.headlineSmall,
                  color = colors.mainText,
                  textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = strings.activityCompletedSubtitle,
                  style = MaterialTheme.typography.bodyMedium,
                  color = colors.secondaryText,
                  textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                  shape = RoundedCornerShape(22.dp),
                  color = RewardGold.copy(alpha = 0.2f),
                  border = BorderStroke(1.5.dp, RewardGold),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    ShinyCoinIcon(size = 34.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = strings.earnedCoinsLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.secondaryText,
                      )
                      Text(
                        text = "+${act.coinReward} ${strings.coinsLabel}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.mainText,
                      )
                    }
                  }
                }

                if (claimError != null) {
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = claimError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    textAlign = TextAlign.Center,
                  )
                }

                Spacer(modifier = Modifier.height(22.dp))

                CandyJellyButton(
                  text = strings.claimReward,
                  enabled = !isClaiming,
                  onClick = {
                    isClaiming = true
                    claimError = null
                    viewModel.claimActivityReward(activityId, sessionToken) { result ->
                      isClaiming = false
                      when (result) {
                        is ClaimActivityResult.Success -> {
                          claimResult = result
                          stage = ActivityStage.CLAIMED_SUCCESS
                        }
                        ClaimActivityResult.AlreadyClaimed -> {
                          claimError = strings.alreadyClaimedError
                        }
                        ClaimActivityResult.NotFound -> {
                          onClose()
                        }
                      }
                    }
                  },
                  reducedMotion = reducedMotion,
                  topColor = Color(0xFFF5C75E),
                  bottomColor = Color(0xFFE59B22),
                  lipColor = Color(0xFFB3700E),
                  leadingIcon = Icons.Rounded.AutoAwesome,
                  modifier = Modifier.fillMaxWidth(),
                  testTag = "claim_reward_button",
                )
              }
            }

            ActivityStage.CLAIMED_SUCCESS -> {
              val res = claimResult
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Box(
                  modifier = Modifier.size(150.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  StarCoinCelebrationBurst(
                    reducedMotion = reducedMotion,
                    modifier = Modifier.fillMaxSize(),
                  )
                  ShinyCoinIcon(size = 76.dp)
                }

                Text(
                  text = strings.rewardClaimedToast,
                  style = MaterialTheme.typography.headlineSmall,
                  color = colors.mainText,
                  textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (res != null) {
                  Text(
                    text = "+${res.activityCoins} ${strings.coinsLabel}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.rewardGoldDeep,
                  )
                  if (res.dailyChallengeCoins > 0 && res.completedChallengeDay != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                      shape = RoundedCornerShape(16.dp),
                      color = colors.successMint.copy(alpha = 0.24f),
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Icon(
                          imageVector = Icons.Rounded.EmojiEvents,
                          contentDescription = null,
                          tint = colors.successMintDeep,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "${strings.dayLabel} ${res.completedChallengeDay}: +${res.dailyChallengeCoins} ${strings.coinsLabel}",
                          style = MaterialTheme.typography.labelLarge,
                          color = colors.mainText,
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (res?.unlockedDay7Grand == true) {
                  CandyJellyButton(
                    text = strings.claimGrandReward,
                    onClick = onOpenWeeklyChallenge,
                    reducedMotion = reducedMotion,
                    leadingIcon = Icons.Rounded.EmojiEvents,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "open_day7_grand_button",
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                  onClick = onClose,
                  modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("completion_done_button"),
                  shape = RoundedCornerShape(20.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                ) {
                  Text(strings.continueLabel, modifier = Modifier.padding(vertical = 6.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InfoBadgePill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  tint: Color,
) {
  val colors = LocalDayoraColors.current
  Surface(
    shape = RoundedCornerShape(50),
    color = tint.copy(alpha = 0.14f),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = colors.mainText,
      )
    }
  }
}
