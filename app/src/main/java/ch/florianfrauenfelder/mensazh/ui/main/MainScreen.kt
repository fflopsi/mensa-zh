package ch.florianfrauenfelder.mensazh.ui.main

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.navigation.NavigationDetail
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.ui.main.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.main.list.LocationList
import ch.florianfrauenfelder.mensazh.ui.navigation.label
import ch.florianfrauenfelder.mensazh.ui.navigation.ui
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
  viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
  navigateToSettings: () -> Unit,
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()

  val visibility by viewModel.visibilitySettings.collectAsStateWithLifecycle()
  val destination by viewModel.destinationSettings.collectAsStateWithLifecycle()
  val detail by viewModel.detailSettings.collectAsStateWithLifecycle()

  val params by viewModel.params.collectAsStateWithLifecycle()
  val locations by viewModel.locations.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val navigator = rememberListDetailPaneScaffoldNavigator<NavigationDetail>()
  val selectedMensa by remember(locations, navigator.currentDestination?.contentKey) {
    derivedStateOf {
      locations
        .flatMap { it.mensas }
        .firstOrNull { it.mensa.id == navigator.currentDestination?.contentKey?.mensaId }
    }
  }
  val selectedMenu by remember(locations, navigator.currentDestination?.contentKey) {
    derivedStateOf {
      selectedMensa
        ?.menus
        ?.elementAtOrNull(
          navigator.currentDestination?.contentKey?.menuIndex ?: return@derivedStateOf null
        )
    }
  }
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

  LaunchedEffect(Unit) {
    viewModel.deleteExpired()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = selectedMensa?.mensa?.title ?: stringResource(R.string.app_name)) },
        navigationIcon = {
          AnimatedVisibility(
            visible = navigator.canNavigateBack(),
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
          ) {
            IconButton(onClick = { scope.launch { navigator.navigateBack() } }) {
              Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
            }
          }
        },
        actions = {
          AnimatedVisibility(visible = navigator.currentDestination?.contentKey != null) {
            IconButton(
              onClick = {
                Intent(Intent.ACTION_VIEW).apply {
                  data = selectedMensa?.mensa?.url.toString().toUri()
                  context.startActivity(this)
                }
              },
            ) {
              Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_in_browser))
            }
          }
          IconButton(onClick = viewModel::forceRefresh) {
            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
          }
          SettingsDropdown(
            visibility = visibility,
            setShowOnlyOpenMensas = { viewModel.updateSetting(Setting.SetShowOnlyOpenMensas(it)) },
            setShowOnlyExpandedMensas = {
              viewModel.updateSetting(Setting.SetShowOnlyExpandedMensas(it))
            },
            setLanguage = { viewModel.updateSetting(Setting.SetMenusLanguage(it)) },
            navigateToSettings = navigateToSettings,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { insets ->

    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = viewModel::forceRefresh,
      modifier = Modifier
        .padding(insets)
        .consumeWindowInsets(insets),
    ) {
      @Composable
      fun InnerScaffold() = Scaffold(
        snackbarHost = {
          SnackbarHost(
            hostState = snackbarState,
            modifier = Modifier.padding(bottom = with(density) { tabRowSize.height.toDp() }),
          )
        },
      ) { innerInsets ->
        Column(
          modifier = Modifier
            .padding(innerInsets)
            .consumeWindowInsets(innerInsets),
        ) {
          NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
              AnimatedPane(modifier = Modifier.preferredWidth(450.dp)) {
                LocationList(
                  locations = locations,
                  toggleFavoriteMensa = { viewModel.updateSetting(Setting.SetIsFavoriteMensa(it)) },
                  saveIsExpandedMensa = { mensa, expanded ->
                    viewModel.updateSetting(Setting.SetIsExpandedMensa(mensa, expanded))
                  },
                  detail = detail,
                  onMenuClick = { mensa, menu ->
                    scope.launch {
                      navigator.navigateTo(
                        ListDetailPaneScaffoldRole.Detail,
                        NavigationDetail(mensa.mensa.id, mensa.menus.indexOf(menu)),
                      )
                    }
                  },
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            },
            detailPane = {
              AnimatedPane {
                selectedMensa?.let { mensa ->
                  MenuList(
                    menus = mensa.menus,
                    selectedMenu = selectedMenu,
                    selectMenu = { menu ->
                      scope.launch {
                        navigator.navigateTo(
                          ListDetailPaneScaffoldRole.Detail,
                          NavigationDetail(mensa.mensa.id, mensa.menus.indexOf(menu)),
                        )
                      }
                    },
                    autoShowImage = detail.autoShowImage,
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
              }
            },
            modifier = Modifier.weight(1f),
          )
          AnimatedVisibility(
            visible = params.destination in listOf(Destination.ThisWeek, Destination.NextWeek),
            modifier = Modifier.onSizeChanged { tabRowSize = it },
          ) {
            SecondaryTabRow(selectedTabIndex = params.weekday.ordinal) {
              Weekday.entries.forEach {
                Tab(
                  selected = params.weekday == it,
                  onClick = { viewModel.setNew(it) },
                  text = { Text(text = stringResource(it.label)) },
                )
              }
            }
          }
        }
      }

      if (destination.showAny) {
        NavigationSuiteScaffold(
          navigationSuiteItems = {
            buildList {
              add(Destination.Today)
              if (destination.showTomorrow) add(Destination.Tomorrow)
              if (destination.showThisWeek) add(Destination.ThisWeek)
              if (destination.showNextWeek) add(Destination.NextWeek)
            }.forEach {
              item(
                icon = { Icon(it.ui.icon, stringResource(it.ui.label)) },
                label = { Text(stringResource(it.ui.label)) },
                selected = it == params.destination,
                onClick = {
                  if (it != params.destination) {
                    viewModel.setNew(it)
                  } else if (navigator.canNavigateBack()) {
                    scope.launch { navigator.navigateBack() }
                  }
                },
              )
            }
          },
        ) {
          InnerScaffold()
        }
      } else {
        InnerScaffold()
      }

      AnimatedVisibility(
        visible = isRefreshing,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }
    }
  }
}
