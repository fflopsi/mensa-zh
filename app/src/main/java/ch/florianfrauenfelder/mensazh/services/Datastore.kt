package ch.florianfrauenfelder.mensazh.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore by preferencesDataStore(name = "settings")

object Prefs {
  object Keys {
    val FAVORITE_MENSAS = stringSetPreferencesKey("favorite_mensas")
    val SHOW_ONLY_OPEN_MENSAS = booleanPreferencesKey("show_only_open_mensas")
    val SHOW_ONLY_FAVORITE_MENSAS = booleanPreferencesKey("show_only_favorite_mensas")
    val SHOW_MENUS_IN_GERMAN = booleanPreferencesKey("german_menus")
    val SHOWN_LOCATIONS = stringSetPreferencesKey("shown_locations")
    val HIDDEN_MENSAS = stringSetPreferencesKey("hidden_mensas")
    val SHOW_TOMORROW = booleanPreferencesKey("show_tomorrow")
    val SHOW_THIS_WEEK = booleanPreferencesKey("show_this_week")
    val SHOW_NEXT_WEEK = booleanPreferencesKey("show_next_week")
    val THEME = intPreferencesKey("theme")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("dyanmic_color")
  }

  object Defaults {
    val FAVORITE_MENSAS = emptySet<String>()
    const val SHOW_ONLY_OPEN_MENSAS = false
    const val SHOW_ONLY_FAVORITE_MENSAS = false
    const val SHOW_MENUS_IN_GERMAN = false
    val SHOWN_LOCATIONS = listOf(
      "99120f22-7a65-4b36-8619-9eb318334950", // ETH Zentrum
      "b5c9bc49-c1e0-4f24-807f-e2212a9933fe", // ETH Höngg
      "ce04e654-d13b-4733-b878-884514d079b7", // ETH Oerlikon
      "125c00f8-dfbb-4db6-9683-6113cb78aa68", // UZH Zentrum
      "99222f55-901f-417a-bcbc-191e38628485", // UZH Irchel
      "8b4b82af-64ae-45c9-bc43-dc8a4580a019", // UZH Other
    ).map { UUID.fromString(it) }
    val HIDDEN_MENSAS = emptyList<UUID>()
    const val SHOW_TOMORROW = false
    const val SHOW_THIS_WEEK = true
    const val SHOW_NEXT_WEEK = true
    const val THEME = 0
    const val USE_DYNAMIC_COLOR = true
  }
}

suspend fun Context.saveIsFavoriteMensa(mensa: Mensa, favorite: Boolean) {
  dataStore.edit {
    it[Prefs.Keys.FAVORITE_MENSAS] = it[Prefs.Keys.FAVORITE_MENSAS].orEmpty().toMutableSet().apply {
      if (favorite) this.add(mensa.id.toString()) else this.remove(mensa.id.toString())
    }
  }
}

val Context.favoriteMensasFlow
  get() = dataStore.data.map { it[Prefs.Keys.FAVORITE_MENSAS] ?: Prefs.Defaults.FAVORITE_MENSAS }

suspend fun Context.saveShowOnlyOpenMensas(showOnlyOpenMensas: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_ONLY_OPEN_MENSAS] = showOnlyOpenMensas }
}

val Context.showOnlyOpenMensasFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_ONLY_OPEN_MENSAS] ?: Prefs.Defaults.SHOW_ONLY_OPEN_MENSAS
  }

suspend fun Context.saveShowOnlyFavoriteMensas(showOnlyFavoriteMensas: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_ONLY_FAVORITE_MENSAS] = showOnlyFavoriteMensas }
}

val Context.showOnlyFavoriteMensasFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_ONLY_FAVORITE_MENSAS] ?: Prefs.Defaults.SHOW_ONLY_FAVORITE_MENSAS
  }

suspend fun Context.saveShowMenusInGerman(showMenusInGerman: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_MENUS_IN_GERMAN] = showMenusInGerman }
}

val Context.showMenusInGermanFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_MENUS_IN_GERMAN] ?: Prefs.Defaults.SHOW_MENUS_IN_GERMAN
  }

suspend fun Context.saveShownLocations(shownLocations: List<Location>) {
  dataStore.edit { pref ->
    pref[Prefs.Keys.SHOWN_LOCATIONS] = emptySet() // necessary to conserve order
  }
  dataStore.edit { pref ->
    pref[Prefs.Keys.SHOWN_LOCATIONS] = shownLocations.map { it.id.toString() }.toSet()
  }
}

val Context.shownLocationsFlow
  get() = dataStore.data.map { pref ->
    pref[Prefs.Keys.SHOWN_LOCATIONS]?.map { UUID.fromString(it) } ?: Prefs.Defaults.SHOWN_LOCATIONS
  }

suspend fun Context.saveHiddenMensas(hiddenMensas: List<UUID>) {
  dataStore.edit { pref ->
    pref[Prefs.Keys.HIDDEN_MENSAS] = hiddenMensas.map { it.toString() }.toSet()
  }
}

val Context.hiddenMensasFlow
  get() = dataStore.data.map { pref ->
    pref[Prefs.Keys.HIDDEN_MENSAS]?.map { UUID.fromString(it) } ?: Prefs.Defaults.HIDDEN_MENSAS
  }

suspend fun Context.saveShowTomorrow(showTomorrow: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_TOMORROW] = showTomorrow }
}

val Context.showTomorrowFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_TOMORROW] ?: Prefs.Defaults.SHOW_TOMORROW
  }

suspend fun Context.saveShowThisWeek(showThisWeek: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_THIS_WEEK] = showThisWeek }
}

val Context.showThisWeekFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_THIS_WEEK] ?: Prefs.Defaults.SHOW_THIS_WEEK
  }

suspend fun Context.saveShowNextWeek(showNextWeek: Boolean) {
  dataStore.edit { it[Prefs.Keys.SHOW_NEXT_WEEK] = showNextWeek }
}

val Context.showNextWeekFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.SHOW_NEXT_WEEK] ?: Prefs.Defaults.SHOW_NEXT_WEEK
  }

suspend fun Context.saveTheme(theme: Int) {
  require(theme in 0..2) { "Value $theme is not allowed for theme." }
  dataStore.edit { it[Prefs.Keys.THEME] = theme }
}

val Context.themeFlow
  get() = dataStore.data.map { it[Prefs.Keys.THEME] ?: Prefs.Defaults.THEME }

suspend fun Context.saveUseDynamicColor(useDynamicColor: Boolean) {
  dataStore.edit { it[Prefs.Keys.USE_DYNAMIC_COLOR] = useDynamicColor }
}

val Context.useDynamicColorFlow
  get() = dataStore.data.map {
    it[Prefs.Keys.USE_DYNAMIC_COLOR] ?: Prefs.Defaults.USE_DYNAMIC_COLOR
  }
