package ch.florianfrauenfelder.mensazh.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.data.local.datastore.Prefs
import ch.florianfrauenfelder.mensazh.data.local.datastore.autoShowImageFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.favoriteMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.hiddenMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.listShowAllergensFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.listUseShortDescriptionFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveAutoShowImage
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveFavoriteMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveHiddenMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveListShowAllergens
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveListUseShortDescription
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowNextWeek
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowOnlyExpandedMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowOnlyOpenMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowThisWeek
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowTomorrow
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShownLocations
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveTheme
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveUseDynamicColor
import ch.florianfrauenfelder.mensazh.data.local.datastore.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showNextWeekFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showOnlyExpandedMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showOnlyOpenMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showThisWeekFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showTomorrowFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.shownLocationsFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.themeFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.useDynamicColorFlow
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.showMenusInGermanToLanguage
import ch.florianfrauenfelder.mensazh.ui.main.MainScreen
import ch.florianfrauenfelder.mensazh.ui.navigation.Route
import ch.florianfrauenfelder.mensazh.ui.settings.SettingsScreen
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(
  destination: Destination,
  setDestination: (Destination) -> Unit,
  weekday: Weekday,
  setWeekday: (Weekday) -> Unit,
  locations: List<Location>,
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
  val showOnlyExpandedMensas by context.showOnlyExpandedMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_EXPANDED_MENSAS,
  )
  val language by context.showMenusInGermanFlow.map { it.showMenusInGermanToLanguage }
    .collectAsStateWithLifecycle(initialValue = Language.default)
  val shownLocationsIds by context.shownLocationsFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOWN_LOCATIONS,
  )
  val favoriteMensas by context.favoriteMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.FAVORITE_MENSAS,
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
  val listUseShortDescription by context.listUseShortDescriptionFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.LIST_USE_SHORT_DESCRIPTION,
  )
  val listShowAllergens by context.listShowAllergensFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.LIST_SHOW_ALLERGENS,
  )
  val autoShowImage by context.autoShowImageFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.AUTO_SHOW_IMAGE,
  )

  val shownLocations by remember(locations, shownLocationsIds) {
    derivedStateOf {
      locations
        .filter { shownLocationsIds.contains(it.id) }
        .sortedBy { shownLocationsIds.indexOf(it.id) }
    }
  }

  val favoriteLocationTitle = stringResource(R.string.favorites)
  val listedLocations by remember(locations, shownLocationsIds, favoriteMensas) {
    derivedStateOf {
      shownLocations.map { location ->
        location.copy(
          id = location.id,
          title = location.title,
          mensas = location.mensas.filter { !favoriteMensas.contains(it.mensa.id) },
        )
      }.toMutableStateList().apply {
        add(
          0,
          Location(
            id = UUID.randomUUID(),
            title = favoriteLocationTitle,
            mensas = locations
              .flatMap { it.mensas }
              .filter { favoriteMensas.contains(it.mensa.id) }
              .sortedBy { favoriteMensas.indexOf(it.mensa.id) },
          ),
        )
      }
    }
  }

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
          locations = listedLocations,
          hiddenMensas = hiddenMensas,
          saveFavoriteMensas = { scope.launch { context.saveFavoriteMensas(it) } },
          language = language,
          setLanguage = { scope.launch { context.saveShowMenusInGerman(it.showMenusInGerman) } },
          isRefreshing = isRefreshing,
          onRefresh = onRefresh,
          showOnlyOpenMensas = showOnlyOpenMensas,
          setShowOnlyOpenMensas = { scope.launch { context.saveShowOnlyOpenMensas(it) } },
          showOnlyExpandedMensas = showOnlyExpandedMensas,
          setShowOnlyExpandedMensas = { scope.launch { context.saveShowOnlyExpandedMensas(it) } },
          showTomorrow = showTomorrow,
          showThisWeek = showThisWeek,
          showNextWeek = showNextWeek,
          listUseShortDescription = listUseShortDescription,
          listShowAllergens = listShowAllergens,
          autoShowImage = autoShowImage,
          navigateToSettings = { navController.navigate(Route.Settings) },
        )
      }
      composable<Route.Settings> {
        SettingsScreen(
          showOnlyOpenMensas = showOnlyOpenMensas,
          setShowOnlyOpenMensas = { scope.launch { context.saveShowOnlyOpenMensas(it) } },
          showOnlyExpandedMensas = showOnlyExpandedMensas,
          setShowOnlyExpandedMensas = { scope.launch { context.saveShowOnlyExpandedMensas(it) } },
          language = language,
          setLanguage = { scope.launch { context.saveShowMenusInGerman(it.showMenusInGerman) } },
          locations = locations,
          shownLocations = shownLocations,
          saveShownLocations = { scope.launch { context.saveShownLocations(it) } },
          favoriteMensas = locations
            .flatMap { location -> location.mensas.map { it.mensa } }
            .filter { favoriteMensas.contains(it.id) && !hiddenMensas.contains(it.id) }
            .sortedBy { favoriteMensas.indexOf(it.id) },
          saveFavoriteMensas = { scope.launch { context.saveFavoriteMensas(it) } },
          hiddenMensas = shownLocations
            .flatMap { location -> location.mensas.map { it.mensa } }
            .filter { hiddenMensas.contains(it.id) && !favoriteMensas.contains(it.id) },
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
          listUseShortDescription = listUseShortDescription,
          saveListUseShortDescription = { scope.launch { context.saveListUseShortDescription(it) } },
          listShowAllergens = listShowAllergens,
          saveListShowAllergens = { scope.launch { context.saveListShowAllergens(it) } },
          autoShowImage = autoShowImage,
          saveAutoShowImage = { scope.launch { context.saveAutoShowImage(it) } },
          openSystemSettings = {
            Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.fromParts("package", context.packageName, null),
            ).apply { context.startActivity(this) }
          },
          navigateUp = { navController.navigateUp() },
        )
      }
    }
  }
}
