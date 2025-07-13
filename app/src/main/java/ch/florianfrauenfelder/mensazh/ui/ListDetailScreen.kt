package ch.florianfrauenfelder.mensazh.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.saveIsFavoriteMensa
import ch.florianfrauenfelder.mensazh.services.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.services.saveShowOnlyFavoriteMensas
import ch.florianfrauenfelder.mensazh.services.showOnlyFavoriteMensasFlow
import ch.florianfrauenfelder.mensazh.ui.detail.MenuList
import ch.florianfrauenfelder.mensazh.ui.list.LocationList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
  locations: List<Location>,
  showMenusInGerman: Boolean,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<Menu>()
  val detailListState = rememberLazyListState()

  val showOnlyFavoriteMensas by context.showOnlyFavoriteMensasFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_ONLY_FAVORITE_MENSAS,
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "${stringResource(R.string.app_name)}${
              if (scaffoldNavigator.currentDestination?.contentKey != null) {
                ": ${(scaffoldNavigator.currentDestination?.contentKey as Menu).mensa!!.title}"
              } else ""
            }",
          )
        },
        actions = {
          var expanded by remember { mutableStateOf(false) }
          Box {
            IconButton(
              onClick = { expanded = !expanded },
              modifier = Modifier.focusable(),
            ) {
              Icon(Icons.Default.MoreHoriz, stringResource(R.string.settings))
            }
            DropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
            ) {
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = stringResource(R.string.show_only_expanded),
                      modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
                    )
                    Checkbox(
                      checked = showOnlyFavoriteMensas,
                      onCheckedChange = {
                        scope.launch { context.saveShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) }
                      },
                    )
                  }
                },
                onClick = {
                  scope.launch { context.saveShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) }
                },
              )
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = stringResource(R.string.show_menus_in_german),
                      modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
                    )
                    Checkbox(
                      checked = showMenusInGerman,
                      onCheckedChange = {
                        scope.launch { context.saveShowMenusInGerman(!showMenusInGerman) }
                      },
                    )
                  }
                },
                onClick = { scope.launch { context.saveShowMenusInGerman(!showMenusInGerman) } },
              )
            }
          }
        },
        scrollBehavior = scrollBehavior,
      )
    },
    floatingActionButton = {
      AnimatedVisibility(visible = scaffoldNavigator.currentDestination?.contentKey != null) {
        ExtendedFloatingActionButton(
          text = { Text(text = stringResource(R.string.open_in_browser)) },
          icon = { Icon(Icons.Default.OpenInBrowser, stringResource(R.string.open_in_browser)) },
          expanded = scrollBehavior.state.collapsedFraction != 1f || !detailListState.canScrollForward,
          onClick = {
            Intent(Intent.ACTION_VIEW).apply {
              data =
                scaffoldNavigator.currentDestination?.contentKey?.mensa!!.url.toString().toUri()
              context.startActivity(this)
            }
          },
        )
      }
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { insets ->

    NavigableListDetailPaneScaffold(
      navigator = scaffoldNavigator,
      listPane = {
        AnimatedPane(modifier = Modifier.preferredWidth(450.dp)) {
          Box {
            AnimatedVisibility(
              visible = isRefreshing,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically(),
            ) {
              LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LocationList(
              locations = locations,
              isRefreshing = isRefreshing,
              onRefresh = onRefresh,
              showOnlyFavoriteMensas = showOnlyFavoriteMensas,
              saveIsFavoriteMensa = { mensa, favorite ->
                scope.launch { context.saveIsFavoriteMensa(mensa, favorite) }
              },
              onMenuClick = {
                scope.launch {
                  scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                }
              },
              listBottomPadding = insets.calculateBottomPadding()
            )
          }
        }
      },
      detailPane = {
        AnimatedPane {
          scaffoldNavigator.currentDestination?.contentKey?.let {
            MenuList(
              menus = it.mensa!!.menus,
              selectedMenu = it,
              listState = detailListState,
              bottomSpacer = true,
              listBottomPadding = insets.calculateBottomPadding(),
            )
          }
        }
      },
      modifier = Modifier
        .consumeWindowInsets(insets)
        .padding(
          top = insets.calculateTopPadding(),
          start = insets.calculateStartPadding(LocalLayoutDirection.current),
          end = insets.calculateEndPadding(LocalLayoutDirection.current),
        ),
    )
  }
}
