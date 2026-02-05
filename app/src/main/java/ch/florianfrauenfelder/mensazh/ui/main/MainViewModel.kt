package ch.florianfrauenfelder.mensazh.ui.main

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.MensaApplication
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import ch.florianfrauenfelder.mensazh.data.repository.PreferencesRepository
import ch.florianfrauenfelder.mensazh.data.util.currentWeekday
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Params
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.SelectionSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.uuid.Uuid

class MainViewModel(
  private val mensaRepository: MensaRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  private val _params =
    MutableStateFlow(Params(destination = Destination.Today, weekday = currentWeekday()))
  val params = _params.asStateFlow()

  val visibilitySettings = preferencesRepository.visibilitySettings.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = VisibilitySettings(),
  )

  val selectionSettings = preferencesRepository.selectionSettings.stateIn(
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

  private val allLocations = combine(params, visibilitySettings) { params, visibility ->
    params to visibility.language
  }.flatMapLatest { (params, language) ->
    viewModelScope.launch { mensaRepository.refreshIfNeeded(params.destination, language) }
    locationListFlow(params.destination, params.weekday, language)
  }

  val locations = combine(allLocations, selectionSettings) { locations, selection ->
    locations
      .filter { selection.shownLocations.contains(it.id) }
      .sortedBy { selection.shownLocations.indexOf(it.id) }
      .map { location ->
        location.copy(
          id = location.id,
          title = location.title,
          mensas = location.mensas.filter { !selection.favoriteMensas.contains(it.mensa.id) },
        )
      }
      .toMutableStateList()
      .apply {
        add(
          0,
          Location(
            id = Uuid.random(),
            title = "★",
            mensas = locations
              .flatMap { it.mensas }
              .filter { selection.favoriteMensas.contains(it.mensa.id) }
              .sortedBy { selection.favoriteMensas.indexOf(it.mensa.id) },
          ),
        )
      }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  val isRefreshing = mensaRepository.isRefreshing.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = false,
  )

  fun setNew(newDestination: Destination) = _params.update { it.copy(destination = newDestination) }

  fun setNew(newWeekday: Weekday) = _params.update { it.copy(weekday = newWeekday) }

  fun updateSetting(setting: Setting) = viewModelScope.launch {
    preferencesRepository.updateSetting(setting)
  }

  fun forceRefresh() = viewModelScope.launch {
    mensaRepository.forceRefresh(params.value.destination, visibilitySettings.value.language)
  }

  fun deleteExpired() = viewModelScope.launch { mensaRepository.deleteExpired() }

  private fun locationListFlow(
    destination: Destination,
    weekday: Weekday,
    language: Language,
  ): Flow<List<Location>> = mensaRepository.baseLocations.flatMapLatest { baseLocations ->
    val date = computeDate(destination, weekday)
    val locationFlows = baseLocations.map { location ->
      val mensaFlows = location.mensas.map {
        mensaStateFlow(mensa = it.mensa, language = language, date = date)
      }
      combine(mensaFlows) {
        Location(id = location.id, title = location.title, mensas = it.toList())
      }
    }
    combine(locationFlows) { it.toList() }
  }

  private fun mensaStateFlow(
    mensa: Mensa,
    language: Language,
    date: LocalDate,
  ): Flow<MensaState> = combine(
    visibilitySettings.map { it.expandedMensas },
    mensaRepository.observeMenus(mensaId = mensa.id, language = language, date = date),
  ) { expandedMensas, menus ->
    MensaState(
      mensa = mensa,
      menus = menus,
      state = when {
        menus.isEmpty() -> MensaState.State.Closed
        expandedMensas.contains(mensa.id) -> MensaState.State.Expanded
        else -> MensaState.State.Available
      },
    )
  }

  private fun computeDate(destination: Destination, weekday: Weekday): LocalDate {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (destination == Destination.NextWeek) plus(7, DateTimeUnit.DAY) else this
    }
    return when (destination) {
      Destination.Today -> today
      Destination.Tomorrow -> today.plus(1, DateTimeUnit.DAY)
      Destination.ThisWeek, Destination.NextWeek -> {
        monday.plus(weekday.ordinal, DateTimeUnit.DAY)
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer {
        val application = checkNotNull(
          this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MensaApplication,
        )
        MainViewModel(
          mensaRepository = application.container.mensaRepository,
          preferencesRepository = application.container.preferencesRepository,
        )
      }
    }
  }
}
