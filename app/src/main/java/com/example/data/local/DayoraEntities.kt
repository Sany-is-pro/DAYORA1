package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val nameEn: String,
  val nameBn: String,
  val descriptionEn: String,
  val descriptionBn: String,
  val imageKey: String,
  val accentColorHex: Long,
  val sortOrder: Int,
  val isEnabled: Boolean = true,
)

@Entity(
  tableName = "activities",
  foreignKeys = [
    ForeignKey(
      entity = CategoryEntity::class,
      parentColumns = ["id"],
      childColumns = ["categoryId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index("categoryId")],
)
data class ActivityEntity(
  @PrimaryKey val id: String,
  val categoryId: String,
  val titleEn: String,
  val titleBn: String,
  val descriptionEn: String,
  val descriptionBn: String,
  val coverImageKey: String,
  val durationMinutes: Int,
  val coinReward: Int,
  val isFeatured: Boolean = false,
  val isEnabled: Boolean = true,
  val sortOrder: Int = 0,
)

@Entity(
  tableName = "activity_steps",
  foreignKeys = [
    ForeignKey(
      entity = ActivityEntity::class,
      parentColumns = ["id"],
      childColumns = ["activityId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index("activityId")],
)
data class ActivityStepEntity(
  @PrimaryKey val id: String,
  val activityId: String,
  val stepNumber: Int,
  val titleEn: String,
  val titleBn: String,
  val instructionEn: String,
  val instructionBn: String,
  val imageKey: String,
)

@Entity(tableName = "activity_progress")
data class ActivityProgressEntity(
  @PrimaryKey val activityId: String,
  val currentStepIndex: Int,
  val totalSteps: Int,
  val isStarted: Boolean,
  val sessionToken: String,
  val lastUpdatedMillis: Long,
)

@Entity(
  tableName = "completion_records",
  indices = [Index(value = ["claimToken"], unique = true), Index("activityId"), Index("completedEpochDay")],
)
data class CompletionRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val activityId: String,
  val activityTitleEn: String,
  val activityTitleBn: String,
  val coinsEarned: Int,
  val completedEpochDay: Long,
  val completedAtMillis: Long,
  val claimToken: String,
)

@Entity(
  tableName = "coin_transactions",
  indices = [Index(value = ["dedupKey"], unique = true)],
)
data class CoinTransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val amount: Int,
  val type: String, // ACTIVITY, DAILY_CHALLENGE, WEEKLY_GRAND, REWARD_REDEEM
  val titleEn: String,
  val titleBn: String,
  val dedupKey: String,
  val createdAtMillis: Long,
)

@Entity(tableName = "reward_items")
data class RewardItemEntity(
  @PrimaryKey val id: String,
  val titleEn: String,
  val titleBn: String,
  val descriptionEn: String,
  val descriptionBn: String,
  val imageKey: String,
  val coinCost: Int,
  val isDay7GrandReward: Boolean = false,
  val isEnabled: Boolean = true,
  val sortOrder: Int = 0,
)

@Entity(tableName = "reward_claims")
data class RewardClaimEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val rewardId: String,
  val rewardTitleEn: String,
  val rewardTitleBn: String,
  val imageKey: String,
  val coinsSpent: Int,
  val claimedAtMillis: Long,
)

@Entity(tableName = "weekly_challenge_config")
data class WeeklyChallengeConfigEntity(
  @PrimaryKey val id: Int = 1,
  val titleEn: String,
  val titleBn: String,
  val ruleDescriptionEn: String,
  val ruleDescriptionBn: String,
  val dailyCoinReward: Int,
  val day7GrandCoins: Int,
  val day7GrandTitleEn: String,
  val day7GrandTitleBn: String,
  val resetOnMissedDay: Boolean = true,
)

@Entity(
  tableName = "weekly_challenge_days",
  primaryKeys = ["cycleNumber", "dayNumber"],
)
data class WeeklyChallengeDayEntity(
  val cycleNumber: Int,
  val dayNumber: Int,
  val completedEpochDay: Long? = null,
  val coinsAwarded: Int = 0,
  val claimedAtMillis: Long? = null,
  val isGrandRewardClaimed: Boolean = false,
)
