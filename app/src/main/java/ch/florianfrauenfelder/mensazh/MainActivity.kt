package ch.florianfrauenfelder.mensazh

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.data.local.datastore.themeFlow
import ch.florianfrauenfelder.mensazh.ui.App
import ch.florianfrauenfelder.mensazh.ui.MainViewModel
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

    setContent {
      val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)

      LaunchedEffect(Unit) {
        viewModel.deleteExpired()
      }

      val params by viewModel.params.collectAsStateWithLifecycle()
      val locations by viewModel.locations.collectAsStateWithLifecycle()

      App(
        destination = params.destination,
        setDestination = viewModel::setNew,
        weekday = params.weekday,
        setWeekday = viewModel::setNew,
        locations = locations,
        isRefreshing = viewModel.isRefreshing.collectAsStateWithLifecycle().value,
        onRefresh = viewModel::forceRefresh,
        clearCache = viewModel::clearCache,
      )
    }
  }
}
