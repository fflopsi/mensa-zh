package ch.florianfrauenfelder.mensazh.ui.domain

import androidx.annotation.StringRes
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.value.MenuType

val MenuType.label: Int
  @StringRes get() = when (this) {
    MenuType.Standard -> R.string.standard
    MenuType.Vegetarian -> R.string.vegetarian
    MenuType.Vegan -> R.string.vegan
  }
