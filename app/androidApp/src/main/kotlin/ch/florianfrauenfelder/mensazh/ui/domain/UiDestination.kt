package ch.florianfrauenfelder.mensazh.ui.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination

data class UiDestination(@StringRes val label: Int, @DrawableRes val icon: Int)

val Destination.ui: UiDestination
  get() = when (this) {
    Destination.Today -> UiDestination(R.string.today, R.drawable.ic_today_24)
    Destination.Tomorrow -> UiDestination(R.string.tomorrow, R.drawable.ic_event_24)
    Destination.ThisWeek -> UiDestination(R.string.this_week, R.drawable.ic_date_range_24)
    Destination.NextWeek -> UiDestination(R.string.next_week, R.drawable.ic_calendar_month_24)
  }
