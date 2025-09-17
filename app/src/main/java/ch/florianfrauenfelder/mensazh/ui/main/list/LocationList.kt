package ch.florianfrauenfelder.mensazh.ui.main.list

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.ui.components.InfoLinks
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LocationList(
  locations: List<Location>,
  hiddenMensas: List<UUID>,
  showOnlyOpenMensas: Boolean,
  showOnlyFavoriteMensas: Boolean,
  saveIsFavoriteMensa: (Mensa, Boolean) -> Unit,
  onMenuClick: (Menu) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  val showEmptyNotice by remember(
    locations,
    hiddenMensas,
    showOnlyOpenMensas,
    showOnlyFavoriteMensas,
  ) {
    derivedStateOf {
      locations.isEmpty()
        || locations.flatMap { it.mensas }.all { hiddenMensas.contains(it.id) }
        || (showOnlyOpenMensas && locations.flatMap { it.mensas }
        .filter { !hiddenMensas.contains(it.id) }.all { it.state == Mensa.State.Closed })
        || (showOnlyFavoriteMensas && locations.flatMap { it.mensas }
        .filter { !hiddenMensas.contains(it.id) }.none { it.state == Mensa.State.Expanded })
    }
  }

  LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier,
  ) {
    item(key = -1) {
      AnimatedVisibility(
        visible = showEmptyNotice,
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
    items(
      items = locations,
      key = { it.title },
    ) { location ->
      val showLocation by remember(
        location,
        hiddenMensas,
        showOnlyOpenMensas,
        showOnlyFavoriteMensas
      ) {
        derivedStateOf {
          !(location.mensas.all { hiddenMensas.contains(it.id) }
            || (showOnlyOpenMensas && location.mensas.filter { !hiddenMensas.contains(it.id) }
            .all { it.state == Mensa.State.Closed })
            || (showOnlyFavoriteMensas && location.mensas.filter { !hiddenMensas.contains(it.id) }
            .none { it.state == Mensa.State.Expanded }))
        }
      }

      AnimatedVisibility(
        visible = showLocation,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        LocationRow(
          location = location,
          hiddenMensas = hiddenMensas,
          showOnlyOpenMensas = showOnlyOpenMensas,
          showOnlyFavoriteMensas = showOnlyFavoriteMensas,
          saveIsFavoriteMensa = saveIsFavoriteMensa,
          onMenuClick = onMenuClick,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
    item(key = 0) {
      InfoLinks(
        modifier = Modifier
          .padding(
            start = 8.dp,
            end = 8.dp,
            top = 32.dp,
            bottom = 8.dp,
          )
          .fillMaxWidth(),
      )
    }
  }
}
