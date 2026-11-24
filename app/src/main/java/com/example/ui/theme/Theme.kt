package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode(val storageValue: String) {
  SYSTEM("system"),
  LIGHT("light"),
  DARK("dark");

  companion object {
    fun fromStorage(value: String?): ThemeMode =
      entries.firstOrNull { it.storageValue == value } ?: SYSTEM
  }
}

@Immutable
data class DayoraExtendedColors(
  val isDark: Boolean,
  val cardSurface: Color,
  val softPink: Color,
  val lavenderTint: Color,
  val popupSurface: Color,
  val mainText: Color,
  val secondaryText: Color,
  val primaryAccent: Color,
  val pinkAccent: Color,
  val rewardGold: Color,
  val rewardGoldDeep: Color,
  val successMint: Color,
  val successMintDeep: Color,
  val subtleBorder: Color,
  val danger: Color,
)

private val LightExtendedColors = DayoraExtendedColors(
  isDark = false,
  cardSurface = LightCard,
  softPink = LightSoftPink,
  lavenderTint = LightLavender,
  popupSurface = LightPopupSurface,
  mainText = LightMainText,
  secondaryText = LightSecondaryText,
  primaryAccent = LightPrimaryLavender,
  pinkAccent = LightPinkAccent,
  rewardGold = RewardGold,
  rewardGoldDeep = RewardGoldDeep,
  successMint = SuccessMint,
  successMintDeep = SuccessMintDeep,
  subtleBorder = LightOutline,
  danger = DangerCoral,
)

private val DarkExtendedColors = DayoraExtendedColors(
  isDark = true,
  cardSurface = DarkCard,
  softPink = DarkSoftPink,
  lavenderTint = DarkLavender,
  popupSurface = DarkPopup,
  mainText = DarkMainText,
  secondaryText = DarkSecondaryText,
  primaryAccent = DarkPrimaryPurple,
  pinkAccent = DarkPink,
  rewardGold = RewardGold,
  rewardGoldDeep = RewardGoldDeep,
  successMint = SuccessMint,
  successMintDeep = SuccessMintDeep,
  subtleBorder = DarkOutline,
  danger = DangerCoral,
)

val LocalDayoraColors = staticCompositionLocalOf { LightExtendedColors }

private val DayoraLightScheme = lightColorScheme(
  primary = LightPrimaryLavender,
  onPrimary = Color.White,
  primaryContainer = LightLavender,
  onPrimaryContainer = LightMainText,
  secondary = LightPinkAccent,
  onSecondary = Color.White,
  secondaryContainer = LightSoftPink,
  onSecondaryContainer = LightMainText,
  tertiary = SuccessMint,
  onTertiary = LightMainText,
  background = LightBackground,
  onBackground = LightMainText,
  surface = LightCard,
  onSurface = LightMainText,
  surfaceVariant = LightLavender,
  onSurfaceVariant = LightSecondaryText,
  outline = LightOutline,
  error = DangerCoral,
)

private val DayoraDarkScheme = darkColorScheme(
  primary = DarkPrimaryPurple,
  onPrimary = Color(0xFF1A0D2E),
  primaryContainer = DarkLavender,
  onPrimaryContainer = DarkMainText,
  secondary = DarkPink,
  onSecondary = Color(0xFF2B091D),
  secondaryContainer = DarkSoftPink,
  onSecondaryContainer = DarkMainText,
  tertiary = SuccessMint,
  onTertiary = Color(0xFF0A2A20),
  background = DarkBackground,
  onBackground = DarkMainText,
  surface = DarkCard,
  onSurface = DarkMainText,
  surfaceVariant = DarkPopup,
  onSurfaceVariant = DarkSecondaryText,
  outline = DarkOutline,
  error = DangerCoral,
)

val DayoraShapes = Shapes(
  extraSmall = RoundedCornerShape(10.dp),
  small = RoundedCornerShape(14.dp),
  medium = RoundedCornerShape(20.dp),
  large = RoundedCornerShape(26.dp),
  extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun DayoraTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  language: AppLanguage = AppLanguage.ENGLISH,
  content: @Composable () -> Unit,
) {
  val useDark = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }
  val colorScheme = if (useDark) DayoraDarkScheme else DayoraLightScheme
  val extended = if (useDark) DarkExtendedColors else LightExtendedColors

  CompositionLocalProvider(LocalDayoraColors provides extended) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = dayoraTypography(language),
      shapes = DayoraShapes,
      content = content,
    )
  }
}
