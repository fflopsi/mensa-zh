package ch.florianfrauenfelder.mensazh.domain.preferences

import kotlin.uuid.Uuid

data class SelectionSettings(
  val shownLocations: List<Uuid> = Defaults.SHOWN_LOCATIONS,
  val favoriteMensas: List<Uuid> = Defaults.FAVORITE_MENSAS,
  val hiddenMensas: List<Uuid> = Defaults.HIDDEN_MENSAS,
)
