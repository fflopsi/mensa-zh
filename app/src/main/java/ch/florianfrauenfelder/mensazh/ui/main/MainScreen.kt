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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.saveIsExpandedMensa
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday
import ch.florianfrauenfelder.mensazh.ui.main.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.main.list.LocationList
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreen(
  destination: Destination,
  setDestination: (Destination) -> Unit,
  weekday: Weekday,
  setWeekday: (Weekday) -> Unit,
  locations: List<Location>,
  hiddenMensas: List<UUID>,
  language: MensaProvider.Language,
  setLanguage: (MensaProvider.Language) -> Unit,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  showOnlyOpenMensas: Boolean,
  setShowOnlyOpenMensas: (Boolean) -> Unit,
  showOnlyExpandedMensas: Boolean,
  setShowOnlyExpandedMensas: (Boolean) -> Unit,
  showTomorrow: Boolean,
  showThisWeek: Boolean,
  showNextWeek: Boolean,
  listUseShortDescription: Boolean,
  listShowAllergens: Boolean,
  navigateToSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val navigator = rememberListDetailPaneScaffoldNavigator<Menu>()
  val snackbarState = remember { SnackbarHostState() }

  var tabRowSize by remember { mutableStateOf(IntSize.Zero) }

  LaunchedEffect(isRefreshing) {
    if (!isRefreshing && locations.flatMap { it.mensas }.flatMap { it.menus }.isEmpty()) {
      snackbarState.showSnackbar(
        message = context.getString(R.string.no_internet_or_menus),
        withDismissAction = true,
      )
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = navigator.currentDestination?.contentKey?.mensa?.title ?: stringResource(R.string.app_name),
          )
        },
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
                  data = navigator.currentDestination?.contentKey?.mensa?.url.toString().toUri()
                  context.startActivity(this)
                }
              },
            ) {
              Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_in_browser))
            }
          }
          IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
          }
          SettingsDropdown(
            showOnlyOpenMensas = showOnlyOpenMensas,
            setShowOnlyOpenMensas = { setShowOnlyOpenMensas(it) },
            showOnlyExpandedMensas = showOnlyExpandedMensas,
            setShowOnlyExpandedMensas = setShowOnlyExpandedMensas,
            language = language,
            setLanguage = setLanguage,
            navigateToSettings = navigateToSettings,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { insets ->

    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
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
                  hiddenMensas = hiddenMensas,
                  showOnlyOpenMensas = showOnlyOpenMensas,
                  showOnlyExpandedMensas = showOnlyExpandedMensas,
                  saveIsExpandedMensa = { mensa, expanded ->
                    scope.launch { context.saveIsExpandedMensa(mensa, expanded) }
                  },
                  listUseShortDescription = listUseShortDescription,
                  listShowAllergens = listShowAllergens,
                  onMenuClick = {
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }
                  },
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            },
            detailPane = {
              AnimatedPane {
                navigator.currentDestination?.contentKey?.let {
                  MenuList(
                    menus = it.mensa?.menus ?: emptyList(),
                    selectedMenu = it,
                    selectMenu = { menu ->
                      scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, menu)
                      }
                    },
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
              }
            },
            modifier = Modifier.weight(1f),
          )
          AnimatedVisibility(
            visible = destination in listOf(Destination.ThisWeek, Destination.NextWeek),
            modifier = Modifier.onSizeChanged { tabRowSize = it },
          ) {
            SecondaryTabRow(selectedTabIndex = weekday.ordinal) {
              Weekday.entries.forEach {
                Tab(
                  selected = weekday == it,
                  onClick = { setWeekday(it) },
                  text = { Text(text = stringResource(it.label)) },
                )
              }
            }
          }
        }
      }

      if (showTomorrow || showThisWeek || showNextWeek) {
        NavigationSuiteScaffold(
          navigationSuiteItems = {
            buildList {
              add(Destination.Today)
              if (showTomorrow) add(Destination.Tomorrow)
              if (showThisWeek) add(Destination.ThisWeek)
              if (showNextWeek) add(Destination.NextWeek)
            }.forEach {
              item(
                icon = { Icon(it.icon, stringResource(it.label)) },
                label = { Text(stringResource(it.label)) },
                selected = it == destination,
                onClick = {
                  if (it != destination) {
                    setDestination(it)
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
