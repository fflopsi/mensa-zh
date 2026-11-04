package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Room
import androidx.room.RoomDatabase
import ch.florianfrauenfelder.mensazh.data.local.getAppDirectory
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase> {
  val dbFile = File(getAppDirectory(), databaseName)
  return Room.databaseBuilder<CacheDatabase>(name = dbFile.absolutePath)
}
