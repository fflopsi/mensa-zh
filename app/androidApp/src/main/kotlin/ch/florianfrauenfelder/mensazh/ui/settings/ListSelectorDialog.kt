package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import ch.florianfrauenfelder.mensazh.R

@Composable
fun <T> ListSelectorDialog(
  show: MutableState<Boolean>,
  entireList: List<T>,
  selectedList: List<T>,
  saveList: (List<T>) -> Unit,
  getId: (T) -> String,
  getTitle: @Composable (T) -> String,
  @DrawableRes icon: Int,
  modifier: Modifier = Modifier,
  @StringRes title: Int,
  @StringRes subtitle: Int? = null,
  @StringRes subtitleAvailableItems: Int? = null,
  showMoveButtons: Boolean = false,
) {
  if (show.value) {
    val selected = remember { selectedList.toMutableStateList() }

    AlertDialog(
      onDismissRequest = { show.value = false },
      confirmButton = {
        TextButton(
          onClick = {
            show.value = false
            if (selected.toList() != selectedList) saveList(selected)
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
      icon = { Icon(painterResource(icon), null) },
      title = { Text(text = stringResource(title)) },
      text = {
        LazyColumn {
          subtitle?.let {
            item(key = -1) {
              Text(
                text = stringResource(it),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
          itemsIndexed(
            items = selected,
            key = { _, item -> getId(item) },
          ) { index, item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = getTitle(item),
                modifier = Modifier.weight(1f),
              )
              if (showMoveButtons) {
                AnimatedVisibility(visible = index < selected.lastIndex) {
                  IconButton(onClick = { selected.add(index + 1, selected.removeAt(index)) }) {
                    Icon(
                      painterResource(R.drawable.ic_keyboard_arrow_down_24),
                      stringResource(R.string.move_down),
                    )
                  }
                }
                AnimatedVisibility(visible = index > 0) {
                  IconButton(onClick = { selected.add(index - 1, selected.removeAt(index)) }) {
                    Icon(
                      painterResource(R.drawable.ic_keyboard_arrow_up_24),
                      stringResource(R.string.move_up),
                    )
                  }
                }
              }
              IconButton(onClick = { selected.removeAt(index) }) {
                Icon(painterResource(R.drawable.ic_remove_24), stringResource(R.string.remove))
              }
            }
          }
          subtitleAvailableItems?.let {
            if ((entireList - selected).isNotEmpty()) {
              item(key = 0) {
                Column(modifier = Modifier.animateItem()) {
                  HorizontalDivider()
                  Text(
                    text = stringResource(it),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                  )
                }
              }
            }
          }
          items(
            items = entireList - selected,
            key = getId,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = getTitle(it),
                modifier = Modifier.weight(1f),
              )
              IconButton(onClick = { selected.add(it) }) {
                Icon(painterResource(R.drawable.ic_add_24), stringResource(R.string.add))
              }
            }
          }
        }
      },
      modifier = modifier,
    )
  }
}
