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
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import java.util.UUID

@Composable
fun LocationRow(
  location: Location,
  hiddenMensas: List<UUID>,
  showOnlyOpenMensas: Boolean,
  showOnlyExpandedMensas: Boolean,
  saveIsExpandedMensa: (Mensa, Boolean) -> Unit,
  listUseShortDescription: Boolean,
  listShowAllergens: Boolean,
  onMenuClick: (MensaState, Menu) -> Unit,
  favoriteMensas: List<Mensa>,
  saveFavoriteMensas: (List<Mensa>) -> Unit,
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
      location.mensas.forEach { mensa ->
        val showMensa by remember(mensa, hiddenMensas, showOnlyOpenMensas, showOnlyExpandedMensas) {
          derivedStateOf {
            !(hiddenMensas.contains(mensa.mensa.id)
              || (showOnlyOpenMensas && mensa.state == MensaState.State.Closed)
              || (showOnlyExpandedMensas && mensa.state != MensaState.State.Expanded))
          }
        }

        AnimatedVisibility(visible = showMensa) {
          MensaRow(
            mensa = mensa,
            saveIsExpandedMensa = saveIsExpandedMensa,
            listUseShortDescription = listUseShortDescription,
            listShowAllergens = listShowAllergens,
            onMenuClick = { onMenuClick(mensa, it) },
            isFavoriteMensa = favoriteMensas.contains(mensa.mensa),
            changeIsFavoriteMensa = {
              saveFavoriteMensas(
                if (favoriteMensas.contains(mensa.mensa)) favoriteMensas - mensa.mensa
                else favoriteMensas + mensa.mensa,
              )
            },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
