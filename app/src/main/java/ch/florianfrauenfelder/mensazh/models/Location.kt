package ch.florianfrauenfelder.mensazh.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Parcelize
//@Serializable
data class Location(
  val title: String,
  val mensas: List<Mensa>,
) {
//  init {
//    mensas.forEach { it.location = this }
//  }

  override fun toString() = title

  companion object {
    val dummy = Location(
      title = "Zentrum",
      mensas = List(3) { Mensa.dummy },
    )
  }
}
