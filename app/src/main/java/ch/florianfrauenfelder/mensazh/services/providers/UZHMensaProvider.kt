package ch.florianfrauenfelder.mensazh.services.providers

import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.models.Weekday
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.SerializationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
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
        id = UUID.fromString(uzhLocation.id),
        title = uzhLocation.title,
        mensas = uzhLocation.mensas.map {
          uzhMensas += it
          it.toMensa()
        },
      )
    }
  }

  override suspend fun getMenus(
    language: Language,
    nextWeek: Boolean,
    ignoreCache: Boolean,
  ): List<Mensa> {
    val mensas = mutableListOf<Mensa>()
    try {
      val json = loadFromApi(nextWeek, ignoreCache) ?: return emptyList()
      val menuPerMensa =
        parseApiRoot(SerializationService.deserialize<Api.Root>(json), uzhMensas, language)
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
    nextWeek: Boolean,
    ignoreCache: Boolean,
  ): String? {
    val date = Date().apply { if (nextWeek) time += 7 * 24 * 60 * 60 * 1000 }
    val cacheKey = cacheProviderPrefix + getDateTimeString(date)

    if (!ignoreCache) {
      cacheService.readString(cacheKey)?.let { return it }
    }

    val location = loadLocationFromApi(nextWeek)
    location?.let { cacheService.saveString(cacheKey, it) }
    return location
  }

  private suspend fun loadLocationFromApi(nextWeek: Boolean): String? =
    withContext(Dispatchers.IO) {
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
          val requestBody =
            "{\"query\":\"query Client {" +
              "organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) {" +
              "  outlets(take: 100) { slug calendar {" +
              "    week${if (nextWeek) "(week: \\\"next\\\")" else ""} {" +
              "      daily {" +
              "        date { weekdayNumber }" +
              "        menuItems {" +
              "          prices { amount }" +
              "          ... on OutletMenuItemDish {" +
              "            category { name path }" +
              "            dish {" +
              "              allergens { allergen { name } }" +
              "              name_i18n { label locale }" +
              "              media { media { url } }" +
              "              isVegetarian isVegan" +
              "            }" +
              "          }" +
              "        }" +
              "      }" +
              "    }" +
              "  } }" +
              "}" +
              "}\",\"operationName\":\"Client\"}"
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
    root: Api.Root,
    mensas: List<UzhMensa>,
    language: Language,
  ): Map<UzhMensa, List<Menu>> {
    val result = mutableMapOf<UzhMensa, List<Menu>>()

    mensas.forEach { mensa ->
      val menusByWeekday =
        root.data?.organisation?.outlets?.find { it.slug == mensa.slug }?.calendar?.week?.daily
          ?.associate { Weekday.entries[it.date.weekdayNumber - 1] to it.menuItems.orEmpty() }?.toMutableMap()
          ?: return@forEach
      if (mensa.categoryPath != null) {
        menusByWeekday.keys.forEach { weekday ->
          menusByWeekday[weekday] = menusByWeekday[weekday]?.filter { item ->
            item.category?.path?.any { it.contains(mensa.categoryPath) } == true
          }.orEmpty()
        }
      }

      val parsedMenus = mutableListOf<Menu>()
      menusByWeekday.forEach { weekday, items ->
        items.forEach { relevantMenu ->
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
            isVegetarian = relevantMenu.dish?.isVegetarian ?: false,
            isVegan = relevantMenu.dish?.isVegan ?: false,
            weekday = weekday,
          ).run {
            if (isNoMenuNotice(this, language)) return@forEach
            parsedMenus.add(this)
          }
        }
      }

      result[mensa] = parsedMenus
    }

    return result
  }

  private fun isNoMenuNotice(menu: Menu, language: Language): Boolean = when (language) {
    Language.English -> arrayOf(
      "kein Abendessen",
      "no dinner",
      "geschlossen",
      "is closed",
      "Wir sind ab Vollsemester",
    )

    Language.German -> arrayOf(
      "kein Abendessen",
      "geschlossen",
      "Wir sind ab Vollsemester",
      "Betriebsferien",
    )
  }.any { menu.description.contains(it) }

  @Serializable
  private data class UzhLocation(val id: String, val title: String, val mensas: List<UzhMensa>)

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

  private object Api {
    @Serializable
    data class Root(val data: Data? = null)

    @Serializable
    data class Data(val organisation: Organisation? = null)

    @Serializable
    data class Organisation(val outlets: List<Outlet>? = null)

    @Serializable
    data class Outlet(val slug: String? = null, val calendar: Calendar? = null)

    @Serializable
    data class Calendar(val week: Week? = null)

    @Serializable
    data class Week(val daily: List<Day>)

    @Serializable
    data class Day(val date: Api.Date, val menuItems: List<MenuItem>? = null)

    @Serializable
    data class Date(val weekdayNumber: Int)

    @Serializable
    data class MenuItem(
      val prices: List<Price>? = null,
      val category: Category? = null,
      val dish: Dish? = null,
    )

    @Serializable
    data class Price(val amount: String? = null)

    @Serializable
    data class Category(val name: String? = null, val path: List<String>? = null)

    @Serializable
    data class Dish(
      val name_i18n: List<I18nName>? = null,
      val allergens: List<AllergenContainer>? = null,
      val media: List<MediaContainer>? = null,
      val isVegetarian: Boolean? = null,
      val isVegan: Boolean? = null,
    )

    @Serializable
    data class I18nName(val label: String? = null, val locale: String? = null)

    @Serializable
    data class AllergenContainer(val allergen: Allergen? = null)

    @Serializable
    data class Allergen(val name: String? = null)

    @Serializable
    data class MediaContainer(val media: Media? = null)

    @Serializable
    data class Media(val url: String? = null)
  }
}
