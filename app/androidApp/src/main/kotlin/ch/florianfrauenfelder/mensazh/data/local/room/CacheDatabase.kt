package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [RoomMenu::class, FetchInfo::class],
  version = 2,
  autoMigrations = [
    AutoMigration(from = 1, to = 2),
  ],
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
  abstract fun menuDao(): MenuDao
  abstract fun fetchInfoDao(): FetchInfoDao
}
