package ch.florianfrauenfelder.mensazh.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import ch.florianfrauenfelder.mensazh.data.local.getAppDirectory
import java.io.File

fun createDataStore(): DataStore<Preferences> = createDataStore(
  storage = FileStorage(
    serializer = PreferencesFileSerializer,
    produceFile = { File(getAppDirectory(), dataStoreFileName) }
  )
)
