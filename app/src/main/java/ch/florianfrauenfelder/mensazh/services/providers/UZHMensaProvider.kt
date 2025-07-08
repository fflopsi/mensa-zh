package ch.florianfrauenfelder.mensazh.services.providers

import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.SerializationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UZHMensaProvider(
  private val cacheService: CacheService,
  private val assetService: AssetService,
) : AbstractMensaProvider(cacheService) {
  private val mensaMap: MutableMap<Mensa, UzhMensa> = HashMap()

  suspend fun getMenus(language: Language, date: Date, ignoreCache: Boolean): List<Mensa> = try {
    val json = loadFromApi(ignoreCache, date) ?: return emptyList()
    val menuPerMensa = parseApiRoot(
      SerializationService.deserialize<ApiRoot>(json), mensaMap.values.toList(), language
    )

    val updateMensas = mutableListOf<Mensa>()
    menuPerMensa.entries.forEach { uzhMensa ->
      mensaMap.entries.find { it.value == uzhMensa.key }?.key?.run {
        menus = uzhMensa.value
        updateMensas.add(this)
      } ?: return@forEach
    }

    updateMensas
  } catch (_: Exception) {
    emptyList()
  }


  override fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile("uzh/locations_zfv.json") ?: return emptyList()
    return SerializationService.deserializeList<UzhLocation>(json).map { uzhLocation ->
      Location(
        title = uzhLocation.title,
        mensas = uzhLocation.mensas.map {
          Mensa(
            id = UUID.fromString(it.id),
            title = it.title,
            mealTime = it.mealTime,
            url = URI("https://www.mensa.uzh.ch/de/menueplaene/${it.infoUrlSlug}.html"),
          ).apply { mensaMap[this] = it }
        },
      )
    }
  }

  private suspend fun loadFromApi(
    ignoreCache: Boolean,
    date: Date,
  ): String? {
    val cacheKey = CACHE_PROVIDER_PREFIX + getDateTimeString(date)

    if (!ignoreCache) {
      cacheService.readString(cacheKey)?.let { return it }
    }

    val location = loadLocationFromApi(date)
    location?.let { cacheService.saveString(cacheKey, it) }
//    Log.d("Location", location.toString())
    return location
  }

  private suspend fun loadLocationFromApi(date: Date): String? = withContext(Dispatchers.IO) {
    (URL("https://api.zfv.ch/graphql").openConnection() as HttpURLConnection).run {
      requestMethod = "POST"
      try {
        // Set up the connection
        doOutput = true
        setRequestProperty("Accept", "*/*")
        setRequestProperty("Content-Type", "application/json")
        // API key included directly here to enable builds in fdroid
        // in any case, the API keys would have also been found in the APK
        setRequestProperty(
          "api-key",
          "Y203MHVwaXU0OWFyeXM2MHRscWUyZncwcTpTQTJRRy83eXE5NmEzczNyRS91TjhBaysrYWl4aCs5SGhRUE9xOTk3ZzdDa1ZpdFVvQkJhK3hHN0Yyd1lLaTNu"
        )
        val isoDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
        val requestBody =
          "{\"query\":\"query Client { organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) { outlets(take: 100) { slug calendar { day(date: \\\"$isoDateString\\\") { menuItems { prices { amount } ... on OutletMenuItemDish { category { name path } dish { allergens { allergen { name } } name_i18n { label locale } } } } } } } }}\",\"operationName\":\"Client\"}"
        outputStream.run {
          write(requestBody.toByteArray())
          flush()
          close()
        }
        return@withContext inputStream.bufferedReader().use { it.readText() }
      } catch (_: Exception) {
        // do not care, likely because of network errors. in any case, cannot recover
      } finally {
        disconnect()
      }
    }
    null
  }

  private fun parseApiRoot(
    root: ApiRoot,
    mensas: List<UzhMensa>,
    language: Language,
  ): Map<UzhMensa, List<Menu>> {
    val result = mutableMapOf<UzhMensa, List<Menu>>()

    mensas.forEach { mensa ->
      var menuItems =
        root.data?.organisation?.outlets?.find { it.slug == mensa.slug }?.calendar?.day?.menuItems
          ?: return@forEach

      // filter by category if it exists and does not result in no entry
      if (mensa.categoryPath != null) {
        menuItems =
          menuItems.filter { item -> item.category?.path?.any { it.contains(mensa.categoryPath) } == true }
            .toTypedArray()
      }

      val parsedMenus = mutableListOf<Menu>()
      menuItems.forEach { relevantMenu ->
        val deDescription = relevantMenu.dish?.name_i18n?.find { it.locale == "de" }?.label
        val enDescription = relevantMenu.dish?.name_i18n?.find { it.locale == "en" }?.label
        val descriptionRaw =
          if (language == Language.German && deDescription != null) deDescription else enDescription
        Menu(
          title = relevantMenu.category?.name ?: return@forEach,
          description = descriptionRaw?.replaceFirst(",", "\n")?.replace("\n ", "\n")
            ?: return@forEach,
          price = relevantMenu.prices?.mapNotNull { it.amount }?.map { it.toFloat() }?.sorted()
            ?.map { String.format(Locale.US, "%.2f", it) } ?: emptyList(),
          allergens = relevantMenu.dish?.allergens?.mapNotNull { it.allergen?.name }
            ?.joinToString(separator = ", "),
        ).run {
          if (isNoMenuNotice(this, language)) return@forEach
          parsedMenus.add(this)
        }
      }

      result[mensa] = parsedMenus
    }

    return result
  }

  private fun isNoMenuNotice(menu: Menu, language: Language): Boolean = when (language) {
    Language.English -> arrayOf("no dinner", "is closed", "geschlossen", "Wir sind ab Vollsemester")
    Language.German -> arrayOf("kein Abendessen", "geschlossen", "Wir sind ab Vollsemester")
  }.any { menu.description.contains(it) }

  companion object {
    const val CACHE_PROVIDER_PREFIX = "uzh_zfv"
  }

  @Serializable
  protected class ApiRoot {
    var data: ApiData? = null
  }

  @Serializable
  protected class ApiData {
    var organisation: ApiOrganisation? = null
  }

  @Serializable
  protected class ApiOrganisation {
    var outlets: Array<Outlet>? = null
  }

  @Serializable
  protected class Outlet {
    var slug: String? = null
    var calendar: Calendar? = null
  }

  @Serializable
  protected class Calendar {
    var day: Day? = null
  }

  @Serializable
  protected class Day {
    var menuItems: Array<MenuItem>? = null
  }

  @Serializable
  protected class MenuItem {
    var prices: Array<Price>? = null
    var category: Category? = null
    var dish: Dish? = null
  }

  @Serializable
  protected class Category {
    var name: String? = null
    var path: Array<String>? = null
  }

  @Serializable
  protected class Dish {
    var name_i18n: Array<i18nValue>? = null
    var allergens: Array<AllergenContainer>? = null
  }

  @Serializable
  protected class AllergenContainer {
    var allergen: Allergen? = null
  }

  @Serializable
  protected class Allergen {
    var name: String? = null
  }

  @Serializable
  protected class Price {
    var amount: String? = null
  }

  @Serializable
  protected class i18nValue {
    var label: String? = null
    var locale: String? = null
  }

  @Serializable
  data class UzhLocation(val title: String, val mensas: List<UzhMensa>)

  @Serializable
  open class UzhMensa(
    val id: String,
    val title: String,
    val mealTime: String,
    val infoUrlSlug: String,
    val slug: String,
    val categoryPath: String? = null,
  )
}
