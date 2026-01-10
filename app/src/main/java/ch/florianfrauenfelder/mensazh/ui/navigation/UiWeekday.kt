package ch.florianfrauenfelder.mensazh.ui.navigation

import androidx.annotation.StringRes
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday

val Weekday.label: Int
  @StringRes get() = when (this) {
    Weekday.Monday -> R.string.monday
    Weekday.Tuesday -> R.string.tuesday
    Weekday.Wednesday -> R.string.wednesday
    Weekday.Thursday -> R.string.thursday
    Weekday.Friday -> R.string.friday
    Weekday.Saturday -> R.string.saturday
    Weekday.Sunday -> R.string.sunday
  }
