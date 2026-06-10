package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Entity
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language

@Entity(tableName = "fetchinfo", primaryKeys = ["institution", "destination", "language"])
data class FetchInfo(
  val institution: Institution,
  val destination: Destination,
  val language: Language,
  val fetchDate: Long = System.currentTimeMillis(),
)
