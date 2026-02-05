package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.MensaApplication
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import ch.florianfrauenfelder.mensazh.data.repository.PreferencesRepository
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.SelectionSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val mensaRepository: MensaRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  val visibilitySettings = preferencesRepository.visibilitySettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = VisibilitySettings(),
  )

  private val selectionSettings = preferencesRepository.selectionSettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SelectionSettings(),
  )

  val destinationSettings = preferencesRepository.destinationSettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DestinationSettings(),
  )

  val detailSettings = preferencesRepository.detailSettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DetailSettings(),
  )

  val themeSettings = preferencesRepository.themeSettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ThemeSettings(),
  )

  val baseLocations = mensaRepository.baseLocations

  val shownLocations = combine(baseLocations, selectionSettings) { locations, selection ->
    locations
      .filter { selection.shownLocations.contains(it.id) }
      .sortedBy { selection.shownLocations.indexOf(it.id) }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  val hiddenMensas = combine(shownLocations, selectionSettings) { locations, selection ->
    locations
      .flatMap { location -> location.mensas.map { it.mensa } }
      .filter {
        selection.hiddenMensas.contains(it.id) && !selection.favoriteMensas.contains(it.id)
      }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  val favoriteMensas = combine(baseLocations, selectionSettings) { locations, selection ->
    locations
      .flatMap { location -> location.mensas.map { it.mensa } }
      .filter {
        selection.favoriteMensas.contains(it.id) && !selection.hiddenMensas.contains(it.id)
      }
      .sortedBy { selection.favoriteMensas.indexOf(it.id) }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  fun clearCache() = viewModelScope.launch { mensaRepository.clearCache() }

  fun updateSetting(setting: Setting) = viewModelScope.launch {
    preferencesRepository.updateSetting(setting)
  }

  companion object {
    val Factory = viewModelFactory {
      initializer {
        val application = checkNotNull(
          this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MensaApplication,
        )
        SettingsViewModel(
          mensaRepository = application.container.mensaRepository,
          preferencesRepository = application.container.preferencesRepository,
        )
      }
    }
  }
}
