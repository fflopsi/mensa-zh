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
) : MensaProvider(cacheService) {
  override val cacheProviderPrefix = "uzh_zfv"

  private val uzhMensas = mutableListOf<UzhMensa>()

  override suspend fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile("uzh/locations_zfv.json") ?: return emptyList()
    return SerializationService.deserializeList<UzhLocation>(json).map { uzhLocation ->
      Location(
        title = uzhLocation.title,
        mensas = uzhLocation.mensas.map {
          uzhMensas += it
          it.toMensa()
        },
      )
    }
  }

  suspend fun getMenus(date: Date, language: Language, ignoreCache: Boolean): List<Mensa> {
    val mensas = mutableListOf<Mensa>()
    try {
      val json = loadFromApi(ignoreCache, date) ?: return emptyList()
      val menuPerMensa = parseApiRoot(SerializationService.deserialize<ApiRoot>(json), uzhMensas, language)
      uzhMensas.forEach { uzhMensa ->
        mensas += uzhMensa.toMensa().apply {
          menus = menuPerMensa[uzhMensa].orEmpty()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return mensas
  }

  private suspend fun loadFromApi(
    ignoreCache: Boolean,
    date: Date,
  ): String? {
    val cacheKey = cacheProviderPrefix + getDateTimeString(date)

    if (!ignoreCache) {
      cacheService.readString(cacheKey)?.let { return it }
    }

    val location = loadLocationFromApi(date)
    location?.let { cacheService.saveString(cacheKey, it) }
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
        setRequestProperty("api-key", "Y203MHVwaXU0OWFyeXM2MHRscWUyZncwcTpTQTJRRy83eXE5NmEzczNyRS91TjhBaysrYWl4aCs5SGhRUE9xOTk3ZzdDa1ZpdFVvQkJhK3hHN0Yyd1lLaTNu")
        val isoDateString = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date)
        val requestBody = "{\"query\":\"query Client { organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) { outlets(take: 100) { slug calendar { day(date: \\\"$isoDateString\\\") { menuItems { prices { amount } ... on OutletMenuItemDish { category { name path } dish { allergens { allergen { name } } name_i18n { label locale } } } } } } } }}\",\"operationName\":\"Client\"}"
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
      var menuItems = root.data?.organisation?.outlets?.find { it.slug == mensa.slug }?.calendar?.day?.menuItems
        ?: return@forEach

      // filter by category if it exists and does not result in no entry
      if (mensa.categoryPath != null) {
        menuItems = menuItems.filter { item ->
          item.category?.path?.any { it.contains(mensa.categoryPath) } == true
        }.toTypedArray()
      }

      val parsedMenus = mutableListOf<Menu>()
      menuItems.forEach { relevantMenu ->
        val deDescription = relevantMenu.dish?.name_i18n?.find { it.locale == "de" }?.label
        val descriptionRaw = if (language == Language.German && deDescription != null) {
          deDescription
        } else {
          relevantMenu.dish?.name_i18n?.find { it.locale == "en" }?.label
        }

        Menu(
          title = relevantMenu.category?.name ?: return@forEach,
          description = descriptionRaw?.replaceFirst(",", "\n")?.replace("\n ", "\n")
            ?: return@forEach,
          price = relevantMenu.prices?.mapNotNull { it.amount?.toFloat() }?.sorted()?.map {
            String.format(Locale.US, "%.2f", it)
          } ?: emptyList(),
          allergens = relevantMenu.dish?.allergens?.mapNotNull {
            it.allergen?.name
          }?.joinToString(separator = ", "),
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

  @Serializable
  private data class UzhLocation(val title: String, val mensas: List<UzhMensa>)

  @Serializable
  private data class UzhMensa(
    val id: String,
    val title: String,
    val mealTime: String,
    val infoUrlSlug: String,
    val slug: String,
    val categoryPath: String? = null,
  ) {
    fun toMensa() = Mensa(
      id = UUID.fromString(id),
      title = title,
      mealTime = mealTime,
      url = URI("https://www.mensa.uzh.ch/de/menueplaene/$infoUrlSlug.html"),
    )
  }

  @Serializable
  private class ApiRoot {
    var data: ApiData? = null
  }

  @Serializable
  private class ApiData {
    var organisation: ApiOrganisation? = null
  }

  @Serializable
  private class ApiOrganisation {
    var outlets: Array<Outlet>? = null
  }

  @Serializable
  private class Outlet {
    var slug: String? = null
    var calendar: Calendar? = null
  }

  @Serializable
  private class Calendar {
    var day: Day? = null
  }

  @Serializable
  private class Day {
    var menuItems: Array<MenuItem>? = null
  }

  @Serializable
  private class MenuItem {
    var prices: Array<Price>? = null
    var category: Category? = null
    var dish: Dish? = null
  }

  @Serializable
  private class Category {
    var name: String? = null
    var path: Array<String>? = null
  }

  @Serializable
  private class Dish {
    var name_i18n: Array<i18nValue>? = null
    var allergens: Array<AllergenContainer>? = null
  }

  @Serializable
  private class AllergenContainer {
    var allergen: Allergen? = null
  }

  @Serializable
  private class Allergen {
    var name: String? = null
  }

  @Serializable
  private class Price {
    var amount: String? = null
  }

  @Serializable
  private class i18nValue {
    var label: String? = null
    var locale: String? = null
  }
}
