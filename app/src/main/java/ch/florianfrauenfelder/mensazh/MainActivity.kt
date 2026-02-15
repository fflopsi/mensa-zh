package ch.florianfrauenfelder.mensazh

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import ch.florianfrauenfelder.mensazh.ui.AppViewModel
import ch.florianfrauenfelder.mensazh.ui.MensaApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      (application as MensaApplication).container.preferencesRepository.themeSettings.collect {
        val dark = when (it.theme) {
          Theme.Auto -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
          Theme.Light -> false
          Theme.Dark -> true
        }
        enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(
            Color.TRANSPARENT,
            Color.TRANSPARENT,
          ) { dark },
          navigationBarStyle = SystemBarStyle.auto(
            Color.TRANSPARENT,
            Color.TRANSPARENT,
          ) { dark },
        )
      }
    }

    setContent {
      val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
      val theme by appViewModel.themeSettings.collectAsStateWithLifecycle()

      MensaApp(theme = theme)
    }
  }
}
