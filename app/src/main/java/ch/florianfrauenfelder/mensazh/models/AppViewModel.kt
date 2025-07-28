package ch.florianfrauenfelder.mensazh.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.UZHMensaProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AppViewModel(
  assetService: AssetService,
  private val cacheService: CacheService,
  private val favoriteMensas: Flow<Set<String>>,
) : ViewModel() {
  private val _locations = mutableStateListOf<Location>()
  val locations: List<Location> = _locations

  var isRefreshing by mutableStateOf(false)
    private set

  private val ethMensaProvider = ETHMensaProvider(cacheService, assetService)
  private val uzhMensaProvider = UZHMensaProvider(cacheService, assetService)

  init {
    viewModelScope.launch {
      val locations = withContext(Dispatchers.IO) {
        val ethAsync = async { ethMensaProvider.getLocations() }
        val uzhAsync = async { uzhMensaProvider.getLocations() }
        ethAsync.await() + uzhAsync.await()
      }
      _locations.clear()
      _locations.addAll(locations)
    }
  }

  fun refresh(date: Date, language: MensaProvider.Language, ignoreCache: Boolean = false) =
    viewModelScope.launch {
      isRefreshing = true
      cacheService.startObserveCacheUsage()
      val updatedMensas = withContext(Dispatchers.IO) {
        val ethAsync = async { ethMensaProvider.getMenus(date, language, ignoreCache) }
        val uzhAsync = async { uzhMensaProvider.getMenus(date, language, ignoreCache) }
        ethAsync.await() + uzhAsync.await()
      }
      cacheService.removeAllUntouchedCacheEntries()
      applyUpdatedMenus(updatedMensas)
      isRefreshing = false
    }

  private suspend fun applyUpdatedMenus(updated: List<Mensa>) {
    val favorites = withContext(Dispatchers.IO) { favoriteMensas.first() }
    _locations.forEach { location ->
      location.mensas.forEach { mensa ->
        updated.first { it.id == mensa.id }.let {
          mensa.menus = it.menus
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
