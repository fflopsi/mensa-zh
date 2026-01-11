package ch.florianfrauenfelder.mensazh.domain.model

import java.net.URI
import java.util.UUID

data class Mensa(
  override val id: UUID,
  override val title: String,
  val mealTime: String,
  val url: URI,
  val imagePath: String? = null,
) : IdTitleItem {
  override fun toString() = title

  fun toMensaState() = MensaState(mensa = this)

  companion object {
    val dummy = Mensa(
      id = UUID.randomUUID(),
      title = "Mensa Polyterrasse",
      mealTime = "11:00 - 14:00",
      url = URI("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/zentrum/mensa-polyterrasse.html"),
    )
  }
}
