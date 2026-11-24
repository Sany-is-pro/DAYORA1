package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    CategoryEntity::class,
    ActivityEntity::class,
    ActivityStepEntity::class,
    ActivityProgressEntity::class,
    CompletionRecordEntity::class,
    CoinTransactionEntity::class,
    RewardItemEntity::class,
    RewardClaimEntity::class,
    WeeklyChallengeConfigEntity::class,
    WeeklyChallengeDayEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class DayoraDatabase : RoomDatabase() {
  abstract fun dayoraDao(): DayoraDao

  companion object {
    @Volatile
    private var INSTANCE: DayoraDatabase? = null

    fun getInstance(context: Context): DayoraDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DayoraDatabase::class.java,
          "dayora_offline.db",
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
