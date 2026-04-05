package ch.florianfrauenfelder.mensazh.domain.model

import android.os.Parcelable
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.value.NutrientsPer
import kotlinx.parcelize.Parcelize

@Parcelize
data class Menu(
  val index: Int,
  val title: String,
  val description: String,
  val price: List<String>,
  val energy: Double?,
  val fat: Double?,
  val saturatedFattyAcids: Double?,
  val carbohydrates: Double?,
  val sugar: Double?,
  val fiber: Double?,
  val protein: Double?,
  val salt: Double?,
  val nutrientsPer: NutrientsPer,
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
      energy = 145.0,
      fat = 1.3,
      saturatedFattyAcids = 0.3,
      carbohydrates = 45.0,
      sugar = 7.8,
      fiber = 14.0,
      protein = 4.6,
      salt = 0.9,
      nutrientsPer = NutrientsPer.OneHundredGrams,
      allergens = "Sellerie, Nüsse, Schweinefleisch",
      isVegetarian = true,
      isVegan = false,
      imageUrl = null,
      weekday = Weekday.Monday,
    )
  }
}
