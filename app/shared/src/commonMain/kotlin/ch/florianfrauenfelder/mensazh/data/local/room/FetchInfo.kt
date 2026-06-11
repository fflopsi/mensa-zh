package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Entity
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlin.time.Clock

@Entity(tableName = "fetchinfo", primaryKeys = ["institution", "destination", "language"])
data class FetchInfo(
  val institution: Institution,
  val destination: Destination,
  val language: Language,
  val fetchDate: Long = Clock.System.now().toEpochMilliseconds(),
)
