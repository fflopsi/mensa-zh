package ch.florianfrauenfelder.mensazh.ui.panes.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import ch.florianfrauenfelder.mensazh.ui.theme.animateItemModifier
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.active
import mensazh.app.shared.generated.resources.auto
import mensazh.app.shared.generated.resources.auto_show_image
import mensazh.app.shared.generated.resources.auto_show_image_desc
import mensazh.app.shared.generated.resources.available_locations
import mensazh.app.shared.generated.resources.available_mensas
import mensazh.app.shared.generated.resources.clear_app_cache
import mensazh.app.shared.generated.resources.clear_app_cache_desc
import mensazh.app.shared.generated.resources.dark
import mensazh.app.shared.generated.resources.favorite_mensas
import mensazh.app.shared.generated.resources.hidden_menu_types
import mensazh.app.shared.generated.resources.hide_mensas
import mensazh.app.shared.generated.resources.ic_arrow_next_24
import mensazh.app.shared.generated.resources.ic_brightness_auto_24
import mensazh.app.shared.generated.resources.ic_check_24
import mensazh.app.shared.generated.resources.ic_dark_mode_24
import mensazh.app.shared.generated.resources.ic_edit_location_24
import mensazh.app.shared.generated.resources.ic_filter_list_off_24
import mensazh.app.shared.generated.resources.ic_hotel_class_24
import mensazh.app.shared.generated.resources.ic_keyboard_arrow_down_24
import mensazh.app.shared.generated.resources.ic_keyboard_arrow_up_24
import mensazh.app.shared.generated.resources.ic_light_mode_24
import mensazh.app.shared.generated.resources.ic_no_meals_24
import mensazh.app.shared.generated.resources.light
import mensazh.app.shared.generated.resources.more_settings
import mensazh.app.shared.generated.resources.none_selected
import mensazh.app.shared.generated.resources.select_locations
import mensazh.app.shared.generated.resources.select_menu_types
import mensazh.app.shared.generated.resources.select_menu_types_desc
import mensazh.app.shared.generated.resources.short_description_overview
import mensazh.app.shared.generated.resources.short_description_overview_desc
import mensazh.app.shared.generated.resources.show_allergens_overview
import mensazh.app.shared.generated.resources.show_allergens_overview_desc
import mensazh.app.shared.generated.resources.show_menus_in_german
import mensazh.app.shared.generated.resources.show_menus_in_german_desc
import mensazh.app.shared.generated.resources.show_next_week
import mensazh.app.shared.generated.resources.show_next_week_desc
import mensazh.app.shared.generated.resources.show_only_expanded
import mensazh.app.shared.generated.resources.show_only_expanded_desc
import mensazh.app.shared.generated.resources.show_only_open
import mensazh.app.shared.generated.resources.show_only_open_desc
import mensazh.app.shared.generated.resources.show_this_week
import mensazh.app.shared.generated.resources.show_this_week_desc
import mensazh.app.shared.generated.resources.show_tomorrow
import mensazh.app.shared.generated.resources.show_tomorrow_desc
import mensazh.app.shared.generated.resources.theme
import mensazh.app.shared.generated.resources.use_expressive_animations
import mensazh.app.shared.generated.resources.use_expressive_animations_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsList(
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
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(),
) {
  val layoutDirection = LocalLayoutDirection.current

  val showLocationSelector = remember { mutableStateOf(false) }
  val showFavoriteMensaSelector = remember { mutableStateOf(false) }
  val showHiddenMensaSelector = remember { mutableStateOf(false) }
  val showMenuTypeSelector = remember { mutableStateOf(false) }
  var showMoreSettings by remember { mutableStateOf(false) }

  ListSelectorDialog(
    show = showLocationSelector,
    entireList = baseLocations,
    selectedList = shownLocations,
    saveList = { locations -> update(Setting.SetShownLocations(locations.map { it.id })) },
    getId = { it.id.toString() },
    getTitle = { it.title },
    icon = Res.drawable.ic_edit_location_24,
    title = Res.string.select_locations,
    subtitleAvailableItems = Res.string.available_locations,
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
    icon = Res.drawable.ic_hotel_class_24,
    title = Res.string.favorite_mensas,
    subtitleAvailableItems = Res.string.available_mensas,
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
    icon = Res.drawable.ic_filter_list_off_24,
    title = Res.string.hide_mensas,
    subtitleAvailableItems = Res.string.available_mensas,
  )
  ListSelectorDialog(
    show = showMenuTypeSelector,
    entireList = MenuType.entries,
    selectedList = visibility.menuTypes,
    saveList = { update(Setting.SetMenuTypes(it)) },
    getId = MenuType::code,
    getTitle = { stringResource(it.label) },
    icon = Res.drawable.ic_no_meals_24,
    title = Res.string.select_menu_types,
    subtitle = Res.string.select_menu_types_desc,
    subtitleAvailableItems = Res.string.hidden_menu_types,
    showMoveButtons = true,
  )

  LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier,
  ) {
    item(key = 0) {
      SettingsRow(
        title = stringResource(Res.string.show_only_open),
        subtitle = stringResource(Res.string.show_only_open_desc),
        onClick = { update(Setting.SetShowOnlyOpenMensas(!visibility.showOnlyOpenMensas)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = visibility.showOnlyOpenMensas, onCheckedChange = null)
      }
    }
    item(key = 1) {
      SettingsRow(
        title = stringResource(Res.string.show_only_expanded),
        subtitle = stringResource(Res.string.show_only_expanded_desc),
        onClick = {
          update(Setting.SetShowOnlyExpandedMensas(!visibility.showOnlyExpandedMensas))
        },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = visibility.showOnlyExpandedMensas, onCheckedChange = null)
      }
    }
    item(key = 2) {
      SettingsRow(
        title = stringResource(Res.string.show_menus_in_german),
        subtitle = stringResource(Res.string.show_menus_in_german_desc),
        onClick = { update(Setting.SetMenusLanguage(!visibility.language)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = visibility.language.showMenusInGerman, onCheckedChange = null)
      }
    }
    item(key = 3) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
    item(key = 4) {
      SettingsRow(
        title = stringResource(Res.string.select_locations),
        subtitle = shownLocations
          .map { it.title }
          .ifEmpty { stringResource(Res.string.none_selected) }
          .toString(),
        onClick = { showLocationSelector.value = true },
        modifier = Modifier.animateItemModifier(),
      ) {
        Icon(painterResource(Res.drawable.ic_arrow_next_24), null)
      }
    }
    item(key = 5) {
      SettingsRow(
        title = stringResource(Res.string.favorite_mensas),
        subtitle = favoriteMensas
          .map { it.title }
          .ifEmpty { stringResource(Res.string.none_selected) }
          .toString(),
        onClick = { showFavoriteMensaSelector.value = true },
        modifier = Modifier.animateItemModifier(),
      ) {
        Icon(painterResource(Res.drawable.ic_arrow_next_24), null)
      }
    }
    item(key = 6) {
      SettingsRow(
        title = stringResource(Res.string.hide_mensas),
        subtitle = hiddenMensas
          .map { it.title }
          .ifEmpty { stringResource(Res.string.none_selected) }
          .toString(),
        onClick = { showHiddenMensaSelector.value = true },
        modifier = Modifier.animateItemModifier(),
      ) {
        Icon(painterResource(Res.drawable.ic_arrow_next_24), null)
      }
    }
    item(key = 7) {
      SettingsRow(
        title = stringResource(Res.string.select_menu_types),
        subtitle = visibility
          .menuTypes
          .map { stringResource(it.label) }
          .ifEmpty { stringResource(Res.string.none_selected) }
          .toString(),
        onClick = { showMenuTypeSelector.value = true },
        modifier = Modifier.animateItemModifier(),
      ) {
        Icon(painterResource(Res.drawable.ic_arrow_next_24), null)
      }
    }
    item(key = 8) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
    item(key = 9) {
      SettingsRow(
        title = stringResource(Res.string.show_tomorrow),
        subtitle = stringResource(Res.string.show_tomorrow_desc),
        onClick = { update(Setting.SetShowTomorrow(!destination.showTomorrow)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = destination.showTomorrow, onCheckedChange = null)
      }
    }
    item(key = 10) {
      SettingsRow(
        title = stringResource(Res.string.show_this_week),
        subtitle = stringResource(Res.string.show_this_week_desc),
        onClick = { update(Setting.SetShowThisWeek(!destination.showThisWeek)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = destination.showThisWeek, onCheckedChange = null)
      }
    }
    item(key = 11) {
      SettingsRow(
        title = stringResource(Res.string.show_next_week),
        subtitle = stringResource(Res.string.show_next_week_desc),
        onClick = { update(Setting.SetShowNextWeek(!destination.showNextWeek)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = destination.showNextWeek, onCheckedChange = null)
      }
    }
    item(key = 12) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
    item(key = 13) {
      SettingsRow(
        title = stringResource(Res.string.short_description_overview),
        subtitle = stringResource(Res.string.short_description_overview_desc),
        onClick = { update(Setting.SetListUseShortDescription(!detail.listUseShortDescription)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = detail.listUseShortDescription, onCheckedChange = null)
      }
    }
    item(key = 14) {
      SettingsRow(
        title = stringResource(Res.string.show_allergens_overview),
        subtitle = stringResource(Res.string.show_allergens_overview_desc),
        onClick = { update(Setting.SetListShowAllergens(!detail.listShowAllergens)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = detail.listShowAllergens, onCheckedChange = null)
      }
    }
    item(key = 15) {
      SettingsRow(
        title = stringResource(Res.string.auto_show_image),
        subtitle = stringResource(Res.string.auto_show_image_desc),
        onClick = { update(Setting.SetAutoShowImage(!detail.autoShowImage)) },
        modifier = Modifier.animateItemModifier(),
      ) {
        Switch(checked = detail.autoShowImage, onCheckedChange = null)
      }
    }
    item(key = 16) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
    item(key = 17) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .animateItemModifier()
          .clickable { showMoreSettings = !showMoreSettings }
          .padding(horizontal = 16.dp, vertical = 8.dp),
      ) {
        Text(
          text = stringResource(Res.string.more_settings),
          fontStyle = FontStyle.Italic,
          modifier = Modifier
            .padding(end = 16.dp)
            .weight(1f),
        )
        Icon(
          painterResource(
            if (showMoreSettings) {
              Res.drawable.ic_keyboard_arrow_up_24
            } else {
              Res.drawable.ic_keyboard_arrow_down_24
            }
          ),
          null,
        )
      }
    }
    if (showMoreSettings) {
      item(key = 18) {
        SettingsRow(
          title = stringResource(Res.string.theme),
          subtitle = stringResource(
            when (theme.theme) {
              Theme.Auto -> Res.string.auto
              Theme.Light -> Res.string.light
              Theme.Dark -> Res.string.dark
            },
          ),
          weightTitle = false,
          modifier = Modifier.animateItemModifier(),
        ) {
          val contentPadding = ButtonDefaults.ButtonWithIconContentPadding
          val icons = listOf(
            Res.drawable.ic_brightness_auto_24,
            Res.drawable.ic_light_mode_24,
            Res.drawable.ic_dark_mode_24,
          )
          val labels = listOf(Res.string.auto, Res.string.light, Res.string.dark)
          val interactionSources = remember { List(3) { MutableInteractionSource() } }

          ButtonGroup(overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) }) {
            Theme.entries.forEach { entry ->
              customItem(
                buttonGroupContent = {
                  ToggleButton(
                    checked = theme.theme == entry,
                    onCheckedChange = { update(Setting.SetTheme(entry)) },
                    shapes = when (entry) {
                      Theme.Auto -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                      Theme.Light -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                      Theme.Dark -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    },
                    contentPadding = contentPadding,
                    interactionSource = interactionSources[entry.code],
                    modifier = Modifier.animateWidth(
                      interactionSource = interactionSources[entry.code],
                      compressionLimit = contentPadding.calculateEndPadding(layoutDirection),
                    ),
                  ) {
                    Icon(painterResource(icons[entry.code]), null)
                    Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                      text = stringResource(labels[entry.code]),
                      softWrap = false,
                      maxLines = 1,
                      overflow = TextOverflow.Visible,
                    )
                  }
                },
                menuContent = {
                  DropdownMenuItem(
                    leadingIcon = { Icon(painterResource(icons[entry.code]), null) },
                    text = { Text(text = stringResource(labels[entry.code])) },
                    trailingIcon = {
                      if (theme.theme == entry) {
                        Icon(
                          painterResource(Res.drawable.ic_check_24),
                          stringResource(Res.string.active),
                        )
                      }
                    },
                    onClick = { update(Setting.SetTheme(entry)) },
                    interactionSource = interactionSources[entry.code],
                  )
                },
              )
            }
          }
        }
      }
      dynamicColorSetting(theme, update)
      item(key = 181) {
        SettingsRow(
          title = stringResource(Res.string.use_expressive_animations),
          subtitle = stringResource(Res.string.use_expressive_animations_desc),
          onClick = { update(Setting.SetUseExpressiveAnimations(!theme.useExpressiveAnimations)) },
          modifier = Modifier.animateItemModifier(),
        ) {
          Switch(checked = theme.useExpressiveAnimations, onCheckedChange = null)
        }
      }
      item(key = 20) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
      item(key = 21) {
        SettingsRow(
          title = stringResource(Res.string.clear_app_cache),
          subtitle = stringResource(Res.string.clear_app_cache_desc),
          onClick = clearCache,
          modifier = Modifier.animateItemModifier(),
        )
      }
      openSystemSettingsSetting()
    }
    item(key = 23) { HorizontalDivider(modifier = Modifier.animateItemModifier()) }
    item(key = 24) {
      InfoLinks(
        modifier = Modifier
          .animateItemModifier()
          .padding(16.dp)
          .fillMaxWidth(),
      )
    }
  }
}
