package com.example

import com.example.data.local.LocalDayClock
import com.example.data.local.SeedData
import com.example.data.local.UserPrefsSnapshot
import com.example.ui.i18n.stringsFor
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun seedData_hasCompleteBilingualCatalog() {
    assertEquals(10, SeedData.categories.size)
    assertEquals(20, SeedData.activities.size)
    assertEquals(5, SeedData.rewards.size)

    SeedData.categories.forEach { cat ->
      assertTrue(cat.nameEn.isNotBlank())
      assertTrue(cat.nameBn.isNotBlank())
      assertTrue(SeedData.activities.any { it.categoryId == cat.id })
    }

    SeedData.activities.forEach { act ->
      assertTrue(act.titleEn.isNotBlank())
      assertTrue(act.titleBn.isNotBlank())
      assertTrue(act.coinReward > 0)
      assertTrue(act.durationMinutes > 0)
      val steps = SeedData.steps.filter { it.activityId == act.id }
      assertTrue("Activity ${act.id} should have steps", steps.size >= 3)
    }
  }

  @Test
  fun localDayClock_incrementsAcross24Hours() {
    val t1 = 1_700_000_000_000L
    val d1 = LocalDayClock.currentEpochDay(t1)
    val d2 = LocalDayClock.currentEpochDay(t1 + 86_400_000L)
    assertEquals(d1 + 1L, d2)
  }

  @Test
  fun bilingualStrings_andEnums_roundTripCleanly() {
    val en = stringsFor(AppLanguage.ENGLISH)
    val bn = stringsFor(AppLanguage.BENGALI)
    assertEquals("DAYORA", en.appName)
    assertEquals("DAYORA", bn.appName)
    assertTrue(en.welcomeTitle != bn.welcomeTitle)
    assertTrue(en.changeProfilePhoto.isNotBlank() && bn.changeProfilePhoto.isNotBlank())
    assertTrue(en.cropPhotoTitle.isNotBlank() && bn.cropPhotoTitle.isNotBlank())
    assertEquals("", UserPrefsSnapshot().profileImageUri)
    assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("dark"))
    assertEquals(AppLanguage.BENGALI, AppLanguage.fromCode("bn"))
  }
}
