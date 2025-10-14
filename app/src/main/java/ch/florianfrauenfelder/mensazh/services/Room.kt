package ch.florianfrauenfelder.mensazh.services

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.ui.Weekday
import kotlinx.datetime.LocalDate

@Entity(tableName = "menus", primaryKeys = ["mensaId", "language", "title", "date"])
data class RoomMenu(
  val mensaId: String,
  val language: String,
  val title: String,
  val description: String,
  val price: String,
  val allergens: String?,
  val isVegetarian: Boolean,
  val isVegan: Boolean,
  val imageUrl: String?,
  val date: String,
  val creationDate: Long = System.currentTimeMillis(),
) {
  fun toMenu(): Menu = Menu(
    title = title,
    description = description,
    price = SerializationService.deserializeList(price),
    allergens = allergens,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    imageUrl = imageUrl,
    weekday = Weekday.entries[LocalDate.parse(date).dayOfWeek.ordinal],
  )
}

@Entity(tableName = "fetchinfo", primaryKeys = ["institution", "destination", "language"])
data class FetchInfo(
  val institution: String,
  val destination: String,
  val language: String,
  val fetchDate: Long = System.currentTimeMillis(),
)

@Dao
interface MenuDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenu(menu: RoomMenu)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenus(menus: List<RoomMenu>)

  @Query("SELECT * FROM menus WHERE date = :date")
  suspend fun getMenus(date: String): List<RoomMenu>

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date = :date")
  suspend fun getMenus(mensaId: String, language: String, date: String): List<RoomMenu>

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date BETWEEN :startDate AND :endDate")
  suspend fun getMenus(
    mensaId: String,
    language: String,
    startDate: String,
    endDate: String,
  ): List<RoomMenu>

  @Query("DELETE FROM menus WHERE creationDate < :expiredBefore")
  suspend fun deleteExpired(expiredBefore: Long)

  @Query("DELETE FROM menus")
  suspend fun clearAll()
}

@Dao
interface FetchInfoDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFetchInfo(fetchInfo: FetchInfo)

  @Query("SELECT * FROM fetchinfo WHERE institution = :institution AND destination = :destination AND language = :language LIMIT 1")
  suspend fun getFetchInfo(institution: String, destination: String, language: String): FetchInfo?
}

@Database(entities = [RoomMenu::class, FetchInfo::class], version = 1)
abstract class CacheDatabase : RoomDatabase() {
  abstract fun menuDao(): MenuDao
  abstract fun fetchInfoDao(): FetchInfoDao
}
