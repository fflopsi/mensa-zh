package ch.florianfrauenfelder.mensazh.data.providers

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.data.util.SerializationService
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.domain.value.toLanguage
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.Locale
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class UZHMensaProvider(
  menuDao: MenuDao,
  fetchInfoDao: FetchInfoDao,
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
  override suspend fun fetchMenus(
    language: Language,
    destination: Destination,
  ) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (destination == Destination.NextWeek) {
        plus(7, DateTimeUnit.DAY)
      } else this
    }

    updateFetchInfo(destination, language)
    val json = fetchJson(language, destination) ?: return
    val root = SerializationService.deserialize<Api.Root>(json)

    apiMensas.forEach { apiMensa ->
      apiMensa.toMensa().also { mensa ->
        val days =
          root.data?.organisation?.outlets?.find { it.slug == apiMensa.slug }?.calendar?.day?.let {
            listOf(it)
          }
            ?: root.data?.organisation?.outlets?.find { it.slug == apiMensa.slug }?.calendar?.week?.daily
            ?: return@forEach
        var menusByWeekday =
          days.associate { Weekday.entries[it.date.weekdayNumber - 1] to it.menuItems.orEmpty() }
        if (apiMensa.categoryPath != null) {
          menusByWeekday = menusByWeekday.mapValues { (_, menus) ->
            menus.filter { item ->
              item.category?.path?.any { it.contains(apiMensa.categoryPath) } == true
            }
          }
        }
        menusByWeekday.forEach { (weekday, items) ->
          items.forEachIndexed { index, relevantMenu ->
            relevantMenu.dish?.nameI18n
              ?.filter { name -> (name.locale ?: "") in Language.entries.map { it.toString() } }
              ?.forEach { i18nName ->
                Menu(
                  title = relevantMenu.category?.name ?: return@forEach,
                  description = i18nName.label?.replaceFirst(",", "\n")?.replace("\n ", "\n")
                    ?: return@forEach,
                  price = relevantMenu.prices?.mapNotNull { it.amount?.toFloat() }?.sorted()?.map {
                    String.format(Locale.US, "%.2f", it)
                  } ?: emptyList(),
                  allergens = relevantMenu.dish.allergens?.mapNotNull {
                    it.allergen?.name
                  }?.joinToString(separator = ", "),
                  isVegetarian = relevantMenu.dish.isVegetarian ?: false,
                  isVegan = relevantMenu.dish.isVegan ?: false,
                  imageUrl = relevantMenu.dish.media?.firstOrNull()?.media?.url,
                  weekday = weekday,
                ).run {
                  if (!isNoMenuNotice(this, language)) {
                    cacheMenu(
                      facilityId = mensa.id.toString(),
                      date = monday.plus(weekday.ordinal, DateTimeUnit.DAY),
                      language = i18nName.locale!!.toLanguage,
                      index = index,
                      menu = this,
                    )
                  }
                }
              }
          }
        }
      }
    }
  }

  @OptIn(ExperimentalTime::class)
  override suspend fun fetchJson(language: Language, destination: Destination): String? {
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
    val requestBody = when (destination) {
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

    val request = Request
      .Builder()
      .url("https://api.zfv.ch/graphql")
      .post(requestBody.toRequestBody("application/json".toMediaType()))
      .header("Accept", "*/*")
      .header("Content-Type", "application/json")
      .header(
        "api-key",
        "Y21nMGdleDdkN2xwbXM2MHRhemIyZWl1MjpBS0dLVkdPSnM5RjJEeDdrVUdySnZGaGZ4dWtpUUN2UHBaRjJrNUt5RENEQldObHRNNUZJUk84MU5JMkdCdmc3",
      )
      .build()

    return try {
      withContext(Dispatchers.IO) {
        client.newCall(request).execute().use {
          if (it.isSuccessful) it.body.string()
          else null
        }
      }
    } catch (_: Exception) {
      null
    }
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
  }
    .onEach { it.lowercase() }
    .any { menu.description.lowercase().contains(it) }
    || menu.description.isBlank()

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
