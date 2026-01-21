package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.IdTitleItem

@Composable
fun <T : IdTitleItem> ListSelectorDialog(
  show: MutableState<Boolean>,
  entireList: List<T>,
  selectedList: List<T>,
  saveList: (List<T>) -> Unit,
  icon: ImageVector,
  @StringRes title: Int,
  @StringRes subtitleAvailableItems: Int,
  modifier: Modifier = Modifier,
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
      icon = { Icon(icon, null) },
      title = { Text(text = stringResource(title)) },
      text = {
        LazyColumn {
          itemsIndexed(
            items = selected,
            key = { _, item -> item.id },
          ) { index, item ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.animateItem(),
            ) {
              Text(
                text = item.title,
                modifier = Modifier.weight(1f),
              )
              if (showMoveButtons) {
                AnimatedVisibility(visible = index < selected.lastIndex) {
                  IconButton(onClick = { selected.add(index + 1, selected.removeAt(index)) }) {
                    Icon(Icons.Default.KeyboardArrowDown, stringResource(R.string.move_down))
                  }
                }
                AnimatedVisibility(visible = index > 0) {
                  IconButton(onClick = { selected.add(index - 1, selected.removeAt(index)) }) {
                    Icon(Icons.Default.KeyboardArrowUp, stringResource(R.string.move_up))
                  }
                }
              }
              IconButton(onClick = { selected.removeAt(index) }) {
                Icon(Icons.Default.Remove, stringResource(R.string.remove))
              }
            }
          }
          if ((entireList - selected).isNotEmpty()) {
            item(key = 0) {
              Column(modifier = Modifier.animateItem()) {
                HorizontalDivider()
                Text(
                  text = stringResource(subtitleAvailableItems),
                  fontStyle = FontStyle.Italic,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            }
          }
          items(
            items = entireList - selected,
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
              IconButton(onClick = { selected.add(it) }) {
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
