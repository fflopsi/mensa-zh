package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.ui.shared.InfoLinks

@Composable
fun LocationList(
  locations: List<Location>,
  detail: DetailSettings,
  onMenuClick: (MensaState, Menu) -> Unit,
  toggleExpandedMensa: (Mensa) -> Unit,
  toggleFavoriteMensa: (Mensa) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier,
  ) {
    item(key = -1) {
      if (locations.isEmpty()) {
        Row(
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .animateItem()
            .fillMaxWidth(),
        ) {
          Text(text = stringResource(R.string.no_expanded_canteens))
        }
      }
    }
    items(
      items = locations,
      key = { it.id },
    ) { location ->
      LocationRow(
        location = location,
        detail = detail,
        onMenuClick = onMenuClick,
        toggleExpandedMensa = toggleExpandedMensa,
        toggleFavoriteMensa = toggleFavoriteMensa,
        modifier = Modifier
          .animateItem()
          .fillMaxWidth(),
      )
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
          .animateItem()
          .fillMaxWidth(),
      )
    }
  }
}
