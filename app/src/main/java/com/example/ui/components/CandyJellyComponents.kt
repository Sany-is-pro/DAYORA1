package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.LocalDayoraColors
import com.example.ui.theme.RewardGold
import com.example.ui.theme.RewardGoldDeep
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Glossy Candy-Jelly interactive button for game-like moments (activity popups, step navigation,
 * claiming coins, unlocking rewards, and celebration triggers).
 */
@Composable
fun CandyJellyButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  reducedMotion: Boolean = false,
  topColor: Color = LocalDayoraColors.current.primaryAccent,
  bottomColor: Color = LocalDayoraColors.current.pinkAccent,
  lipColor: Color = Color(0xFF7B48C4),
  leadingIcon: ImageVector? = null,
  testTag: String? = null,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed && enabled && !reducedMotion) 0.95f else 1f,
    animationSpec = spring(dampingRatio = 0.5f, stiffness = 450f),
    label = "jelly_scale",
  )

  val activeTop = if (enabled) topColor else topColor.copy(alpha = 0.45f)
  val activeBottom = if (enabled) bottomColor else bottomColor.copy(alpha = 0.45f)
  val activeLip = if (enabled) lipColor else lipColor.copy(alpha = 0.35f)

  Box(
    modifier = modifier
      .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
      .minimumInteractiveComponentSize()
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
      }
      .shadow(
        elevation = if (enabled) 10.dp else 0.dp,
        shape = RoundedCornerShape(24.dp),
        ambientColor = activeBottom.copy(alpha = 0.4f),
        spotColor = activeTop.copy(alpha = 0.45f),
      )
      .clip(RoundedCornerShape(24.dp))
      .background(activeLip)
      .padding(bottom = 5.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(activeTop, activeBottom),
        )
      )
      .border(
        width = 1.5.dp,
        color = Color.White.copy(alpha = if (enabled) 0.55f else 0.25f),
        shape = RoundedCornerShape(22.dp),
      )
      .clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center,
  ) {
    // Top glossy specular highlight pill
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(horizontal = 18.dp, vertical = 4.dp)
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(50))
        .background(
          Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0.0f))
          )
        )
    )

    Row(
      modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (leadingIcon != null) {
        Icon(
          imageVector = leadingIcon,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * Cloud-like Candy-Jelly popup header with soft scalloped curves and subtle sparkles.
 */
@Composable
fun CandyCloudHeader(
  title: String,
  subtitle: String? = null,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val colors = LocalDayoraColors.current
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.linearGradient(
          colors = listOf(colors.primaryAccent, colors.pinkAccent)
        )
      )
      .drawBehind {
        // Dreamy overlapping cloud arcs along bottom edge
        val w = size.width
        val h = size.height
        val cloudColor = colors.popupSurface
        drawCircle(color = cloudColor, radius = w * 0.14f, center = Offset(w * 0.12f, h + w * 0.04f))
        drawCircle(color = cloudColor, radius = w * 0.18f, center = Offset(w * 0.38f, h + w * 0.05f))
        drawCircle(color = cloudColor, radius = w * 0.16f, center = Offset(w * 0.66f, h + w * 0.04f))
        drawCircle(color = cloudColor, radius = w * 0.14f, center = Offset(w * 0.90f, h + w * 0.05f))
        // Subtle glossy sparkles
        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 14f, center = Offset(w * 0.15f, h * 0.28f))
        drawCircle(color = Color.White.copy(alpha = 0.18f), radius = 9f, center = Offset(w * 0.82f, h * 0.24f))
      }
      .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 28.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        if (!subtitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
          )
        }
      }
      if (onClose != null) {
        IconButton(
          onClick = onClose,
          modifier = Modifier
            .testTag("popup_close_button")
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f)),
        ) {
          Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Close",
            tint = Color.White,
          )
        }
      }
    }
  }
}

/**
 * Shiny gold coin pill with subtle shine & count display.
 */
@Composable
fun CoinPill(
  coins: Int,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val colors = LocalDayoraColors.current
  Surface(
    modifier = modifier
      .then(
        if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
      ),
    shape = RoundedCornerShape(50),
    color = if (colors.isDark) colors.popupSurface else Color.White,
    border = BorderStroke(1.5.dp, RewardGold.copy(alpha = 0.7f)),
    shadowElevation = 4.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ShinyCoinIcon(size = 22.dp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = coins.toString(),
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = colors.mainText,
      )
    }
  }
}

/**
 * Custom Canvas glossy golden coin vector illustration.
 */
@Composable
fun ShinyCoinIcon(size: Dp = 28.dp, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.size(size)) {
    val r = this.size.minDimension / 2f
    val center = Offset(this.size.width / 2f, this.size.height / 2f)
    drawCircle(color = RewardGoldDeep, radius = r, center = center)
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF0A8), RewardGold, RewardGoldDeep),
        center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
        radius = r * 1.15f,
      ),
      radius = r * 0.88f,
      center = center,
    )
    // Inner 4-point star sparkle
    val starR = r * 0.42f
    val path = Path().apply {
      moveTo(center.x, center.y - starR)
      quadraticTo(center.x, center.y, center.x + starR, center.y)
      quadraticTo(center.x, center.y, center.x, center.y + starR)
      quadraticTo(center.x, center.y, center.x - starR, center.y)
      quadraticTo(center.x, center.y, center.x, center.y - starR)
      close()
    }
    drawPath(path, color = Color(0xFFFFFCE0))
  }
}

