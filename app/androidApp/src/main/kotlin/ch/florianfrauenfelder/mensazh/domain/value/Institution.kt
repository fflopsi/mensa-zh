package ch.florianfrauenfelder.mensazh.domain.value

enum class Institution(val code: String) {
  ETH("ETH"), UZH("UZH");

  companion object {
    fun fromCode(code: String) =
      entries.firstOrNull { it.code == code } ?: error("$code is not an institution")
  }
}
