package ch.florianfrauenfelder.mensazh.domain.model

import kotlin.uuid.Uuid

data class Location(
  val id: Uuid,
  val title: String,
  val mensas: List<MensaState>,
) {
  override fun toString() = title

  companion object {
    val dummy = Location(
      id = Uuid.random(),
      title = "Zentrum",
      mensas = List(3) { MensaState.dummy },
    )

    val favoritesUuid = Uuid.parse("e2b3688c-d305-4efd-bc34-9a3b2974d4e9")
  }
}
