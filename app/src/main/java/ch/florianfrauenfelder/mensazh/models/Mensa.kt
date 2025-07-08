package ch.florianfrauenfelder.mensazh.models

import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.net.URI
import java.util.UUID

@Parcelize
//@Serializable
data class Mensa(
//  @Serializable(with = UUIDSerializer::class)
  val id: UUID,
  val title: String,
  val mealTime: String,
//  @Serializable(with = URISerializer::class)
  val url: URI,
  val imagePath: String? = null,
  private var _menus: @RawValue SnapshotStateList<Menu> = mutableStateListOf(),
  private val initialState: State = State.Initial,
) : Parcelable {
  enum class State { Initial, Closed, Available, Expanded }

  var menus: List<Menu>
    get() = _menus
    set(value) = synchronized(this){
      _menus.clear()
      _menus.addAll(value.onEach { it.mensa = this })
      Log.d("Mensa", title)
    }

  init {
    // Trigger menus setter logic
    menus = _menus
  }

//  @IgnoredOnParcel
//  var location: Location? = null

  @IgnoredOnParcel
  var state by mutableStateOf(initialState)

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
