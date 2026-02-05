package ch.florianfrauenfelder.mensazh.domain.preferences

import ch.florianfrauenfelder.mensazh.domain.value.Theme

data class ThemeSettings(
  val theme: Theme = Defaults.THEME,
  val useDynamicColor: Boolean = Defaults.USE_DYNAMIC_COLOR,
)
