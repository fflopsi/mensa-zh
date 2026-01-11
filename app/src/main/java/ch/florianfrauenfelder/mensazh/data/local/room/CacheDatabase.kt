package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomMenu::class, FetchInfo::class], version = 1)
abstract class CacheDatabase : RoomDatabase() {
  abstract fun menuDao(): MenuDao
  abstract fun fetchInfoDao(): FetchInfoDao
}
