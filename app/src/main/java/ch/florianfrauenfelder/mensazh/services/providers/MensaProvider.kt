package ch.florianfrauenfelder.mensazh.services.providers

import android.util.Log
import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.FetchInfo
import ch.florianfrauenfelder.mensazh.services.FetchInfoDao
import ch.florianfrauenfelder.mensazh.services.MenuDao
import ch.florianfrauenfelder.mensazh.services.RoomMenu
import ch.florianfrauenfelder.mensazh.services.SerializationService
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider.Language.English
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider.Language.German
import ch.florianfrauenfelder.mensazh.ui.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

abstract class MensaProvider<L : MensaProvider.ApiLocation<M>, M : MensaProvider.ApiMensa>(
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val assetService: AssetService,
) {
  abstract val institution: Institution
  protected abstract val locationsFile: String
  protected abstract val locationSerializer: KSerializer<L>
  protected val apiMensas = mutableListOf<M>()

  suspend fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile(locationsFile) ?: return emptyList()
    return SerializationService
      .deserializeLocationList(json, locationSerializer)
      .map { apiLocation ->
        Location(
          id = UUID.fromString(apiLocation.id),
          title = apiLocation.title,
          mensas = apiLocation.mensas.map {
            apiMensas += it
            it.toMensa()
          },
        )
      }
  }

  abstract suspend fun getMenus(
    language: Language,
    destination: Destination,
    ignoreCache: Boolean,
  ): List<Mensa>

  @OptIn(ExperimentalTime::class)
  protected suspend fun tryGetMenusFromCache(
    mensaId: String,
    destination: Destination,
    language: Language,
  ): List<Menu> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

    return when (destination) {
      Destination.Today -> menuDao.getMenus(
        mensaId = mensaId,
        language = language.toString(),
        date = today.toString(),
      )
      Destination.Tomorrow -> menuDao.getMenus(
        mensaId = mensaId,
        language = language.toString(),
        date = today.plus(1, DateTimeUnit.DAY).toString(),
      )
      Destination.ThisWeek -> menuDao.getMenus(
        mensaId = mensaId,
        language = language.toString(),
        startDate = monday.toString(),
        endDate = monday.plus(6, DateTimeUnit.DAY).toString(),
      )
      Destination.NextWeek -> menuDao.getMenus(
        mensaId = mensaId,
        language = language.toString(),
        startDate = monday.plus(7, DateTimeUnit.DAY).toString(),
        endDate = monday.plus(7 + 6, DateTimeUnit.DAY).toString(),
      )
    }.sortedBy { it.index }.map { it.toMenu() }
  }

  protected suspend fun getCachedRequest(url: URL): String? {
    return try {
      fetchJsonFromUrl(url)
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

  protected suspend fun cacheMenu(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
    menu: Menu,
  ) = menuDao.insertMenu(menu.toRoomMenu(facilityId, date, language, index))

  protected suspend fun cacheMenus(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
    menus: List<Menu>,
  ) = menuDao.insertMenus(menus.map { it.toRoomMenu(facilityId, date, language, index) })

  protected suspend fun updateFetchInfo(
    destination: Destination,
    language: Language,
  ) {
    if (institution == Institution.ETH && destination != Destination.NextWeek) {
      listOf(Destination.Today, Destination.Tomorrow, Destination.ThisWeek).forEach {
        fetchInfoDao.insertFetchInfo(
          FetchInfo(
            institution = institution.toString(),
            destination = it.toString(),
            language = language.toString()
          )
        )
      }
    } else {
      fetchInfoDao.insertFetchInfo(
        FetchInfo(
          institution = institution.toString(),
          destination = destination.toString(),
          language = language.toString()
        )
      )
    }
  }

  protected abstract fun isNoMenuNotice(menu: Menu, language: Language): Boolean

  protected fun normalizeText(text: String): String =
    // remove too much whitespace
    text.apply {
      replace("  ", " ")
      replace(" \n", "\n")
      replace("\n ", "\n")
    }

  protected fun fallbackLanguage(language: Language) = !language

  private fun Menu.toRoomMenu(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
  ) = RoomMenu(
    mensaId = facilityId,
    index = index,
    language = language.toString(),
    title = title,
    description = description,
    price = SerializationService.serialize(price),
    allergens = allergens,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    imageUrl = imageUrl,
    date = date.toString(),
  )

  enum class Institution {
    ETH, UZH;

    override fun toString() = when (this) {
      ETH -> "ETH"
      UZH -> "UZH"
    }
  }

  @Serializable
  sealed class ApiLocation<M : ApiMensa> {
    abstract val id: String
    abstract val title: String
    abstract val mensas: List<M>
  }

  @Serializable
  sealed class ApiMensa {
    abstract val id: String
    abstract val title: String
    abstract val mealTime: String
    abstract fun toMensa(): Mensa
  }

  enum class Language {
    German, English;

    override fun toString() = when (this) {
      German -> "de"
      English -> "en"
    }

    operator fun not(): Language {
      if (this == German) return English
      return German
    }

    val showMenusInGerman get() = this == German

    companion object {
      val default get() = English
    }
  }
}

val Boolean.showMenusInGermanToLanguage get() = if (this) German else English
