package ch.florianfrauenfelder.mensazh.domain.value

enum class NutrientsPer(val code: String) {
  Serving("serving"), OneHundredGrams("100g");

  companion object {
    fun fromCode(code: String) =
      entries.firstOrNull { it.code == code } ?: error("$code is not a nutrients measurement")
  }
}
