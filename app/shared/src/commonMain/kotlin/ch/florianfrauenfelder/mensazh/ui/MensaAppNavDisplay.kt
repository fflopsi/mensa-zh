package ch.florianfrauenfelder.mensazh.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ch.florianfrauenfelder.mensazh.AppContainer
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.ui.panes.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.panes.list.LocationList
import ch.florianfrauenfelder.mensazh.ui.panes.settings.SettingsList
import ch.florianfrauenfelder.mensazh.ui.panes.settings.SettingsViewModel

@Composable
fun MensaAppNavDisplay(
  backStack: NavBackStack<NavKey>,
  density: Density,
  tabRowSize: IntSize,
  container: AppContainer,
  locations: List<Location>,
  selectedMensa: MensaState?,
  selectedMenu: Menu?,
  destinationSettings: DestinationSettings,
  detailSettings: DetailSettings,
  updateSetting: (Setting) -> Unit,
  innerPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val sceneStrategy = rememberListDetailSceneStrategy<NavKey>(
    paneExpansionDragHandle = {
      val interactionSource = remember { MutableInteractionSource() }
      VerticalDragHandle(
        interactionSource = interactionSource,
        modifier = Modifier.paneExpansionDraggable(
          state = it,
          minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
          interactionSource = interactionSource
        ),
      )
    },
  )

  val animatedBottomPadding by animateDpAsState(
    targetValue = if (destinationSettings.showAny) {
      0.dp
    } else {
      innerPadding.calculateBottomPadding()
    }
  )

  NavDisplay(
    backStack = backStack,
    sceneStrategies = listOf(sceneStrategy),
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { it }) togetherWith
        slideOutHorizontally(targetOffsetX = { -it })
    },
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })
    },
    predictivePopTransitionSpec = {
      slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })
    },
    modifier = modifier
      .padding(bottom = with(density) { tabRowSize.height.toDp() })
      .fillMaxSize(),
    entryProvider = entryProvider {
      entry<Route.Main.List>(
        metadata = ListDetailSceneStrategy.listPane(
          detailPlaceholder = {
            Box(modifier = Modifier.fillMaxSize()) {
              Text("Select a menu", modifier = Modifier.align(Alignment.Center))
            }
          },
        ),
      ) {
        LocationList(
          locations = locations,
          detail = detailSettings,
          onMenuClick = { mensa, menu ->
            backStack.add(Route.Main.Detail(mensa.mensa, mensa.menus.indexOf(menu)))
          },
          toggleExpandedMensa = { updateSetting(Setting.SetIsExpandedMensa(it)) },
          toggleFavoriteMensa = { updateSetting(Setting.SetIsFavoriteMensa(it)) },
          hideMensa = { updateSetting(Setting.SetIsHiddenMensa(it)) },
          contentPadding = PaddingValues(bottom = animatedBottomPadding),
          modifier = Modifier.fillMaxWidth(),
        )
      }
      entry<Route.Main.Detail>(metadata = ListDetailSceneStrategy.detailPane()) {
        selectedMensa?.let { mensa ->
          MenuList(
            menus = mensa.menus,
            selectedMenu = selectedMenu,
            selectMenu = { menu ->
              backStack.add(Route.Main.Detail(mensa.mensa, mensa.menus.indexOf(menu)))
            },
            autoShowImage = detailSettings.autoShowImage,
            contentPadding = PaddingValues(bottom = animatedBottomPadding),
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
      entry<Route.Settings>(metadata = ListDetailSceneStrategy.extraPane()) {
        val viewModel: SettingsViewModel =
          viewModel(factory = SettingsViewModel.Factory(container))
        val visibility by viewModel.visibilitySettings.collectAsStateWithLifecycle()
        val destination by viewModel.destinationSettings.collectAsStateWithLifecycle()
        val detail by viewModel.detailSettings.collectAsStateWithLifecycle()
        val theme by viewModel.themeSettings.collectAsStateWithLifecycle()
        val baseLocations by viewModel.baseLocations.collectAsStateWithLifecycle()
        val shownLocations by viewModel.shownLocations.collectAsStateWithLifecycle()
        val hiddenMensas by viewModel.hiddenMensas.collectAsStateWithLifecycle()
        val favoriteMensas by viewModel.favoriteMensas.collectAsStateWithLifecycle()

        SettingsList(
          visibility = visibility,
          destination = destination,
          detail = detail,
          theme = theme,
          baseLocations = baseLocations,
          shownLocations = shownLocations,
          hiddenMensas = hiddenMensas,
          favoriteMensas = favoriteMensas,
          update = viewModel::updateSetting,
          clearCache = viewModel::clearCache,
          contentPadding = PaddingValues(bottom = animatedBottomPadding),
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
  )
}
