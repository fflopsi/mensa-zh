package ch.florianfrauenfelder.mensazh.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.AppContainer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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

class MainViewModel(
  private val mensaRepository: MensaRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  private val _params =
    MutableStateFlow(Params(destination = Destination.Today, weekday = currentWeekday()))
  val params = _params.asStateFlow()

  val events = mensaRepository.eventChannel.receiveAsFlow()

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

  private val destinationTriggers = combine(
    params.map { it.destination },
    destinationSettings,
  ) { destination, destinationSettings ->
    destination to destinationSettings
  }.distinctUntilChanged()

  private val refreshTriggers = combine(
    params,
    visibilitySettings.map { it.language },
  ) { params, language ->
    params.destination to language
  }.distinctUntilChanged()

  init {
    refreshTriggers
      .onEach { (destination, language) ->
        mensaRepository.refreshIfNeeded(destination, language)
      }
      .launchIn(viewModelScope)
    destinationTriggers
      .onEach { (destination, destinationSettings) ->
        when (destination) {
          Destination.Tomorrow -> {
            if (!destinationSettings.showTomorrow) {
              setParams { it.copy(destination = Destination.Today) }
            }
          }
          Destination.ThisWeek -> {
            if (!destinationSettings.showThisWeek) {
              setParams { it.copy(destination = Destination.Today) }
            }
          }
          Destination.NextWeek -> {
            if (!destinationSettings.showNextWeek) {
              setParams { it.copy(destination = Destination.Today) }
            }
          }
          else -> {}
        }
      }
      .launchIn(viewModelScope)
  }

  private val allLocations = combine(params, visibilitySettings) { params, visibility ->
    params to visibility.language
  }.flatMapLatest { (params, language) ->
    locationListFlow(params.destination, params.weekday, language)
  }

  val locations = combine(
    allLocations,
    visibilitySettings,
    selectionSettings,
  ) { locations, visibility, selection ->
    val shownOrder = selection.shownLocations.withIndex().associate { it.value to it.index }
    val favoriteOrder = selection.favoriteMensas.withIndex().associate { it.value to it.index }
    val favorites = locations
      .flatMap { it.mensas }
      .filter { it.mensa.id in favoriteOrder }
      .sortedBy { favoriteOrder[it.mensa.id] }

    val filteredLocations = locations
      .filter { it.id in shownOrder }
      .sortedBy { shownOrder[it.id] }
      .map { location ->
        location.copy(
          mensas = location.mensas.filter {
            it.mensa.id !in selection.hiddenMensas
              && it.mensa.id !in selection.favoriteMensas
              && !(visibility.showOnlyOpenMensas && it.state == MensaState.State.Closed)
              && !(visibility.showOnlyExpandedMensas && it.state != MensaState.State.Expanded)
          },
        )
      }
      .filter { it.mensas.isNotEmpty() }

    buildList {
      if (favorites.isNotEmpty()) {
        add(Location(id = Location.favoritesUuid, title = "★", mensas = favorites))
      }
      addAll(filteredLocations)
    }
  }
    .flowOn(Dispatchers.Default)
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val isRefreshing = mensaRepository.isRefreshing.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = false,
  )

  fun setParams(transform: (Params) -> Params) = _params.update(transform)

  fun updateSetting(setting: Setting) = viewModelScope.launch {
    preferencesRepository.updateSetting(setting)
  }

  fun forceRefresh() = viewModelScope.launch {
    mensaRepository.forceRefresh(params.value.destination, visibilitySettings.value.language)
  }

  private fun locationListFlow(
    destination: Destination,
    weekday: Weekday,
    language: Language,
  ): Flow<List<Location>> = mensaRepository.baseLocations.flatMapLatest { baseLocations ->
    val date = computeDate(destination, weekday)

    baseLocations
      .map { location ->
        location.mensas
          .map {
            mensaStateFlow(it.mensa, language, date)
          }
          .let { mensaFlows ->
            combine(mensaFlows) {
              Location(
                id = location.id,
                title = location.title,
                mensas = it.toList(),
              )
            }
          }
      }
      .let { locationFlows ->
        combine(locationFlows) { it.toList() }
      }
  }

  private fun mensaStateFlow(
    mensa: Mensa,
    language: Language,
    date: LocalDate,
  ): Flow<MensaState> = combine(
    visibilitySettings.map { it.expandedMensas },
    selectionSettings.map { it.favoriteMensas },
    mensaRepository.observeMenus(mensaId = mensa.id, language = language, date = date),
    mensaRepository.observeMenus(mensaId = mensa.id, language = !language, date = date),
  ) { expandedMensas, favoriteMensas, menus, fallbackMenus ->
    val returnedMenus = if (menus.isEmpty() && fallbackMenus.isNotEmpty()) fallbackMenus else menus
    MensaState(
      mensa = mensa,
      menus = returnedMenus.sortedBy { it.index },
      state = when {
        returnedMenus.isEmpty() -> MensaState.State.Closed
        expandedMensas.contains(mensa.id) -> MensaState.State.Expanded
        else -> MensaState.State.Available
      },
      favorite = mensa.id in favoriteMensas,
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
    fun Factory(container: AppContainer) = viewModelFactory {
      initializer {
        MainViewModel(
          mensaRepository = container.mensaRepository,
          preferencesRepository = container.preferencesRepository,
        )
      }
    }
  }
}
