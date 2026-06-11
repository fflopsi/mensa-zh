package ch.florianfrauenfelder.mensazh

import android.content.Context
import ch.florianfrauenfelder.mensazh.data.local.datastore.createDataStore
import ch.florianfrauenfelder.mensazh.data.local.room.getDatabaseBuilder
import ch.florianfrauenfelder.mensazh.data.local.room.getRoomDatabase

fun createAppcontainer(context: Context): AppContainer {
  val appContext = context.applicationContext
  return AppContainer(
    dataStore = createDataStore(appContext),
    database = getRoomDatabase(getDatabaseBuilder(appContext)),
  )
}
