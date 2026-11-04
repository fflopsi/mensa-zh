package ch.florianfrauenfelder.mensazh.data.util

import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

fun currentWeekday() =
  Weekday.entries[Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek.ordinal]
