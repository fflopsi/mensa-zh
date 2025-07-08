package ch.florianfrauenfelder.mensazh.services

import android.content.Context
import ch.florianfrauenfelder.mensazh.models.Menu
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CacheService(private val context: Context) {
  private val touchedCacheKeys = hashSetOf<String>()

  fun startObserveCacheUsage() = touchedCacheKeys.clear()

  fun saveMenus(key: String, menus: List<Menu>) =
    saveToCache(getCacheKey(key, CacheType.Menu), menus)

  fun readMenus(key: String): List<Menu>? = readFromCache(getCacheKey(key, CacheType.Menu))

  fun saveMensaIds(key: String, mensaIds: List<String>) =
    saveToCache(getCacheKey(key, CacheType.MensaIds), mensaIds)

  fun readMensaIds(key: String): List<String>? = readFromCache(getCacheKey(key, CacheType.MensaIds))

  fun saveString(key: String, value: String) = saveRaw(getCacheKey(key, CacheType.String), value)

  fun readString(key: String): String? = readRaw(getCacheKey(key, CacheType.String))

  fun removeAllUntouchedCacheEntries() {
    for (file in context.cacheDir.listFiles() ?: return) {
      if (file.name.startsWith(CACHE_PREFIX) && file.name !in touchedCacheKeys) {
        file.delete()
      }
    }
  }

  private inline fun <reified T : Any> saveToCache(key: String, value: T) =
    getFile(key).writeText(SerializationService.serialize(value))

  private inline fun <reified T : Any> readFromCache(key: String): T? {
    val file = getFile(key)
    return if (file.exists()) {
      touchedCacheKeys.add(file.name)
      try {
        SerializationService.deserialize(file.readText())
      } catch (_: Exception) {
        null
      }
    } else null
  }

  private fun saveRaw(key: String, value: String) = getFile(key).writeText(value)

  private fun readRaw(key: String): String? {
    val file = getFile(key)
    return if (file.exists()) {
      touchedCacheKeys.add(file.name)
      file.readText()
    } else null
  }

  private fun getCacheKey(key: String, cacheType: CacheType): String {
    val cacheKey = "$CACHE_PREFIX.$cacheType.$key"
    touchedCacheKeys.add(cacheKey)
    return cacheKey
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun getFile(key: String): File =
    File(context.cacheDir, Base64.UrlSafe.encode(key.toByteArray()))

  private enum class CacheType { Menu, MensaIds, String }

  companion object {
    const val CACHE_PREFIX = "cache"
  }
}
