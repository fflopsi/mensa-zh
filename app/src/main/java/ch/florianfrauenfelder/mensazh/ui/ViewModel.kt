package ch.florianfrauenfelder.mensazh.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.data.local.datastore.expandedMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.data.providers.UZHMensaProvider
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.data.util.currentWeekday
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Params
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

class ViewModel(
  private val mensaRepository: MensaRepository,
  private val favoriteMensas: Flow<Set<String>>,
  language: Flow<Language>,
) : ViewModel() {
  private val _params =
    MutableStateFlow(Params(destination = Destination.Today, weekday = currentWeekday()))
  val params = _params.asStateFlow()

  private val language = language.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = Language.default,
  )

  val locations = combine(params, language) { params, language ->
    params to language
  }.flatMapLatest { (params, language) ->
    viewModelScope.launch { mensaRepository.refreshIfNeeded(params.destination, language) }
    locationListFlow(params.destination, params.weekday, language)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  private val baseLocations = MutableStateFlow<List<Location>>(emptyList())

  val isRefreshing = mensaRepository.isRefreshing.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = false,
  )

  private val initCompleted = CompletableDeferred<Unit>()

  init {
    viewModelScope.launch {
      baseLocations.value = withContext(Dispatchers.IO) { mensaRepository.getLocations() }
      initCompleted.complete(Unit)
    }
  }

  fun setNew(newDestination: Destination) = _params.update { it.copy(destination = newDestination) }

  fun setNew(newWeekday: Weekday) = _params.update { it.copy(weekday = newWeekday) }

  fun forceRefresh() = viewModelScope.launch {
    initCompleted.await()
    mensaRepository.forceRefresh(params.value.destination, language.value)
  }

  fun refreshIfNeeded() = viewModelScope.launch {
    initCompleted.await()
    mensaRepository.refreshIfNeeded(params.value.destination, language.value)
  }

  fun clearCache() = viewModelScope.launch { mensaRepository.clearCache() }

  private fun locationListFlow(
    destination: Destination,
    weekday: Weekday,
    language: Language,
  ): Flow<List<Location>> = baseLocations.flatMapLatest { baseLocations ->
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
    favoriteMensas,
    mensaRepository.observeMenus(mensaId = mensa.id, language = language, date = date),
  ) { favorites, menus ->
    MensaState(
      mensa = mensa,
      menus = menus,
      state = when {
        menus.isEmpty() -> MensaState.State.Closed
        favorites.contains(mensa.id.toString()) -> MensaState.State.Expanded
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
    fun Factory(menuDao: MenuDao, fetchInfoDao: FetchInfoDao) = viewModelFactory {
      initializer {
        val application =
          checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        val assetService = AssetService(application.assets)
        ViewModel(
          mensaRepository = MensaRepository(
            menuDao = menuDao,
            fetchInfoDao = fetchInfoDao,
            providers = mapOf(
              Institution.ETH to ETHMensaProvider(menuDao, fetchInfoDao, assetService),
              Institution.UZH to UZHMensaProvider(menuDao, fetchInfoDao, assetService),
            ),
          ),
          favoriteMensas = application.expandedMensasFlow,
          language = application.showMenusInGermanFlow.map { Language.fromBoolean(it) },
        )
      }
    }
  }
}
