package ch.florianfrauenfelder.mensazh

import android.content.Context
import androidx.room.Room
import ch.florianfrauenfelder.mensazh.data.local.datastore.dataStore
import ch.florianfrauenfelder.mensazh.data.local.room.CacheDatabase
import ch.florianfrauenfelder.mensazh.data.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.data.providers.UZHMensaProvider
import ch.florianfrauenfelder.mensazh.data.repository.MensaRepository
import ch.florianfrauenfelder.mensazh.data.repository.PreferencesRepository
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
  private val database =
    Room.databaseBuilder(context, CacheDatabase::class.java, "cache-database").build()

  private val menuDao = database.menuDao()
  private val fetchInfoDao = database.fetchInfoDao()

  private val assetService = AssetService(context.assets)

  val mensaRepository = MensaRepository(
    menuDao = menuDao,
    fetchInfoDao = fetchInfoDao,
    providers = mapOf(
      Institution.ETH to ETHMensaProvider(menuDao, fetchInfoDao, assetService),
      Institution.UZH to UZHMensaProvider(menuDao, fetchInfoDao, assetService),
    ),
    appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
  )

  val preferencesRepository = PreferencesRepository(dataStore = context.dataStore)
}
