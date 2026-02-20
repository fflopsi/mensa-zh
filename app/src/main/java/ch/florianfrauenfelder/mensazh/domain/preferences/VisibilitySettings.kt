package ch.florianfrauenfelder.mensazh.domain.preferences

import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.MenuType
import kotlin.uuid.Uuid

data class VisibilitySettings(
  val expandedMensas: List<Uuid> = Defaults.EXPANDED_MENSAS,
  val showOnlyOpenMensas: Boolean = Defaults.SHOW_ONLY_OPEN_MENSAS,
  val showOnlyExpandedMensas: Boolean = Defaults.SHOW_ONLY_EXPANDED_MENSAS,
  val language: Language = Defaults.MENU_LANGUAGE,
  val menuTypes: List<MenuType> = Defaults.MENU_TYPES,
)
