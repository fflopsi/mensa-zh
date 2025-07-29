package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.favoriteMensasFlow
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.ui.ListDetailScreen
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import java.util.Date

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
        ) as T
      }
    }

    setContent {
      val viewModel: AppViewModel = viewModel(factory = viewModelFactory)
      val context = LocalContext.current

      val showMenusInGerman by context.showMenusInGermanFlow.collectAsStateWithLifecycle(
        initialValue = Prefs.Defaults.SHOW_MENUS_IN_GERMAN,
      )
      val language = remember(showMenusInGerman) {
        if (showMenusInGerman) MensaProvider.Language.German else MensaProvider.Language.English
      }

      LaunchedEffect(showMenusInGerman) {
        viewModel.refresh(date = Date(), language = language)
      }

      MensaZHTheme {
        ListDetailScreen(
          locations = viewModel.locations,
          showMenusInGerman = showMenusInGerman,
          isRefreshing = viewModel.isRefreshing,
          onRefresh = { viewModel.refresh(date = Date(), language = language, ignoreCache = true) },
        )
      }
    }
  }
}
