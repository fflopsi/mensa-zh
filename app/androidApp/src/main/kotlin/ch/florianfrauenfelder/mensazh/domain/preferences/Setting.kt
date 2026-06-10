package ch.florianfrauenfelder.mensazh.domain.preferences

import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.MenuType
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import kotlin.uuid.Uuid

sealed interface Setting {
  data class SetIsExpandedMensa(val mensa: Mensa) : Setting
  data class SetShowOnlyOpenMensas(val enabled: Boolean) : Setting
  data class SetShowOnlyExpandedMensas(val enabled: Boolean) : Setting
  data class SetMenusLanguage(val language: Language) : Setting
  data class SetMenuTypes(val menuTypes: List<MenuType>) : Setting

  data class SetShownLocations(val locations: List<Uuid>) : Setting
  data class SetFavoriteMensas(val mensas: List<Uuid>) : Setting
  data class SetIsFavoriteMensa(val mensa: Mensa) : Setting
  data class SetHiddenMensas(val mensas: List<Uuid>) : Setting
  data class SetIsHiddenMensa(val mensa: Mensa) : Setting

  data class SetShowTomorrow(val enabled: Boolean) : Setting
  data class SetShowThisWeek(val enabled: Boolean) : Setting
  data class SetShowNextWeek(val enabled: Boolean) : Setting

  data class SetListUseShortDescription(val enabled: Boolean) : Setting
  data class SetListShowAllergens(val enabled: Boolean) : Setting
  data class SetAutoShowImage(val enabled: Boolean) : Setting

  data class SetTheme(val theme: Theme) : Setting
  data class SetUseDynamicColor(val enabled: Boolean) : Setting
}
