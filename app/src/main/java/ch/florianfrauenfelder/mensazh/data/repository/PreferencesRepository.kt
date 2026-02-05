package ch.florianfrauenfelder.mensazh.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import ch.florianfrauenfelder.mensazh.data.local.datastore.autoShowImageFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.expandedMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.favoriteMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.hiddenMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.listShowAllergensFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.listUseShortDescriptionFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.menusLanguageFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveAutoShowImage
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveFavoriteMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveHiddenMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveIsExpandedMensa
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveListShowAllergens
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveListUseShortDescription
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveMenusLanguage
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowNextWeek
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowOnlyExpandedMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowOnlyOpenMensas
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowThisWeek
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShowTomorrow
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveShownLocations
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveTheme
import ch.florianfrauenfelder.mensazh.data.local.datastore.saveUseDynamicColor
import ch.florianfrauenfelder.mensazh.data.local.datastore.showNextWeekFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showOnlyExpandedMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showOnlyOpenMensasFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showThisWeekFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.showTomorrowFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.shownLocationsFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.themeFlow
import ch.florianfrauenfelder.mensazh.data.local.datastore.useDynamicColorFlow
import ch.florianfrauenfelder.mensazh.domain.preferences.DestinationSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.SelectionSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import ch.florianfrauenfelder.mensazh.domain.preferences.VisibilitySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class PreferencesRepository(val dataStore: DataStore<Preferences>) {
  // Grouping because combining more than 5 flows is cumbersome
  val visibilitySettings = combine(
    dataStore.expandedMensasFlow,
    dataStore.showOnlyOpenMensasFlow,
    dataStore.showOnlyExpandedMensasFlow,
    dataStore.menusLanguageFlow,
  ) { expandedMensas, showOnlyOpenMensas, showOnlyExpandedMensas, language ->
    VisibilitySettings(
      expandedMensas = expandedMensas,
      showOnlyOpenMensas = showOnlyOpenMensas,
      showOnlyExpandedMensas = showOnlyExpandedMensas,
      language = language,
    )
  }

  val selectionSettings = combine(
    dataStore.shownLocationsFlow,
    dataStore.favoriteMensasFlow,
    dataStore.hiddenMensasFlow,
  ) { shownLocations, favoriteMensas, hiddenMensas ->
    SelectionSettings(
      shownLocations = shownLocations,
      favoriteMensas = favoriteMensas,
      hiddenMensas = hiddenMensas,
    )
  }

  val destinationSettings = combine(
    dataStore.showTomorrowFlow,
    dataStore.showThisWeekFlow,
    dataStore.showNextWeekFlow,
  ) { showTomorrow, showThisWeek, showNextWeek ->
    DestinationSettings(
      showTomorrow = showTomorrow,
      showThisWeek = showThisWeek,
      showNextWeek = showNextWeek,
    )
  }

  val detailSettings = combine(
    dataStore.listUseShortDescriptionFlow,
    dataStore.listShowAllergensFlow,
    dataStore.autoShowImageFlow,
  ) { listUseShortDescription, listShowAllergens, autoShowImage ->
    DetailSettings(
      listUseShortDescription = listUseShortDescription,
      listShowAllergens = listShowAllergens,
      autoShowImage = autoShowImage,
    )
  }

  val themeSettings = combine(
    dataStore.themeFlow,
    dataStore.useDynamicColorFlow,
  ) { theme, useDynamicColor ->
    ThemeSettings(
      theme = theme,
      useDynamicColor = useDynamicColor,
    )
  }

  suspend fun updateSetting(setting: Setting) = withContext(Dispatchers.IO) {
    when (val s = setting) {
      is Setting.SetIsExpandedMensa -> dataStore.saveIsExpandedMensa(s.mensa, s.expanded)
      is Setting.SetShowOnlyOpenMensas -> dataStore.saveShowOnlyOpenMensas(s.enabled)
      is Setting.SetShowOnlyExpandedMensas -> dataStore.saveShowOnlyExpandedMensas(s.enabled)
      is Setting.SetMenusLanguage -> dataStore.saveMenusLanguage(s.language)

      is Setting.SetShownLocations -> dataStore.saveShownLocations(s.locations)
      is Setting.SetFavoriteMensas -> dataStore.saveFavoriteMensas(s.mensas)
      is Setting.SetHiddenMensas -> dataStore.saveHiddenMensas(s.mensas)

      is Setting.SetShowTomorrow -> dataStore.saveShowTomorrow(s.enabled)
      is Setting.SetShowThisWeek -> dataStore.saveShowThisWeek(s.enabled)
      is Setting.SetShowNextWeek -> dataStore.saveShowNextWeek(s.enabled)

      is Setting.SetListUseShortDescription -> dataStore.saveListUseShortDescription(s.enabled)
      is Setting.SetListShowAllergens -> dataStore.saveListShowAllergens(s.enabled)
      is Setting.SetAutoShowImage -> dataStore.saveAutoShowImage(s.enabled)

      is Setting.SetTheme -> dataStore.saveTheme(s.theme)
      is Setting.SetUseDynamicColor -> dataStore.saveUseDynamicColor(s.enabled)
    }
  }
}
