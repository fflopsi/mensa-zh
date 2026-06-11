package ch.florianfrauenfelder.mensazh.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.AppContainer
import ch.florianfrauenfelder.mensazh.data.repository.PreferencesRepository
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  val themeSettings = preferencesRepository.themeSettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ThemeSettings(),
  )

  companion object {
    fun Factory(container: AppContainer) = viewModelFactory {
      initializer {
        AppViewModel(
          preferencesRepository = container.preferencesRepository,
        )
      }
    }
  }
}
