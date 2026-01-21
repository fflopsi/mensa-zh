package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import java.util.UUID

@Dao
interface MenuDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenu(menu: RoomMenu)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenus(menus: List<RoomMenu>)

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date = :date")
  fun getMenus(mensaId: UUID, language: Language, date: LocalDate): Flow<List<RoomMenu>>

  @Query("SELECT * FROM menus WHERE mensaId = :mensaId AND language = :language AND date BETWEEN :startDate AND :endDate")
  fun getMenus(
    mensaId: UUID,
    language: Language,
    startDate: LocalDate,
    endDate: LocalDate,
  ): Flow<List<RoomMenu>>

  @Query("DELETE FROM menus WHERE creationDate < :expiredBefore")
  suspend fun deleteExpired(expiredBefore: Long)

  @Query("DELETE FROM menus")
  suspend fun clearAll()
}
