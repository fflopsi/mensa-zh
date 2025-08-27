package ch.florianfrauenfelder.mensazh.ui

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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Destination
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.models.Weekday
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.saveIsFavoriteMensa
import ch.florianfrauenfelder.mensazh.services.showOnlyFavoriteMensasFlow
import ch.florianfrauenfelder.mensazh.services.showOnlyOpenMensasFlow
import ch.florianfrauenfelder.mensazh.ui.components.SettingsDropdown
import ch.florianfrauenfelder.mensazh.ui.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.list.LocationList
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
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val navigator = rememberListDetailPaneScaffoldNavigator<Menu>()
  val snackbarState = remember { SnackbarHostState() }
  val detailListState = rememberLazyListState()

  val showOnlyOpenMensas by context.showOnlyOpenMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_OPEN_MENSAS,
  )
  val showOnlyFavoriteMensas by context.showOnlyFavoriteMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_FAVORITE_MENSAS,
  )

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
            text = "${stringResource(R.string.app_name)}${
              navigator.currentDestination?.contentKey?.let { ": ${it.mensa!!.title}" } ?: ""
            }",
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
          IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
          }
          SettingsDropdown(
            showOnlyOpenMensas = showOnlyOpenMensas,
            showOnlyFavoriteMensas = showOnlyFavoriteMensas,
            language = language,
            setLanguage = setLanguage,
          )
        },
        scrollBehavior = scrollBehavior,
      )
    },
    floatingActionButton = {
      AnimatedVisibility(visible = navigator.currentDestination?.contentKey != null) {
        ExtendedFloatingActionButton(
          text = { Text(text = stringResource(R.string.open_in_browser)) },
          icon = { Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_in_browser)) },
          expanded = scrollBehavior.state.collapsedFraction != 1f || !detailListState.canScrollForward,
          onClick = {
            Intent(Intent.ACTION_VIEW).apply {
              data = navigator.currentDestination?.contentKey?.mensa!!.url.toString().toUri()
              context.startActivity(this)
            }
          },
        )
      }
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(snackbarState) },
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { insets ->

    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
      modifier = Modifier
        .consumeWindowInsets(insets)
        .padding(top = insets.calculateTopPadding()),
    ) {
      NavigationSuiteScaffold(
        navigationSuiteItems = {
          Destination.entries.forEach {
            item(
              icon = { Icon(it.icon, stringResource(it.label)) },
              label = { Text(stringResource(it.label)) },
              selected = it == destination,
              onClick = { setDestination(it) },
            )
          }
        },
      ) {
        Column {
          NavigableListDetailPaneScaffold(
            navigator = navigator,
            listPane = {
              AnimatedPane(modifier = Modifier.preferredWidth(450.dp)) {
                LocationList(
                  locations = locations,
                  showOnlyOpenMensas = showOnlyOpenMensas,
                  showOnlyFavoriteMensas = showOnlyFavoriteMensas,
                  saveIsFavoriteMensa = { mensa, favorite ->
                    scope.launch { context.saveIsFavoriteMensa(mensa, favorite) }
                  },
                  onMenuClick = {
                    scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }
                  },
                  listBottomPadding = insets.calculateBottomPadding(),
                )
              }
            },
            detailPane = {
              AnimatedPane {
                navigator.currentDestination?.contentKey?.let {
                  MenuList(
                    menus = it.mensa!!.menus,
                    selectedMenu = it,
                    selectMenu = { menu ->
                      scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, menu) }
                    },
                    listState = detailListState,
                    bottomSpacer = true,
                    listBottomPadding = insets.calculateBottomPadding(),
                  )
                }
              }
            },
            modifier = Modifier
              .padding(
                start = insets.calculateStartPadding(LocalLayoutDirection.current),
                end = insets.calculateEndPadding(LocalLayoutDirection.current),
              )
              .weight(1f),
          )
          AnimatedVisibility(
            visible = destination in listOf(Destination.ThisWeek, Destination.NextWeek),
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
