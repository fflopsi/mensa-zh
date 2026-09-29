package ch.florianfrauenfelder.mensazh.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.uuid.Uuid

@Serializable
sealed interface Route : NavKey {
  @Serializable
  data object List : Route

  @Serializable
  data class Detail(val mensaId: Uuid) : Route

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

fun NavBackStack<NavKey>.moveToTopOrAdd(route: NavKey) {
  if (lastOrNull() == route) return
  removeAll { it == route }
  add(route)
}
