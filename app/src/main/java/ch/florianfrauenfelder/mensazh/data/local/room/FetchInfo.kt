package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Entity

@Entity(tableName = "fetchinfo", primaryKeys = ["institution", "destination", "language"])
data class FetchInfo(
  val institution: String,
  val destination: String,
  val language: String,
  val fetchDate: Long = System.currentTimeMillis(),
)
