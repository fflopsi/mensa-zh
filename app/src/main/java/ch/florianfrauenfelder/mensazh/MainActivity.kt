package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.favoriteMensasFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
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

    enableEdgeToEdge()
    setContent {
      val viewModel: AppViewModel = viewModel(factory = viewModelFactory)
      MensaZHApp(viewModel = viewModel)
    }
  }
}
