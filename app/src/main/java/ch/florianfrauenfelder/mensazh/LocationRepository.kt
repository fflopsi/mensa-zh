package ch.florianfrauenfelder.mensazh

import android.content.Context
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.providers.AbstractMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.ETHMensaProvider
import ch.florianfrauenfelder.mensazh.services.providers.UZHMensaProvider
import java.util.Date

class LocationRepository(
  private val cacheService: CacheService,
  assetService: AssetService,
) {
  companion object {
    private var defaultInstance: LocationRepository? = null

    fun getInstance(context: Context): LocationRepository {
      synchronized(this) {
        if (defaultInstance == null) {
          defaultInstance = LocationRepository(CacheService(context), AssetService(context.assets))
        }
        return defaultInstance!!
      }
    }
  }

  private val ethMensaProvider = ETHMensaProvider(cacheService, assetService)
  private val uzhMensaProvider = UZHMensaProvider(cacheService, assetService)

  suspend fun loadLocations(): List<Location> {
    val eth = ethMensaProvider.getLocations()
    val uzh = uzhMensaProvider.getLocations()
    return eth + uzh
  }

  suspend fun refresh(
    date: Date,
    language: AbstractMensaProvider.Language,
    ignoreCache: Boolean = false,
  ): List<Mensa> {
    cacheService.startObserveCacheUsage()
    val ethMensas = ethMensaProvider.getMenus(date, language, ignoreCache)
    val uzhMensas = uzhMensaProvider.getMenus(language, date, ignoreCache)
    cacheService.removeAllUntouchedCacheEntries()
    return ethMensas + uzhMensas
  }
}
