package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

enum class AppLanguage(val code: String) {
  ENGLISH("en"),
  BENGALI("bn");

  companion object {
    fun fromCode(code: String?): AppLanguage =
      entries.firstOrNull { it.code == code } ?: ENGLISH
  }
}

val PoppinsFontFamily = FontFamily(
  Font(R.font.poppins, FontWeight.Normal),
  Font(R.font.poppins, FontWeight.Medium),
  Font(R.font.poppins, FontWeight.SemiBold),
  Font(R.font.poppins, FontWeight.Bold),
)

val InterFontFamily = FontFamily(
  Font(R.font.inter, FontWeight.Normal),
  Font(R.font.inter, FontWeight.Medium),
  Font(R.font.inter, FontWeight.SemiBold),
)

val HindSiliguriFontFamily = FontFamily(
  Font(R.font.hind_siliguri, FontWeight.Normal),
  Font(R.font.hind_siliguri, FontWeight.Medium),
  Font(R.font.hind_siliguri, FontWeight.SemiBold),
  Font(R.font.hind_siliguri, FontWeight.Bold),
)

val NotoSansBengaliFontFamily = FontFamily(
  Font(R.font.noto_sans_bengali, FontWeight.Normal),
  Font(R.font.noto_sans_bengali, FontWeight.Medium),
  Font(R.font.noto_sans_bengali, FontWeight.SemiBold),
)

fun dayoraTypography(language: AppLanguage): Typography {
  val displayFamily =
    if (language == AppLanguage.BENGALI) HindSiliguriFontFamily else PoppinsFontFamily
  val bodyFamily =
    if (language == AppLanguage.BENGALI) NotoSansBengaliFontFamily else InterFontFamily

  return Typography(
    displayLarge = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 34.sp,
      lineHeight = 42.sp,
    ),
    headlineLarge = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 23.sp,
      lineHeight = 31.sp,
    ),
    headlineSmall = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 20.sp,
      lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 16.sp,
      lineHeight = 23.sp,
    ),
    titleSmall = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
      fontFamily = displayFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = 14.sp,
      lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 12.sp,
      lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
      fontFamily = bodyFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 14.sp,
    ),
  )
}
