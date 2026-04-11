package ch.florianfrauenfelder.mensazh.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoMeals
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import ch.florianfrauenfelder.mensazh.domain.value.MenuType
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import ch.florianfrauenfelder.mensazh.ui.domain.label
import ch.florianfrauenfelder.mensazh.ui.shared.InfoLinks

@Composable
fun SettingsScreen(
  visibility: VisibilitySettings,
  destination: DestinationSettings,
  detail: DetailSettings,
  theme: ThemeSettings,
  baseLocations: List<Location>,
  shownLocations: List<Location>,
  hiddenMensas: List<Mensa>,
  favoriteMensas: List<Mensa>,
  update: (Setting) -> Unit,
  clearCache: () -> Unit,
  openSystemSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  val layoutDirection = LocalLayoutDirection.current

  val showLocationSelector = remember { mutableStateOf(false) }
  val showFavoriteMensaSelector = remember { mutableStateOf(false) }
  val showHiddenMensaSelector = remember { mutableStateOf(false) }
  val showMenuTypeSelector = remember { mutableStateOf(false) }
  var showMoreSettings by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
          IconButton(onClick = navigateUp) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
          }
        },
        windowInsets = WindowInsets.statusBars,
      )
    },
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    ListSelectorDialog(
      show = showLocationSelector,
      entireList = baseLocations,
      selectedList = shownLocations,
      saveList = { locations -> update(Setting.SetShownLocations(locations.map { it.id })) },
      getId = { it.id.toString() },
      getTitle = { it.title },
      icon = Icons.Default.EditLocation,
      title = R.string.select_locations,
      subtitleAvailableItems = R.string.available_locations,
      showMoveButtons = true,
    )
    ListSelectorDialog(
      show = showFavoriteMensaSelector,
      entireList = baseLocations.flatMap { location -> location.mensas.map { it.mensa } }
        .filter { !hiddenMensas.contains(it) },
      selectedList = favoriteMensas,
      saveList = { mensas -> update(Setting.SetFavoriteMensas(mensas.map { it.id })) },
      getId = { it.id.toString() },
      getTitle = { it.title },
      icon = Icons.Default.HotelClass,
      title = R.string.favorite_mensas,
      subtitleAvailableItems = R.string.available_mensas,
      showMoveButtons = true,
    )
    ListSelectorDialog(
      show = showHiddenMensaSelector,
      entireList = shownLocations.flatMap { location -> location.mensas.map { it.mensa } }
        .filter { !favoriteMensas.contains(it) },
      selectedList = hiddenMensas,
      saveList = { mensas -> update(Setting.SetHiddenMensas(mensas.map { it.id })) },
      getId = { it.id.toString() },
      getTitle = { it.title },
      icon = Icons.Default.FilterListOff,
      title = R.string.hide_mensas,
      subtitleAvailableItems = R.string.available_mensas,
    )
    ListSelectorDialog(
      show = showMenuTypeSelector,
      entireList = MenuType.entries,
      selectedList = visibility.menuTypes,
      saveList = { update(Setting.SetMenuTypes(it)) },
      getId = MenuType::code,
      getTitle = { stringResource(it.label) },
      icon = Icons.Default.NoMeals,
      title = R.string.select_menu_types,
      subtitle = R.string.select_menu_types_desc,
      subtitleAvailableItems = R.string.hidden_menu_types,
      showMoveButtons = true,
    )

    LazyColumn(
      contentPadding = PaddingValues(
        top = innerPadding.calculateTopPadding(),
        bottom = innerPadding.calculateBottomPadding(),
        end = innerPadding.calculateEndPadding(layoutDirection),
      ),
      modifier = Modifier.consumeWindowInsets(innerPadding),
    ) {
      item(key = 0) {
        SettingsRow(
          title = stringResource(R.string.show_only_open),
          subtitle = stringResource(R.string.show_only_open_desc),
          onClick = { update(Setting.SetShowOnlyOpenMensas(!visibility.showOnlyOpenMensas)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = visibility.showOnlyOpenMensas, onCheckedChange = null)
        }
      }
      item(key = 1) {
        SettingsRow(
          title = stringResource(R.string.show_only_expanded),
          subtitle = stringResource(R.string.show_only_expanded_desc),
          onClick = {
            update(Setting.SetShowOnlyExpandedMensas(!visibility.showOnlyExpandedMensas))
          },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = visibility.showOnlyExpandedMensas, onCheckedChange = null)
        }
      }
      item(key = 2) {
        SettingsRow(
          title = stringResource(R.string.show_menus_in_german),
          subtitle = stringResource(R.string.show_menus_in_german_desc),
          onClick = { update(Setting.SetMenusLanguage(!visibility.language)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = visibility.language.showMenusInGerman, onCheckedChange = null)
        }
      }
      item(key = 3) { HorizontalDivider(modifier = Modifier.animateItem()) }
      item(key = 4) {
        SettingsRow(
          title = stringResource(R.string.select_locations),
          subtitle = shownLocations
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showLocationSelector.value = true },
          modifier = Modifier.animateItem(),
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item(key = 5) {
        SettingsRow(
          title = stringResource(R.string.favorite_mensas),
          subtitle = favoriteMensas
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showFavoriteMensaSelector.value = true },
          modifier = Modifier.animateItem(),
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item(key = 6) {
        SettingsRow(
          title = stringResource(R.string.hide_mensas),
          subtitle = hiddenMensas
            .map { it.title }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showHiddenMensaSelector.value = true },
          modifier = Modifier.animateItem(),
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item(key = 7) {
        SettingsRow(
          title = stringResource(R.string.select_menu_types),
          subtitle = visibility
            .menuTypes
            .map { stringResource(it.label) }
            .ifEmpty { stringResource(R.string.none_selected) }
            .toString(),
          onClick = { showMenuTypeSelector.value = true },
          modifier = Modifier.animateItem(),
        ) {
          Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
      }
      item(key = 8) { HorizontalDivider(modifier = Modifier.animateItem()) }
      item(key = 9) {
        SettingsRow(
          title = stringResource(R.string.show_tomorrow),
          subtitle = stringResource(R.string.show_tomorrow_desc),
          onClick = { update(Setting.SetShowTomorrow(!destination.showTomorrow)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = destination.showTomorrow, onCheckedChange = null)
        }
      }
      item(key = 10) {
        SettingsRow(
          title = stringResource(R.string.show_this_week),
          subtitle = stringResource(R.string.show_this_week_desc),
          onClick = { update(Setting.SetShowThisWeek(!destination.showThisWeek)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = destination.showThisWeek, onCheckedChange = null)
        }
      }
      item(key = 11) {
        SettingsRow(
          title = stringResource(R.string.show_next_week),
          subtitle = stringResource(R.string.show_next_week_desc),
          onClick = { update(Setting.SetShowNextWeek(!destination.showNextWeek)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = destination.showNextWeek, onCheckedChange = null)
        }
      }
      item(key = 12) { HorizontalDivider(modifier = Modifier.animateItem()) }
      item(key = 13) {
        SettingsRow(
          title = stringResource(R.string.short_description_overview),
          subtitle = stringResource(R.string.short_description_overview_desc),
          onClick = { update(Setting.SetListUseShortDescription(!detail.listUseShortDescription)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = detail.listUseShortDescription, onCheckedChange = null)
        }
      }
      item(key = 14) {
        SettingsRow(
          title = stringResource(R.string.show_allergens_overview),
          subtitle = stringResource(R.string.show_allergens_overview_desc),
          onClick = { update(Setting.SetListShowAllergens(!detail.listShowAllergens)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = detail.listShowAllergens, onCheckedChange = null)
        }
      }
      item(key = 15) {
        SettingsRow(
          title = stringResource(R.string.auto_show_image),
          subtitle = stringResource(R.string.auto_show_image_desc),
          onClick = { update(Setting.SetAutoShowImage(!detail.autoShowImage)) },
          modifier = Modifier.animateItem(),
        ) {
          Switch(checked = detail.autoShowImage, onCheckedChange = null)
        }
      }
      item(key = 16) { HorizontalDivider(modifier = Modifier.animateItem()) }
      item(key = 17) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .animateItem()
            .clickable { showMoreSettings = !showMoreSettings }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
          Text(
            text = stringResource(R.string.more_settings),
            fontStyle = FontStyle.Italic,
            modifier = Modifier
              .padding(end = 16.dp)
              .weight(1f),
          )
          Icon(
            if (showMoreSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            null,
          )
        }
      }
      if (showMoreSettings) {
        item(key = 18) {
          var themeSelectorExpanded by remember { mutableStateOf(false) }
          SettingsRow(
            title = stringResource(R.string.theme),
            subtitle = stringResource(
              when (theme.theme) {
                Theme.Auto -> R.string.auto
                Theme.Light -> R.string.light
                Theme.Dark -> R.string.dark
              },
            ),
            onClick = { themeSelectorExpanded = true },
            modifier = Modifier.animateItem(),
          ) {
            Box {
              Icon(Icons.Default.MoreVert, null)
              DropdownMenu(
                expanded = themeSelectorExpanded,
                onDismissRequest = { themeSelectorExpanded = false },
              ) {
                DropdownMenuItem(
                  text = { Text(text = stringResource(R.string.auto)) },
                  onClick = { update(Setting.SetTheme(Theme.Auto)) },
                  leadingIcon = { Icon(Icons.Default.BrightnessAuto, null) },
                  trailingIcon = {
                    if (theme.theme == Theme.Auto) {
                      Icon(Icons.Default.Check, stringResource(R.string.active))
                    }
                  },
                )
                HorizontalDivider()
                DropdownMenuItem(
                  text = { Text(text = stringResource(R.string.light)) },
                  onClick = { update(Setting.SetTheme(Theme.Light)) },
                  leadingIcon = { Icon(Icons.Default.LightMode, null) },
                  trailingIcon = {
                    if (theme.theme == Theme.Light) {
                      Icon(Icons.Default.Check, stringResource(R.string.active))
                    }
                  },
                )
                DropdownMenuItem(
                  text = { Text(text = stringResource(R.string.dark)) },
                  onClick = { update(Setting.SetTheme(Theme.Dark)) },
                  leadingIcon = { Icon(Icons.Default.DarkMode, null) },
                  trailingIcon = {
                    if (theme.theme == Theme.Dark) {
                      Icon(Icons.Default.Check, stringResource(R.string.active))
                    }
                  },
                )
              }
            }
          }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          item(key = 19) {
            SettingsRow(
              title = stringResource(R.string.use_dynamic_colors),
              onClick = { update(Setting.SetUseDynamicColor(!theme.useDynamicColor)) },
              modifier = Modifier.animateItem(),
            ) {
              Switch(checked = theme.useDynamicColor, onCheckedChange = null)
            }
          }
        }
        item(key = 20) { HorizontalDivider(modifier = Modifier.animateItem()) }
        item(key = 21) {
          SettingsRow(
            title = stringResource(R.string.clear_app_cache),
            subtitle = stringResource(R.string.clear_app_cache_desc),
            onClick = clearCache,
            modifier = Modifier.animateItem(),
          )
        }
        item(key = 22) {
          SettingsRow(
            title = stringResource(R.string.more_settings),
            subtitle = stringResource(R.string.more_settings_desc),
            onClick = openSystemSettings,
            modifier = Modifier.animateItem(),
          ) {
            Icon(Icons.AutoMirrored.Default.NavigateNext, null)
          }
        }
      }
      item(key = 23) { HorizontalDivider(modifier = Modifier.animateItem()) }
      item(key = 24) {
        InfoLinks(
          modifier = Modifier
            .animateItem()
            .padding(16.dp)
            .fillMaxWidth(),
        )
      }
    }
  }
}
