package ch.florianfrauenfelder.mensazh.ui.navigation

import androidx.navigation3.runtime.NavKey
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
  @Serializable
  data object Main: Route {
    @Serializable
    data object List : Route

    @Serializable
    data class Detail(val mensa: Mensa, val menuIndex: Int) : Route
  }
  @Serializable
  data object Settings : Route
}

