package ch.florianfrauenfelder.mensazh.domain.value

sealed interface Event {
  object NoInternet : Event
  object ApiError : Event
}
