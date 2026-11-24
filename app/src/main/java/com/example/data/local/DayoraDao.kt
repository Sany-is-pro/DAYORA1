package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

sealed interface ClaimActivityResult {
  data class Success(
    val activityCoins: Int,
    val dailyChallengeCoins: Int,
    val completedChallengeDay: Int?,
    val unlockedDay7Grand: Boolean,
  ) : ClaimActivityResult

  data object AlreadyClaimed : ClaimActivityResult
  data object NotFound : ClaimActivityResult
}

sealed interface RedeemRewardResult {
  data class Success(val rewardTitleEn: String, val rewardTitleBn: String) : RedeemRewardResult
  data class InsufficientCoins(val missingCoins: Int) : RedeemRewardResult
  data object NotFound : RedeemRewardResult
}

sealed interface ClaimGrandRewardResult {
  data class Success(val grandCoins: Int, val nextCycleNumber: Int) : ClaimGrandRewardResult
  data object NotEligible : ClaimGrandRewardResult
}

@Dao
interface DayoraDao {
  // Categories
  @Query("SELECT * FROM categories ORDER BY sortOrder ASC, nameEn ASC")
  fun observeAllCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE isEnabled = 1 ORDER BY sortOrder ASC, nameEn ASC")
  fun observeEnabledCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT COUNT(*) FROM categories")
  suspend fun countCategories(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(items: List<CategoryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCategory(category: CategoryEntity)

  @Query("DELETE FROM categories WHERE id = :categoryId")
  suspend fun deleteCategoryById(categoryId: String)

  // Activities
  @Query("SELECT * FROM activities ORDER BY sortOrder ASC, titleEn ASC")
  fun observeAllActivities(): Flow<List<ActivityEntity>>

  @Query("SELECT * FROM activities WHERE isEnabled = 1 ORDER BY sortOrder ASC, titleEn ASC")
  fun observeEnabledActivities(): Flow<List<ActivityEntity>>

  @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
  fun observeActivityById(activityId: String): Flow<ActivityEntity?>

  @Query("SELECT * FROM activities WHERE id = :activityId LIMIT 1")
  suspend fun getActivityById(activityId: String): ActivityEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivities(items: List<ActivityEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertActivity(activity: ActivityEntity)

  @Query("DELETE FROM activities WHERE id = :activityId")
  suspend fun deleteActivityById(activityId: String)

  // Activity Steps
  @Query("SELECT * FROM activity_steps ORDER BY stepNumber ASC")
  fun observeAllSteps(): Flow<List<ActivityStepEntity>>

  @Query("SELECT * FROM activity_steps WHERE activityId = :activityId ORDER BY stepNumber ASC")
  fun observeStepsForActivity(activityId: String): Flow<List<ActivityStepEntity>>

  @Query("SELECT * FROM activity_steps WHERE activityId = :activityId ORDER BY stepNumber ASC")
  suspend fun getStepsForActivity(activityId: String): List<ActivityStepEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSteps(items: List<ActivityStepEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertStep(step: ActivityStepEntity)

  @Query("DELETE FROM activity_steps WHERE id = :stepId")
  suspend fun deleteStepById(stepId: String)

  // Activity Progress
  @Query("SELECT * FROM activity_progress")
  fun observeAllProgress(): Flow<List<ActivityProgressEntity>>

  @Query("SELECT * FROM activity_progress WHERE activityId = :activityId LIMIT 1")
  fun observeProgressForActivity(activityId: String): Flow<ActivityProgressEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProgress(progress: ActivityProgressEntity)

  @Query("DELETE FROM activity_progress WHERE activityId = :activityId")
  suspend fun clearProgressForActivity(activityId: String)

  @Query("DELETE FROM activity_progress")
  suspend fun clearAllProgress()

  // Completions & Coins
  @Query("SELECT * FROM completion_records ORDER BY completedAtMillis DESC")
  fun observeAllCompletions(): Flow<List<CompletionRecordEntity>>

  @Query("SELECT COUNT(*) FROM completion_records WHERE claimToken = :claimToken")
  suspend fun countCompletionsByToken(claimToken: String): Int

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertCompletion(record: CompletionRecordEntity): Long

  @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transactions")
  fun observeCoinBalance(): Flow<Int>

  @Query("SELECT COALESCE(SUM(amount), 0) FROM coin_transactions")
  suspend fun getCoinBalanceNow(): Int

  @Query("SELECT * FROM coin_transactions ORDER BY createdAtMillis DESC")
  fun observeCoinTransactions(): Flow<List<CoinTransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertCoinTransaction(tx: CoinTransactionEntity): Long

  @Query("DELETE FROM completion_records")
  suspend fun clearCompletionRecords()

  @Query("DELETE FROM coin_transactions")
  suspend fun clearCoinTransactions()

  // Rewards
  @Query("SELECT * FROM reward_items ORDER BY sortOrder ASC, coinCost ASC")
  fun observeAllRewards(): Flow<List<RewardItemEntity>>

  @Query("SELECT * FROM reward_items WHERE isEnabled = 1 ORDER BY sortOrder ASC, coinCost ASC")
  fun observeEnabledRewards(): Flow<List<RewardItemEntity>>

  @Query("SELECT * FROM reward_items WHERE id = :rewardId LIMIT 1")
  suspend fun getRewardById(rewardId: String): RewardItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRewards(items: List<RewardItemEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertReward(reward: RewardItemEntity)

  @Query("DELETE FROM reward_items WHERE id = :rewardId")
  suspend fun deleteRewardById(rewardId: String)

  @Query("SELECT * FROM reward_claims ORDER BY claimedAtMillis DESC")
  fun observeRewardClaims(): Flow<List<RewardClaimEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRewardClaim(claim: RewardClaimEntity): Long

  @Query("DELETE FROM reward_claims")
  suspend fun clearRewardClaims()

  // Weekly Challenge
  @Query("SELECT * FROM weekly_challenge_config WHERE id = 1 LIMIT 1")
  fun observeChallengeConfig(): Flow<WeeklyChallengeConfigEntity?>

  @Query("SELECT * FROM weekly_challenge_config WHERE id = 1 LIMIT 1")
  suspend fun getChallengeConfigNow(): WeeklyChallengeConfigEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertChallengeConfig(config: WeeklyChallengeConfigEntity)

  @Query("SELECT COALESCE(MAX(cycleNumber), 1) FROM weekly_challenge_days")
  suspend fun getCurrentCycleNumber(): Int

  @Query("SELECT * FROM weekly_challenge_days WHERE cycleNumber = (SELECT COALESCE(MAX(cycleNumber), 1) FROM weekly_challenge_days) ORDER BY dayNumber ASC")
  fun observeCurrentChallengeDays(): Flow<List<WeeklyChallengeDayEntity>>

  @Query("SELECT * FROM weekly_challenge_days WHERE cycleNumber = :cycleNumber ORDER BY dayNumber ASC")
  suspend fun getChallengeDaysForCycle(cycleNumber: Int): List<WeeklyChallengeDayEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChallengeDays(days: List<WeeklyChallengeDayEntity>)

  @Update
  suspend fun updateChallengeDay(day: WeeklyChallengeDayEntity)

  @Query("DELETE FROM weekly_challenge_days")
  suspend fun clearChallengeDays()

  @Transaction
  suspend fun claimActivityCompletionAtomic(
    activityId: String,
    claimToken: String,
    nowMillis: Long,
    todayEpochDay: Long,
  ): ClaimActivityResult {
    if (countCompletionsByToken(claimToken) > 0) {
      return ClaimActivityResult.AlreadyClaimed
    }
    val activity = getActivityById(activityId) ?: return ClaimActivityResult.NotFound

    val insertedCompletion = insertCompletion(
      CompletionRecordEntity(
        activityId = activity.id,
        activityTitleEn = activity.titleEn,
        activityTitleBn = activity.titleBn,
        coinsEarned = activity.coinReward,
        completedEpochDay = todayEpochDay,
        completedAtMillis = nowMillis,
        claimToken = claimToken,
      )
    )
    if (insertedCompletion == -1L) {
      return ClaimActivityResult.AlreadyClaimed
    }

    insertCoinTransaction(
      CoinTransactionEntity(
        amount = activity.coinReward,
        type = "ACTIVITY",
        titleEn = activity.titleEn,
        titleBn = activity.titleBn,
        dedupKey = "activity_claim:$claimToken",
        createdAtMillis = nowMillis,
      )
    )
    clearProgressForActivity(activityId)

    // Update Weekly Challenge state using consistent calendar epoch day
    val config = getChallengeConfigNow()
    var dailyCoinsAwarded = 0
    var awardedDayNumber: Int? = null
    var reachedDay7 = false

    if (config != null) {
      var cycleNum = getCurrentCycleNumber()
      var days = getChallengeDaysForCycle(cycleNum)
      if (days.isEmpty()) {
        days = (1..7).map { WeeklyChallengeDayEntity(cycleNumber = cycleNum, dayNumber = it) }
        insertChallengeDays(days)
      }

      val lastCompleted = days.lastOrNull { it.completedEpochDay != null }
      val alreadyClaimedToday = days.any { it.completedEpochDay == todayEpochDay }

      // Check missed-day reset rule if enabled and previous cycle wasn't finished
      val all7Done = days.count { it.completedEpochDay != null } >= 7
      if (!alreadyClaimedToday && !all7Done && config.resetOnMissedDay && lastCompleted != null) {
        val gap = todayEpochDay - (lastCompleted.completedEpochDay ?: todayEpochDay)
        if (gap > 1L) {
          cycleNum += 1
          days = (1..7).map { WeeklyChallengeDayEntity(cycleNumber = cycleNum, dayNumber = it) }
          insertChallengeDays(days)
        }
      }

      if (!alreadyClaimedToday) {
        val nextDay = days.firstOrNull { it.completedEpochDay == null }
        if (nextDay != null) {
          dailyCoinsAwarded = config.dailyCoinReward
          awardedDayNumber = nextDay.dayNumber
          updateChallengeDay(
            nextDay.copy(
              completedEpochDay = todayEpochDay,
              coinsAwarded = dailyCoinsAwarded,
              claimedAtMillis = nowMillis,
            )
          )
          insertCoinTransaction(
            CoinTransactionEntity(
              amount = dailyCoinsAwarded,
              type = "DAILY_CHALLENGE",
              titleEn = "Challenge Day ${nextDay.dayNumber} Bonus",
              titleBn = "চ্যালেঞ্জ দিন ${nextDay.dayNumber} বোনাস",
              dedupKey = "challenge_day:${cycleNum}:${nextDay.dayNumber}:$todayEpochDay",
              createdAtMillis = nowMillis,
            )
          )
          if (nextDay.dayNumber == 7) {
            reachedDay7 = true
          }
        }
      }
    }

    return ClaimActivityResult.Success(
      activityCoins = activity.coinReward,
      dailyChallengeCoins = dailyCoinsAwarded,
      completedChallengeDay = awardedDayNumber,
      unlockedDay7Grand = reachedDay7,
    )
  }

  @Transaction
  suspend fun redeemRewardAtomic(rewardId: String, nowMillis: Long): RedeemRewardResult {
    val reward = getRewardById(rewardId) ?: return RedeemRewardResult.NotFound
    val balance = getCoinBalanceNow()
    if (balance < reward.coinCost) {
      return RedeemRewardResult.InsufficientCoins(reward.coinCost - balance)
    }
    insertCoinTransaction(
      CoinTransactionEntity(
        amount = -reward.coinCost,
        type = "REWARD_REDEEM",
        titleEn = "Unlocked: ${reward.titleEn}",
        titleBn = "আনলক: ${reward.titleBn}",
        dedupKey = "reward_redeem:${reward.id}:$nowMillis",
        createdAtMillis = nowMillis,
      )
    )
    insertRewardClaim(
      RewardClaimEntity(
        rewardId = reward.id,
        rewardTitleEn = reward.titleEn,
        rewardTitleBn = reward.titleBn,
        imageKey = reward.imageKey,
        coinsSpent = reward.coinCost,
        claimedAtMillis = nowMillis,
      )
    )
    return RedeemRewardResult.Success(reward.titleEn, reward.titleBn)
  }

  @Transaction
  suspend fun claimDay7GrandRewardAtomic(nowMillis: Long): ClaimGrandRewardResult {
    val config = getChallengeConfigNow() ?: return ClaimGrandRewardResult.NotEligible
    val cycleNum = getCurrentCycleNumber()
    val days = getChallengeDaysForCycle(cycleNum)
    val day7 = days.firstOrNull { it.dayNumber == 7 } ?: return ClaimGrandRewardResult.NotEligible
    if (day7.completedEpochDay == null || day7.isGrandRewardClaimed) {
      return ClaimGrandRewardResult.NotEligible
    }

    updateChallengeDay(day7.copy(isGrandRewardClaimed = true))
    insertCoinTransaction(
      CoinTransactionEntity(
        amount = config.day7GrandCoins,
        type = "WEEKLY_GRAND",
        titleEn = config.day7GrandTitleEn,
        titleBn = config.day7GrandTitleBn,
        dedupKey = "weekly_grand:$cycleNum",
        createdAtMillis = nowMillis,
      )
    )
    insertRewardClaim(
      RewardClaimEntity(
        rewardId = "weekly_grand_cycle_$cycleNum",
        rewardTitleEn = config.day7GrandTitleEn,
        rewardTitleBn = config.day7GrandTitleBn,
        imageKey = "img_reward_chest",
        coinsSpent = 0,
        claimedAtMillis = nowMillis,
      )
    )

    // Start fresh next cycle from Day 1
    val nextCycle = cycleNum + 1
    insertChallengeDays((1..7).map { WeeklyChallengeDayEntity(cycleNumber = nextCycle, dayNumber = it) })
    return ClaimGrandRewardResult.Success(config.day7GrandCoins, nextCycle)
  }

  @Transaction
  suspend fun seedDatabaseIfEmptyAtomic(
    categories: List<CategoryEntity>,
    activities: List<ActivityEntity>,
    steps: List<ActivityStepEntity>,
    rewards: List<RewardItemEntity>,
    challengeConfig: WeeklyChallengeConfigEntity,
  ) {
    if (countCategories() == 0) {
      insertCategories(categories)
      insertActivities(activities)
      insertSteps(steps)
      insertRewards(rewards)
      upsertChallengeConfig(challengeConfig)
      insertChallengeDays((1..7).map { WeeklyChallengeDayEntity(cycleNumber = 1, dayNumber = it) })
    } else {
      if (getChallengeConfigNow() == null) {
        upsertChallengeConfig(challengeConfig)
      }
      val cycleNum = getCurrentCycleNumber()
      if (getChallengeDaysForCycle(cycleNum).isEmpty()) {
        insertChallengeDays((1..7).map { WeeklyChallengeDayEntity(cycleNumber = cycleNum, dayNumber = it) })
      }
    }
  }

  @Transaction
  suspend fun clearPersonalProgressAndHistoryAtomic() {
    clearAllProgress()
    clearCompletionRecords()
    clearCoinTransactions()
    clearRewardClaims()
    clearChallengeDays()
    insertChallengeDays((1..7).map { WeeklyChallengeDayEntity(cycleNumber = 1, dayNumber = it) })
  }
}
