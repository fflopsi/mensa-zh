package ch.florianfrauenfelder.mensazh.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldValue
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack
import ch.florianfrauenfelder.mensazh.AppContainer
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.value.Event
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import ch.florianfrauenfelder.mensazh.ui.domain.label
import ch.florianfrauenfelder.mensazh.ui.domain.ui
import ch.florianfrauenfelder.mensazh.ui.shared.OpenInBrowserButton
import ch.florianfrauenfelder.mensazh.ui.shared.SettingsDropdown
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import ch.florianfrauenfelder.mensazh.ui.theme.myMotionSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.api_error
import mensazh.app.shared.generated.resources.app_name
import mensazh.app.shared.generated.resources.back
import mensazh.app.shared.generated.resources.cancel
import mensazh.app.shared.generated.resources.ic_arrow_back_24
import mensazh.app.shared.generated.resources.ic_open_in_browser_24
import mensazh.app.shared.generated.resources.ic_refresh_24
import mensazh.app.shared.generated.resources.no_internet
import mensazh.app.shared.generated.resources.open_in_browser
import mensazh.app.shared.generated.resources.refresh
import mensazh.app.shared.generated.resources.settings
import mensazh.app.shared.generated.resources.slow_internet
import mensazh.app.shared.generated.resources.unknown_error
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MensaApp(container: AppContainer) {
  val layoutDirection = LocalLayoutDirection.current

  val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(container))
  val params by appViewModel.params.collectAsStateWithLifecycle()
  val menuIndices by appViewModel.menuIndices.collectAsStateWithLifecycle()
  val locations by appViewModel.locations.collectAsStateWithLifecycle()
  val mensasById by appViewModel.mensasById.collectAsStateWithLifecycle()
  val isRefreshing by appViewModel.isRefreshing.collectAsStateWithLifecycle()
  val visibilitySettings by appViewModel.visibilitySettings.collectAsStateWithLifecycle()
  val destinationSettings by appViewModel.destinationSettings.collectAsStateWithLifecycle()
  val detailSettings by appViewModel.detailSettings.collectAsStateWithLifecycle()
  val themeSettings by appViewModel.themeSettings.collectAsStateWithLifecycle()
  fun updateSetting(setting: Setting) = appViewModel.updateSetting(setting)

  val backStack = rememberNavBackStack(routeConfig, Route.List)
  val selectedMensa =
    (backStack.lastOrNull { it is Route.Detail } as? Route.Detail)?.let { mensasById[it.mensaId] }

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val snackbarState = remember { SnackbarHostState() }
  val navSuiteScaffoldState = rememberNavigationSuiteScaffoldState(
    initialValue = if (destinationSettings.showAny) {
      NavigationSuiteScaffoldValue.Visible
    } else {
      NavigationSuiteScaffoldValue.Hidden
    }
  )
  LaunchedEffect(destinationSettings.showAny) {
    if (destinationSettings.showAny) {
      navSuiteScaffoldState.show()
    } else {
      navSuiteScaffoldState.hide()
    }
  }

  val noInternetMessage = stringResource(Res.string.no_internet)
  val apiErrorMessage = stringResource(Res.string.api_error)
  val unknownErrorMessage = stringResource(Res.string.unknown_error)
  val slowInternetMessage = stringResource(Res.string.slow_internet)
  val cancelMessage = stringResource(Res.string.cancel)
  val slowInternetSnackbarJobs = remember { mutableStateListOf<Job?>() }
  LaunchedEffect(Unit) {
    appViewModel.events.collect { event ->
      when (event) {
        Event.NoInternet -> {
          snackbarState.showSnackbar(message = noInternetMessage, withDismissAction = true)
        }
        Event.ApiError -> {
          snackbarState.showSnackbar(message = apiErrorMessage, withDismissAction = true)
        }
        Event.UnknownError -> {
          snackbarState.showSnackbar(message = unknownErrorMessage, withDismissAction = true)
        }
        is Event.SlowInternet -> {
          slowInternetSnackbarJobs.add(
            this@LaunchedEffect.launch {
              val result = snackbarState.showSnackbar(
                message = slowInternetMessage,
                actionLabel = cancelMessage,
              )
              if (result == SnackbarResult.ActionPerformed) {
                event.onCancel()
              }
            }
          )
        }
        Event.DismissSlowInternet -> {
          slowInternetSnackbarJobs.firstOrNull { it?.isActive == true }?.cancel()
        }
      }
    }
  }

  LaunchedEffect(slowInternetSnackbarJobs) {
    slowInternetSnackbarJobs.forEach {
      if (it?.isActive == false) slowInternetSnackbarJobs.remove(it)
    }
  }

  MensaZHTheme(
    darkTheme = when (themeSettings.theme) {
      Theme.Auto -> isSystemInDarkTheme()
      Theme.Light -> false
      Theme.Dark -> true
    },
    dynamicColor = themeSettings.useDynamicColor,
    expressiveAnimations = themeSettings.useExpressiveAnimations,
  ) {
    NavigationSuiteScaffold(
      state = navSuiteScaffoldState,
      navigationItems = {
        buildList {
          add(Destination.Today)
          if (destinationSettings.showTomorrow) add(Destination.Tomorrow)
          if (destinationSettings.showThisWeek) add(Destination.ThisWeek)
          if (destinationSettings.showNextWeek) add(Destination.NextWeek)
        }.forEach { destination ->
          NavigationSuiteItem(
            icon = { Icon(painterResource(destination.ui.icon), null) },
            label = { Text(stringResource(destination.ui.label)) },
            selected = destination == params.destination,
            onClick = {
              if (destination != params.destination) {
                appViewModel.setParams { it.copy(destination = destination) }
              } else if (backStack.size > 1) {
                backStack.removeLastOrNull()
              }
            },
          )
        }
      },
    ) {
      Scaffold(
        topBar = {
          TopAppBar(
            title = {
              Text(
                text = when (backStack.lastOrNull()) {
                  is Route.Detail -> {
                    selectedMensa?.mensa?.title ?: stringResource(Res.string.app_name)
                  }
                  Route.Settings -> stringResource(Res.string.settings)
                  else -> stringResource(Res.string.app_name)
                },
              )
            },
            navigationIcon = {
              AnimatedVisibility(
                visible = backStack.size > 1,
                enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
              ) {
                IconButton(onClick = { backStack.removeLastOrNull() }) {
                  Icon(
                    painterResource(Res.drawable.ic_arrow_back_24),
                    stringResource(Res.string.back),
                  )
                }
              }
            },
            actions = {
              AnimatedVisibility(
                visible = (backStack.lastOrNull() as? Route.Detail) != null,
                enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
              ) {
                OpenInBrowserButton(selectedMensa = selectedMensa) {
                  Icon(
                    painterResource(Res.drawable.ic_open_in_browser_24),
                    stringResource(Res.string.open_in_browser),
                  )
                }
              }
              IconButton(onClick = appViewModel::forceRefresh) {
                Icon(
                  painterResource(Res.drawable.ic_refresh_24),
                  stringResource(Res.string.refresh),
                )
              }
              AnimatedVisibility(
                visible = (backStack.lastOrNull() as? Route.Settings) == null,
                enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
                exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec()),
              ) {
                SettingsDropdown(
                  visibility = visibilitySettings,
                  setShowOnlyOpenMensas = { updateSetting(Setting.SetShowOnlyOpenMensas(it)) },
                  setShowOnlyExpandedMensas = { updateSetting(Setting.SetShowOnlyExpandedMensas(it)) },
                  setLanguage = { updateSetting(Setting.SetMenusLanguage(it)) },
                  navigateToSettings = { backStack.moveToTopOrAdd(Route.Settings) },
                )
              }
            },
            scrollBehavior = scrollBehavior,
          )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      ) { innerPadding ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              start = innerPadding.calculateStartPadding(layoutDirection),
              top = innerPadding.calculateTopPadding(),
              end = innerPadding.calculateEndPadding(layoutDirection),
            )
            .consumeWindowInsets(innerPadding),
        ) {
          val state = rememberPullToRefreshState()
          PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = appViewModel::forceRefresh,
            enabled = thisPlatform.isMobile,
            state = state,
            indicator = {
              PullToRefreshDefaults.LoadingIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
              )
            },
            modifier = Modifier.weight(1f).fillMaxSize(),
          ) {
            MensaAppNavDisplay(
              backStack = backStack,
              container = container,
              locations = locations,
              mensasById = mensasById,
              menuIndices = menuIndices,
              selectMenu = appViewModel::selectMenu,
              detailSettings = detailSettings,
              updateSetting = ::updateSetting,
              innerPadding = innerPadding,
            )
            androidx.compose.animation.AnimatedVisibility(
              visible = isRefreshing,
              enter = expandVertically(myMotionSpec()),
              exit = shrinkVertically(myMotionSpec()),
              modifier = Modifier.align(Alignment.TopCenter),
            ) {
              LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            SnackbarHost(
              hostState = snackbarState,
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = innerPadding.calculateBottomPadding()),
            )
          }
          AnimatedVisibility(
            visible = params.destination in listOf(Destination.ThisWeek, Destination.NextWeek),
            enter = expandVertically(myMotionSpec()),
            exit = shrinkVertically(myMotionSpec()),
          ) {
            SecondaryTabRow(selectedTabIndex = params.weekday.ordinal) {
              Weekday.entries.forEach { weekday ->
                Tab(
                  selected = params.weekday == weekday,
                  onClick = { appViewModel.setParams { it.copy(weekday = weekday) } },
                  text = { Text(text = stringResource(weekday.label)) },
                )
              }
            }
          }
        }
      }
    }
  }
}
