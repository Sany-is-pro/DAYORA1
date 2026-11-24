package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.stringsFor
import com.example.ui.navigation.DayoraNavHost
import com.example.ui.theme.DayoraTheme
import com.example.ui.viewmodel.DayoraViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: DayoraViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val state by viewModel.uiState.collectAsStateWithLifecycle()
      val strings = stringsFor(state.prefs.language)

      DayoraTheme(
        themeMode = state.prefs.themeMode,
        language = state.prefs.language,
      ) {
        CompositionLocalProvider(LocalAppStrings provides strings) {
          DayoraNavHost(state = state, viewModel = viewModel)
        }
      }
    }
  }
}
