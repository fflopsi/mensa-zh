package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.ui.App

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    setContent {
      val viewModel: AppViewModel by viewModels { AppViewModel.Factory }

      LaunchedEffect(Unit) {
        viewModel.refresh()
      }

      App(
        destination = viewModel.destination,
        setDestination = viewModel::setDestination,
        weekday = viewModel.weekday,
        setWeekday = viewModel::setWeekday,
        locations = viewModel.locations,
        language = viewModel.language,
        setLanguage = viewModel::setLanguage,
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refresh(ignoreCache = true) },
      )
    }
  }
}
