package ch.florianfrauenfelder.mensazh.models

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.expandedMensasFlow
import ch.florianfrauenfelder.mensazh.services.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.UZHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.showMenusInGermanToLanguage
import ch.florianfrauenfelder.mensazh.services.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.THURSDAY
import kotlinx.datetime.DayOfWeek.TUESDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class Destination(@param:StringRes val label: Int, val icon: ImageVector) {
  Today(label = R.string.today, icon = Icons.Default.Today),
  Tomorrow(label = R.string.tomorrow, icon = Icons.Default.Event),
  ThisWeek(label = R.string.this_week, icon = Icons.Default.DateRange),
  NextWeek(label = R.string.next_week, icon = Icons.Default.CalendarMonth),
}

enum class Weekday(@param:StringRes val label: Int) {
  Monday(label = R.string.monday),
  Tuesday(label = R.string.tuesday),
  Wednesday(label = R.string.wednesday),
  Thursday(label = R.string.thursday),
  Friday(label = R.string.friday),
  Saturday(label = R.string.saturday),
  Sunday(label = R.string.sunday);

  fun next(): Weekday = when (this) {
    Monday -> Tuesday
    Tuesday -> Wednesday
    Wednesday -> Thursday
    Thursday -> Friday
    Friday -> Saturday
    Saturday -> Sunday
    Sunday -> Monday
  }

  companion object {
    @OptIn(ExperimentalTime::class)
    fun fromNow(): Weekday =
      when (Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek) {
        MONDAY -> Monday
        TUESDAY -> Tuesday
        WEDNESDAY -> Wednesday
        THURSDAY -> Thursday
        FRIDAY -> Friday
        SATURDAY -> Saturday
        SUNDAY -> Sunday
      }
  }
}

class AppViewModel(
  assetService: AssetService,
  private val cacheService: CacheService,
  private val favoriteMensas: Flow<Set<String>>,
  initialLanguage: Flow<MensaProvider.Language>,
  private val saveLanguage: suspend (MensaProvider.Language) -> Unit,
) : ViewModel() {
  var destination by mutableStateOf(Destination.Today)
    private set

  var weekday by mutableStateOf(Weekday.fromNow())
    private set

  var language by mutableStateOf(MensaProvider.Language.default)
    private set

  private val _locations = mutableStateListOf<Location>()
  val locations: List<Location> = _locations

  var isRefreshing by mutableStateOf(false)
    private set

  private val ethMensaProvider = ETHMensaProvider(cacheService, assetService)
  private val uzhMensaProvider = UZHMensaProvider(cacheService, assetService)

  private val initCompleted = CompletableDeferred<Unit>()

  init {
    viewModelScope.launch {
      language = withContext(Dispatchers.IO) { initialLanguage.first() }
      val locations = withContext(Dispatchers.IO) {
        val ethAsync = async { ethMensaProvider.getLocations() }
        val uzhAsync = async { uzhMensaProvider.getLocations() }
        ethAsync.await() + uzhAsync.await()
      }
      _locations.clear()
      _locations.addAll(locations)
      initCompleted.complete(Unit)
    }
  }

  fun setDestination(
    newDestination: Destination,
    refresh: Boolean = true,
    ignoreCache: Boolean = false,
  ) {
    destination = newDestination
    if (refresh) refresh(ignoreCache = ignoreCache)
  }

  fun setWeekday(
    newWeekday: Weekday,
    refresh: Boolean = true,
    ignoreCache: Boolean = false,
  ) {
    weekday = newWeekday
    if (refresh) refresh(ignoreCache = ignoreCache)
  }

  fun setLanguage(
    newLanguage: MensaProvider.Language,
    refresh: Boolean = true,
    ignoreCache: Boolean = false,
  ) {
    viewModelScope.launch { withContext(Dispatchers.IO) { saveLanguage(newLanguage) } }
    language = newLanguage
    if (refresh) refresh(ignoreCache = ignoreCache)
  }

  fun refresh(ignoreCache: Boolean = false) = viewModelScope.launch {
    initCompleted.await()
    isRefreshing = true
    cacheService.startObserveCacheUsage()

    val updatedMensas = withContext(Dispatchers.IO) {
      val ethAsync = async {
        ethMensaProvider.getMenus(language, destination == Destination.NextWeek, ignoreCache)
      }
      val uzhAsync = async {
        uzhMensaProvider.getMenus(language, destination == Destination.NextWeek, ignoreCache)
      }
      ethAsync.await() + uzhAsync.await()
    }.onEach { mensa ->
      mensa.menus = mensa.menus.filter {
        when (destination) {
          Destination.Today -> it.weekday == Weekday.fromNow()
          Destination.Tomorrow -> it.weekday == Weekday.fromNow().next()
          else -> it.weekday == weekday
        }
      }
    }

    cacheService.removeAllUntouchedCacheEntries()
    applyUpdatedMenus(updatedMensas)
    isRefreshing = false
  }

  private suspend fun applyUpdatedMenus(updated: List<Mensa>) {
    val favorites = withContext(Dispatchers.IO) { favoriteMensas.first() }
    _locations.forEach { location ->
      location.mensas.forEach { mensa ->
        updated.firstOrNull { it.id == mensa.id }.let {
          mensa.menus = it?.menus ?: emptyList()
          mensa.state = when {
            mensa.menus.isEmpty() -> Mensa.State.Closed
            favorites.contains(mensa.id.toString()) -> Mensa.State.Expanded
            else -> Mensa.State.Available
          }
        }
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer {
        val application = checkNotNull(this[APPLICATION_KEY])
        AppViewModel(
          assetService = AssetService(application.assets),
          cacheService = CacheService(application.cacheDir),
          favoriteMensas = application.expandedMensasFlow,
          initialLanguage = application.showMenusInGermanFlow.map { it.showMenusInGermanToLanguage },
          saveLanguage = { application.saveShowMenusInGerman(it.showMenusInGerman) },
        )
      }
    }
  }
}
