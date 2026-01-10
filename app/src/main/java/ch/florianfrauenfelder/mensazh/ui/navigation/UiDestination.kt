package ch.florianfrauenfelder.mensazh.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination

data class UiDestination(@StringRes val label: Int, val icon: ImageVector)

val Destination.ui: UiDestination
  get() = when (this) {
    Destination.Today -> UiDestination(R.string.today, Icons.Default.Today)
    Destination.Tomorrow -> UiDestination(R.string.tomorrow, Icons.Default.Event)
    Destination.ThisWeek -> UiDestination(R.string.this_week, Icons.Default.DateRange)
    Destination.NextWeek -> UiDestination(R.string.next_week, Icons.Default.CalendarMonth)
  }
