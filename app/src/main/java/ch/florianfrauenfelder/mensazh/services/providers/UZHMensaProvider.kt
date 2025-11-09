package ch.florianfrauenfelder.mensazh.services.providers

import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.FetchInfoDao
import ch.florianfrauenfelder.mensazh.services.MenuDao
import ch.florianfrauenfelder.mensazh.services.SerializationService
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class UZHMensaProvider(
  menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  assetService: AssetService,
) : MensaProvider<UZHMensaProvider.UzhLocation, UZHMensaProvider.UzhMensa>(
  menuDao = menuDao,
  fetchInfoDao = fetchInfoDao,
  assetService = assetService,
) {
  override val institution = Institution.UZH
  override val locationsFile = "uzh/locations_zfv.json"
  override val locationSerializer = UzhLocation.serializer()

  @OptIn(ExperimentalTime::class)
  override suspend fun getMenus(
    language: Language,
    destination: Destination,
    ignoreCache: Boolean,
  ): List<Mensa> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (destination == Destination.NextWeek) {
        plus(7, DateTimeUnit.DAY)
      } else this
    }

    val mensas = mutableListOf<Mensa>()
    if (!ignoreCache && System.currentTimeMillis() - (fetchInfoDao.getFetchInfo(
        institution = institution.toString(),
        destination = destination.toString(),
        language = language.toString(),
      )?.fetchDate ?: 0) < 12.hours.inWholeMilliseconds
    ) {
      apiMensas.forEach {
        mensas += it.toMensa().apply {
          menus = tryGetMenusFromCache(it.id, destination, language).orEmpty()
        }
      }
    } else {
      val json = loadLocationFromApi(destination) ?: return emptyList()
      val menuPerMensa =
        parseApiRoot(SerializationService.deserialize<Api.Root>(json), apiMensas, language)
      apiMensas.forEach {
        mensas += it.toMensa().apply {
          menus = menuPerMensa[it].orEmpty()
        }
      }
      mensas.forEach { mensa ->
        mensa.menus.forEachIndexed { index, it ->
          cacheMenu(
            facilityId = mensa.id.toString(),
            date = monday.plus(it.weekday.ordinal, DateTimeUnit.DAY),
            language = language,
            index = index,
            menu = it,
          )
        }
      }
      updateFetchInfo(destination, language)
    }

    return mensas
  }

  @OptIn(ExperimentalTime::class)
  private suspend fun loadLocationFromApi(destination: Destination): String? =
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
            "Y21nMGdleDdkN2xwbXM2MHRhemIyZWl1MjpBS0dLVkdPSnM5RjJEeDdrVUdySnZGaGZ4dWtpUUN2UHBaRjJrNUt5RENEQldObHRNNUZJUk84MU5JMkdCdmc3"
          )
          val isoDateString =
            Clock.System
              .now()
              .run { if (destination == Destination.Tomorrow) plus(1.days) else this }
              .toString()
          val dayQuery =
            "date { weekdayNumber }" +
              "menuItems {" +
              "  prices { amount }" +
              "  ... on OutletMenuItemDish {" +
              "    category { name path }" +
              "    dish {" +
              "      allergens { allergen { name } }" +
              "      name_i18n { label locale }" +
              "      media { media { url } }" +
              "      isVegetarian isVegan" +
              "    }" +
              "  }" +
              "}"
          val request = when (destination) {
            Destination.Today, Destination.Tomorrow -> {
              "{\"query\":\"query Client {" +
                "organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) {" +
                "  outlets(take: 100) { slug calendar {" +
                "    day(date: \\\"$isoDateString\\\") { $dayQuery }" +
                "  }" +
                "} }}\",\"operationName\":\"Client\"}"
            }
            Destination.ThisWeek, Destination.NextWeek -> {
              "{\"query\":\"query Client {" +
                "organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) {" +
                "  outlets(take: 100) { slug calendar {" +
                "    week${if (destination == Destination.NextWeek) "(week: \\\"next\\\")" else ""} {" +
                "      daily { $dayQuery }" +
                "    }" +
                "  } }" +
                "}" +
                "}\",\"operationName\":\"Client\"}"
            }
          }
          outputStream.run {
            write(request.toByteArray())
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
      val days =
        root.data?.organisation?.outlets?.find { it.slug == mensa.slug }?.calendar?.day?.let {
          listOf(it)
        } ?: root.data?.organisation?.outlets?.find { it.slug == mensa.slug }?.calendar?.week?.daily
        ?: return@forEach
      var menusByWeekday =
        days.associate { Weekday.entries[it.date.weekdayNumber - 1] to it.menuItems.orEmpty() }
      if (mensa.categoryPath != null) {
        menusByWeekday = menusByWeekday.mapValues { (_, menus) ->
          menus.filter { item ->
            item.category?.path?.any { it.contains(mensa.categoryPath) } == true
          }
        }
      }

      val parsedMenus = mutableListOf<Menu>()
      menusByWeekday.forEach { (weekday, items) ->
        items.forEach { relevantMenu ->
          val deDescription =
            relevantMenu.dish?.nameI18n?.find { it.locale == Language.German.toString() }?.label
          val enDescription =
            relevantMenu.dish?.nameI18n?.find { it.locale == Language.English.toString() }?.label
          val descriptionRaw = when (language) {
            Language.English -> enDescription ?: deDescription
            Language.German -> deDescription ?: enDescription
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
            imageUrl = relevantMenu.dish?.media?.firstOrNull()?.media?.url,
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

  override fun isNoMenuNotice(menu: Menu, language: Language): Boolean = when (language) {
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
  data class UzhLocation(
    override val id: String,
    override val title: String,
    override val mensas: List<UzhMensa>,
  ) : ApiLocation<UzhMensa>()

  @Serializable
  data class UzhMensa(
    override val id: String,
    override val title: String,
    override val mealTime: String,
    val infoUrlSlug: String,
    val slug: String,
    val categoryPath: String? = null,
  ) : ApiMensa() {
    override fun toMensa() = Mensa(
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
    data class Calendar(val day: Day? = null, val week: Week? = null)

    @Serializable
    data class Week(val daily: List<Day>)

    @Serializable
    data class Day(val date: Date, val menuItems: List<MenuItem>? = null)

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
      @SerialName("name_i18n") val nameI18n: List<I18nName>? = null,
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
