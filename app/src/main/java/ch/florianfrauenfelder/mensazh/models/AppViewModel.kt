package ch.florianfrauenfelder.mensazh.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.FetchInfoDao
import ch.florianfrauenfelder.mensazh.services.MenuDao
import ch.florianfrauenfelder.mensazh.services.expandedMensasFlow
import ch.florianfrauenfelder.mensazh.services.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.UZHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.showMenusInGermanToLanguage
import ch.florianfrauenfelder.mensazh.services.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class AppViewModel(
  assetService: AssetService,
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val favoriteMensas: Flow<Set<String>>,
  initialLanguage: Flow<MensaProvider.Language>,
  private val saveLanguage: suspend (MensaProvider.Language) -> Unit,
) : ViewModel() {
  private val params = MutableStateFlow(
    MenuParams(Destination.Today, Weekday.fromNow(), MensaProvider.Language.default),
  )

  var destination by mutableStateOf(params.value.destination)
    private set

  var weekday by mutableStateOf(params.value.weekday)
    private set

  var language by mutableStateOf(params.value.language)
    private set

  private val _locations = mutableStateListOf<Location>()
  val locations: List<Location> = _locations

  var isRefreshing by mutableStateOf(false)
    private set

  private val ethMensaProvider = ETHMensaProvider(menuDao, fetchInfoDao, assetService)
  private val uzhMensaProvider = UZHMensaProvider(menuDao, fetchInfoDao, assetService)

  private val initCompleted = CompletableDeferred<Unit>()

  init {
    viewModelScope.launch {
      val firstLanguage = withContext(Dispatchers.IO) { initialLanguage.first() }
      updateParams { it.copy(language = firstLanguage) }
      val locations = withContext(Dispatchers.IO) {
        val eth = async { ethMensaProvider.getLocations() }
        val uzh = async { uzhMensaProvider.getLocations() }
        eth.await() + uzh.await()
      }
      _locations.clear()
      _locations.addAll(locations)
      initCompleted.complete(Unit)
      observeMenus()
    }
  }

  private fun updateParams(transform: (MenuParams) -> MenuParams) {
    val new = transform(params.value)
    params.value = new

    destination = new.destination
    weekday = new.weekday
    language = new.language
  }

  fun setNew(newDestination: Destination) =
    updateParams { it.copy(destination = newDestination) }

  fun setNew(newWeekday: Weekday) = updateParams { it.copy(weekday = newWeekday) }

  fun setNew(newLanguage: MensaProvider.Language) {
    viewModelScope.launch { withContext(Dispatchers.IO) { saveLanguage(newLanguage) } }
    updateParams { it.copy(language = newLanguage) }
  }

  fun forceRefresh() = viewModelScope.launch {
    initCompleted.await()
    refresh(params.value)
  }

  fun refreshIfNeeded() = viewModelScope.launch {
    initCompleted.await()
    val p = params.value
    if (shouldRefresh(p)) refresh(p)
  }

  private fun observeMenus() = viewModelScope.launch {
    params.collectLatest {
      coroutineScope {
        launch { if (shouldRefresh(it)) refresh(it) }
        launch { observeForParams(it) }
      }
    }
  }

  private suspend fun shouldRefresh(p: MenuParams): Boolean {
    val now = System.currentTimeMillis()

    MensaProvider.Institution.entries.forEach {
      val fetchInfo = withContext(Dispatchers.IO) {
        fetchInfoDao.getFetchInfo(
          institution = it.toString(),
          destination = p.destination.toString(),
          language = p.language.toString(),
        )
      }

      if (now - (fetchInfo?.fetchDate ?: 0) > 12.hours.inWholeMilliseconds) {
        return true
      }
    }
    return false
  }

  private suspend fun observeForParams(p: MenuParams) = coroutineScope {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (p.destination == Destination.NextWeek) plus(7, DateTimeUnit.DAY) else this
    }
    val date = when (p.destination) {
      Destination.Today -> today.toString()
      Destination.Tomorrow -> today.plus(1, DateTimeUnit.DAY).toString()
      Destination.ThisWeek, Destination.NextWeek -> monday
        .plus(p.weekday.ordinal, DateTimeUnit.DAY)
        .toString()
    }

    _locations.forEach { location ->
      location.mensas.forEach { mensa ->
        launch {
          combine(
            favoriteMensas,
            menuDao.getMenus2(
              mensaId = mensa.id.toString(),
              language = p.language.toString(),
              date = date,
            ),
          ) { favorites, roomMenus ->
            favorites to roomMenus
          }.collectLatest { (favorites, roomMenus) ->
            withContext(Dispatchers.Main) {
              mensa.menus = roomMenus.map { it.toMenu() }
              // ?? THIS IS BAD AND NEEDS TO BE CHANGED, POSSIBLY COMPOSE STATE TO PROPERLY OBSERVE CHANGES ??
              mensa.state = when {
                mensa.menus.isEmpty() -> Mensa.State.Closed
                favorites.contains(mensa.id.toString()) -> Mensa.State.Expanded
                else -> Mensa.State.Available
              }
            }
          }
        }
      }
    }
  }

  private val refreshMutex = Mutex()

  private suspend fun refresh(p: MenuParams) = refreshMutex.withLock {
    isRefreshing = true
    coroutineScope {
      launch(Dispatchers.IO) { ethMensaProvider.getMenus(p.language, p.destination) }
      launch(Dispatchers.IO) { uzhMensaProvider.getMenus(p.language, p.destination) }
    }
    isRefreshing = false
  }

  private data class MenuParams(
    val destination: Destination,
    val weekday: Weekday,
    val language: MensaProvider.Language,
  )

  companion object {
    fun Factory(menuDao: MenuDao, fetchInfoDao: FetchInfoDao) = viewModelFactory {
      initializer {
        val application = checkNotNull(this[APPLICATION_KEY])
        AppViewModel(
          assetService = AssetService(application.assets),
          menuDao = menuDao,
          fetchInfoDao = fetchInfoDao,
          favoriteMensas = application.expandedMensasFlow,
          initialLanguage = application.showMenusInGermanFlow.map { it.showMenusInGermanToLanguage },
          saveLanguage = { application.saveShowMenusInGerman(it.showMenusInGerman) },
        )
      }
    }
  }
}
