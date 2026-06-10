package ch.florianfrauenfelder.mensazh.domain.value

enum class Theme(val code: Int) {
  Auto(0), Light(1), Dark(2);

  companion object {
    val default = Auto

    fun fromCode(code: Int) =
      entries.firstOrNull { it.code == code } ?: error("$code is not a theme")
  }
}
