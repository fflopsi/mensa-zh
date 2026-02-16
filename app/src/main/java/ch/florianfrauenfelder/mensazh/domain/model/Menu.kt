package ch.florianfrauenfelder.mensazh.domain.model

import android.os.Parcelable
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
  val index: Int,
  val title: String,
  val description: String,
  val price: List<String>,
  val allergens: String?,
  val isVegetarian: Boolean,
  val isVegan: Boolean,
  val imageUrl: String?,
  val weekday: Weekday,
) : Parcelable {
  override fun toString() = title + description

  companion object {
    val dummy = Menu(
      index = 1,
      title = "vitality",
      description = "Gnocchi mit Quornwürfel und Tomatensauce",
      price = listOf("14.50", "20.50", "21.30"),
      allergens = "Sellerie, Nüsse, Schweinefleisch",
      isVegetarian = true,
      isVegan = false,
      imageUrl = null,
      weekday = Weekday.Monday,
    )
  }
}
