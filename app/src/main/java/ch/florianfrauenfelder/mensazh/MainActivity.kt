package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.favoriteMensasFlow
import ch.florianfrauenfelder.mensazh.services.providers.showMenusInGermanToLanguage
import ch.florianfrauenfelder.mensazh.services.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.ui.App
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val viewModelFactory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(
          assetService = AssetService(this@MainActivity.assets),
          cacheService = CacheService(this@MainActivity.cacheDir),
          favoriteMensas = this@MainActivity.favoriteMensasFlow,
          initialLanguage = this@MainActivity.showMenusInGermanFlow.map { it.showMenusInGermanToLanguage },
          saveLanguage = { this@MainActivity.saveShowMenusInGerman(it.showMenusInGerman) },
        ) as T
      }
    }

    setContent {
      val viewModel: AppViewModel = viewModel(factory = viewModelFactory)

      LaunchedEffect(Unit) {
        viewModel.refresh()
      }

      MensaZHTheme {
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
}
