package ch.florianfrauenfelder.mensazh.ui.domain

import ch.florianfrauenfelder.mensazh.domain.value.MenuType
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.standard
import mensazh.app.shared.generated.resources.vegan
import mensazh.app.shared.generated.resources.vegetarian
import org.jetbrains.compose.resources.StringResource

val MenuType.label: StringResource
  get() = when (this) {
    MenuType.Standard -> Res.string.standard
    MenuType.Vegetarian -> Res.string.vegetarian
    MenuType.Vegan -> Res.string.vegan
  }
