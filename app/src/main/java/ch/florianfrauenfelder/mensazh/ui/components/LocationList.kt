package ch.florianfrauenfelder.mensazh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LocationList(
  locations: List<Location>,
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  showOnlyFavoriteMensas: Boolean,
  saveIsFavoriteMensa: (Mensa, Boolean) -> Unit,
  onMenuClick: (Menu) -> Unit,
  modifier: Modifier = Modifier,
  listBottomPadding: Dp = 0.dp,
) {
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier.fillMaxWidth(),
  ) {
    LazyColumn(
      contentPadding = PaddingValues(bottom = listBottomPadding),
      modifier = Modifier.fillMaxWidth(),
    ) {
      item {
        AnimatedVisibility(
          visible = showOnlyFavoriteMensas && !locations.any {
            it.mensas.any { it.state == Mensa.State.Expanded }
          },
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
        ) {
          Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(text = stringResource(R.string.no_expanded_canteens))
          }
        }
      }
      items(locations) {
        AnimatedVisibility(
          visible = !showOnlyFavoriteMensas || it.mensas.any { it.state == Mensa.State.Expanded },
          enter = fadeIn() + expandVertically(),
          exit = fadeOut() + shrinkVertically(),
        ) {
          LocationRow(
            location = it,
            showOnlyFavoriteMensas = showOnlyFavoriteMensas,
            saveIsFavoriteMensa = saveIsFavoriteMensa,
            onMenuClick = onMenuClick,
          )
        }
      }
      item {
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = buildAnnotatedString { append("aha") },
            modifier = Modifier.padding(
              end = 8.dp,
              top = 32.dp,
              bottom = 8.dp,
            ),
          )
        }
      }
    }
  }
}
