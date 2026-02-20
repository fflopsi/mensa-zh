package ch.florianfrauenfelder.mensazh.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Params
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import ch.florianfrauenfelder.mensazh.domain.value.Event
import ch.florianfrauenfelder.mensazh.ui.Route
import ch.florianfrauenfelder.mensazh.ui.domain.ui
import kotlinx.coroutines.flow.Flow

@Composable
fun MainScreen(
  params: Params,
  locations: List<Location>,
  isRefreshing: Boolean,
  events: Flow<Event>,
  visibilitySettings: VisibilitySettings,
  destinationSettings: DestinationSettings,
  detailSettings: DetailSettings,
  sceneStrategy: ListDetailSceneStrategy<NavKey>,
  refresh: () -> Unit,
  setParams: ((Params) -> Params) -> Unit,
  updateSetting: (Setting) -> Unit,
  navigateToSettings: () -> Unit,
) {
  val backStack = rememberNavBackStack(Route.Main.List)

  if (destinationSettings.showAny) {
    NavigationSuiteScaffold(
      navigationItems = {
        buildList {
          add(Destination.Today)
          if (destinationSettings.showTomorrow) add(Destination.Tomorrow)
          if (destinationSettings.showThisWeek) add(Destination.ThisWeek)
          if (destinationSettings.showNextWeek) add(Destination.NextWeek)
        }.forEach { destination ->
          NavigationSuiteItem(
            icon = { Icon(destination.ui.icon, stringResource(destination.ui.label)) },
            label = { Text(stringResource(destination.ui.label)) },
            selected = destination == params.destination,
            onClick = {
              if (destination != params.destination) {
                setParams { it.copy(destination = destination) }
              } else if (backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
          )
        }
      }
    ) {
      MainScreenScaffold(
        params = params,
        locations = locations,
        isRefreshing = isRefreshing,
        events = events,
        visibilitySettings = visibilitySettings,
        detailSettings = detailSettings,
        backStack = backStack,
        sceneStrategy = sceneStrategy,
        refresh = refresh,
        setParams = setParams,
        updateSetting = updateSetting,
        navigateToSettings = navigateToSettings,
      )
    }
  } else {
    MainScreenScaffold(
      params = params,
      locations = locations,
      isRefreshing = isRefreshing,
      events = events,
      visibilitySettings = visibilitySettings,
      detailSettings = detailSettings,
      backStack = backStack,
      sceneStrategy = sceneStrategy,
      refresh = refresh,
      setParams = setParams,
      updateSetting = updateSetting,
      navigateToSettings = navigateToSettings,
      useContentPadding = true,
    )
  }
}
