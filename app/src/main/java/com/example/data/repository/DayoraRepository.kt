package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.data.local.ActivityEntity
import com.example.data.local.ActivityProgressEntity
import com.example.data.local.ActivityStepEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ClaimActivityResult
import com.example.data.local.ClaimGrandRewardResult
import com.example.data.local.CoinTransactionEntity
import com.example.data.local.CompletionRecordEntity
import com.example.data.local.DayoraDao
import com.example.data.local.LocalDayClock
import com.example.data.local.RedeemRewardResult
import com.example.data.local.RewardClaimEntity
import com.example.data.local.RewardItemEntity
import com.example.data.local.SeedData
import com.example.data.local.UserPreferencesStore
import com.example.data.local.UserPrefsSnapshot
import com.example.data.local.WeeklyChallengeConfigEntity
import com.example.data.local.WeeklyChallengeDayEntity
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Future-ready repository boundary.
 * Phase 1 uses strictly local Room + DataStore persistence.
 * Phase 2 can decorate or swap this interface with Firebase Firestore / Cloud Storage sync.
 */
interface DayoraRepository {
  val userPrefs: Flow<UserPrefsSnapshot>
  val enabledCategories: Flow<List<CategoryEntity>>
  val allCategories: Flow<List<CategoryEntity>>
  val enabledActivities: Flow<List<ActivityEntity>>
  val allActivities: Flow<List<ActivityEntity>>
  val allSteps: Flow<List<ActivityStepEntity>>
  val allProgress: Flow<List<ActivityProgressEntity>>
  val completions: Flow<List<CompletionRecordEntity>>
  val coinBalance: Flow<Int>
  val coinTransactions: Flow<List<CoinTransactionEntity>>
  val enabledRewards: Flow<List<RewardItemEntity>>
  val allRewards: Flow<List<RewardItemEntity>>
  val rewardClaims: Flow<List<RewardClaimEntity>>
  val challengeConfig: Flow<WeeklyChallengeConfigEntity?>
  val currentChallengeDays: Flow<List<WeeklyChallengeDayEntity>>

  suspend fun ensureSeeded()
  suspend fun saveFullName(name: String)
  suspend fun confirmYoutubeAndFinishOnboarding()
  suspend fun setThemeMode(mode: ThemeMode)
  suspend fun setLanguage(language: AppLanguage)
  suspend fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int)
  suspend fun setReducedMotion(reduced: Boolean)
  suspend fun resetOnboarding()
  suspend fun clearActivityHistory()
  suspend fun resetAllPersonalAppData()

  suspend fun setAdminPasscode(passcode: String)
  suspend fun verifyAdminPasscode(passcode: String): Boolean

  fun observeActivity(activityId: String): Flow<ActivityEntity?>
  fun observeStepsForActivity(activityId: String): Flow<List<ActivityStepEntity>>
  fun observeProgressForActivity(activityId: String): Flow<ActivityProgressEntity?>

  suspend fun saveActivityProgress(activityId: String, stepIndex: Int, totalSteps: Int, sessionToken: String)
  suspend fun claimActivityCompletion(activityId: String, claimToken: String): ClaimActivityResult
  suspend fun redeemReward(rewardId: String): RedeemRewardResult
  suspend fun claimDay7GrandReward(): ClaimGrandRewardResult

  // Admin CRUD
  suspend fun upsertCategory(category: CategoryEntity)
  suspend fun deleteCategory(categoryId: String)
  suspend fun upsertActivity(activity: ActivityEntity)
  suspend fun deleteActivity(activityId: String)
  suspend fun upsertStep(step: ActivityStepEntity)
  suspend fun deleteStep(stepId: String)
  suspend fun swapStepOrder(stepA: ActivityStepEntity, stepB: ActivityStepEntity)
  suspend fun upsertReward(reward: RewardItemEntity)
  suspend fun deleteReward(rewardId: String)
  suspend fun upsertChallengeConfig(config: WeeklyChallengeConfigEntity)
  suspend fun copyPickedImageToAppStorage(uri: Uri): String?
  suspend fun saveCroppedProfileAvatar(bitmap: Bitmap): String?
  suspend fun resetProfileAvatarToDefault()
}

