package ch.florianfrauenfelder.mensazh.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ch.florianfrauenfelder.mensazh.models.Mensa
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object Prefs {
  object Keys {
    val FAVORITE_MENSAS = stringSetPreferencesKey("favorite_mensas")
    val SHOW_ONLY_OPEN_MENSAS = booleanPreferencesKey("show_only_open_mensas")
    val SHOW_ONLY_FAVORITE_MENSAS = booleanPreferencesKey("show_only_favorite_mensas")
    val SHOW_MENUS_IN_GERMAN = booleanPreferencesKey("german_menus")
  }

  object Defaults {
    val FAVORITE_MENSAS = emptySet<String>()
    const val SHOW_ONLY_OPEN_MENSAS = false
    const val SHOW_ONLY_FAVORITE_MENSAS = false
    const val SHOW_MENUS_IN_GERMAN = false
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
