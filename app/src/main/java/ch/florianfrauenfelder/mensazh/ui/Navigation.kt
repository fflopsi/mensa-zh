package ch.florianfrauenfelder.mensazh.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import ch.florianfrauenfelder.mensazh.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface Route {
  @Serializable
  object Main : Route

  @Serializable
  object Settings : Route
}

enum class Destination(@param:StringRes val label: Int, val icon: ImageVector) {
  Today(label = R.string.today, icon = Icons.Default.Today),
  Tomorrow(label = R.string.tomorrow, icon = Icons.Default.Event),
  ThisWeek(label = R.string.this_week, icon = Icons.Default.DateRange),
  NextWeek(label = R.string.next_week, icon = Icons.Default.CalendarMonth),
}

enum class Weekday(@param:StringRes val label: Int) {
  Monday(label = R.string.monday),
  Tuesday(label = R.string.tuesday),
  Wednesday(label = R.string.wednesday),
  Thursday(label = R.string.thursday),
  Friday(label = R.string.friday),
  Saturday(label = R.string.saturday),
  Sunday(label = R.string.sunday);

  fun next() = entries[(this.ordinal + 1) % 7]

  companion object {
    @OptIn(ExperimentalTime::class)
    fun fromNow() =
      entries[Clock.System.todayIn(TimeZone.Companion.currentSystemDefault()).dayOfWeek.ordinal]
  }
}
