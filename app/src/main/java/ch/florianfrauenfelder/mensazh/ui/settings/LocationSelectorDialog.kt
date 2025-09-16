package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import ch.florianfrauenfelder.mensazh.models.Location

@Composable
fun LocationSelectorDialog(
  show: MutableState<Boolean>,
  locations: List<Location>,
  shownLocations: List<Location>,
  saveShownLocations: (List<Location>) -> Unit,
  modifier: Modifier = Modifier,
) {
  val shown = shownLocations.toMutableStateList()

  if (show.value) {
    AlertDialog(
      onDismissRequest = { show.value = false },
      confirmButton = {
        TextButton(
          onClick = {
            show.value = false
            if (shown.toList() != shownLocations) saveShownLocations(shown)
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
      icon = { Icon(Icons.Default.EditLocation, null) },
      title = { Text(text = stringResource(R.string.select_locations)) },
      text = {
        LazyColumn {
          itemsIndexed(
            items = shown,
            key = { _, location -> location.id },
          ) { index, location ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = location.title,
                modifier = Modifier.weight(1f),
              )
              AnimatedVisibility(visible = index < shown.lastIndex) {
                IconButton(onClick = { shown.add(index + 1, shown.removeAt(index)) }) {
                  Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.move_down))
                }
              }
              AnimatedVisibility(visible = index > 0) {
                IconButton(onClick = { shown.add(index - 1, shown.removeAt(index)) }) {
                  Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.move_up))
                }
              }
              IconButton(onClick = { shown.removeAt(index) }) {
                Icon(Icons.Default.Remove, stringResource(R.string.remove))
              }
            }
          }
          if ((locations - shown).isNotEmpty()) {
            item(key = 0) {
              Column(modifier = Modifier.animateItem()) {
                HorizontalDivider()
                Text(
                  text = stringResource(R.string.available_locations),
                  fontStyle = FontStyle.Italic,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
          items(
            items = locations - shown,
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
              IconButton(onClick = { shown.add(it) }) {
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
