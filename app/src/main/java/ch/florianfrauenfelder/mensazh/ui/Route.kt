package ch.florianfrauenfelder.mensazh.ui

import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable
  object Main : Route

  @Serializable
  object Settings : Route
}
