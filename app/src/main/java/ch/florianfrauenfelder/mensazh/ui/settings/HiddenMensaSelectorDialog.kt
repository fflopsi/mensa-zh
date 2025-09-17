package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Mensa
import java.util.UUID

@Composable
fun HiddenMensaSelectorDialog(
  show: MutableState<Boolean>,
  mensas: List<Mensa>,
  hiddenMensas: List<UUID>,
  saveHiddenMensas: (List<UUID>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hidden = mensas.filter { hiddenMensas.contains(it.id) }.toMutableStateList()

  if (show.value) {
    AlertDialog(
      onDismissRequest = { show.value = false },
      confirmButton = {
        TextButton(
          onClick = {
            show.value = false
            if (hidden.toSet() != hiddenMensas.toSet()) saveHiddenMensas(hidden.map { it.id })
          },
        ) {
          Text(text = stringResource(R.string.ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { show.value = false }) {
          Text(text = stringResource(R.string.cancel))
        }
      },
      icon = { Icon(Icons.Default.FilterListOff, null) },
      title = { Text(text = stringResource(R.string.hide_mensas)) },
      text = {
        LazyColumn {
          itemsIndexed(
            items = hidden,
            key = { _, mensa -> mensa.id },
          ) { index, mensa ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = mensa.title,
                modifier = Modifier.weight(1f),
              )
              IconButton(onClick = { hidden.removeAt(index) }) {
                Icon(Icons.Default.Remove, stringResource(R.string.remove))
              }
            }
          }
          if ((mensas - hidden).isNotEmpty()) {
            item(key = 0) {
              Column(modifier = Modifier.animateItem()) {
                HorizontalDivider()
                Text(
                  text = stringResource(R.string.available_mensas),
                  fontStyle = FontStyle.Italic,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
          items(
            items = mensas - hidden,
            key = { it.id },
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = it.title,
                modifier = Modifier.weight(1f),
              )
              IconButton(onClick = { hidden.add(it) }) {
                Icon(Icons.Default.Add, stringResource(R.string.add))
              }
            }
          }
        }
      },
      modifier = modifier,
    )
  }
}
