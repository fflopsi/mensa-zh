package ch.florianfrauenfelder.mensazh

import ch.florianfrauenfelder.mensazh.data.local.datastore.createDataStore
import ch.florianfrauenfelder.mensazh.data.local.room.getDatabaseBuilder
import ch.florianfrauenfelder.mensazh.data.local.room.getRoomDatabase

fun createAppContainer(): AppContainer =
  AppContainer(createDataStore(), getRoomDatabase(getDatabaseBuilder()))
