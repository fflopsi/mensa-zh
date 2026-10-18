package ch.florianfrauenfelder.mensazh.domain.navigation

import kotlinx.serialization.Serializable

@Serializable
enum class Weekday {
  Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday;

  fun next() = entries[(this.ordinal + 1) % 7]
}
