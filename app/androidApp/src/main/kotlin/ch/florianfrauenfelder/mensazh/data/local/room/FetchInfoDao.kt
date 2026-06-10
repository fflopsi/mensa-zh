package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language

@Dao
interface FetchInfoDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFetchInfo(fetchInfo: FetchInfo)

  @Query("SELECT * FROM fetchinfo WHERE institution = :institution AND destination = :destination AND language = :language LIMIT 1")
  suspend fun getFetchInfo(
    institution: Institution,
    destination: Destination,
    language: Language,
  ): FetchInfo?

  @Query("DELETE FROM fetchinfo")
  suspend fun clearAll()
}
