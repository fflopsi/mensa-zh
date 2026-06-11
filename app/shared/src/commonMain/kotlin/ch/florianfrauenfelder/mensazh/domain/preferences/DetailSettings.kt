package ch.florianfrauenfelder.mensazh.domain.preferences

data class DetailSettings(
  val listUseShortDescription: Boolean = Defaults.LIST_USE_SHORT_DESCRIPTION,
  val listShowAllergens: Boolean = Defaults.LIST_SHOW_ALLERGENS,
  val autoShowImage: Boolean = Defaults.AUTO_SHOW_IMAGE,
)
