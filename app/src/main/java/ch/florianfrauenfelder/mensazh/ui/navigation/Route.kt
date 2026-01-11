package ch.florianfrauenfelder.mensazh.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable
  object Main : Route

  @Serializable
  object Settings : Route
}

