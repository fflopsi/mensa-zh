package ch.florianfrauenfelder.mensazh.ui.domain

import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.friday
import mensazh.app.shared.generated.resources.monday
import mensazh.app.shared.generated.resources.saturday
import mensazh.app.shared.generated.resources.sunday
import mensazh.app.shared.generated.resources.thursday
import mensazh.app.shared.generated.resources.tuesday
import mensazh.app.shared.generated.resources.wednesday
import org.jetbrains.compose.resources.StringResource

val Weekday.label: StringResource
  get() = when (this) {
    Weekday.Monday -> Res.string.monday
    Weekday.Tuesday -> Res.string.tuesday
    Weekday.Wednesday -> Res.string.wednesday
    Weekday.Thursday -> Res.string.thursday
    Weekday.Friday -> Res.string.friday
    Weekday.Saturday -> Res.string.saturday
    Weekday.Sunday -> Res.string.sunday
  }
