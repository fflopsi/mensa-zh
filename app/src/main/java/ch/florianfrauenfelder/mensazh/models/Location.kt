package ch.florianfrauenfelder.mensazh.models

data class Location(
  val title: String,
  val mensas: List<Mensa>,
) {
  override fun toString() = title

  companion object {
    val dummy = Location(
      title = "Zentrum",
      mensas = List(3) { Mensa.dummy },
    )
  }
}
