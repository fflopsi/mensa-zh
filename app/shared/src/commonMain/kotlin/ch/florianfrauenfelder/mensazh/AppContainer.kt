package ch.florianfrauenfelder.mensazh

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import ch.florianfrauenfelder.mensazh.data.local.room.CacheDatabase
import ch.florianfrauenfelder.mensazh.data.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.data.providers.UZHMensaProvider
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import ch.florianfrauenfelder.mensazh.data.repository.PreferencesRepository
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(dataStore: DataStore<Preferences>, database: CacheDatabase) {
  private val menuDao = database.menuDao()
  private val fetchInfoDao = database.fetchInfoDao()

  val mensaRepository = MensaRepository(
    menuDao = menuDao,
    fetchInfoDao = fetchInfoDao,
    providers = mapOf(
      Institution.ETH to ETHMensaProvider(menuDao, fetchInfoDao),
      Institution.UZH to UZHMensaProvider(menuDao, fetchInfoDao),
    ),
    appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
  )

  val preferencesRepository = PreferencesRepository(dataStore)
}
