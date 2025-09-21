package ch.florianfrauenfelder.mensazh.services.providers

import ch.florianfrauenfelder.mensazh.models.Location
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu
import ch.florianfrauenfelder.mensazh.models.Weekday
import ch.florianfrauenfelder.mensazh.services.AssetService
import ch.florianfrauenfelder.mensazh.services.CacheService
import ch.florianfrauenfelder.mensazh.services.SerializationService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URL
import java.util.Date
import java.util.Locale
import java.util.UUID

class ETHMensaProvider(
  private val cacheService: CacheService,
  private val assetService: AssetService,
) : MensaProvider(cacheService) {
  override val cacheProviderPrefix = "eth"

  private val ethMensas = mutableListOf<EthMensa>()

  override suspend fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile("eth/locations.json") ?: return emptyList()
    return SerializationService.deserializeList<EthLocation>(json).map { ethLocation ->
      Location(
        id = UUID.fromString(ethLocation.id),
        title = ethLocation.title,
        mensas = ethLocation.mensas.map {
          ethMensas += it
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
      val menuByFacilityIds = getMenuByFacilityId(language, nextWeek, ignoreCache)
      ethMensas.forEach { ethMensa ->
        mensas += ethMensa.toMensa().apply {
          menus = menuByFacilityIds[ethMensa.getMapId()].orEmpty()
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return mensas
  }

  private suspend fun getMenuByFacilityId(
    language: Language,
    nextWeek: Boolean,
    ignoreCache: Boolean,
  ): Map<String, List<Menu>> {
    val date = Date().apply { if (nextWeek) time += 7 * 24 * 60 * 60 * 1000 }
    if (!ignoreCache) {
      getMenuByMensaIdFromCache(date, language)?.let { return it }
    }

    val menuByFacilityIds = getMensaMenusFromCookpit(language, nextWeek, ignoreCache)

    cacheService.saveMensaIds(
      key = getMensaIdCacheKey(date, language),
      mensaIds = menuByFacilityIds.keys.toList(),
    )

    for ((facilityId, menus) in menuByFacilityIds) {
      cacheMenus(cacheProviderPrefix, facilityId, date, language, menus)
    }

    return menuByFacilityIds
  }

  private fun getMenuByMensaIdFromCache(date: Date, language: Language): Map<String, List<Menu>>? {
    val impactedMensas =
      cacheService.readMensaIds(getMensaIdCacheKey(date, language)) ?: return null

    val menuByMensaId = hashMapOf<String, List<Menu>>()
    impactedMensas.forEach { mensaId ->
      tryGetMenusFromCache(
        providerPrefix = cacheProviderPrefix,
        mensaId = mensaId,
        date = date,
        language = language,
      )?.let { menuByMensaId[mensaId] = it }
    }

    return menuByMensaId
  }

  private fun getMensaIdCacheKey(date: Date, language: Language): String =
    "$cacheProviderPrefix.${getDateTimeString(date)}.$language"

  private suspend fun getMensaMenusFromCookpit(
    language: Language,
    nextWeek: Boolean,
    ignoreCache: Boolean,
  ): Map<String, List<Menu>> {
    // Observation: dateslug is ignored by API; all future entries are returned in any case
    val dateSlug =
      getDateTimeStringOfMonday(Date().apply { if (nextWeek) time += 7 * 24 * 60 * 60 * 1000 })
    val url =
      URL("https://idapps.ethz.ch/cookpit-pub-services/v1/weeklyrotas?client-id=ethz-wcms&lang=$language&rs-first=0&rs-size=50&valid-after=$dateSlug")
    val json = getCachedRequest(url, ignoreCache) ?: throw Exception("Cannot load web content")
    val data: Api.Root = SerializationService.deserialize(json)

    val menuByFacilityIds = hashMapOf<String, List<Menu>>()
    data.weeklyRotaArray.filter { it.validFrom == dateSlug }.forEach { weeklyRotaArray ->
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

  private fun isNoMenuNotice(menu: Menu, language: Language): Boolean = when (language) {
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
      weekday = weekday,
    )

  @Serializable
  private data class EthLocation(val id: String, val title: String, val mensas: List<EthMensa>)

  @Serializable
  private data class EthMensa(
    val id: String,
    val title: String,
    val mealTime: String,
    val idSlug: Int,
    val facilityId: Int,
    val timeSlug: String,
    val infoUrlSlug: String,
  ) {
    fun getMapId(): String = facilityId.toString() + "_" + timeSlug

    fun toMensa() = Mensa(
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
      @SerialName("valid-to") val validTo: String? = null,
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
