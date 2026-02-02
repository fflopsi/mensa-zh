package ch.florianfrauenfelder.mensazh

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import ch.florianfrauenfelder.mensazh.data.local.datastore.themeFlow
import ch.florianfrauenfelder.mensazh.ui.MensaApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    lifecycleScope.launch {
      themeFlow.collect {
        val dark = when (it) {
          1 -> false
          2 -> true
          else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
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

    setContent { MensaApp() }
  }
}
