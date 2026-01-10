package ch.florianfrauenfelder.mensazh.domain.value

enum class Institution {
  ETH, UZH;

  override fun toString() = when (this) {
    ETH -> "ETH"
    UZH -> "UZH"
  }
}
