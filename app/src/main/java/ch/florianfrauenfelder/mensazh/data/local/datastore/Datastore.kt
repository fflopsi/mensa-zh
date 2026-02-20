package ch.florianfrauenfelder.mensazh.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.preferences.Defaults
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.MenuType
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

val Context.dataStore by preferencesDataStore(name = "settings")

suspend fun DataStore<Preferences>.toggleExpandedMensa(mensa: Mensa) {
  edit { pref ->
    pref[Keys.EXPANDED_MENSAS] = pref[Keys.EXPANDED_MENSAS].orEmpty().let {
      if (mensa.id.toString() in it) it - mensa.id.toString() else it + mensa.id.toString()
    }
  }
}

val DataStore<Preferences>.expandedMensasFlow
  get() = data.map { it[Keys.EXPANDED_MENSAS]?.map(Uuid::parse) ?: Defaults.EXPANDED_MENSAS }

suspend fun DataStore<Preferences>.saveShowOnlyOpenMensas(showOnlyOpenMensas: Boolean) {
  edit { it[Keys.SHOW_ONLY_OPEN_MENSAS] = showOnlyOpenMensas }
}

val DataStore<Preferences>.showOnlyOpenMensasFlow
  get() = data.map { it[Keys.SHOW_ONLY_OPEN_MENSAS] ?: Defaults.SHOW_ONLY_OPEN_MENSAS }

suspend fun DataStore<Preferences>.saveShowOnlyExpandedMensas(showOnlyExpandedMensas: Boolean) {
  edit { it[Keys.SHOW_ONLY_EXPANDED_MENSAS] = showOnlyExpandedMensas }
}

val DataStore<Preferences>.showOnlyExpandedMensasFlow
  get() = data.map { it[Keys.SHOW_ONLY_EXPANDED_MENSAS] ?: Defaults.SHOW_ONLY_EXPANDED_MENSAS }

suspend fun DataStore<Preferences>.saveMenusLanguage(language: Language) {
  edit { it[Keys.MENU_LANGUAGE] = language.showMenusInGerman }
}

val DataStore<Preferences>.menusLanguageFlow
  get() = data.map { it[Keys.MENU_LANGUAGE]?.let(Language::fromBoolean) ?: Defaults.MENU_LANGUAGE }

suspend fun DataStore<Preferences>.saveMenuTypes(menuTypes: List<MenuType>) {
  edit { it[Keys.MENU_TYPES] = emptySet() } // necessary to conserve order
  edit { it[Keys.MENU_TYPES] = menuTypes.map(MenuType::code).toSet() }
}

val DataStore<Preferences>.menuTypesFlow
  get() = data.map { it[Keys.MENU_TYPES]?.map(MenuType::fromCode) ?: Defaults.MENU_TYPES }

suspend fun DataStore<Preferences>.saveShownLocations(shownLocations: List<Uuid>) {
  edit { it[Keys.SHOWN_LOCATIONS] = emptySet() } // necessary to conserve order
  edit { it[Keys.SHOWN_LOCATIONS] = shownLocations.map(Uuid::toString).toSet() }
}

val DataStore<Preferences>.shownLocationsFlow
  get() = data.map { it[Keys.SHOWN_LOCATIONS]?.map(Uuid::parse) ?: Defaults.SHOWN_LOCATIONS }

suspend fun DataStore<Preferences>.saveFavoriteMensas(favoriteMensas: List<Uuid>) {
  edit { it[Keys.FAVORITE_MENSAS] = emptySet() } // necessary to conserve order
  edit { it[Keys.FAVORITE_MENSAS] = favoriteMensas.map(Uuid::toString).toSet() }
}

suspend fun DataStore<Preferences>.toggleFavoriteMensa(mensa: Mensa) {
  edit { pref ->
    pref[Keys.FAVORITE_MENSAS] = pref[Keys.FAVORITE_MENSAS].orEmpty().let {
      if (mensa.id.toString() in it) it - mensa.id.toString() else it + mensa.id.toString()
    }
  }
}

