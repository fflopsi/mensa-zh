package ch.florianfrauenfelder.mensazh.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<CacheDatabase> {
  val appContext = context.applicationContext
  val dbFile = appContext.getDatabasePath(databaseName)
  return Room.databaseBuilder<CacheDatabase>(
    context = appContext,
    name = dbFile.absolutePath,
  )
}
