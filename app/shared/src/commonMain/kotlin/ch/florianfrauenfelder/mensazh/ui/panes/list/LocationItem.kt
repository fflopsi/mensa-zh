package ch.florianfrauenfelder.mensazh.ui.panes.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.ui.theme.animateItemModifier

fun LazyListScope.locationItem(
  location: Location,
  detail: DetailSettings,
  onMenuClick: (MensaState, Menu) -> Unit,
  toggleExpandedMensa: (Mensa) -> Unit,
  toggleFavoriteMensa: (Mensa) -> Unit,
  hideMensa: (Mensa) -> Unit,
) {
  item(key = location.id) {
    Text(
      text = location.title,
      modifier = Modifier
        .padding(
          start = 16.dp,
          end = 8.dp,
          top = 16.dp,
          bottom = 4.dp,
        )
        .animateItemModifier(),
    )
  }
  items(
    items = location.mensas,
    key = { it.mensa.id },
  ) { mensa ->
    MensaRow(
      mensa = mensa,
      detail = detail,
      onMenuClick = { onMenuClick(mensa, it) },
      toggleIsExpandedMensa = { toggleExpandedMensa(mensa.mensa) },
      toggleIsFavoriteMensa = { toggleFavoriteMensa(mensa.mensa) },
      hideMensa = { hideMensa(mensa.mensa) },
      modifier = Modifier
        .animateItemModifier()
        .fillMaxWidth(),
    )
  }
}
