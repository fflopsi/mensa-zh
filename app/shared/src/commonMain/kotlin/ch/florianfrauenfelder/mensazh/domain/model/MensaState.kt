package ch.florianfrauenfelder.mensazh.domain.model

data class MensaState(
  val mensa: Mensa,
  val menus: List<Menu> = emptyList(),
  val state: State = State.Initial,
  val favorite: Boolean = false,
) {
  enum class State {
    Initial, Closed, Available, Expanded;

    val active
      get() = this == Available || this == Expanded
  }

  companion object {
    val dummy = MensaState(
      mensa = Mensa.dummy,
      menus = List(5) { Menu.dummy },
      state = State.Available,
      favorite = false,
    )
  }
}
