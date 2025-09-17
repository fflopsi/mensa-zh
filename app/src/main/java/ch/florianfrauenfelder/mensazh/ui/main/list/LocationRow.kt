package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import java.util.UUID

@Composable
fun LocationRow(
  location: Location,
  hiddenMensas: List<UUID>,
  showOnlyOpenMensas: Boolean,
  showOnlyFavoriteMensas: Boolean,
  saveIsFavoriteMensa: (Mensa, Boolean) -> Unit,
  onMenuClick: (Menu) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
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
        val showMensa by remember(it, hiddenMensas, showOnlyOpenMensas, showOnlyFavoriteMensas) {
          derivedStateOf {
            !(hiddenMensas.contains(it.id)
              || (showOnlyOpenMensas && it.state == Mensa.State.Closed)
              || (showOnlyFavoriteMensas && it.state != Mensa.State.Expanded))
          }
        }

        AnimatedVisibility(visible = showMensa) {
          MensaRow(
            mensa = it,
            saveIsFavoriteMensa = saveIsFavoriteMensa,
            onMenuClick = onMenuClick,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
