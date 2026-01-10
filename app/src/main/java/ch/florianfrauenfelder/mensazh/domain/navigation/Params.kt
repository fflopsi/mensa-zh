package ch.florianfrauenfelder.mensazh.domain.navigation

import ch.florianfrauenfelder.mensazh.domain.value.Language

data class Params(
  val destination: Destination,
  val weekday: Weekday,
  val language: Language,
)
