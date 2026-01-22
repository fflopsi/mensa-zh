package ch.florianfrauenfelder.mensazh.data.providers

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.local.room.RoomMenu
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Request
import java.net.URL
import java.util.Locale
import kotlin.time.Clock
import kotlin.uuid.Uuid

class ETHMensaProvider(menuDao: MenuDao, fetchInfoDao: FetchInfoDao, assetService: AssetService) :
  MensaProvider<ETHMensaProvider.EthLocation, ETHMensaProvider.EthMensa, ETHMensaProvider.EthApi.Root>(
    menuDao = menuDao,
    fetchInfoDao = fetchInfoDao,
    assetService = assetService,
  ) {
  override val institution = Institution.ETH
  override val locationsFile = "eth/locations.json"
  override val locationSerializer = EthLocation.serializer()
  override val apiRootSerializer = EthApi.Root.serializer()

  override fun buildRequest(destination: Destination, language: Language): Request {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (destination == Destination.NextWeek) {
        plus(7, DateTimeUnit.DAY)
      } else this
    }

    return Request
      .Builder()
      .url(
        "https://idapps.ethz.ch/cookpit-pub-services/v1/weeklyrotas" +
          "?client-id=ethz-wcms&lang=${language.code}&rs-first=0&rs-size=150&valid-after=$monday",
      )
      .get()
      .build()
  }

  override fun extractMenus(root: EthApi.Root, monday: LocalDate, language: Language) =
    root.weeklyRotaArray
      .filter { monday in LocalDate.parse(it.validFrom)..LocalDate.parse(it.validTo) }
      .flatMap { weeklyRota ->
        weeklyRota.dayOfWeekArray.flatMap { dayOfWeek ->
          dayOfWeek.openingHourArray.orEmpty().flatMap { openingHour ->
            openingHour.mealTimeArray.orEmpty().flatMap { mealTime ->
              val ethMensa = mealTime.lineArray?.let {
                apiMensas.firstOrNull {
                  it.getMapId() == "${weeklyRota.facilityId}_${parseMealTime(mealTime.name)}"
                }
              } ?: return@flatMap emptyList()
              mealTime.lineArray.mapIndexedNotNull { index, line ->
                parseApiMenu(
                  line = line,
                  ethMensa = ethMensa,
                  index = index,
                  language = language,
                  date = monday.plus(dayOfWeek.dayOfWeekCode - 1, DateTimeUnit.DAY),
                )
              }
            }
          }
        }
      }

  private fun parseMealTime(mealTime: String): String? = when {
    listOf("mittag", "lunch", "pranzo").any { mealTime.lowercase().contains(it) } -> "lunch"
    listOf("abend", "dinner", "cena").any { mealTime.lowercase().contains(it) } -> "dinner"
    else -> null
  }

  private fun parseApiMenu(
    line: EthApi.Line,
    ethMensa: EthMensa,
    index: Int,
    language: Language,
    date: LocalDate,
  ): RoomMenu? =
    if (line.meal == null) null
    else RoomMenu(
      mensaId = ethMensa.id,
      index = index,
      language = language,
      title = line.name,
      description = "${line.meal.name.trim()}\n${
        line.meal.description.replace("\\s+".toRegex(), " ")
      }",
      price = line.meal.mealPriceArray.orEmpty().map { String.format(Locale.US, "%.2f", it.price) },
      allergens = line.meal.allergenArray?.joinToString(separator = ", ") { it.desc },
      isVegetarian = line.meal.mealClassArray?.any { it.desc.contains("vegetari", true) } ?: false,
      isVegan = line.meal.mealClassArray?.any { it.desc.contains("vegan", true) } ?: false,
      imageUrl = line.meal.imageUrl?.let { "$it?client-id=ethz-wcms" },
      date = date,
    ).run { if (hasClosedNotice) null else this }

  override suspend fun updateFetchInfo(destination: Destination, language: Language) {
    if (destination != Destination.NextWeek) {
      listOf(Destination.Today, Destination.Tomorrow, Destination.ThisWeek).forEach {
        insertFetchInfo(it, language)
      }
    } else {
      insertFetchInfo(destination, language)
    }
  }

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
      id = Uuid.parse(id),
      title = title,
      mealTime = mealTime,
      url = URL("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/$infoUrlSlug"),
      imagePath = "eth/images/${infoUrlSlug.substring(infoUrlSlug.indexOf("/") + 1)}.jpg",
    )
  }

  object EthApi {
    @Serializable
    data class Root(@SerialName("weekly-rota-array") val weeklyRotaArray: List<WeeklyRota>) :
      Api.Root()

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
