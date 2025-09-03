package ch.florianfrauenfelder.mensazh.models

import java.util.UUID

data class Location(
  val id: UUID,
  val title: String,
  val mensas: List<Mensa>,
) {
  override fun toString() = title

  companion object {
    val dummy = Location(
      id = UUID.randomUUID(),
      title = "Zentrum",
      mensas = List(3) { Mensa.dummy },
    )
  }
}
