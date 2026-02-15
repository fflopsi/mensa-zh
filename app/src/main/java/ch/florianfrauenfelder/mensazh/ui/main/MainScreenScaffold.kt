package ch.florianfrauenfelder.mensazh.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Params
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import ch.florianfrauenfelder.mensazh.ui.main.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.main.list.LocationList
import ch.florianfrauenfelder.mensazh.ui.navigation.Route
import ch.florianfrauenfelder.mensazh.ui.navigation.label

@Composable
fun MainScreenScaffold(
  params: Params,
  locations: List<Location>,
  isRefreshing: Boolean,
  visibilitySettings: VisibilitySettings,
  detailSettings: DetailSettings,
  backStack: NavBackStack<NavKey>,
  sceneStrategy: ListDetailSceneStrategy<NavKey>,
  refresh: () -> Unit,
  setParams: ((Params) -> Params) -> Unit,
  updateSetting: (Setting) -> Unit,
  navigateToSettings: () -> Unit,
  useContentPadding: Boolean = false,
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current

  val selectedMensa by remember(locations, backStack) {
    derivedStateOf {
      locations
        .flatMap { it.mensas }
        .firstOrNull { it.mensa.id == (backStack.last() as? Route.Main.Detail)?.mensa?.id }
    }
  }
  val selectedMenu by remember(locations, backStack) {
    derivedStateOf {
      selectedMensa
        ?.menus
        ?.elementAtOrNull(
          (backStack.last() as? Route.Main.Detail)?.menuIndex ?: return@derivedStateOf null
        )
    }
  }

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val snackbarState = remember { SnackbarHostState() }
  var tabRowSize by remember { mutableStateOf(IntSize.Zero) }

  val snackbarMessage = stringResource(R.string.no_internet_or_menus)
  LaunchedEffect(isRefreshing, locations, params) {
    if (!isRefreshing && locations.flatMap { it.mensas }.flatMap { it.menus }.isEmpty()) {
      snackbarState.showSnackbar(
        message = snackbarMessage,
        withDismissAction = true,
      )
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = selectedMensa?.mensa?.title ?: stringResource(R.string.app_name)) },
        navigationIcon = {
          AnimatedVisibility(
            visible = backStack.size > 1,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
          ) {
            IconButton(onClick = { backStack.removeLastOrNull() }) {
              Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
            }
          }
        },
        actions = {
          AnimatedVisibility(visible = (backStack.last() as? Route.Main.Detail) != null) {
            IconButton(
              onClick = {
                Intent(Intent.ACTION_VIEW).apply {
                  data = selectedMensa?.mensa?.url?.toUri()
                  context.startActivity(this)
                }
              },
            ) {
              Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_in_browser))
            }
          }
          IconButton(onClick = refresh) {
            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
          }
          SettingsDropdown(
            visibility = visibilitySettings,
            setShowOnlyOpenMensas = { updateSetting(Setting.SetShowOnlyOpenMensas(it)) },
            setShowOnlyExpandedMensas = {
              updateSetting(Setting.SetShowOnlyExpandedMensas(it))
            },
            setLanguage = { updateSetting(Setting.SetMenusLanguage(it)) },
            navigateToSettings = navigateToSettings,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarState,
        modifier = Modifier.padding(bottom = with(density) { tabRowSize.height.toDp() }),
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { innerPadding ->
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = refresh,
      modifier = Modifier
        .padding(
          top = innerPadding.calculateTopPadding(),
          bottom = if (useContentPadding) 0.dp else innerPadding.calculateBottomPadding(),
          start = innerPadding.calculateStartPadding(layoutDirection),
        )
        .consumeWindowInsets(innerPadding),
    ) {
      AnimatedVisibility(
        visible = isRefreshing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = Modifier.align(Alignment.TopCenter),
      ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }
      NavDisplay(
        backStack = backStack,
        sceneStrategy = sceneStrategy,
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
          entry<Route.Main.List>(metadata = ListDetailSceneStrategy.listPane()) {
            LocationList(
              locations = locations,
              detail = detailSettings,
              onMenuClick = { mensa, menu ->
                backStack.add(Route.Main.Detail(mensa.mensa, mensa.menus.indexOf(menu)))
              },
              toggleExpandedMensa = { updateSetting(Setting.SetIsExpandedMensa(it)) },
              toggleFavoriteMensa = { updateSetting(Setting.SetIsFavoriteMensa(it)) },
              contentPadding = PaddingValues(
                bottom = if (useContentPadding) innerPadding.calculateBottomPadding() else 0.dp
              ),
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
                contentPadding = PaddingValues(
                  bottom = if (useContentPadding) innerPadding.calculateBottomPadding() else 0.dp
                ),
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        },
        modifier = Modifier
          .padding(bottom = with(density) { tabRowSize.height.toDp() })
          .fillMaxSize(),
      )
      AnimatedVisibility(
        visible = params.destination in listOf(Destination.ThisWeek, Destination.NextWeek),
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = Modifier
          .onSizeChanged { tabRowSize = it }
          .align(Alignment.BottomCenter),
      ) {
        SecondaryTabRow(selectedTabIndex = params.weekday.ordinal) {
          Weekday.entries.forEach { weekday ->
            Tab(
              selected = params.weekday == weekday,
              onClick = { setParams { it.copy(weekday = weekday) } },
              text = { Text(text = stringResource(weekday.label)) },
            )
          }
        }
      }
    }
  }

}
