package ch.florianfrauenfelder.mensazh.ui

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface Route : NavKey {
  @Serializable
  data object Main : Route {
    @Serializable
    data object List : Route

    @Serializable
    data class Detail(val mensa: Mensa, val menuIndex: Int) : Route
  }

  @Serializable
  data object Settings : Route
}

val routeConfig = SavedStateConfiguration {
  serializersModule = SerializersModule {
    polymorphic(NavKey::class) {
      subclassesOfSealed<Route>()
    }
  }
}
