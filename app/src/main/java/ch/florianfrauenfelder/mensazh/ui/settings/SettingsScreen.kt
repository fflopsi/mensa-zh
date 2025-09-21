package ch.florianfrauenfelder.mensazh.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.HotelClass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.ui.components.InfoLinks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  showOnlyOpenMensas: Boolean,
  setShowOnlyOpenMensas: (Boolean) -> Unit,
  showOnlyExpandedMensas: Boolean,
  setShowOnlyExpandedMensas: (Boolean) -> Unit,
  language: MensaProvider.Language,
  setLanguage: (MensaProvider.Language) -> Unit,
  locations: List<Location>,
  shownLocations: List<Location>,
  saveShownLocations: (List<Location>) -> Unit,
  favoriteMensas: List<Mensa>,
  saveFavoriteMensas: (List<Mensa>) -> Unit,
  hiddenMensas: List<Mensa>,
  saveHiddenMensas: (List<Mensa>) -> Unit,
  showTomorrow: Boolean,
  saveShowTomorrow: (Boolean) -> Unit,
  showThisWeek: Boolean,
  saveShowThisWeek: (Boolean) -> Unit,
  showNextWeek: Boolean,
  saveShowNextWeek: (Boolean) -> Unit,
  theme: Int,
  saveTheme: (Int) -> Unit,
  dynamicColor: Boolean,
  saveDynamicColor: (Boolean) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val showLocationSelector = remember { mutableStateOf(false) }
  val showFavoriteMensaSelector = remember { mutableStateOf(false) }
  val showHiddenMensaSelector = remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
          }
        },
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
    modifier = modifier,
  ) { insets ->
    ListSelectorDialog(
      show = showLocationSelector,
      entireList = locations,
      selectedList = shownLocations,
      saveList = saveShownLocations,
      icon = Icons.Default.EditLocation,
      title = R.string.select_locations,
      subtitleAvailableItems = R.string.available_locations,
      showMoveButtons = true,
    )
    ListSelectorDialog(
      show = showFavoriteMensaSelector,
      entireList = locations.flatMap { it.mensas }.filter { !hiddenMensas.contains(it) },
      selectedList = favoriteMensas,
      saveList = saveFavoriteMensas,
      icon = Icons.Default.HotelClass,
      title = R.string.favorite_mensas,
      subtitleAvailableItems = R.string.available_mensas,
      showMoveButtons = true,
    )
    ListSelectorDialog(
      show = showHiddenMensaSelector,
      entireList = shownLocations.flatMap { it.mensas }.filter { !favoriteMensas.contains(it) },
      selectedList = hiddenMensas,
      saveList = saveHiddenMensas,
      icon = Icons.Default.FilterListOff,
      title = R.string.hide_mensas,
      subtitleAvailableItems = R.string.available_mensas,
    )

    LazyColumn(
      contentPadding = insets,
    ) {
      item {
        SettingsRow(
          title = stringResource(R.string.show_only_open),
          subtitle = stringResource(R.string.show_only_open_desc),
          onClick = { setShowOnlyOpenMensas(!showOnlyOpenMensas) },
        ) {
          Switch(
            checked = showOnlyOpenMensas,
            onCheckedChange = { setShowOnlyOpenMensas(!showOnlyOpenMensas) },
          )
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.show_only_expanded),
          subtitle = stringResource(R.string.show_only_expanded_desc),
          onClick = { setShowOnlyExpandedMensas(!showOnlyExpandedMensas) },
        ) {
          Switch(
            checked = showOnlyExpandedMensas,
            onCheckedChange = { setShowOnlyExpandedMensas(!showOnlyExpandedMensas) },
          )
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.show_menus_in_german),
          subtitle = stringResource(R.string.show_menus_in_german_desc),
          onClick = { setLanguage(!language) },
        ) {
          Switch(
            checked = language.showMenusInGerman,
            onCheckedChange = { setLanguage(!language) },
          )
        }
      }
      item { HorizontalDivider() }
      item {
        SettingsRow(
          title = stringResource(R.string.select_locations),
          subtitle = shownLocations
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showLocationSelector.value = true },
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.favorite_mensas),
          subtitle = favoriteMensas
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showFavoriteMensaSelector.value = true },
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.hide_mensas),
          subtitle = hiddenMensas
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showHiddenMensaSelector.value = true },
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item { HorizontalDivider() }
      item {
        SettingsRow(
          title = stringResource(R.string.show_tomorrow),
          subtitle = stringResource(R.string.show_tomorrow_desc),
          onClick = { saveShowTomorrow(!showTomorrow) },
        ) {
          Switch(
            checked = showTomorrow,
            onCheckedChange = { saveShowTomorrow(!showTomorrow) },
          )
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.show_this_week),
          subtitle = stringResource(R.string.show_this_week_desc),
          onClick = { saveShowThisWeek(!showThisWeek) },
        ) {
          Switch(
            checked = showThisWeek,
            onCheckedChange = { saveShowThisWeek(!showThisWeek) },
          )
        }
      }
      item {
        SettingsRow(
          title = stringResource(R.string.show_next_week),
          subtitle = stringResource(R.string.show_next_week_desc),
          onClick = { saveShowNextWeek(!showNextWeek) },
        ) {
          Switch(
            checked = showNextWeek,
            onCheckedChange = { saveShowNextWeek(!showNextWeek) },
          )
        }
      }
      item { HorizontalDivider() }
      item {
        var themeSelectorExpanded by remember { mutableStateOf(false) }
        SettingsRow(
          title = stringResource(R.string.theme),
          subtitle = stringResource(
            when (theme) {
              1 -> R.string.light
              2 -> R.string.dark
              else -> R.string.auto
            }
          ),
          onClick = { themeSelectorExpanded = true },
        ) {
          Box {
            Icon(Icons.Default.MoreVert, null)
            DropdownMenu(
              expanded = themeSelectorExpanded,
              onDismissRequest = { themeSelectorExpanded = false },
            ) {
              DropdownMenuItem(
                text = { Text(text = stringResource(R.string.auto)) },
                onClick = { saveTheme(0) },
                leadingIcon = { Icon(Icons.Default.BrightnessAuto, null) },
                trailingIcon = {
                  if (theme == 0) Icon(Icons.Default.Check, stringResource(R.string.active))
                },
              )
              HorizontalDivider()
              DropdownMenuItem(
                text = { Text(text = stringResource(R.string.light)) },
                onClick = { saveTheme(1) },
                leadingIcon = { Icon(Icons.Default.LightMode, null) },
                trailingIcon = {
                  if (theme == 1) Icon(Icons.Default.Check, stringResource(R.string.active))
                },
              )
              DropdownMenuItem(
                text = { Text(text = stringResource(R.string.dark)) },
                onClick = { saveTheme(2) },
                leadingIcon = { Icon(Icons.Default.DarkMode, null) },
                trailingIcon = {
                  if (theme == 2) Icon(Icons.Default.Check, stringResource(R.string.active))
                },
              )
            }
          }
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        item {
          SettingsRow(
            title = stringResource(R.string.use_dynamic_colors),
            onClick = { saveDynamicColor(!dynamicColor) },
          ) {
            Switch(checked = dynamicColor, onCheckedChange = null)
          }
        }
      }
      item { HorizontalDivider() }
      item {
        InfoLinks(
          modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        )
      }
    }
  }
}
