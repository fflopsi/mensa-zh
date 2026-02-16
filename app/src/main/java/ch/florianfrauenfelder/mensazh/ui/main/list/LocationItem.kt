package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
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
        .animateItem(
          fadeInSpec = spring(stiffness = Spring.StiffnessHigh),
          fadeOutSpec = spring(stiffness = Spring.StiffnessHigh),
          placementSpec = spring(stiffness = Spring.StiffnessHigh),
        ),
    )
  }
  items(
    items = location.mensas,
    key = { it.mensa.id },
  ) { mensa ->
    Box(
      modifier = Modifier
        .animateContentSize(
          animationSpec = spring(stiffness = Spring.StiffnessHigh),
        )
        .animateItem(
          fadeInSpec = spring(stiffness = Spring.StiffnessHigh),
          fadeOutSpec = spring(stiffness = Spring.StiffnessHigh),
          placementSpec = spring(stiffness = Spring.StiffnessHigh),
        )
        .fillMaxWidth(),
    ) {
      MensaRow(
        mensa = mensa,
        detail = detail,
        onMenuClick = { onMenuClick(mensa, it) },
        toggleIsExpandedMensa = { toggleExpandedMensa(mensa.mensa) },
        toggleIsFavoriteMensa = { toggleFavoriteMensa(mensa.mensa) },
        hideMensa = { hideMensa(mensa.mensa) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
