package ch.florianfrauenfelder.mensazh.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun LocationRow(
  location: Location,
  showOnlyFavoriteMensas: Boolean,
  saveIsFavoriteMensa: (Mensa, Boolean) -> Unit,
  onMenuClick: (Menu) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = location.title,
      modifier = Modifier.padding(
        start = 16.dp,
        end = 8.dp,
        top = 16.dp,
        bottom = 4.dp,
      ),
    )
    Column(modifier = Modifier.fillMaxWidth()) {
      location.mensas.forEach {
        AnimatedVisibility(visible = !showOnlyFavoriteMensas || it.state == Mensa.State.Expanded) {
          MensaRow(
            mensa = it,
            saveIsFavoriteMensa = saveIsFavoriteMensa,
            onMenuClick = onMenuClick,
          )
        }
      }
    }
  }
}
