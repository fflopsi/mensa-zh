package ch.florianfrauenfelder.mensazh.services.providers

import android.annotation.SuppressLint
import android.util.Log
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.CacheService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

abstract class MensaProvider(private val cacheService: CacheService) {
  abstract fun getLocations(): List<Location>

  protected fun tryGetMenusFromCache(
    providerPrefix: String,
    mensaId: String,
    date: Date,
    language: Language,
  ): List<Menu>? = cacheService.readMenus(getCacheKey(providerPrefix, mensaId, date, language))


  private fun getMensaRequestCacheKey(url: URL): String =
    "${ETHMensaProvider.CACHE_PROVIDER_PREFIX}.$url"

  protected fun getCachedRequest(url: URL, ignoreCache: Boolean): String? {
    val cacheKey = getMensaRequestCacheKey(url)

    if (!ignoreCache) {
      cacheService.readString(cacheKey)?.let { return it }
    }

    return try {
      val json = url.readText()
      cacheService.saveString(cacheKey, json)
      json
    } catch (e: Exception) {
      Log.e("AbstractMensaProvider", "cached request failed: $url", e)
      null
    }
  }

  protected suspend fun getCachedRequest2(url: URL, ignoreCache: Boolean): String? {
    val cacheKey = getMensaRequestCacheKey(url)

    if (!ignoreCache) {
      cacheService.readString(cacheKey)?.let { return it }
    }

    return try {
      val json = fetchJsonFromUrl(url)
      cacheService.saveString(cacheKey, json)
      json
    } catch (e: Exception) {
      Log.e("AbstractMensaProvider", "cached request failed: $url", e)
      null
    }
  }

  private suspend fun fetchJsonFromUrl(url: URL): String = withContext(Dispatchers.IO) {
    val connection = (url.openConnection() as HttpURLConnection).apply {
      connectTimeout = 5000
      readTimeout = 5000
      requestMethod = "GET"
      setRequestProperty("Accept", "application/json")
    }
    try {
      if (connection.responseCode in 200..299) {
        connection.inputStream.bufferedReader().use { it.readText() }
      } else {
        throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")
      }
    } finally {
      connection.disconnect()
    }
  }

  protected fun cacheMenus(
    providerPrefix: String,
    facilityId: String,
    date: Date,
    language: Language,
    menus: List<Menu>,
  ) = cacheService.saveMenus(getCacheKey(providerPrefix, facilityId, date, language), menus)

  private fun getCacheKey(
    providerPrefix: String,
    facilityId: String,
    date: Date,
    language: Language,
  ): String = "$providerPrefix.$facilityId.${getDateTimeString(date)}.$language"

  @SuppressLint("SimpleDateFormat")
  protected fun getDateTimeString(date: Date): String = SimpleDateFormat("yyyy-MM-dd").format(date)

  @SuppressLint("SimpleDateFormat")
  protected fun getDateTimeStringOfMonday(date: Date): String =
    SimpleDateFormat("yyyy-MM-dd").format(
      Calendar.getInstance().apply {
        time = date
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      }.time,
    )

  protected fun normalizeText(text: String): String =
    // remove too much whitespace
    text.apply {
      replace("  ", " ")
      replace(" \n", "\n")
      replace("\n ", "\n")
    }

  protected fun fallbackLanguage(language: Language): Language = when (language) {
    Language.German -> Language.English
    Language.English -> Language.German
  }

  enum class Language {
    German, English;

    override fun toString() = when (this) {
      German -> "de"
      English -> "en"
    }
  }
}
