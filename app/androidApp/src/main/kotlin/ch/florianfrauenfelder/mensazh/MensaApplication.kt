package ch.florianfrauenfelder.mensazh

import android.app.Application
import ch.florianfrauenfelder.mensazh.data.local.datastore.createDataStore
import ch.florianfrauenfelder.mensazh.data.local.room.getDatabaseBuilder
import ch.florianfrauenfelder.mensazh.data.local.room.getRoomDatabase

class MensaApplication : Application() {
  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(
      dataStore = createDataStore(applicationContext),
      database = getRoomDatabase(getDatabaseBuilder(applicationContext)),
    )
  }
}
