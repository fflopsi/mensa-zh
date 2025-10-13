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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
  assetService: AssetService,
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
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

  private val ethMensaProvider = ETHMensaProvider(menuDao, fetchInfoDao, assetService)
  private val uzhMensaProvider = UZHMensaProvider(menuDao, fetchInfoDao, assetService)

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

    val ethJob = launch(Dispatchers.IO) {
      applyUpdatedMenus(ethMensaProvider, ignoreCache)
    }
    val uzhJob = launch(Dispatchers.IO) {
      applyUpdatedMenus(uzhMensaProvider, ignoreCache)
    }

    joinAll(ethJob, uzhJob)
    isRefreshing = false
  }

  private suspend fun <
    L : MensaProvider.ApiLocation<M>,
    M : MensaProvider.ApiMensa,
    > applyUpdatedMenus(provider: MensaProvider<L, M>, ignoreCache: Boolean) {
    val updated = provider.getFilteredMenus(ignoreCache)
    val favorites = withContext(Dispatchers.IO) { favoriteMensas.first() }
    _locations.filter { it.title.contains(provider.institution.toString()) }.forEach { location ->
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

  private suspend fun <
    L : MensaProvider.ApiLocation<M>,
    M : MensaProvider.ApiMensa,
    > MensaProvider<L, M>.getFilteredMenus(ignoreCache: Boolean): List<Mensa> =
    getMenus(language, destination, ignoreCache).onEach { mensa ->
      mensa.menus = mensa.menus.filter {
        when (destination) {
          Destination.Today -> it.weekday == Weekday.fromNow()
          Destination.Tomorrow -> it.weekday == Weekday.fromNow().next()
          else -> it.weekday == weekday
        }
      }
    }

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
