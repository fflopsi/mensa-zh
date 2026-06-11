package ch.florianfrauenfelder.mensazh.domain.value

enum class Language(val code: String) {
  German("de"), English("en");

  operator fun not(): Language {
    if (this == German) return English
    return German
  }

  val showMenusInGerman get() = this == German

  companion object {
    val default = English

    fun fromCode(code: String) =
      entries.firstOrNull { it.code == code } ?: error("$code is not a language")

    fun fromBoolean(showMenusInGerman: Boolean) = if (showMenusInGerman) German else English
  }
}
