package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DayoraApplication
import com.example.data.local.ActivityEntity
import com.example.data.local.ActivityProgressEntity
import com.example.data.local.ActivityStepEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ClaimActivityResult
import com.example.data.local.ClaimGrandRewardResult
import com.example.data.local.CoinTransactionEntity
import com.example.data.local.CompletionRecordEntity
import com.example.data.local.LocalDayClock
import com.example.data.local.RedeemRewardResult
import com.example.data.local.RewardClaimEntity
import com.example.data.local.RewardItemEntity
import com.example.data.local.UserPrefsSnapshot
import com.example.data.local.WeeklyChallengeConfigEntity
import com.example.data.local.WeeklyChallengeDayEntity
import com.example.data.repository.DayoraRepository
import com.example.notifications.DailyReminderScheduler
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DayoraAppUiState(
  val isReady: Boolean = false,
  val prefs: UserPrefsSnapshot = UserPrefsSnapshot(),
  val enabledCategories: List<CategoryEntity> = emptyList(),
  val allCategories: List<CategoryEntity> = emptyList(),
  val enabledActivities: List<ActivityEntity> = emptyList(),
  val allActivities: List<ActivityEntity> = emptyList(),
  val allSteps: List<ActivityStepEntity> = emptyList(),
  val allProgress: List<ActivityProgressEntity> = emptyList(),
  val completions: List<CompletionRecordEntity> = emptyList(),
  val coinBalance: Int = 0,
  val coinTransactions: List<CoinTransactionEntity> = emptyList(),
  val enabledRewards: List<RewardItemEntity> = emptyList(),
  val allRewards: List<RewardItemEntity> = emptyList(),
  val rewardClaims: List<RewardClaimEntity> = emptyList(),
  val challengeConfig: WeeklyChallengeConfigEntity? = null,
  val challengeDays: List<WeeklyChallengeDayEntity> = emptyList(),
  val todayCompletedCount: Int = 0,
  val todayEpochDay: Long = LocalDayClock.currentEpochDay(),
)

private data class CoreFlows(
  val prefs: UserPrefsSnapshot,
  val enabledCategories: List<CategoryEntity>,
  val allCategories: List<CategoryEntity>,
  val enabledActivities: List<ActivityEntity>,
  val allActivities: List<ActivityEntity>,
)

private data class ProgressFlows(
  val allSteps: List<ActivityStepEntity>,
  val allProgress: List<ActivityProgressEntity>,
  val completions: List<CompletionRecordEntity>,
  val coinBalance: Int,
  val coinTransactions: List<CoinTransactionEntity>,
)

private data class RewardFlows(
  val enabledRewards: List<RewardItemEntity>,
  val allRewards: List<RewardItemEntity>,
  val rewardClaims: List<RewardClaimEntity>,
  val challengeConfig: WeeklyChallengeConfigEntity?,
  val challengeDays: List<WeeklyChallengeDayEntity>,
)

class DayoraViewModel(application: Application) : AndroidViewModel(application) {
  private val app = application as DayoraApplication
  private val repository: DayoraRepository = app.repository

  private val _seeded = MutableStateFlow(false)
  private val _isAdminUnlocked = MutableStateFlow(false)
  val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

  private val coreFlow = combine(
    repository.userPrefs,
    repository.enabledCategories,
    repository.allCategories,
    repository.enabledActivities,
    repository.allActivities,
  ) { prefs, enCats, allCats, enActs, allActs ->
    CoreFlows(prefs, enCats, allCats, enActs, allActs)
  }

  private val progressFlow = combine(
    repository.allSteps,
    repository.allProgress,
    repository.completions,
    repository.coinBalance,
    repository.coinTransactions,
  ) { steps, progress, completions, coins, tx ->
    ProgressFlows(steps, progress, completions, coins, tx)
  }

