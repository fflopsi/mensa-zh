package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.NutrientsPer
import kotlinx.datetime.LocalDate

@Entity(tableName = "menus", primaryKeys = ["mensaId", "language", "title", "date"])
data class RoomMenu(
  val mensaId: String,
  val index: Int,
  val language: Language,
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
  @ColumnInfo(defaultValue = "100g") val nutrientsPer: NutrientsPer,
  val allergens: String?,
  val isVegetarian: Boolean,
  val isVegan: Boolean,
  val imageUrl: String?,
  val date: LocalDate,
  val creationDate: Long = System.currentTimeMillis(),
) {
  fun toMenu(): Menu = Menu(
    index = index,
    title = title,
    description = description,
    price = price,
    energy = energy,
    fat = fat,
    saturatedFattyAcids = saturatedFattyAcids,
    carbohydrates = carbohydrates,
    sugar = sugar,
    fiber = fiber,
    protein = protein,
    salt = salt,
    nutrientsPer = nutrientsPer,
    allergens = allergens,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    imageUrl = imageUrl,
    weekday = Weekday.entries[date.dayOfWeek.ordinal],
  )
}
