package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings

@Composable
fun LocationRow(
  location: Location,
  saveIsExpandedMensa: (Mensa, Boolean) -> Unit,
  detail: DetailSettings,
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
        AnimatedVisibility(visible = true) {
          MensaRow(
            mensa = mensa,
            saveIsExpandedMensa = saveIsExpandedMensa,
            detail = detail,
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
