package ch.florianfrauenfelder.mensazh.domain.model

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Mensa(
  val id: Uuid,
  val title: String,
  val mealTime: String,
  val url: String,
  val imagePath: String? = null,
) {
  override fun toString() = title

  fun toMensaState() = MensaState(mensa = this)

  companion object {
    val dummy = Mensa(
      id = Uuid.random(),
      title = "Mensa Polyterrasse",
      mealTime = "11:00 - 14:00",
      url = "https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/mensa-polyterrasse.html",
    )
  }
}
