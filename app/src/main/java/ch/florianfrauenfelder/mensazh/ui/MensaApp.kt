package ch.florianfrauenfelder.mensazh.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ch.florianfrauenfelder.mensazh.MensaApplication
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import ch.florianfrauenfelder.mensazh.ui.main.MainScreen
import ch.florianfrauenfelder.mensazh.ui.main.MainViewModel
import ch.florianfrauenfelder.mensazh.ui.settings.SettingsScreen
import ch.florianfrauenfelder.mensazh.ui.settings.SettingsViewModel
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme

@Composable
fun MensaApp(theme: ThemeSettings) {
  val context = LocalContext.current
  val container = (context.applicationContext as MensaApplication).container

  val backStack = rememberNavBackStack(Route.Main)
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

  MensaZHTheme(
    darkTheme = when (theme.theme) {
      Theme.Auto -> isSystemInDarkTheme()
      Theme.Light -> false
      Theme.Dark -> true
    },
    dynamicColor = theme.useDynamicColor,
  ) {
    Surface {
      NavDisplay(
        backStack = backStack,
        sceneStrategy = sceneStrategy,
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
        entryProvider = entryProvider {
          entry<Route.Main>(metadata = ListDetailSceneStrategy.listPane() + ListDetailSceneStrategy.detailPane()) {
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(container))
            val params by viewModel.params.collectAsStateWithLifecycle()
            val locations by viewModel.locations.collectAsStateWithLifecycle()
            val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
            val visibilitySettings by viewModel.visibilitySettings.collectAsStateWithLifecycle()
            val destinationSettings by viewModel.destinationSettings.collectAsStateWithLifecycle()
            val detailSettings by viewModel.detailSettings.collectAsStateWithLifecycle()

            MainScreen(
              params = params,
              locations = locations,
              isRefreshing = isRefreshing,
              events = viewModel.events,
              visibilitySettings = visibilitySettings,
              destinationSettings = destinationSettings,
              detailSettings = detailSettings,
              sceneStrategy = sceneStrategy,
              refresh = viewModel::forceRefresh,
              setParams = viewModel::setParams,
              updateSetting = viewModel::updateSetting,
              navigateToSettings = { backStack.add(Route.Settings) },
            )
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

            SettingsScreen(
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
              openSystemSettings = {
                Intent(
                  Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                  Uri.fromParts("package", context.packageName, null),
                ).apply { context.startActivity(this) }
              },
              navigateUp = { backStack.removeLastOrNull() },
            )
          }
        },
      )
    }
  }
}
