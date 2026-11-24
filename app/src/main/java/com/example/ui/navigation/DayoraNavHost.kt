package com.example.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.screens.ActivitiesBrowseScreen
import com.example.ui.screens.ActivityExperienceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NameEntryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WeeklyChallengeScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.YoutubeSubscribeScreen
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.viewmodel.DayoraAppUiState
import com.example.ui.viewmodel.DayoraViewModel

object DayoraRoutes {
  const val SPLASH = "splash"
  const val WELCOME = "welcome"
  const val NAME_ENTRY = "name_entry"
  const val YOUTUBE_SUB = "youtube_sub"
  const val MAIN = "main"
  const val CATEGORY = "category/{categoryId}"
  const val ACTIVITY = "activity/{activityId}"
  const val WEEKLY_CHALLENGE = "weekly_challenge"
  const val SETTINGS = "settings"
  const val ADMIN = "admin"

  fun category(categoryId: String) = "category/${Uri.encode(categoryId)}"
  fun activity(activityId: String) = "activity/${Uri.encode(activityId)}"
}

private data class BottomTabItem(
  val index: Int,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val tag: String,
)

@Composable
fun DayoraNavHost(
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
) {
  val navController = rememberNavController()
  var selectedMainTab by rememberSaveable { mutableIntStateOf(0) }

  NavHost(
    navController = navController,
    startDestination = DayoraRoutes.SPLASH,
  ) {
    composable(DayoraRoutes.SPLASH) {
      SplashScreen(
        isDataReady = state.isReady,
        reducedMotion = state.prefs.reducedMotion,
        onSplashFinished = {
          val dest = if (state.prefs.onboardingCompleted && state.prefs.fullName.isNotBlank()) {
            DayoraRoutes.MAIN
          } else {
            DayoraRoutes.WELCOME
          }
          navController.navigate(dest) {
            popUpTo(DayoraRoutes.SPLASH) { inclusive = true }
          }
        },
      )
    }

    composable(DayoraRoutes.WELCOME) {
      WelcomeScreen(
        currentLanguage = state.prefs.language,
        reducedMotion = state.prefs.reducedMotion,
        onLanguageChange = { viewModel.setLanguage(it) },
        onGetStarted = { navController.navigate(DayoraRoutes.NAME_ENTRY) },
      )
    }

    composable(DayoraRoutes.NAME_ENTRY) {
      NameEntryScreen(
        initialName = state.prefs.fullName,
        reducedMotion = state.prefs.reducedMotion,
        onSaveNameAndContinue = { name ->
          viewModel.saveFullName(name) {
            navController.navigate(DayoraRoutes.YOUTUBE_SUB)
          }
        },
      )
    }

    composable(DayoraRoutes.YOUTUBE_SUB) {
      YoutubeSubscribeScreen(
        reducedMotion = state.prefs.reducedMotion,
        onConfirmSubscribed = {
          viewModel.confirmYoutubeAndCompleteOnboarding {
            selectedMainTab = 0
            navController.navigate(DayoraRoutes.MAIN) {
              popUpTo(DayoraRoutes.WELCOME) { inclusive = true }
            }
          }
        },
      )
    }

    composable(DayoraRoutes.MAIN) {
      MainShell(
        selectedTab = selectedMainTab,
        onSelectTab = { selectedMainTab = it },
        state = state,
        viewModel = viewModel,
        onOpenCategory = { catId -> navController.navigate(DayoraRoutes.category(catId)) },
        onOpenActivity = { actId -> navController.navigate(DayoraRoutes.activity(actId)) },
        onOpenWeeklyChallenge = { navController.navigate(DayoraRoutes.WEEKLY_CHALLENGE) },
        onOpenSettings = { navController.navigate(DayoraRoutes.SETTINGS) },
        onAfterResetAll = {
          selectedMainTab = 0
          navController.navigate(DayoraRoutes.WELCOME) {
            popUpTo(DayoraRoutes.MAIN) { inclusive = true }
          }
        },
      )
    }

    composable(
      route = DayoraRoutes.CATEGORY,
      arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
    ) { backStack ->
      val catId = backStack.arguments?.getString("categoryId")?.let { Uri.decode(it) }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.statusBars)
          .windowInsetsPadding(WindowInsets.navigationBars),
      ) {
        ActivitiesBrowseScreen(
          state = state,
          initialCategoryId = catId,
          onBack = { navController.popBackStack() },
          onOpenActivity = { actId -> navController.navigate(DayoraRoutes.activity(actId)) },
        )
      }
    }

    composable(
      route = DayoraRoutes.ACTIVITY,
      arguments = listOf(navArgument("activityId") { type = NavType.StringType }),
    ) { backStack ->
      val actId = backStack.arguments?.getString("activityId")?.let { Uri.decode(it) }.orEmpty()
      ActivityExperienceScreen(
        activityId = actId,
        viewModel = viewModel,
        language = state.prefs.language,
        coinBalance = state.coinBalance,
        reducedMotion = state.prefs.reducedMotion,
        onClose = { navController.popBackStack() },
        onOpenWeeklyChallenge = {
          navController.popBackStack()
          navController.navigate(DayoraRoutes.WEEKLY_CHALLENGE)
        },
      )
    }

    composable(DayoraRoutes.WEEKLY_CHALLENGE) {
      WeeklyChallengeScreen(
        state = state,
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
      )
    }

    composable(DayoraRoutes.SETTINGS) {
      SettingsScreen(
        state = state,
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
        onOpenAdmin = { navController.navigate(DayoraRoutes.ADMIN) },
        onResetOnboardingDone = {
          navController.navigate(DayoraRoutes.WELCOME) {
            popUpTo(DayoraRoutes.MAIN) { inclusive = true }
          }
        },
        onResetAllDone = {
          selectedMainTab = 0
          navController.navigate(DayoraRoutes.WELCOME) {
            popUpTo(DayoraRoutes.MAIN) { inclusive = true }
          }
        },
      )
    }

    composable(DayoraRoutes.ADMIN) {
      AdminScreen(
        state = state,
        viewModel = viewModel,
        onBack = { navController.popBackStack() },
      )
    }
  }
}

