package ch.florianfrauenfelder.mensazh.domain.navigation

enum class Weekday {
  Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday;

  fun next() = entries[(this.ordinal + 1) % 7]
}
