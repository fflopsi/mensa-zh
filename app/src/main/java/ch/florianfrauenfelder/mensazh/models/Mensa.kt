package ch.florianfrauenfelder.mensazh.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.net.URI
import java.util.UUID

data class Mensa(
  val id: UUID,
  val title: String,
  val mealTime: String,
  val url: URI,
  val imagePath: String? = null,
) {
  enum class State { Initial, Closed, Available, Expanded }

  private var _menus = mutableStateListOf<Menu>()
  var menus: List<Menu>
    get() = _menus
    set(value) = synchronized(this) {
      _menus.clear()
      _menus.addAll(value.onEach { it.mensa = this })
    }

  var state by mutableStateOf(State.Initial)

  override fun toString() = title + menus

  companion object {
    val dummy = Mensa(
      id = UUID.randomUUID(),
      title = "Mensa Polyterrasse",
      mealTime = "11:00 - 14:00",
      url = URI("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/mensa-polyterrasse.html"),
    ).apply {
      menus = List(5) { Menu.dummy }
      state = State.Available
    }
  }
}