class LocalDayoraRepository(
  private val context: Context,
  private val dao: DayoraDao,
  private val prefsStore: UserPreferencesStore,
) : DayoraRepository {

  override val userPrefs: Flow<UserPrefsSnapshot> = prefsStore.prefsFlow
  override val enabledCategories: Flow<List<CategoryEntity>> = dao.observeEnabledCategories()
  override val allCategories: Flow<List<CategoryEntity>> = dao.observeAllCategories()
  override val enabledActivities: Flow<List<ActivityEntity>> = dao.observeEnabledActivities()
  override val allActivities: Flow<List<ActivityEntity>> = dao.observeAllActivities()
  override val allSteps: Flow<List<ActivityStepEntity>> = dao.observeAllSteps()
  override val allProgress: Flow<List<ActivityProgressEntity>> = dao.observeAllProgress()
  override val completions: Flow<List<CompletionRecordEntity>> = dao.observeAllCompletions()
  override val coinBalance: Flow<Int> = dao.observeCoinBalance()
  override val coinTransactions: Flow<List<CoinTransactionEntity>> = dao.observeCoinTransactions()
  override val enabledRewards: Flow<List<RewardItemEntity>> = dao.observeEnabledRewards()
  override val allRewards: Flow<List<RewardItemEntity>> = dao.observeAllRewards()
  override val rewardClaims: Flow<List<RewardClaimEntity>> = dao.observeRewardClaims()
  override val challengeConfig: Flow<WeeklyChallengeConfigEntity?> = dao.observeChallengeConfig()
  override val currentChallengeDays: Flow<List<WeeklyChallengeDayEntity>> = dao.observeCurrentChallengeDays()

  override suspend fun ensureSeeded() {
    withContext(Dispatchers.IO) {
      dao.seedDatabaseIfEmptyAtomic(
        categories = SeedData.categories,
        activities = SeedData.activities,
        steps = SeedData.steps,
        rewards = SeedData.rewards,
        challengeConfig = SeedData.defaultChallengeConfig,
      )
    }
  }

  override suspend fun saveFullName(name: String) = prefsStore.saveFullName(name)
  override suspend fun confirmYoutubeAndFinishOnboarding() = prefsStore.confirmYoutubeAndCompleteOnboarding()
  override suspend fun setThemeMode(mode: ThemeMode) = prefsStore.setThemeMode(mode)
  override suspend fun setLanguage(language: AppLanguage) = prefsStore.setLanguage(language)
  override suspend fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int) =
    prefsStore.setReminderSettings(enabled, hour, minute)
  override suspend fun setReducedMotion(reduced: Boolean) = prefsStore.setReducedMotion(reduced)
  override suspend fun resetOnboarding() = prefsStore.resetOnboarding()

  override suspend fun clearActivityHistory() {
    withContext(Dispatchers.IO) {
      dao.clearAllProgress()
      dao.clearCompletionRecords()
    }
  }

  override suspend fun resetAllPersonalAppData() {
    withContext(Dispatchers.IO) {
      dao.clearPersonalProgressAndHistoryAtomic()
      prefsStore.resetAllPersonalPreferences()
      File(context.filesDir, "profile_avatars").deleteRecursively()
    }
  }

  override suspend fun setAdminPasscode(passcode: String) = prefsStore.setAdminPasscode(passcode)
  override suspend fun verifyAdminPasscode(passcode: String): Boolean =
    prefsStore.verifyAdminPasscode(passcode)

  override fun observeActivity(activityId: String): Flow<ActivityEntity?> =
    dao.observeActivityById(activityId)

  override fun observeStepsForActivity(activityId: String): Flow<List<ActivityStepEntity>> =
    dao.observeStepsForActivity(activityId)

  override fun observeProgressForActivity(activityId: String): Flow<ActivityProgressEntity?> =
    dao.observeProgressForActivity(activityId)

  override suspend fun saveActivityProgress(
    activityId: String,
    stepIndex: Int,
    totalSteps: Int,
    sessionToken: String,
  ) {
    withContext(Dispatchers.IO) {
      dao.upsertProgress(
        ActivityProgressEntity(
          activityId = activityId,
          currentStepIndex = stepIndex,
          totalSteps = totalSteps,
          isStarted = true,
          sessionToken = sessionToken,
          lastUpdatedMillis = System.currentTimeMillis(),
        )
      )
    }
  }

  override suspend fun claimActivityCompletion(
    activityId: String,
    claimToken: String,
  ): ClaimActivityResult = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val epochDay = LocalDayClock.currentEpochDay(now)
    dao.claimActivityCompletionAtomic(
      activityId = activityId,
      claimToken = claimToken,
      nowMillis = now,
      todayEpochDay = epochDay,
    )
  }

  override suspend fun redeemReward(rewardId: String): RedeemRewardResult =
    withContext(Dispatchers.IO) {
      dao.redeemRewardAtomic(rewardId = rewardId, nowMillis = System.currentTimeMillis())
    }

  override suspend fun claimDay7GrandReward(): ClaimGrandRewardResult =
    withContext(Dispatchers.IO) {
      dao.claimDay7GrandRewardAtomic(nowMillis = System.currentTimeMillis())
    }

  override suspend fun upsertCategory(category: CategoryEntity) =
    withContext(Dispatchers.IO) { dao.upsertCategory(category) }

  override suspend fun deleteCategory(categoryId: String) =
    withContext(Dispatchers.IO) { dao.deleteCategoryById(categoryId) }

  override suspend fun upsertActivity(activity: ActivityEntity) =
    withContext(Dispatchers.IO) { dao.upsertActivity(activity) }

  override suspend fun deleteActivity(activityId: String) =
    withContext(Dispatchers.IO) { dao.deleteActivityById(activityId) }

  override suspend fun upsertStep(step: ActivityStepEntity) =
    withContext(Dispatchers.IO) { dao.upsertStep(step) }

  override suspend fun deleteStep(stepId: String) =
    withContext(Dispatchers.IO) { dao.deleteStepById(stepId) }

  override suspend fun swapStepOrder(stepA: ActivityStepEntity, stepB: ActivityStepEntity) {
    withContext(Dispatchers.IO) {
      dao.upsertStep(stepA.copy(stepNumber = stepB.stepNumber))
      dao.upsertStep(stepB.copy(stepNumber = stepA.stepNumber))
    }
  }

  override suspend fun upsertReward(reward: RewardItemEntity) =
    withContext(Dispatchers.IO) { dao.upsertReward(reward) }

  override suspend fun deleteReward(rewardId: String) =
    withContext(Dispatchers.IO) { dao.deleteRewardById(rewardId) }

  override suspend fun upsertChallengeConfig(config: WeeklyChallengeConfigEntity) =
    withContext(Dispatchers.IO) { dao.upsertChallengeConfig(config) }

  override suspend fun copyPickedImageToAppStorage(uri: Uri): String? =
    withContext(Dispatchers.IO) {
      runCatching {
        val mediaDir = File(context.filesDir, "admin_media").apply { mkdirs() }
        val target = File(mediaDir, "media_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
          target.outputStream().use { output -> input.copyTo(output) }
        } ?: return@runCatching null
        "file://${target.absolutePath}"
      }.getOrNull()
    }

  override suspend fun saveCroppedProfileAvatar(bitmap: Bitmap): String? =
    withContext(Dispatchers.IO) {
      runCatching {
        val avatarDir = File(context.filesDir, "profile_avatars").apply { mkdirs() }
        // Clean previous custom avatar files so storage stays tidy
        avatarDir.listFiles()?.forEach { it.delete() }
        val target = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
        target.outputStream().use { out ->
          bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        val uriString = "file://${target.absolutePath}"
        prefsStore.setProfileImageUri(uriString)
        uriString
      }.getOrNull()
    }

  override suspend fun resetProfileAvatarToDefault() {
    withContext(Dispatchers.IO) {
      File(context.filesDir, "profile_avatars").deleteRecursively()
      prefsStore.setProfileImageUri("")
    }
  }
}
