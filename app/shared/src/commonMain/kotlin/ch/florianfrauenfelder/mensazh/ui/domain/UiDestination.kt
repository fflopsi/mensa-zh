package ch.florianfrauenfelder.mensazh.ui.domain

import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.ic_calendar_month_24
import mensazh.app.shared.generated.resources.ic_date_range_24
import mensazh.app.shared.generated.resources.ic_event_24
import mensazh.app.shared.generated.resources.ic_today_24
import mensazh.app.shared.generated.resources.next_week
import mensazh.app.shared.generated.resources.this_week
import mensazh.app.shared.generated.resources.today
import mensazh.app.shared.generated.resources.tomorrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class UiDestination(val label: StringResource, val icon: DrawableResource)

val Destination.ui: UiDestination
  get() = when (this) {
    Destination.Today -> UiDestination(Res.string.today, Res.drawable.ic_today_24)
    Destination.Tomorrow -> UiDestination(Res.string.tomorrow, Res.drawable.ic_event_24)
    Destination.ThisWeek -> UiDestination(Res.string.this_week, Res.drawable.ic_date_range_24)
    Destination.NextWeek -> UiDestination(Res.string.next_week, Res.drawable.ic_calendar_month_24)
  }
