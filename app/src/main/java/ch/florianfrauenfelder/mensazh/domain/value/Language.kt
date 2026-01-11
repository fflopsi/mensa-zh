package ch.florianfrauenfelder.mensazh.domain.value

enum class Language {
  German, English;

  override fun toString() = when (this) {
    German -> "de"
    English -> "en"
  }

  operator fun not(): Language {
    if (this == German) return English
    return German
  }

  val showMenusInGerman get() = this == German

  companion object {
    val default = English
  }
}

val String.toLanguage
  get() = when (this) {
    "en" -> Language.English
    "de" -> Language.German
    else -> throw IllegalArgumentException("$this is not a language")
  }

val Boolean.showMenusInGermanToLanguage get() = if (this) Language.German else Language.English
