package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ClaimActivityResult
import com.example.data.local.ClaimGrandRewardResult
import com.example.data.local.DayoraDao
import com.example.data.local.DayoraDatabase
import com.example.data.local.RedeemRewardResult
import com.example.data.local.SeedData
import com.example.data.local.UserPreferencesStore
import com.example.data.local.WeeklyChallengeDayEntity
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: DayoraDatabase
  private lateinit var dao: DayoraDao

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, DayoraDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.dayoraDao()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `app name matches DAYORA`() {
    assertEquals("DAYORA", context.getString(R.string.app_name))
  }

  @Test
  fun `seed data populates categories activities steps rewards and challenge`() = runBlocking {
    seedDatabase()

    val categories = dao.observeEnabledCategories().first()
    val activities = dao.observeEnabledActivities().first()
    val rewards = dao.observeEnabledRewards().first()
    val config = dao.getChallengeConfigNow()

    assertEquals(10, categories.size)
    assertEquals(20, activities.size)
    assertEquals(5, rewards.size)
    assertTrue(config != null)
  }

  @Test
  fun `claimActivityCompletion prevents duplicate coin rewards`() = runBlocking {
    seedDatabase()

    val token = "session-token-1"
    val first = dao.claimActivityCompletionAtomic(
      activityId = "act_morning_hair",
      claimToken = token,
      nowMillis = 1_700_000_000_000L,
      todayEpochDay = 20_000L,
    )
    assertTrue(first is ClaimActivityResult.Success)
    val s = first as ClaimActivityResult.Success
    assertEquals(25, s.activityCoins)
    assertEquals(20, s.dailyChallengeCoins)
    assertEquals(45, dao.getCoinBalanceNow())

    // Attempt duplicate claim with same token
    val duplicate = dao.claimActivityCompletionAtomic(
      activityId = "act_morning_hair",
      claimToken = token,
      nowMillis = 1_700_000_001_000L,
      todayEpochDay = 20_000L,
    )
    assertTrue(duplicate is ClaimActivityResult.AlreadyClaimed)
    assertEquals(45, dao.getCoinBalanceNow())
  }

  @Test
  fun `weekly challenge completes 7 consecutive days unlocks grand reward and starts new cycle`() = runBlocking {
    seedDatabase()

    val baseDay = 20_100L
    for (dayOffset in 0..6) {
      val result = dao.claimActivityCompletionAtomic(
        activityId = "act_morning_hair",
        claimToken = "tok-$dayOffset",
        nowMillis = 1_700_000_000_000L + dayOffset * 86_400_000L,
        todayEpochDay = baseDay + dayOffset,
      )
      assertTrue(result is ClaimActivityResult.Success)
      val success = result as ClaimActivityResult.Success
      assertEquals(dayOffset + 1, success.completedChallengeDay)
      assertEquals(dayOffset == 6, success.unlockedDay7Grand)
    }

    // Total after 7 days: 7*(25 activity + 20 daily) = 315 coins
    assertEquals(315, dao.getCoinBalanceNow())

    val grand = dao.claimDay7GrandRewardAtomic(nowMillis = 1_700_600_000_000L)
    assertTrue(grand is ClaimGrandRewardResult.Success)
    val grandSuccess = grand as ClaimGrandRewardResult.Success
    assertEquals(150, grandSuccess.grandCoins)
    assertEquals(2, grandSuccess.nextCycleNumber)
    assertEquals(465, dao.getCoinBalanceNow())

    // Next cycle starts fresh at 0 completed days
    val newCycleDays = dao.observeCurrentChallengeDays().first()
    assertEquals(7, newCycleDays.size)
    assertTrue(newCycleDays.all { it.cycleNumber == 2 && it.completedEpochDay == null })
  }

  @Test
  fun `redeemReward checks coin balance and deducts atomically`() = runBlocking {
    seedDatabase()

    // Balance is 0, Star Chest costs 60
    val insufficient = dao.redeemRewardAtomic("rew_star_chest", 1_700_000_000_000L)
    assertTrue(insufficient is RedeemRewardResult.InsufficientCoins)

    // Earn 25 + 20 = 45 on day 1, and 25 + 20 = 45 on day 2 => 90 coins
    dao.claimActivityCompletionAtomic("act_morning_hair", "t1", 1000L, 20001L)
    dao.claimActivityCompletionAtomic("act_morning_hair", "t2", 2000L, 20002L)
    assertEquals(90, dao.getCoinBalanceNow())

    val redeemed = dao.redeemRewardAtomic("rew_star_chest", 3000L)
    assertTrue(redeemed is RedeemRewardResult.Success)
    assertEquals(30, dao.getCoinBalanceNow())
    assertEquals(1, dao.observeRewardClaims().first().size)
  }

  @Test
  fun `user preferences store trims name persists settings and hashes admin passcode`() = runBlocking {
    val prefsStore = UserPreferencesStore(context)
    prefsStore.saveFullName("  Moinul Sany  ")
    prefsStore.confirmYoutubeAndCompleteOnboarding()
    prefsStore.setThemeMode(ThemeMode.DARK)
    prefsStore.setLanguage(AppLanguage.BENGALI)
    prefsStore.setAdminPasscode("dayora2026")

    val snapshot = prefsStore.prefsFlow.first()
    assertEquals("Moinul Sany", snapshot.fullName)
    assertTrue(snapshot.onboardingCompleted)
    assertTrue(snapshot.youtubeConfirmed)
    assertEquals(ThemeMode.DARK, snapshot.themeMode)
    assertEquals(AppLanguage.BENGALI, snapshot.language)
    assertTrue(snapshot.hasAdminPasscode)
    assertTrue(prefsStore.verifyAdminPasscode("dayora2026"))
    assertFalse(prefsStore.verifyAdminPasscode("wrong"))
  }

  private suspend fun seedDatabase() {
    dao.insertCategories(SeedData.categories)
    dao.insertActivities(SeedData.activities)
    dao.insertSteps(SeedData.steps)
    dao.insertRewards(SeedData.rewards)
    dao.upsertChallengeConfig(SeedData.defaultChallengeConfig)
    dao.insertChallengeDays((1..7).map { WeeklyChallengeDayEntity(cycleNumber = 1, dayNumber = it) })
  }
}
