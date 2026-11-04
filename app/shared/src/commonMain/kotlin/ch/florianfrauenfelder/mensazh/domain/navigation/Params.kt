package ch.florianfrauenfelder.mensazh.domain.navigation

import kotlinx.serialization.Serializable

@Serializable
data class Params(
  val destination: Destination,
  val weekday: Weekday,
)
