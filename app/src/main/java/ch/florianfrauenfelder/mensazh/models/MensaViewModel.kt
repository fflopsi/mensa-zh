package ch.florianfrauenfelder.mensazh.models

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.florianfrauenfelder.mensazh.LocationRepository
import ch.florianfrauenfelder.mensazh.services.providers.AbstractMensaProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class MensaViewModel(
  private val repository: LocationRepository,
  private val favoriteMensas: Flow<Set<String>>,
) :
  ViewModel() {
  private val _locations = mutableStateListOf<Location>()
  val locations: List<Location> = _locations

  var isRefreshing by mutableStateOf(false)
    private set

  init {
    viewModelScope.launch {
      _locations.clear()
      _locations.addAll(repository.loadLocations())
    }
  }

  fun refresh(date: Date, language: AbstractMensaProvider.Language, ignoreCache: Boolean = false) {
    viewModelScope.launch {
      isRefreshing = true
      val updatedMensas = repository.refresh(date, language, ignoreCache)
      applyUpdatedMenus(updatedMensas)
      isRefreshing = false
    }
  }

  private suspend fun applyUpdatedMenus(updated: List<Mensa>) {
    val favorites = favoriteMensas.first()
    _locations.forEach { location ->
      location.mensas.forEach { mensa ->
        updated.associateBy { it.id }[mensa.id]?.let { updatedMensa ->
//          mensa.menus = updatedMensa.menus // This is not needed, causes problems if uncommented
          Log.d("Menus", updatedMensa.menus.toString())
          mensa.state = if (mensa.menus.isEmpty()) {
            Mensa.State.Closed
          } else if (favorites.contains(mensa.id.toString())) {
            Mensa.State.Expanded
          } else {
            Mensa.State.Available
          }
        }
      }
    }
  }
}
