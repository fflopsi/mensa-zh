package ch.florianfrauenfelder.mensazh.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@Parcelize
data class Mensa(
  override val id: Uuid,
  override val title: String,
  val mealTime: String,
  val url: String,
  val imagePath: String? = null,
) : IdTitleItem, Parcelable {
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
