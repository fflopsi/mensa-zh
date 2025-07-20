package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.MensaViewModel
import ch.florianfrauenfelder.mensazh.services.LocationRepository
import ch.florianfrauenfelder.mensazh.services.favoriteMensasFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val repository = LocationRepository.getInstance(this)
    val viewModelFactory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MensaViewModel(repository, this@MainActivity.favoriteMensasFlow) as T
      }
    }
    enableEdgeToEdge()
    setContent {
      val viewModel: MensaViewModel = viewModel(factory = viewModelFactory)
      MensaZHApp(viewModel = viewModel)
    }
  }
}
