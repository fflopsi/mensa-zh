package ch.florianfrauenfelder.mensazh.ui

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.florianfrauenfelder.mensazh.models.Destination
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Weekday
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.hiddenMensasFlow
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.saveHiddenMensas
import ch.florianfrauenfelder.mensazh.services.saveShowNextWeek
import ch.florianfrauenfelder.mensazh.services.saveShowOnlyFavoriteMensas
import ch.florianfrauenfelder.mensazh.services.saveShowOnlyOpenMensas
import ch.florianfrauenfelder.mensazh.services.saveShowThisWeek
import ch.florianfrauenfelder.mensazh.services.saveShowTomorrow
import ch.florianfrauenfelder.mensazh.services.saveShownLocations
import ch.florianfrauenfelder.mensazh.services.saveTheme
import ch.florianfrauenfelder.mensazh.services.saveUseDynamicColor
import ch.florianfrauenfelder.mensazh.services.showNextWeekFlow
import ch.florianfrauenfelder.mensazh.services.showOnlyFavoriteMensasFlow
import ch.florianfrauenfelder.mensazh.services.showOnlyOpenMensasFlow
import ch.florianfrauenfelder.mensazh.services.showThisWeekFlow
import ch.florianfrauenfelder.mensazh.services.showTomorrowFlow
import ch.florianfrauenfelder.mensazh.services.shownLocationsFlow
import ch.florianfrauenfelder.mensazh.services.themeFlow
import ch.florianfrauenfelder.mensazh.services.useDynamicColorFlow
import ch.florianfrauenfelder.mensazh.ui.main.MainScreen
import ch.florianfrauenfelder.mensazh.ui.settings.SettingsScreen
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(
  destination: Destination,
  setDestination: (Destination) -> Unit,
  weekday: Weekday,
  setWeekday: (Weekday) -> Unit,
  locations: List<Location>,
  language: MensaProvider.Language,
  setLanguage: (MensaProvider.Language) -> Unit,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val navController = rememberNavController()

  val theme by context.themeFlow.collectAsStateWithLifecycle(initialValue = Prefs.Defaults.THEME)
  val dynamicColor by context.useDynamicColorFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.USE_DYNAMIC_COLOR,
  )
  val showOnlyOpenMensas by context.showOnlyOpenMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_OPEN_MENSAS,
  )
  val showOnlyFavoriteMensas by context.showOnlyFavoriteMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_FAVORITE_MENSAS,
  )
  val shownLocations by context.shownLocationsFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOWN_LOCATIONS,
  )
  val hiddenMensas by context.hiddenMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.HIDDEN_MENSAS,
  )
  val showTomorrow by context.showTomorrowFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_TOMORROW,
  )
  val showThisWeek by context.showThisWeekFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_THIS_WEEK,
  )
  val showNextWeek by context.showNextWeekFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_NEXT_WEEK,
  )

  MensaZHTheme(
    darkTheme = when (theme) {
      1 -> false
      2 -> true
      else -> isSystemInDarkTheme()
    },
    dynamicColor = dynamicColor,
  ) {
    NavHost(
      navController = navController,
      startDestination = Route.Main,
      enterTransition = { slideIn { IntOffset(it.width, 0) } },
      exitTransition = { slideOut { IntOffset(-it.width, 0) } },
      popEnterTransition = { slideIn { IntOffset(-it.width, 0) } },
      popExitTransition = { slideOut { IntOffset(it.width, 0) } },
      modifier = modifier,
    ) {
      composable<Route.Main> {
        MainScreen(
          destination = destination,
          setDestination = setDestination,
          weekday = weekday,
          setWeekday = setWeekday,
          locations = locations.filter { shownLocations.contains(it.id) }
            .sortedBy { shownLocations.indexOf(it.id) },
          hiddenMensas = hiddenMensas,
          language = language,
          setLanguage = setLanguage,
          isRefreshing = isRefreshing,
          onRefresh = onRefresh,
          showOnlyOpenMensas = showOnlyOpenMensas,
          setShowOnlyOpenMensas = { scope.launch { context.saveShowOnlyOpenMensas(it) } },
          showOnlyFavoriteMensas = showOnlyFavoriteMensas,
          setShowOnlyFavoriteMensas = { scope.launch { context.saveShowOnlyFavoriteMensas(it) } },
          showTomorrow = showTomorrow,
          showThisWeek = showThisWeek,
          showNextWeek = showNextWeek,
          navigateToSettings = { navController.navigate(Route.Settings) },
        )
      }
      composable<Route.Settings> {
        SettingsScreen(
          showOnlyOpenMensas = showOnlyOpenMensas,
          setShowOnlyOpenMensas = { scope.launch { context.saveShowOnlyOpenMensas(it) } },
          showOnlyFavoriteMensas = showOnlyFavoriteMensas,
          setShowOnlyFavoriteMensas = { scope.launch { context.saveShowOnlyFavoriteMensas(it) } },
          language = language,
          setLanguage = setLanguage,
          locations = locations,
          shownLocations = locations.filter { shownLocations.contains(it.id) }
            .sortedBy { shownLocations.indexOf(it.id) },
          saveShownLocations = { scope.launch { context.saveShownLocations(it) } },
          hiddenMensas = hiddenMensas,
          saveHiddenMensas = { scope.launch { context.saveHiddenMensas(it) } },
          showTomorrow = showTomorrow,
          saveShowTomorrow = { scope.launch { context.saveShowTomorrow(it) } },
          showThisWeek = showThisWeek,
          saveShowThisWeek = { scope.launch { context.saveShowThisWeek(it) } },
          showNextWeek = showNextWeek,
          saveShowNextWeek = { scope.launch { context.saveShowNextWeek(it) } },
          theme = theme,
          saveTheme = { scope.launch { context.saveTheme(it) } },
          dynamicColor = dynamicColor,
          saveDynamicColor = { scope.launch { context.saveUseDynamicColor(it) } },
          navigateUp = { navController.navigateUp() },
        )
      }
    }
  }
}