/**
 * Resolves built-in drawable keys (img_hero_dreamy, img_reward_chest, img_selfcare_glow,
 * img_wellness_active, img_evening_calm, dayora_logo) or locally copied gallery file URIs.
 */
@Composable
fun DayoraIllustration(
  imageKey: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
) {
  val drawableRes = remember(imageKey) {
    when (imageKey) {
      "img_hero_dreamy" -> R.drawable.img_hero_dreamy
      "img_reward_chest" -> R.drawable.img_reward_chest
      "img_selfcare_glow" -> R.drawable.img_selfcare_glow
      "img_wellness_active" -> R.drawable.img_wellness_active
      "img_evening_calm" -> R.drawable.img_evening_calm
      "img_cute_avatar" -> R.drawable.img_cute_avatar
      "dayora_logo" -> R.drawable.dayora_logo
      else -> null
    }
  }

  if (drawableRes != null) {
    Image(
      painter = painterResource(id = drawableRes),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  } else if (imageKey.startsWith("file://") || imageKey.startsWith("content://")) {
    AsyncImage(
      model = imageKey,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
      placeholder = painterResource(id = R.drawable.img_hero_dreamy),
      error = painterResource(id = R.drawable.img_hero_dreamy),
    )
  } else {
    Image(
      painter = painterResource(id = R.drawable.img_hero_dreamy),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = contentScale,
    )
  }
}

/**
 * Displays the user's cropped custom profile photo, or falls back to the cute default avatar.
 */
@Composable
fun ProfileAvatarImage(
  imageUri: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) {
  if (imageUri.isBlank()) {
    Image(
      painter = painterResource(id = R.drawable.img_cute_avatar),
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = ContentScale.Crop,
    )
  } else {
    AsyncImage(
      model = imageUri,
      contentDescription = contentDescription,
      modifier = modifier,
      contentScale = ContentScale.Crop,
      placeholder = painterResource(id = R.drawable.img_cute_avatar),
      error = painterResource(id = R.drawable.img_cute_avatar),
    )
  }
}

/**
 * Lightweight Canvas celebration burst of twinkling stars and coins.
 */
@Composable
fun StarCoinCelebrationBurst(
  reducedMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val progress = remember { Animatable(0f) }
  LaunchedEffect(reducedMotion) {
    if (reducedMotion) {
      progress.snapTo(1f)
    } else {
      progress.snapTo(0f)
      progress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
      )
    }
  }
  val infinite = rememberInfiniteTransition(label = "twinkle")
  val pulse by infinite.animateFloat(
    initialValue = 0.75f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "pulse",
  )

  val colors = LocalDayoraColors.current
  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxDist = size.minDimension * 0.44f * progress.value
    val palette = listOf(RewardGold, colors.pinkAccent, colors.primaryAccent, colors.successMint)
    for (i in 0 until 12) {
      val angle = (i * 30.0) * PI / 180.0
      val dist = if (i % 2 == 0) maxDist else maxDist * 0.72f
      val px = cx + (cos(angle) * dist).toFloat()
      val py = cy + (sin(angle) * dist).toFloat()
      val dotColor = palette[i % palette.size].copy(alpha = (1f - progress.value * 0.25f).coerceIn(0.2f, 0.95f))
      val radius = (if (i % 2 == 0) 9.dp.toPx() else 6.dp.toPx()) * (if (reducedMotion) 1f else pulse)
      drawCircle(color = dotColor, radius = radius, center = Offset(px, py))
    }
  }
}

@Composable
fun ModernSectionCard(
  modifier: Modifier = Modifier,
  containerColor: Color = LocalDayoraColors.current.cardSurface,
  borderColor: Color = LocalDayoraColors.current.subtleBorder,
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(22.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    shadowElevation = 2.dp,
  ) {
    content()
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  icon: ImageVector = Icons.Rounded.AutoAwesome,
  modifier: Modifier = Modifier,
) {
  val colors = LocalDayoraColors.current
  ModernSectionCard(
    modifier = modifier.fillMaxWidth(),
    containerColor = colors.lavenderTint.copy(alpha = 0.55f),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(colors.primaryAccent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = colors.primaryAccent,
          modifier = Modifier.size(26.dp),
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.secondaryText,
      )
    }
  }
}

@Composable
fun CoinStatChip(amount: Int, modifier: Modifier = Modifier) {
  val colors = LocalDayoraColors.current
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(RewardGold.copy(alpha = 0.18f))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Rounded.MonetizationOn,
      contentDescription = null,
      tint = RewardGoldDeep,
      modifier = Modifier.size(15.dp),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = "+$amount",
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
      color = colors.mainText,
    )
  }
}
