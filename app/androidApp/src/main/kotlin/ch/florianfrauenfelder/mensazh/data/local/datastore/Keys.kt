package ch.florianfrauenfelder.mensazh.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object Keys {
  val EXPANDED_MENSAS = stringSetPreferencesKey("expanded_mensas")
  val SHOW_ONLY_OPEN_MENSAS = booleanPreferencesKey("show_only_open_mensas")
  val SHOW_ONLY_EXPANDED_MENSAS = booleanPreferencesKey("show_only_expanded_mensas")
  val MENU_LANGUAGE = booleanPreferencesKey("german_menus")
  val MENU_TYPES = stringSetPreferencesKey("menu_types")

  val SHOWN_LOCATIONS = stringSetPreferencesKey("shown_locations")
  val FAVORITE_MENSAS = stringSetPreferencesKey("favorite_mensas")
  val HIDDEN_MENSAS = stringSetPreferencesKey("hidden_mensas")

  val SHOW_TOMORROW = booleanPreferencesKey("show_tomorrow")
  val SHOW_THIS_WEEK = booleanPreferencesKey("show_this_week")
  val SHOW_NEXT_WEEK = booleanPreferencesKey("show_next_week")

  val LIST_USE_SHORT_DESCRIPTION = booleanPreferencesKey("list_use_short_description")
  val LIST_SHOW_ALLERGENS = booleanPreferencesKey("list_show_allergens")
  val AUTO_SHOW_IMAGE = booleanPreferencesKey("autoshow_image")

  val THEME = intPreferencesKey("theme")
  val USE_DYNAMIC_COLOR = booleanPreferencesKey("dyanmic_color")
}
