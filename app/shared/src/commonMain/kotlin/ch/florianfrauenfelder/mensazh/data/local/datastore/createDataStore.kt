package ch.florianfrauenfelder.mensazh.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
  DataStoreFactory.create(storage = storage)

internal const val dataStoreFileName = "settings"
private const val dataStoreFileSuffix = ".preferences_pb"
internal const val dataStoreFile = dataStoreFileName + dataStoreFileSuffix
