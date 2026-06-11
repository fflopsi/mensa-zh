package ch.florianfrauenfelder.mensazh.domain.value

import ch.florianfrauenfelder.mensazh.domain.model.Menu

enum class MenuType(val code: String) {
  Standard("S"), Vegetarian("V"), Vegan("V+");

  companion object {
    fun fromCode(code: String) =
      entries.firstOrNull { it.code == code } ?: error("$code is not a menu type")
  }
}

val Menu.type: MenuType
  get() = when {
    isVegan -> MenuType.Vegan
    isVegetarian -> MenuType.Vegetarian
    else -> MenuType.Standard
  }
