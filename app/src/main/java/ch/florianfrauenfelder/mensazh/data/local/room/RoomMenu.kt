package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Entity
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.navigation.Weekday
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.datetime.LocalDate

@Entity(tableName = "menus", primaryKeys = ["mensaId", "language", "title", "date"])
data class RoomMenu(
  val mensaId: String,
  val index: Int,
  val language: Language,
  val title: String,
  val description: String,
  val price: List<String>,
  val allergens: String?,
  val isVegetarian: Boolean,
  val isVegan: Boolean,
  val imageUrl: String?,
  val date: LocalDate,
  val creationDate: Long = System.currentTimeMillis(),
) {
  fun toMenu(): Menu = Menu(
    title = title,
    description = description,
    price = price,
    allergens = allergens,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    imageUrl = imageUrl,
    weekday = Weekday.entries[date.dayOfWeek.ordinal],
  )
}
