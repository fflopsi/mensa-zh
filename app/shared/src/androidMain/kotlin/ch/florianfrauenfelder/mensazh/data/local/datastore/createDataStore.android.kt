package ch.florianfrauenfelder.mensazh.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer

fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
  storage = FileStorage(
    serializer = PreferencesFileSerializer,
    produceFile = { context.filesDir.resolve(dataStoreFileName) }
  )
)
