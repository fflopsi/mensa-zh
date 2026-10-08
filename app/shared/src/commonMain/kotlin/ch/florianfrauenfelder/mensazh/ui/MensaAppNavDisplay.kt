package ch.florianfrauenfelder.mensazh.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.ui.panes.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.panes.list.LocationList
import ch.florianfrauenfelder.mensazh.ui.panes.settings.SettingsList
import ch.florianfrauenfelder.mensazh.ui.panes.settings.SettingsViewModel
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.no_menus
import mensazh.app.shared.generated.resources.select_a_menu
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun MensaAppNavDisplay(
  backStack: NavBackStack<NavKey>,
  container: AppContainer,
  locations: List<Location>,
  mensasById: Map<Uuid, MensaState>,
  menuIndices: Map<Uuid, Int>,
  selectMenu: (Uuid, Int) -> Unit,
  detailSettings: DetailSettings,
  updateSetting: (Setting) -> Unit,
  innerPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val motion = MaterialTheme.motionScheme

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
  val listPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())

  NavDisplay(
    backStack = backStack,
    sceneStrategies = listOf(sceneStrategy),
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    transitionSpec = {
      slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = motion.defaultSpatialSpec(),
      ) togetherWith
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Start,
          animationSpec = motion.defaultSpatialSpec(),
        )
    },
    popTransitionSpec = {
      slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = motion.defaultSpatialSpec(),
      ) togetherWith
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.End,
          animationSpec = motion.defaultSpatialSpec(),
        )
    },
    predictivePopTransitionSpec = {
      slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = motion.defaultSpatialSpec(),
      ) togetherWith
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.End,
          animationSpec = motion.defaultSpatialSpec(),
        )
    },
    modifier = modifier.fillMaxSize(),
    entryProvider = entryProvider {
      entry<Route.List>(
        metadata = ListDetailSceneStrategy.listPane(
          detailPlaceholder = {
            Box(modifier = Modifier.fillMaxSize()) {
              Text(
                text = stringResource(Res.string.select_a_menu),
                modifier = Modifier.align(Alignment.Center),
              )
            }
          },
        ),
      ) {
        LocationList(
          locations = locations,
          detail = detailSettings,
          onMenuClick = { mensa, menu ->
            selectMenu(mensa.mensa.id, menu.index)
            backStack.moveToTopOrAdd(Route.Detail(mensa.mensa.id))
          },
          toggleExpandedMensa = { updateSetting(Setting.SetIsExpandedMensa(it)) },
          toggleFavoriteMensa = { updateSetting(Setting.SetIsFavoriteMensa(it)) },
          hideMensa = { updateSetting(Setting.SetIsHiddenMensa(it)) },
          contentPadding = listPadding,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      entry<Route.Detail>(metadata = ListDetailSceneStrategy.detailPane()) { detail ->
        val mensa = mensasById[detail.mensaId]
        if (mensa != null) {
          MenuList(
            menus = mensa.menus,
            selectedMenuIndex = menuIndices[mensa.mensa.id],
            selectMenu = { selectMenu(mensa.mensa.id, it.index) },
            autoShowImage = detailSettings.autoShowImage,
            contentPadding = listPadding,
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          Box(modifier = Modifier.fillMaxSize()) {
            Text(
              text = stringResource(Res.string.no_menus),
              modifier = Modifier.align(Alignment.Center),
            )
          }
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
          contentPadding = listPadding,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
  )
}
