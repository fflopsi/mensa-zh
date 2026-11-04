package ch.florianfrauenfelder.mensazh.domain.preferences

data class DestinationSettings(
  val showTomorrow: Boolean = Defaults.SHOW_TOMORROW,
  val showThisWeek: Boolean = Defaults.SHOW_THIS_WEEK,
  val showNextWeek: Boolean = Defaults.SHOW_NEXT_WEEK,
) {
  val showAny = showTomorrow || showThisWeek || showNextWeek
}
