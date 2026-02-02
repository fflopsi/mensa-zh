package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.MensaApplication
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val mensaRepository: MensaRepository,
) : ViewModel() {
  val baseLocations = mensaRepository.baseLocations

  fun clearCache() = viewModelScope.launch { mensaRepository.clearCache() }

  companion object {
    val Factory = viewModelFactory {
      initializer {
        val application = checkNotNull(
          this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MensaApplication,
        )
        SettingsViewModel(mensaRepository = application.container.mensaRepository)
      }
    }
  }
}
