package ch.florianfrauenfelder.mensazh.domain.model

import java.util.UUID

data class Location(
  override val id: UUID,
  override val title: String,
  val mensas: List<MensaState>,
) : IdTitleItem {
  override fun toString() = title

  companion object {
    val dummy = Location(
      id = UUID.randomUUID(),
      title = "Zentrum",
      mensas = List(3) { MensaState.dummy },
    )
  }
}
