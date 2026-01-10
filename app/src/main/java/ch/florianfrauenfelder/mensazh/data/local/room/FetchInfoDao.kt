package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FetchInfoDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFetchInfo(fetchInfo: FetchInfo)

  @Query("SELECT * FROM fetchinfo WHERE institution = :institution AND destination = :destination AND language = :language LIMIT 1")
  suspend fun getFetchInfo(institution: String, destination: String, language: String): FetchInfo?
}
