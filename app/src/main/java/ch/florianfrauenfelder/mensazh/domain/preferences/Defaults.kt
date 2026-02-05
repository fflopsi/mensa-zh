package ch.florianfrauenfelder.mensazh.domain.preferences

import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import kotlin.uuid.Uuid

object Defaults {
  val EXPANDED_MENSAS = emptyList<Uuid>()
  const val SHOW_ONLY_OPEN_MENSAS = false
  const val SHOW_ONLY_EXPANDED_MENSAS = false
  val MENU_LANGUAGE = Language.default

  val SHOWN_LOCATIONS = listOf(
    "99120f22-7a65-4b36-8619-9eb318334950", // ETH Zentrum
    "b5c9bc49-c1e0-4f24-807f-e2212a9933fe", // ETH Höngg
    "ce04e654-d13b-4733-b878-884514d079b7", // ETH Oerlikon
    "125c00f8-dfbb-4db6-9683-6113cb78aa68", // UZH Zentrum
    "99222f55-901f-417a-bcbc-191e38628485", // UZH Irchel
    "8b4b82af-64ae-45c9-bc43-dc8a4580a019", // UZH Other
  ).map { Uuid.parse(it) }
  val FAVORITE_MENSAS = emptyList<Uuid>()
  val HIDDEN_MENSAS = emptyList<Uuid>()

  const val SHOW_TOMORROW = false
  const val SHOW_THIS_WEEK = true
  const val SHOW_NEXT_WEEK = true

  const val LIST_USE_SHORT_DESCRIPTION = true
  const val LIST_SHOW_ALLERGENS = false
  const val AUTO_SHOW_IMAGE = true

  val THEME = Theme.default
  const val USE_DYNAMIC_COLOR = true
}
