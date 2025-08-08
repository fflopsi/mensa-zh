package ch.florianfrauenfelder.mensazh.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.services.saveShowMenusInGerman
import ch.florianfrauenfelder.mensazh.services.saveShowOnlyFavoriteMensas
import ch.florianfrauenfelder.mensazh.services.saveShowOnlyOpenMensas
import kotlinx.coroutines.launch

@Composable
fun SettingsDropdown(
  showOnlyOpenMensas: Boolean,
  showOnlyFavoriteMensas: Boolean,
  showMenusInGerman: Boolean,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    IconButton(
      onClick = { expanded = !expanded },
      modifier = Modifier.focusable(),
    ) {
      Icon(Icons.Default.MoreHoriz, stringResource(R.string.settings))
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      DropdownMenuItem(
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.show_only_open),
              modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            )
            Checkbox(
              checked = showOnlyOpenMensas,
              onCheckedChange = {
                scope.launch { context.saveShowOnlyOpenMensas(!showOnlyOpenMensas) }
              },
            )
          }
        },
        onClick = { scope.launch { context.saveShowOnlyOpenMensas(!showOnlyOpenMensas) } },
      )
      DropdownMenuItem(
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.show_only_expanded),
              modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            )
            Checkbox(
              checked = showOnlyFavoriteMensas,
              onCheckedChange = {
                scope.launch { context.saveShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) }
              },
            )
          }
        },
        onClick = { scope.launch { context.saveShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) } },
      )
      DropdownMenuItem(
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(R.string.show_menus_in_german),
              modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f),
            )
            Checkbox(
              checked = showMenusInGerman,
              onCheckedChange = {
                scope.launch { context.saveShowMenusInGerman(!showMenusInGerman) }
              },
            )
          }
        },
        onClick = { scope.launch { context.saveShowMenusInGerman(!showMenusInGerman) } },
      )
    }
  }
}
