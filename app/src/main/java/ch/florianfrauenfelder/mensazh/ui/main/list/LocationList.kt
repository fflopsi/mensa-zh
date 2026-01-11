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
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.ui.shared.InfoLinks
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LocationList(
  locations: List<Location>,
  hiddenMensas: List<UUID>,
  saveFavoriteMensas: (List<Mensa>) -> Unit,
  showOnlyOpenMensas: Boolean,
  showOnlyExpandedMensas: Boolean,
  saveIsExpandedMensa: (Mensa, Boolean) -> Unit,
  listUseShortDescription: Boolean,
  listShowAllergens: Boolean,
  onMenuClick: (MensaState, Menu) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  val showEmptyNotice by remember(
    locations,
    hiddenMensas,
    showOnlyOpenMensas,
    showOnlyExpandedMensas,
  ) {
    derivedStateOf {
      locations.isEmpty()
        || locations.flatMap { it.mensas }.all { hiddenMensas.contains(it.mensa.id) }
        || (showOnlyOpenMensas && locations.flatMap { it.mensas }
        .filter { !hiddenMensas.contains(it.mensa.id) }.all { it.state == MensaState.State.Closed })
        || (showOnlyExpandedMensas && locations.flatMap { it.mensas }
        .filter { !hiddenMensas.contains(it.mensa.id) }
        .none { it.state == MensaState.State.Expanded })
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
        location, hiddenMensas, showOnlyOpenMensas, showOnlyExpandedMensas,
      ) {
        derivedStateOf {
          !(location.mensas.all { hiddenMensas.contains(it.mensa.id) }
            || (showOnlyOpenMensas && location.mensas.filter { !hiddenMensas.contains(it.mensa.id) }
            .all { it.state == MensaState.State.Closed })
            || (showOnlyExpandedMensas && location.mensas.filter { !hiddenMensas.contains(it.mensa.id) }
            .none { it.state == MensaState.State.Expanded }))
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
          showOnlyExpandedMensas = showOnlyExpandedMensas,
          saveIsExpandedMensa = saveIsExpandedMensa,
          listUseShortDescription = listUseShortDescription,
          listShowAllergens = listShowAllergens,
          onMenuClick = onMenuClick,
          favoriteMensas = locations.first().mensas.map { it.mensa },
          saveFavoriteMensas = saveFavoriteMensas,
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
