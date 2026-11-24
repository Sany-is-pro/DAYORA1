package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PureBlack
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Cinematic logo reveal on pure black background.
 * Strictly no loading bar, percentage, loading text, tagline, wordmark, or extra decorations.
 */
@Composable
fun SplashScreen(
  isDataReady: Boolean,
  reducedMotion: Boolean,
  onSplashFinished: () -> Unit,
) {
  val logoAlpha = remember { Animatable(0f) }
  val logoScale = remember { Animatable(0.86f) }
  val glowAlpha = remember { Animatable(0f) }

  val currentIsDataReady by rememberUpdatedState(isDataReady)
  val currentOnSplashFinished by rememberUpdatedState(onSplashFinished)
  var hasNavigated by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    if (reducedMotion) {
      logoAlpha.snapTo(1f)
      logoScale.snapTo(1f)
      glowAlpha.snapTo(0.55f)
      delay(800)
    } else {
      delay(180)
      coroutineScope {
        launch {
          glowAlpha.animateTo(
            targetValue = 0.72f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
          )
        }
        launch {
          logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
          )
        }
        launch {
          logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
          )
        }
      }
      delay(650)
    }

    withTimeoutOrNull(5000L) {
      snapshotFlow { currentIsDataReady }.first { it }
    }

    if (!hasNavigated) {
      hasNavigated = true
      currentOnSplashFinished()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(PureBlack),
    contentAlignment = Alignment.Center,
  ) {
    // Subtle purple & pink ambient glow behind the official DAYORA symbol
    Canvas(modifier = Modifier.size(340.dp)) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFFA77BEA).copy(alpha = glowAlpha.value * 0.45f),
            Color(0xFFF47DB7).copy(alpha = glowAlpha.value * 0.25f),
            Color.Transparent,
          )
        )
      )
    }

    Image(
      painter = painterResource(id = R.drawable.dayora_logo),
      contentDescription = "DAYORA",
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .size(164.dp)
        .graphicsLayer {
          alpha = logoAlpha.value
          scaleX = logoScale.value
          scaleY = logoScale.value
        },
    )
  }
}
