package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

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

fun getRoomDatabase(builder: RoomDatabase.Builder<CacheDatabase>): CacheDatabase =
  builder.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()

internal const val databaseName = "cache-database.db"
