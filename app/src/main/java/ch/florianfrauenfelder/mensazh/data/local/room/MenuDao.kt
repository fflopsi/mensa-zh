package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenu(menu: RoomMenu)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenus(menus: List<RoomMenu>)

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date = :date")
  fun getMenus2(mensaId: String, language: String, date: String): Flow<List<RoomMenu>>

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date BETWEEN :startDate AND :endDate")
  fun getMenus2(
    mensaId: String,
    language: String,
    startDate: String,
    endDate: String,
  ): Flow<List<RoomMenu>>

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