  private val rewardFlow = combine(
    repository.enabledRewards,
    repository.allRewards,
    repository.rewardClaims,
    repository.challengeConfig,
    repository.currentChallengeDays,
  ) { enRewards, allRewards, claims, config, days ->
    RewardFlows(enRewards, allRewards, claims, config, days)
  }

  val uiState: StateFlow<DayoraAppUiState> = combine(
    _seeded,
    coreFlow,
    progressFlow,
    rewardFlow,
  ) { seeded, core, prog, rew ->
    val today = LocalDayClock.currentEpochDay()
    val todayCount = prog.completions.count { it.completedEpochDay == today }
    DayoraAppUiState(
      isReady = seeded,
      prefs = core.prefs,
      enabledCategories = core.enabledCategories,
      allCategories = core.allCategories,
      enabledActivities = core.enabledActivities,
      allActivities = core.allActivities,
      allSteps = prog.allSteps,
      allProgress = prog.allProgress,
      completions = prog.completions,
      coinBalance = prog.coinBalance,
      coinTransactions = prog.coinTransactions,
      enabledRewards = rew.enabledRewards,
      allRewards = rew.allRewards,
      rewardClaims = rew.rewardClaims,
      challengeConfig = rew.challengeConfig,
      challengeDays = rew.challengeDays,
      todayCompletedCount = todayCount,
      todayEpochDay = today,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DayoraAppUiState(),
  )

  init {
    viewModelScope.launch {
      try {
        repository.ensureSeeded()
      } finally {
        _seeded.value = true
      }
    }
  }

  fun saveFullName(name: String, onDone: () -> Unit = {}) {
    viewModelScope.launch {
      repository.saveFullName(name)
      onDone()
    }
  }

  fun confirmYoutubeAndCompleteOnboarding(onDone: () -> Unit = {}) {
    viewModelScope.launch {
      repository.confirmYoutubeAndFinishOnboarding()
      onDone()
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    viewModelScope.launch { repository.setThemeMode(mode) }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch {
      repository.setLanguage(language)
      val prefs = uiState.value.prefs
      if (prefs.remindersEnabled) {
        DailyReminderScheduler.scheduleDaily(
          app,
          prefs.reminderHour,
          prefs.reminderMinute,
          language.code,
        )
      }
    }
  }

  fun updateReminders(enabled: Boolean, hour: Int, minute: Int) {
    viewModelScope.launch {
      repository.setReminderSettings(enabled, hour, minute)
      if (enabled) {
        DailyReminderScheduler.scheduleDaily(
          app,
          hour,
          minute,
          uiState.value.prefs.language.code,
        )
      } else {
        DailyReminderScheduler.cancelDaily(app)
      }
    }
  }

  fun sendTestReminder() {
    DailyReminderScheduler.showImmediateReminder(app, uiState.value.prefs.language.code)
  }

  fun setReducedMotion(reduced: Boolean) {
    viewModelScope.launch { repository.setReducedMotion(reduced) }
  }

  fun resetOnboarding(onDone: () -> Unit = {}) {
    viewModelScope.launch {
      repository.resetOnboarding()
      onDone()
    }
  }

  fun clearActivityHistory() {
    viewModelScope.launch { repository.clearActivityHistory() }
  }

  fun resetAllPersonalData(onDone: () -> Unit = {}) {
    viewModelScope.launch {
      DailyReminderScheduler.cancelDaily(app)
      repository.resetAllPersonalAppData()
      onDone()
    }
  }

  fun observeActivity(activityId: String): Flow<ActivityEntity?> =
    repository.observeActivity(activityId)

  fun observeSteps(activityId: String): Flow<List<ActivityStepEntity>> =
    repository.observeStepsForActivity(activityId)

  fun observeProgress(activityId: String): Flow<ActivityProgressEntity?> =
    repository.observeProgressForActivity(activityId)

  fun saveStepProgress(activityId: String, stepIndex: Int, totalSteps: Int, sessionToken: String) {
    viewModelScope.launch {
      repository.saveActivityProgress(activityId, stepIndex, totalSteps, sessionToken)
    }
  }

  fun claimActivityReward(
    activityId: String,
    claimToken: String,
    onResult: (ClaimActivityResult) -> Unit,
  ) {
    viewModelScope.launch {
      val res = repository.claimActivityCompletion(activityId, claimToken)
      onResult(res)
    }
  }

  fun redeemReward(rewardId: String, onResult: (RedeemRewardResult) -> Unit) {
    viewModelScope.launch {
      val res = repository.redeemReward(rewardId)
      onResult(res)
    }
  }

  fun claimDay7GrandReward(onResult: (ClaimGrandRewardResult) -> Unit) {
    viewModelScope.launch {
      val res = repository.claimDay7GrandReward()
      onResult(res)
    }
  }

  fun setAdminPasscodeAndUnlock(passcode: String, onDone: () -> Unit) {
    viewModelScope.launch {
      repository.setAdminPasscode(passcode)
      _isAdminUnlocked.value = true
      onDone()
    }
  }

  fun verifyAndUnlockAdmin(passcode: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
      val ok = repository.verifyAdminPasscode(passcode)
      if (ok) _isAdminUnlocked.value = true
      onResult(ok)
    }
  }

  fun lockAdmin() {
    _isAdminUnlocked.value = false
  }

  fun upsertCategory(category: CategoryEntity) {
    viewModelScope.launch { repository.upsertCategory(category) }
  }

  fun deleteCategory(categoryId: String) {
    viewModelScope.launch { repository.deleteCategory(categoryId) }
  }

  fun upsertActivity(activity: ActivityEntity, createDefaultStepIfNew: Boolean = false) {
    viewModelScope.launch {
      repository.upsertActivity(activity)
      if (createDefaultStepIfNew) {
        val existingSteps = uiState.value.allSteps.filter { it.activityId == activity.id }
        if (existingSteps.isEmpty()) {
          repository.upsertStep(
            ActivityStepEntity(
              id = "step_${UUID.randomUUID()}",
              activityId = activity.id,
              stepNumber = 1,
              titleEn = "Begin Routine",
              titleBn = "শুরু করুন",
              instructionEn = "Take a peaceful moment and enjoy ${activity.titleEn}.",
              instructionBn = "শান্ত মনে '${activity.titleBn}' সম্পন্ন করুন।",
              imageKey = activity.coverImageKey,
            )
          )
        }
      }
    }
  }

  fun deleteActivity(activityId: String) {
    viewModelScope.launch { repository.deleteActivity(activityId) }
  }

  fun upsertStep(step: ActivityStepEntity) {
    viewModelScope.launch { repository.upsertStep(step) }
  }

  fun deleteStep(stepId: String) {
    viewModelScope.launch { repository.deleteStep(stepId) }
  }

  fun swapSteps(stepA: ActivityStepEntity, stepB: ActivityStepEntity) {
    viewModelScope.launch { repository.swapStepOrder(stepA, stepB) }
  }

  fun upsertReward(reward: RewardItemEntity) {
    viewModelScope.launch { repository.upsertReward(reward) }
  }

  fun deleteReward(rewardId: String) {
    viewModelScope.launch { repository.deleteReward(rewardId) }
  }

  fun upsertChallengeConfig(config: WeeklyChallengeConfigEntity) {
    viewModelScope.launch { repository.upsertChallengeConfig(config) }
  }

  fun importGalleryImage(uri: Uri, onResult: (String?) -> Unit) {
    viewModelScope.launch {
      val localUri = repository.copyPickedImageToAppStorage(uri)
      onResult(localUri)
    }
  }

  fun saveCroppedProfileAvatar(bitmap: Bitmap, onDone: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val saved = repository.saveCroppedProfileAvatar(bitmap)
      onDone(saved != null)
    }
  }

  fun resetProfileAvatarToDefault(onDone: () -> Unit = {}) {
    viewModelScope.launch {
      repository.resetProfileAvatarToDefault()
      onDone()
    }
  }
}
