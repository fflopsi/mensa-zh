package ch.florianfrauenfelder.mensazh.domain.navigation

import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday

data class Params(
  val destination: Destination,
  val weekday: Weekday,
  val language: Language,
)