val DataStore<Preferences>.favoriteMensasFlow
  get() = data.map { it[Keys.FAVORITE_MENSAS]?.map(Uuid::parse) ?: Defaults.FAVORITE_MENSAS }

suspend fun DataStore<Preferences>.saveHiddenMensas(hiddenMensas: List<Uuid>) {
  edit { it[Keys.HIDDEN_MENSAS] = hiddenMensas.map(Uuid::toString).toSet() }
}

suspend fun DataStore<Preferences>.toggleHiddenMensa(mensa: Mensa) {
  edit { pref ->
    pref[Keys.HIDDEN_MENSAS] = pref[Keys.HIDDEN_MENSAS].orEmpty().let {
      if (mensa.id.toString() in it) it - mensa.id.toString() else it + mensa.id.toString()
    }
  }
}

val DataStore<Preferences>.hiddenMensasFlow
  get() = data.map { it[Keys.HIDDEN_MENSAS]?.map(Uuid::parse) ?: Defaults.HIDDEN_MENSAS }

suspend fun DataStore<Preferences>.saveShowTomorrow(showTomorrow: Boolean) {
  edit { it[Keys.SHOW_TOMORROW] = showTomorrow }
}

val DataStore<Preferences>.showTomorrowFlow
  get() = data.map { it[Keys.SHOW_TOMORROW] ?: Defaults.SHOW_TOMORROW }

suspend fun DataStore<Preferences>.saveShowThisWeek(showThisWeek: Boolean) {
  edit { it[Keys.SHOW_THIS_WEEK] = showThisWeek }
}

val DataStore<Preferences>.showThisWeekFlow
  get() = data.map { it[Keys.SHOW_THIS_WEEK] ?: Defaults.SHOW_THIS_WEEK }

suspend fun DataStore<Preferences>.saveShowNextWeek(showNextWeek: Boolean) {
  edit { it[Keys.SHOW_NEXT_WEEK] = showNextWeek }
}

val DataStore<Preferences>.showNextWeekFlow
  get() = data.map { it[Keys.SHOW_NEXT_WEEK] ?: Defaults.SHOW_NEXT_WEEK }

suspend fun DataStore<Preferences>.saveListUseShortDescription(listUseShortDescription: Boolean) {
  edit { it[Keys.LIST_USE_SHORT_DESCRIPTION] = listUseShortDescription }
}

val DataStore<Preferences>.listUseShortDescriptionFlow
  get() = data.map { it[Keys.LIST_USE_SHORT_DESCRIPTION] ?: Defaults.LIST_USE_SHORT_DESCRIPTION }

suspend fun DataStore<Preferences>.saveListShowAllergens(listShowAllergens: Boolean) {
  edit { it[Keys.LIST_SHOW_ALLERGENS] = listShowAllergens }
}

val DataStore<Preferences>.listShowAllergensFlow
  get() = data.map { it[Keys.LIST_SHOW_ALLERGENS] ?: Defaults.LIST_SHOW_ALLERGENS }

suspend fun DataStore<Preferences>.saveAutoShowImage(autoShowImage: Boolean) {
  edit { it[Keys.AUTO_SHOW_IMAGE] = autoShowImage }
}

val DataStore<Preferences>.autoShowImageFlow
  get() = data.map { it[Keys.AUTO_SHOW_IMAGE] ?: Defaults.AUTO_SHOW_IMAGE }

suspend fun DataStore<Preferences>.saveTheme(theme: Theme) {
  edit { it[Keys.THEME] = theme.code }
}

val DataStore<Preferences>.themeFlow
  get() = data.map { it[Keys.THEME]?.let(Theme::fromCode) ?: Defaults.THEME }

suspend fun DataStore<Preferences>.saveUseDynamicColor(useDynamicColor: Boolean) {
  edit { it[Keys.USE_DYNAMIC_COLOR] = useDynamicColor }
}

val DataStore<Preferences>.useDynamicColorFlow
  get() = data.map { it[Keys.USE_DYNAMIC_COLOR] ?: Defaults.USE_DYNAMIC_COLOR }
