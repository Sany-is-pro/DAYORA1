package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.CandyJellyButton
import com.example.ui.components.ModernSectionCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalDayoraColors

const val DAYORA_YOUTUBE_URL = "https://youtube.com/@universalsany?si=A7VY8vR2TCpAkOuC"

fun openYouTubeChannel(context: Context) {
  val uri = Uri.parse(DAYORA_YOUTUBE_URL)
  val ytIntent = Intent(Intent.ACTION_VIEW, uri).apply {
    setPackage("com.google.android.youtube")
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
  try {
    context.startActivity(ytIntent)
  } catch (_: ActivityNotFoundException) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(browserIntent) }
  }
}

@Composable
fun WelcomeScreen(
  currentLanguage: AppLanguage,
  reducedMotion: Boolean,
  onLanguageChange: (AppLanguage) -> Unit,
  onGetStarted: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            colors.lavenderTint,
            MaterialTheme.colorScheme.background,
            colors.softPink.copy(alpha = 0.65f),
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
  ) {
    // Language Switcher in Top-Right
    Row(
      modifier = Modifier.align(Alignment.TopEnd),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = currentLanguage == AppLanguage.ENGLISH,
        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
        label = { Text("English") },
        modifier = Modifier.testTag("welcome_lang_en"),
      )
      FilterChip(
        selected = currentLanguage == AppLanguage.BENGALI,
        onClick = { onLanguageChange(AppLanguage.BENGALI) },
        label = { Text("বাংলা") },
        modifier = Modifier.testTag("welcome_lang_bn"),
      )
    }

    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .widthIn(max = 520.dp)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .size(168.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                colors.primaryAccent.copy(alpha = 0.28f),
                colors.pinkAccent.copy(alpha = 0.14f),
                Color.Transparent,
              )
            )
          ),
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = R.drawable.dayora_logo),
          contentDescription = "DAYORA Logo",
          contentScale = ContentScale.Fit,
          modifier = Modifier.size(134.dp),
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = strings.welcomeTitle,
        style = MaterialTheme.typography.headlineLarge,
        color = colors.mainText,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = strings.welcomeSubtitle,
        style = MaterialTheme.typography.bodyLarge,
        color = colors.secondaryText,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(42.dp))

      CandyJellyButton(
        text = strings.getStarted,
        onClick = onGetStarted,
        reducedMotion = reducedMotion,
        leadingIcon = Icons.Rounded.AutoAwesome,
        modifier = Modifier.fillMaxWidth(),
        testTag = "welcome_get_started_button",
      )
    }
  }
}

@Composable
fun NameEntryScreen(
  initialName: String,
  reducedMotion: Boolean,
  onSaveNameAndContinue: (String) -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  var nameInput by remember(initialName) { mutableStateOf(initialName) }
  var showError by remember { mutableStateOf(false) }

  fun submit() {
    val cleaned = nameInput.trim()
    if (cleaned.isEmpty()) {
      showError = true
    } else {
      showError = false
      onSaveNameAndContinue(cleaned)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.dayora_logo),
        contentDescription = "DAYORA",
        modifier = Modifier.size(78.dp),
      )
      Spacer(modifier = Modifier.height(22.dp))

      Text(
        text = strings.namePromptTitle,
        style = MaterialTheme.typography.headlineMedium,
        color = colors.mainText,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(24.dp))

      ModernSectionCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          OutlinedTextField(
            value = nameInput,
            onValueChange = {
              nameInput = it
              if (showError) showError = false
            },
            label = { Text(strings.fullNameLabel) },
            placeholder = { Text(strings.fullNamePlaceholder) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = colors.primaryAccent,
              )
            },
            singleLine = true,
            isError = showError,
            keyboardOptions = KeyboardOptions(
              capitalization = KeyboardCapitalization.Words,
              imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = colors.primaryAccent,
              unfocusedBorderColor = colors.subtleBorder,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("full_name_input"),
          )

          AnimatedVisibility(visible = showError) {
            Text(
              text = strings.nameRequiredError,
              style = MaterialTheme.typography.bodySmall,
              color = colors.danger,
              modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      CandyJellyButton(
        text = strings.continueLabel,
        onClick = { submit() },
        reducedMotion = reducedMotion,
        modifier = Modifier.fillMaxWidth(),
        testTag = "name_continue_button",
      )
    }
  }
}

@Composable
fun YoutubeSubscribeScreen(
  reducedMotion: Boolean,
  onConfirmSubscribed: () -> Unit,
) {
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val context = LocalContext.current
  var openedChannel by remember { mutableStateOf(false) }
  var confirmedBanner by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          listOf(
            MaterialTheme.colorScheme.background,
            colors.softPink.copy(alpha = 0.45f),
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 520.dp)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        painter = painterResource(id = R.drawable.dayora_logo),
        contentDescription = "DAYORA",
        modifier = Modifier.size(84.dp),
      )
      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = strings.youtubeTitle,
        style = MaterialTheme.typography.headlineMedium,
        color = colors.mainText,
        textAlign = TextAlign.Center,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = strings.youtubeDescription,
        style = MaterialTheme.typography.bodyLarge,
        color = colors.secondaryText,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(24.dp))

      ModernSectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.cardSurface,
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.lavenderTint,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.PlayCircle,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(30.dp),
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = strings.channelLabel,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.secondaryText,
                )
                Text(
                  text = strings.channelName,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = colors.mainText,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          CandyJellyButton(
            text = strings.subscribeButton,
            onClick = {
              openedChannel = true
              openYouTubeChannel(context)
            },
            reducedMotion = reducedMotion,
            topColor = Color(0xFFFF5263),
            bottomColor = Color(0xFFD32F2F),
            lipColor = Color(0xFF9A1B1B),
            leadingIcon = Icons.Rounded.PlayCircle,
            modifier = Modifier.fillMaxWidth(),
            testTag = "youtube_subscribe_button",
          )

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = strings.youtubeHonorNote,
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      AnimatedVisibility(visible = confirmedBanner) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = colors.successMint.copy(alpha = 0.25f),
          border = BorderStroke(1.dp, colors.successMint),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = colors.successMintDeep,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.youtubeHonorConfirmed,
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = colors.mainText,
            )
          }
        }
      }

      Button(
        onClick = {
          confirmedBanner = true
          onConfirmSubscribed()
        },
        modifier = Modifier
          .fillMaxWidth()
          .minimumInteractiveComponentSize()
          .testTag("youtube_confirm_continue_button"),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (openedChannel) colors.primaryAccent else colors.lavenderTint,
          contentColor = if (openedChannel) Color.White else colors.mainText,
        ),
      ) {
        Text(
          text = strings.iveSubscribedButton,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
          modifier = Modifier.padding(vertical = 8.dp),
        )
      }
    }
  }
}
