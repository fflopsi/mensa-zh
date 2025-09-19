package ch.florianfrauenfelder.mensazh.ui.main

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider

@Composable
fun SettingsDropdown(
  showOnlyOpenMensas: Boolean,
  setShowOnlyOpenMensas: (Boolean) -> Unit,
  showOnlyFavoriteMensas: Boolean,
  setShowOnlyFavoriteMensas: (Boolean) -> Unit,
  language: MensaProvider.Language,
  setLanguage: (MensaProvider.Language) -> Unit,
  navigateToSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
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
              onCheckedChange = { setShowOnlyOpenMensas(!showOnlyOpenMensas) },
            )
          }
        },
        onClick = { setShowOnlyOpenMensas(!showOnlyOpenMensas) },
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
              onCheckedChange = { setShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) },
            )
          }
        },
        onClick = { setShowOnlyFavoriteMensas(!showOnlyFavoriteMensas) },
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
              checked = language.showMenusInGerman,
              onCheckedChange = { setLanguage(!language) },
            )
          }
        },
        onClick = { setLanguage(!language) },
      )
      HorizontalDivider()
      DropdownMenuItem(
        text = {
          Text(
            text = stringResource(R.string.more_settings),
            modifier = Modifier
              .padding(end = 16.dp)
              .weight(1f),
          )
        },
        onClick = {
          expanded = false
          navigateToSettings()
        },
      )
    }
  }
}
