package ch.florianfrauenfelder.mensazh.domain.value

sealed interface Event {
  data object NoInternet : Event
  data object ApiError : Event
  data class SlowInternet(val onCancel: () -> Unit) : Event
  data object DismissSlowInternet : Event
}