@Composable
private fun MainShell(
  selectedTab: Int,
  onSelectTab: (Int) -> Unit,
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onOpenCategory: (String) -> Unit,
  onOpenActivity: (String) -> Unit,
  onOpenWeeklyChallenge: () -> Unit,
  onOpenSettings: () -> Unit,
  onAfterResetAll: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current

  val tabs = listOf(
    BottomTabItem(0, strings.navHome, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    BottomTabItem(1, strings.navActivities, Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_activities"),
    BottomTabItem(2, strings.navRewards, Icons.Rounded.CardGiftcard, Icons.Outlined.CardGiftcard, "nav_rewards"),
    BottomTabItem(3, strings.navProfile, Icons.Rounded.Person, Icons.Outlined.Person, "nav_profile"),
  )

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    if (useRail) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.statusBars)
          .windowInsetsPadding(WindowInsets.navigationBars),
      ) {
        NavigationRail(containerColor = colors.cardSurface) {
          tabs.forEach { tab ->
            val selected = selectedTab == tab.index
            NavigationRailItem(
              selected = selected,
              onClick = { onSelectTab(tab.index) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                )
              },
              label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
              modifier = Modifier.testTag(tab.tag),
            )
          }
        }
        Box(modifier = Modifier.weight(1f)) {
          MainTabContent(
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
            state = state,
            viewModel = viewModel,
            onOpenCategory = onOpenCategory,
            onOpenActivity = onOpenActivity,
            onOpenWeeklyChallenge = onOpenWeeklyChallenge,
            onOpenSettings = onOpenSettings,
            onAfterResetAll = onAfterResetAll,
          )
        }
      }
    } else {
      Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
          NavigationBar(
            containerColor = colors.cardSurface,
            tonalElevation = 6.dp,
            windowInsets = WindowInsets.navigationBars,
          ) {
            tabs.forEach { tab ->
              val selected = selectedTab == tab.index
              NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab.index) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = {
                  Text(
                    text = tab.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primaryAccent,
                  selectedTextColor = colors.primaryAccent,
                  indicatorColor = colors.lavenderTint,
                  unselectedIconColor = colors.secondaryText,
                  unselectedTextColor = colors.secondaryText,
                ),
                modifier = Modifier.testTag(tab.tag),
              )
            }
          }
        },
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        ) {
          MainTabContent(
            selectedTab = selectedTab,
            onSelectTab = onSelectTab,
            state = state,
            viewModel = viewModel,
            onOpenCategory = onOpenCategory,
            onOpenActivity = onOpenActivity,
            onOpenWeeklyChallenge = onOpenWeeklyChallenge,
            onOpenSettings = onOpenSettings,
            onAfterResetAll = onAfterResetAll,
          )
        }
      }
    }
  }
}

@Composable
private fun MainTabContent(
  selectedTab: Int,
  onSelectTab: (Int) -> Unit,
  state: DayoraAppUiState,
  viewModel: DayoraViewModel,
  onOpenCategory: (String) -> Unit,
  onOpenActivity: (String) -> Unit,
  onOpenWeeklyChallenge: () -> Unit,
  onOpenSettings: () -> Unit,
  onAfterResetAll: () -> Unit,
) {
  when (selectedTab) {
    0 -> HomeScreen(
      state = state,
      onOpenWeeklyChallenge = onOpenWeeklyChallenge,
      onOpenCategory = onOpenCategory,
      onOpenActivity = onOpenActivity,
      onNavigateTabRewards = { onSelectTab(2) },
      onNavigateTabProfile = { onSelectTab(3) },
      onNavigateTabActivities = { onSelectTab(1) },
    )
    1 -> ActivitiesBrowseScreen(
      state = state,
      onOpenActivity = onOpenActivity,
    )
    2 -> RewardsScreen(
      state = state,
      viewModel = viewModel,
    )
    else -> ProfileScreen(
      state = state,
      viewModel = viewModel,
      onOpenSettings = onOpenSettings,
      onAfterResetAll = onAfterResetAll,
    )
  }
}
