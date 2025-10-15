package ch.florianfrauenfelder.mensazh.services.providers

import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.FetchInfoDao
import ch.florianfrauenfelder.mensazh.services.MenuDao
import ch.florianfrauenfelder.mensazh.services.SerializationService
import ch.florianfrauenfelder.mensazh.ui.Destination
import ch.florianfrauenfelder.mensazh.ui.Weekday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URL
import java.util.Locale
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class ETHMensaProvider(
  menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  assetService: AssetService,
) : MensaProvider<ETHMensaProvider.EthLocation, ETHMensaProvider.EthMensa>(
  menuDao = menuDao,
  fetchInfoDao = fetchInfoDao,
  assetService = assetService,
) {
  override val institution = Institution.ETH
  override val locationsFile = "eth/locations.json"
  override val locationSerializer = EthLocation.serializer()

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
      try {
        val menuPerMensa = getMensaMenusFromCookpit(
          language = language,
          nextWeek = destination == Destination.NextWeek,
        )
        apiMensas.forEach {
          mensas += it.toMensa().apply {
            menus = menuPerMensa[it.getMapId()].orEmpty()
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
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return mensas
  }

  @OptIn(ExperimentalTime::class)
  private suspend fun getMensaMenusFromCookpit(
    language: Language,
    nextWeek: Boolean,
  ): Map<String, List<Menu>> {
    // Observation: dateslug is ignored by API; all future entries are returned in any case
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (nextWeek) {
        plus(7, DateTimeUnit.DAY)
      } else this
    }
    val url =
      URL("https://idapps.ethz.ch/cookpit-pub-services/v1/weeklyrotas?client-id=ethz-wcms&lang=$language&rs-first=0&rs-size=150&valid-after=$monday")
    val json = getCachedRequest(url) ?: throw Exception("Cannot load web content")
    val data: Api.Root = SerializationService.deserialize(json)

    val menuByFacilityIds = hashMapOf<String, List<Menu>>()
    data.weeklyRotaArray
      .filter { today in LocalDate.parse(it.validFrom)..LocalDate.parse(it.validTo) }
      .forEach { weeklyRotaArray ->
        weeklyRotaArray.dayOfWeekArray.forEach { dayOfWeekArray ->
          val weekday = Weekday.entries[dayOfWeekArray.dayOfWeekCode - 1]
          dayOfWeekArray.openingHourArray?.forEach { openingHour ->
            openingHour.mealTimeArray?.forEach { mealTime ->
              if (mealTime.lineArray == null) return@forEach
              val time = parseMealTime(mealTime.name) ?: return@forEach
              menuByFacilityIds["${weeklyRotaArray.facilityId}_$time"] =
                menuByFacilityIds["${weeklyRotaArray.facilityId}_$time"].orEmpty() + mealTime.lineArray
                  .mapNotNull { parseApiLineArray(it.name, it.meal, weekday) }
                  .filter { !isNoMenuNotice(it, language) }
            }
          }
        }
      }

    return menuByFacilityIds
  }

  private fun parseMealTime(mealTime: String): String? = when {
    listOf("mittag", "lunch", "pranzo").any { mealTime.lowercase().contains(it) } -> "lunch"
    listOf("abend", "dinner", "cena").any { mealTime.lowercase().contains(it) } -> "dinner"
    else -> null
  }

  override fun isNoMenuNotice(menu: Menu, language: Language): Boolean = when (language) {
    Language.English -> listOf(
      "We look forward to serving you this menu again soon!",
      "is closed",
      "Closed",
      "novalue",
    )

    Language.German -> listOf(
      "Dieses Menu servieren wir Ihnen gerne bald wieder!",
      "geschlossen",
      "Geschlossen",
    )
  }.any { menu.description.contains(it) || menu.title == it }

  private fun parseApiLineArray(name: String, meal: Api.Meal?, weekday: Weekday): Menu? =
    if (meal == null) null
    else Menu(
      title = name,
      description = "${meal.name.trim()}\n${meal.description.replace("\\s+".toRegex(), " ")}",
      price = meal.mealPriceArray.orEmpty().map { String.format(Locale.US, "%.2f", it.price) },
      allergens = meal.allergenArray?.joinToString(separator = ", ") { it.desc },
      isVegetarian = meal.mealClassArray?.any { it.desc.contains("vegetari", true) } ?: false,
      isVegan = meal.mealClassArray?.any { it.desc.contains("vegan", true) } ?: false,
      imageUrl = meal.imageUrl?.let { "$it?client-id=ethz-wcms" },
      weekday = weekday,
    )

  @Serializable
  data class EthLocation(
    override val id: String,
    override val title: String,
    override val mensas: List<EthMensa>,
  ) : ApiLocation<EthMensa>()

  @Serializable
  data class EthMensa(
    override val id: String,
    override val title: String,
    override val mealTime: String,
    val facilityId: Int,
    val timeSlug: String,
    val infoUrlSlug: String,
  ) : ApiMensa() {
    fun getMapId(): String = facilityId.toString() + "_" + timeSlug

    override fun toMensa() = Mensa(
      id = UUID.fromString(id),
      title = title,
      mealTime = mealTime,
      url = URI("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/$infoUrlSlug"),
      imagePath = "eth/images/${infoUrlSlug.substring(infoUrlSlug.indexOf("/") + 1)}.jpg",
    )
  }

  private object Api {
    @Serializable
    data class Root(@SerialName("weekly-rota-array") val weeklyRotaArray: List<WeeklyRota>)

    @Serializable
    data class WeeklyRota(
      @SerialName("weekly-rota-id") val weeklyRotaId: Int,
      @SerialName("facility-id") val facilityId: Int,
      @SerialName("valid-from") val validFrom: String,
      @SerialName("day-of-week-array") val dayOfWeekArray: List<DayOfWeek>,
      @SerialName("valid-to") val validTo: String,
    )

    @Serializable
    data class DayOfWeek(
      @SerialName("day-of-week-code") val dayOfWeekCode: Int,
      @SerialName("day-of-week-desc") val dayOfWeekDesc: String,
      @SerialName("day-of-week-desc-short") val dayOfWeekDescShort: String,
      @SerialName("opening-hour-array") val openingHourArray: List<OpeningHour>? = null,
    )

    @Serializable
    data class OpeningHour(
      @SerialName("time-from") val timeFrom: String,
      @SerialName("time-to") val timeTo: String,
      @SerialName("meal-time-array") val mealTimeArray: List<MealTime>? = null,
    )

    @Serializable
    data class MealTime(
      val name: String,
      @SerialName("time-from") val timeFrom: String,
      @SerialName("time-to") val timeTo: String,
      val menu: ApiMenu? = null,
      @SerialName("line-array") val lineArray: List<Line>? = null,
    )

    @Serializable
    data class ApiMenu(@SerialName("menu-url") val menuUrl: String)

    @Serializable
    data class Line(val name: String, val meal: Meal? = null)

    @Serializable
    data class Meal(
      @SerialName("line-id") val lineId: Int,
      val name: String,
      val description: String,
      @SerialName("price-unit-code") val priceUnitCode: Int,
      @SerialName("price-unit-desc") val priceUnitDesc: String,
      @SerialName("price-unit-desc-short") val priceUnitDescShort: String,
      @SerialName("meal-price-array") val mealPriceArray: List<MealPrice>? = null,
      @SerialName("meal-class-array") val mealClassArray: List<MealClass>? = null,
      @SerialName("allergen-array") val allergenArray: List<Allergen>? = null,
      @SerialName("image-url") val imageUrl: String? = null,
    )

    @Serializable
    data class MealPrice(
      val price: Double,
      @SerialName("customer-group-code") val customerGroupCode: Int,
      @SerialName("customer-group-position") val customerGroupPosition: Int,
      @SerialName("customer-group-desc") val customerGroupDesc: String,
      @SerialName("customer-group-desc-short") val customerGroupDescShort: String,
    )

    @Serializable
    data class MealClass(
      val code: Int,
      val position: Int,
      @SerialName("desc-short") val descShort: String,
      val desc: String,
    )

    @Serializable
    data class Allergen(
      val code: Long,
      val position: Long,
      @SerialName("desc-short") val descShort: String,
      val desc: String,
    )
  }
}
