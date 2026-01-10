package ch.florianfrauenfelder.mensazh.domain.model

data class MensaState(
  val mensa: Mensa,
  val menus: List<Menu> = emptyList(),
  val state: State = State.Initial,
) {
  enum class State { Initial, Closed, Available, Expanded }

  companion object {
    val dummy = MensaState(
      mensa = Mensa.dummy,
      menus = List(5) { Menu.dummy },
      state = State.Available,
    )
  }
}
